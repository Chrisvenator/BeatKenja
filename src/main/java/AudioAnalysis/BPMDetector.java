package AudioAnalysis;

import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.IOException;

/**
 * Automatic BPM (tempo) detection from audio files.
 * <p>
 * The detector autocorrelates the SuperFlux onset detection function ({@link SuperFluxOnsetDetector})
 * and scores every tempo candidate on a log-spaced grid with a harmonic comb (the autocorrelation at
 * 1x, 2x, 3x and 4x the candidate's beat period) weighted by a log-normal prior over mapper tempos.
 * The comb resolves metrical-level confusion (e.g. 300 BPM vs the 180 BPM "3/5 fold" the old
 * 60-200 BPM cap produced), the prior picks the octave mappers actually use.
 * <p>
 * The search range (50-420 BPM) and prior parameters are tuned against the local ground-truth corpus
 * (BPM 105-388, 33/46 maps above the old 200 BPM cap); see docs/research/SYNC_RESEARCH_LOG.md and
 * benchmark_results/. The audio file to be analyzed should be in WAV format.
 */
public class BPMDetector {
    /** The sample rate of the audio file, typically 44100 Hz for CD quality audio. */
    private static final int SAMPLE_RATE = 44100;
    /** FFT window size, matching {@link AudioAnalysis} so the SuperFlux ODF is identical. */
    private static final int FFT_SIZE = 2048;
    /** Hop size between consecutive FFT windows (~5.8 ms per frame at 44.1 kHz). */
    private static final int HOP_SIZE = 256;
    private static final int OVERLAP = FFT_SIZE - HOP_SIZE;
    /** SuperFlux reference-frame distance in frames (same value AudioAnalysis uses). */
    private static final int MU = 2;

    /** Minimum BPM to consider valid. Corpus low end is 105; margin for slow songs. */
    private static final double MIN_BPM = 50.0;
    /** Maximum BPM to consider valid. Corpus high end is 388 (speedcore). */
    private static final double MAX_BPM = 420.0;
    /** Default BPM fallback value for silent or too-short audio. */
    private static final double DEFAULT_BPM = 120.0;

    /** Number of beat-period harmonics summed in the comb score, weighted 1/k. */
    private static final int COMB_HARMONICS = 4;
    /** Multiplicative step of the tempo search grid (~0.5%, well inside the ±4% Accuracy1 window). */
    private static final double GRID_STEP = 1.005;

    /**
     * Center of the log-normal prior over mapper tempos. Corpus-tuned
     * (sweep 2026-07-05, BPMSweepTest: Acc1 35/46); corpus median is ~250 BPM.
     */
    private static final double PRIOR_CENTER_BPM = 260.0;
    /**
     * Width (sigma) of the tempo prior in octaves (log2 units). The broadest value among the
     * sweep's tied winners — keeps the prior weak so the comb decides on unfamiliar material.
     */
    private static final double PRIOR_LOG2_SIGMA = 1.2;

    /**
     * Detects the BPM of an audio file.
     *
     * @param filePath The path to the audio file to analyze
     * @return The detected BPM as a double value
     * @throws UnsupportedAudioFileException if the audio file format is not supported
     * @throws IOException                   if an I/O error occurs while reading the audio file
     */
    public static double detectBPM(String filePath) throws UnsupportedAudioFileException, IOException {
        double[][] spectrogram = SpectrogramCalculator.calculateSpectrogram(filePath, FFT_SIZE, OVERLAP);
        double frameAdvance = HOP_SIZE / (double) SAMPLE_RATE;
        double[] odf = SuperFluxOnsetDetector.computeODF(spectrogram, SAMPLE_RATE, MU);
        return estimateTempo(odf, frameAdvance);
    }

    /**
     * Estimates the tempo of an onset detection function using the corpus-tuned prior.
     *
     * @param odf          onset detection function (one value per frame, e.g. from SuperFlux)
     * @param frameAdvance seconds between consecutive ODF frames
     * @return estimated BPM, rounded to one decimal place
     */
    public static double estimateTempo(double[] odf, double frameAdvance) {
        return estimateTempo(odf, frameAdvance, PRIOR_CENTER_BPM, PRIOR_LOG2_SIGMA);
    }

    /**
     * Estimates the tempo of an onset detection function with an explicit tempo prior
     * (exposed for the benchmark parameter sweep).
     *
     * @param odf            onset detection function
     * @param frameAdvance   seconds between consecutive ODF frames
     * @param priorCenterBpm center of the log-normal tempo prior
     * @param priorLog2Sigma prior width in octaves
     * @return estimated BPM, rounded to one decimal place
     */
    public static double estimateTempo(double[] odf, double frameAdvance,
                                       double priorCenterBpm, double priorLog2Sigma) {
        int maxLag = maxCombLag(frameAdvance);
        if (odf.length < minimumFrames(frameAdvance)) return DEFAULT_BPM;
        double[] autocorr = computeAutocorrelation(odf, Math.min(maxLag, odf.length - 1));
        return pickTempo(autocorr, frameAdvance, priorCenterBpm, priorLog2Sigma);
    }

