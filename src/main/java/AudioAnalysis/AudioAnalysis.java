package AudioAnalysis;


import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.IOException;
import java.util.ArrayList;

/**
 * This class provides methods for analyzing audio files, specifically for detecting peaks in the audio that can be used to generate beat maps for rhythm games.
 * Onset detection uses the SuperFlux algorithm ({@link SuperFluxOnsetDetector}): a log-compressed
 * quarter-tone filterbank spectrogram differenced against a frequency-maximum-filtered earlier frame,
 * followed by local-maximum/local-mean peak picking with per-difficulty thresholds and
 * tempo-aware minimum note gaps.
 * The audio file to be analyzed should be in WAV format.
 * <p>
 * The main functionality includes calculating the spectrogram of the audio, detecting peaks at different difficulty levels,
 * and returning these peaks as potential beat locations.
 * <p>
 * Peak-picking parameters (mu, delta ladder, time shift) are tuned against the local ground-truth
 * corpus of ranked maps; see docs/research/SYNC_RESEARCH_LOG.md and benchmark_results/.
 */
public class AudioAnalysis {
    /** The sample rate of the audio file, typically 44100 Hz for CD quality audio. */
    private static final int SAMPLE_RATE = 44100;
    /** FFT window size. 2048 gives the ~21.5 Hz resolution the quarter-tone filterbank needs. */
    private static final int FFT_SIZE = 2048;
    /** Hop size between consecutive FFT windows (~5.8 ms per frame at 44.1 kHz). */
    private static final int HOP_SIZE = 256;
    private static final int OVERLAP = FFT_SIZE - HOP_SIZE;

    /** SuperFlux reference-frame distance in frames (corpus sweep 2026-07-05: best F@50). */
    private static final int MU = 2;
    /**
     * Onset times are reported at the STFT window center, not the window start
     * (confirmed best on the corpus sweep: +23 ms ≈ FFT_SIZE / 2 / SAMPLE_RATE).
     */
    private static final double TIME_SHIFT_SECONDS = FFT_SIZE / 2.0 / SAMPLE_RATE;

    // Difficulty-based target note spacing at 120 BPM (in seconds)
    private static final double[] BASE_GAP_SECONDS = {0.150, 0.110, 0.090, 0.075, 0.065}; // Easy to Expert+

    /**
     * Per-difficulty peak-picking thresholds as multiples of the ODF's positive mean
     * (Easy to Expert+). Higher = fewer, more salient onsets. The Expert+ value 0.60
     * maximizes F@50ms on the corpus sweep (P .777 / R .852); easier difficulties
     * scale up to keep maps sparser (sweep: 1.5 → R≈.61, 2.5 → R≈.42).
     */
    private static final double[] DELTA_RELATIVE = {2.5, 1.8, 1.3, 0.9, 0.6};

    /**
     * Analyzes the audio file at the given file path and detects peaks that could correspond to beats in the music.
     * The detected peaks are returned for different difficulty levels.
     *
     * @param filePath The path to the audio file to be analyzed.
     * @return A list of lists, where each inner list contains the time positions of detected peaks for a specific difficulty level.
     * @throws UnsupportedAudioFileException if the audio file format is not supported.
     * @throws IOException                   if an I/O error occurs while reading the audio file.
     */
    public static ArrayList<ArrayList<Double>> getPeaksFromAudio(String filePath, double bpm, Double offset) throws UnsupportedAudioFileException, IOException {
        double[][] spec = SpectrogramCalculator.calculateSpectrogram(filePath, FFT_SIZE, OVERLAP);
        double frameAdvance = HOP_SIZE / (double) SAMPLE_RATE;

        double[] odf = SuperFluxOnsetDetector.computeODF(spec, SAMPLE_RATE, MU);

        // Estimate BPM for tempo-aware processing
        double estimatedBPM = bpm;
        if (bpm < 0.0) estimatedBPM = estimateBPM(odf, frameAdvance);

        if (offset == null) offset = TimingOffsetDetector.detectTimingOffset(filePath, estimatedBPM);
        if (offset == null) offset = 0.0;

        double positiveMean = SuperFluxOnsetDetector.positiveMean(odf);

        ArrayList<ArrayList<Double>> peaks = new ArrayList<>();
        for (int difficulty = 0; difficulty < 5; difficulty++) {
            double minGapSeconds = BASE_GAP_SECONDS[difficulty] * 120.0 / estimatedBPM;
            double delta = DELTA_RELATIVE[difficulty] * positiveMean;
            peaks.add(SuperFluxOnsetDetector.pickPeaks(
                    odf, frameAdvance, TIME_SHIFT_SECONDS + offset, delta, minGapSeconds));
        }
        return peaks;
    }

    /**
     * BPM estimation from the already-computed SuperFlux ODF
     * (harmonic-comb autocorrelation, see {@link BPMDetector#estimateTempo}).
     */
    private static double estimateBPM(double[] odf, double frameAdvance) {
        return BPMDetector.estimateTempo(odf, frameAdvance);
    }
}