    /**
     * Normalized autocorrelation of a zero-meaned signal, half-wave rectified.
     * Index = lag in frames; values in [0, 1] with lag 0 mapped to 1.
     *
     * @param signal input signal (e.g. an ODF)
     * @param maxLag largest lag (in frames) to compute
     * @return autocorrelation array of length maxLag + 1, all zeros for a flat signal
     */
    public static double[] computeAutocorrelation(double[] signal, int maxLag) {
        int n = signal.length;
        double mean = 0;
        for (double v : signal) mean += v;
        mean /= n;

        double[] centered = new double[n];
        for (int i = 0; i < n; i++) centered[i] = signal[i] - mean;

        double energy = 0;
        for (double v : centered) energy += v * v;
        double[] autocorr = new double[maxLag + 1];
        if (energy == 0) return autocorr;

        for (int lag = 0; lag <= maxLag; lag++) {
            double sum = 0;
            for (int i = 0; i + lag < n; i++) {
                sum += centered[i] * centered[i + lag];
            }
            // Per-lag normalization compensates the shrinking overlap; rectify: only
            // positive correlation is evidence of periodicity.
            double value = sum / (n - lag) / (energy / n);
            autocorr[lag] = Math.max(0, value);
        }
        return autocorr;
    }

    /**
     * Picks the best tempo from an ODF autocorrelation: scans a log-spaced BPM grid,
     * scoring each candidate with a harmonic comb times a log-normal tempo prior,
     * then refines the winning grid point by parabolic interpolation in log-tempo space.
     *
     * @param autocorr       output of {@link #computeAutocorrelation}
     * @param frameAdvance   seconds between consecutive ODF frames
     * @param priorCenterBpm center of the log-normal tempo prior
     * @param priorLog2Sigma prior width in octaves
     * @return estimated BPM, rounded to one decimal place
     */
    public static double pickTempo(double[] autocorr, double frameAdvance,
                                   double priorCenterBpm, double priorLog2Sigma) {
        int gridSize = (int) Math.ceil(Math.log(MAX_BPM / MIN_BPM) / Math.log(GRID_STEP)) + 1;
        double[] gridBpm = new double[gridSize];
        double[] gridScore = new double[gridSize];

        int best = -1;
        for (int g = 0; g < gridSize; g++) {
            double bpm = MIN_BPM * Math.pow(GRID_STEP, g);
            if (bpm > MAX_BPM) bpm = MAX_BPM;
            gridBpm[g] = bpm;
            gridScore[g] = combScore(autocorr, frameAdvance, bpm) * prior(bpm, priorCenterBpm, priorLog2Sigma);
            if (best < 0 || gridScore[g] > gridScore[best]) best = g;
        }
        if (best < 0 || gridScore[best] <= 0) return DEFAULT_BPM;

        // Parabolic refinement over log-tempo (grid is log-spaced, so offsets are symmetric).
        double bpm = gridBpm[best];
        if (best > 0 && best < gridSize - 1) {
            double left = gridScore[best - 1], center = gridScore[best], right = gridScore[best + 1];
            double denominator = left - 2 * center + right;
            if (denominator < 0) {
                double shift = 0.5 * (left - right) / denominator;
                bpm = gridBpm[best] * Math.pow(GRID_STEP, shift);
            }
        }
        return Math.round(Math.min(MAX_BPM, Math.max(MIN_BPM, bpm)) * 10.0) / 10.0;
    }

    /**
     * Harmonic comb: autocorrelation sampled at 1x..4x the candidate beat period, weighted 1/k.
     * High only when the signal is periodic at the candidate period AND its multiples, which
     * suppresses non-octave metrical folds (3/5, 2/3) that match a single lag by accident.
     */
    private static double combScore(double[] autocorr, double frameAdvance, double bpm) {
        double periodFrames = 60.0 / (bpm * frameAdvance);
        double score = 0;
        for (int k = 1; k <= COMB_HARMONICS; k++) {
            score += autocorrAt(autocorr, k * periodFrames) / k;
        }
        return score;
    }

    /** Log-normal tempo prior: how likely mappers are to use this BPM, in octave distance. */
    private static double prior(double bpm, double centerBpm, double log2Sigma) {
        double octaves = Math.log(bpm / centerBpm) / Math.log(2);
        return Math.exp(-(octaves * octaves) / (2 * log2Sigma * log2Sigma));
    }

    /** Linear interpolation of the autocorrelation at a fractional lag; 0 beyond the array. */
    private static double autocorrAt(double[] autocorr, double lag) {
        int lower = (int) Math.floor(lag);
        if (lower < 0 || lower + 1 >= autocorr.length) return 0;
        double fraction = lag - lower;
        return autocorr[lower] * (1 - fraction) + autocorr[lower + 1] * fraction;
    }

    /** Largest lag the comb can request: 4x the beat period of the slowest tempo. */
    private static int maxCombLag(double frameAdvance) {
        return (int) Math.ceil(COMB_HARMONICS * 60.0 / (MIN_BPM * frameAdvance)) + 1;
    }

    /** Frames needed to see at least two periods of the slowest tempo. */
    private static int minimumFrames(double frameAdvance) {
        return (int) Math.ceil(2 * 60.0 / (MIN_BPM * frameAdvance));
    }

    /**
     * Convenience method that returns BPM as integer for cases where precision isn't critical.
     */
    public static int detectBPMInteger(String filePath) throws UnsupportedAudioFileException, IOException {
        return (int) Math.round(detectBPM(filePath));
    }
}
