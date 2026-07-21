package AudioAnalysis;

import java.util.ArrayList;

/**
 * Automatic song-structure boundary detection via Foote checkerboard novelty
 * (Foote, "Automatic Audio Segmentation Using a Measure of Audio Novelty", ICME 2000;
 * implementation follows the FMP notebooks C4S4 formulation).
 * <p>
 * Pipeline: the log-compressed quarter-tone filterbank spectrogram
 * ({@link SuperFluxOnsetDetector#filteredLogSpectrogram}) is averaged into coarse
 * feature frames (~0.5 s), a cosine self-similarity matrix is built from the unit-normalized
 * frames, and a Gaussian-tapered checkerboard kernel slides along the SSM diagonal. Peaks of
 * the resulting novelty curve mark transitions between musically homogeneous blocks —
 * section boundaries.
 * <p>
 * Intensity rating per section is a heuristic without ground truth: sections are ranked
 * within the song by loudness + onset density and mapped to five tiers
 * (0 = calm … 4 = peak), so tiers always describe contrast inside one song,
 * never absolute energy across songs.
 * <p>
 * Kernel size, minimum section length and peak threshold are corpus-tuned against
 * hand-placed mapper bookmarks; see docs/research/SYNC_RESEARCH_LOG.md §4.3 and
 * benchmark_results/.
 */
public final class FooteSectionDetector {

    /** Length of one coarse feature frame in seconds (SSM resolution). */
    public static final double COARSE_SECONDS = 0.5;

    /**
     * Corpus-tuned defaults (sweep 2026-07-05 vs curated mapper bookmarks, 14 maps:
     * boundary F@±3s = .592, P .624 / R .630; surface flat around the optimum).
     */
    public static final double DEFAULT_KERNEL_SECONDS = 16;
    public static final double DEFAULT_MIN_SECTION_SECONDS = 6;
    public static final double DEFAULT_NOVELTY_THRESHOLD = 0.05;

    /** Number of intensity tiers (0 = calm … 4 = peak). */
    public static final int INTENSITY_TIERS = 5;

    /**
     * Novelty curve plus picked boundaries.
     *
     * @param noveltyTimesSeconds center time of each coarse frame
     * @param novelty             novelty per coarse frame (1.0 = transition between two
     *                            internally identical, mutually anticorrelated blocks)
     * @param boundaries          picked section-boundary times in seconds, ascending
     */
    public record Result(double[] noveltyTimesSeconds, double[] novelty, ArrayList<Double> boundaries) {}

    private FooteSectionDetector() {}

    /**
     * Detects section boundaries in a filtered log spectrogram.
     *
     * @param filteredLogSpectrogram output of {@link SuperFluxOnsetDetector#filteredLogSpectrogram}
     * @param frameAdvanceSeconds    seconds between spectrogram frames (hop / sample rate)
     * @param kernelSeconds          checkerboard kernel size (context on each side = half of it);
     *                               larger = only coarse structure, smaller = more boundaries
     * @param minSectionSeconds      minimum distance between two boundaries
     * @param noveltyThreshold       minimum novelty for a boundary (0..1 on the normalized curve;
     *                               real music transitions typically land around 0.05–0.3)
     * @return novelty curve and boundary times
     */
    /** Detects section boundaries with the corpus-tuned default parameters. */
    public static Result detect(double[][] filteredLogSpectrogram, double frameAdvanceSeconds) {
        return detect(filteredLogSpectrogram, frameAdvanceSeconds,
                DEFAULT_KERNEL_SECONDS, DEFAULT_MIN_SECTION_SECONDS, DEFAULT_NOVELTY_THRESHOLD);
    }

    public static Result detect(double[][] filteredLogSpectrogram, double frameAdvanceSeconds,
                                double kernelSeconds, double minSectionSeconds, double noveltyThreshold) {
        double[][] features = coarseFeatures(filteredLogSpectrogram, frameAdvanceSeconds);
        double[][] ssm = selfSimilarity(features);
        double[] novelty = checkerboardNovelty(ssm, kernelSeconds);

        int frames = novelty.length;
        double[] times = new double[frames];
        for (int i = 0; i < frames; i++) times[i] = (i + 0.5) * COARSE_SECONDS;

        ArrayList<Double> boundaries = pickBoundaries(novelty, times, minSectionSeconds, noveltyThreshold);
        return new Result(times, novelty, boundaries);
    }

    /**
     * Rates the intensity of each section between consecutive boundaries (plus the leading and
     * trailing section) on a 0..4 tier scale by ranking loudness + onset density within the song.
     *
     * @param boundaries             section boundaries in seconds, ascending
     * @param totalDurationSeconds   duration of the song
     * @param filteredLogSpectrogram output of {@link SuperFluxOnsetDetector#filteredLogSpectrogram}
     * @param frameAdvanceSeconds    seconds between spectrogram frames
     * @param onsetTimesSeconds      detected onsets (e.g. Expert+ list), used as density measure
     * @return one tier (0 = calm … 4 = peak) per section; length = boundaries.size() + 1
     */
    public static int[] rateIntensity(ArrayList<Double> boundaries, double totalDurationSeconds,
                                      double[][] filteredLogSpectrogram, double frameAdvanceSeconds,
                                      double[] onsetTimesSeconds) {
        int sections = boundaries.size() + 1;
        double[] starts = new double[sections];
        double[] ends = new double[sections];
        for (int s = 0; s < sections; s++) {
            starts[s] = s == 0 ? 0 : boundaries.get(s - 1);
            ends[s] = s == sections - 1 ? totalDurationSeconds : boundaries.get(s);
        }

        double[] loudness = new double[sections];
        double[] density = new double[sections];
        for (int s = 0; s < sections; s++) {
            int from = (int) (starts[s] / frameAdvanceSeconds);
            int to = Math.min(filteredLogSpectrogram.length, (int) (ends[s] / frameAdvanceSeconds));
            double sum = 0;
            for (int n = from; n < to; n++) {
                for (double band : filteredLogSpectrogram[n]) sum += band;
            }
            loudness[s] = to > from ? sum / (to - from) : 0;

            int onsets = 0;
            for (double t : onsetTimesSeconds) {
                if (t >= starts[s] && t < ends[s]) onsets++;
            }
            density[s] = ends[s] > starts[s] ? onsets / (ends[s] - starts[s]) : 0;
        }

        double[] score = new double[sections];
        for (int s = 0; s < sections; s++) {
            score[s] = zScore(loudness, s) + zScore(density, s);
        }

        // Rank within the song, then spread ranks over the tiers (contrast, not absolute energy).
        int[] tiers = new int[sections];
        for (int s = 0; s < sections; s++) {
            int rank = 0;
            for (double other : score) {
                if (other < score[s]) rank++;
            }
            tiers[s] = Math.min(INTENSITY_TIERS - 1, rank * INTENSITY_TIERS / sections);
        }
        return tiers;
    }

    /**
     * Averages spectrogram frames into coarse feature vectors, z-scores each band over the
     * whole song, then unit-normalizes each frame. Without the z-scoring, raw log-spectral
     * frames of full-band music are all nearly parallel (cosine ≈ .97 everywhere — measured
     * on the corpus, sweep 2026-07-05) and the novelty scale collapses; standardizing per
     * band makes the SSM a correlation of "which bands are above their song average",
     * which is what changes between sections.
     */
    private static double[][] coarseFeatures(double[][] filteredLogSpectrogram, double frameAdvanceSeconds) {
        int window = Math.max(1, (int) Math.round(COARSE_SECONDS / frameAdvanceSeconds));
        int frames = filteredLogSpectrogram.length / window;
        if (frames == 0 || filteredLogSpectrogram[0].length == 0) return new double[0][0];
        int bands = filteredLogSpectrogram[0].length;

        double[][] features = new double[frames][bands];
        for (int c = 0; c < frames; c++) {
            for (int n = c * window; n < (c + 1) * window; n++) {
                for (int b = 0; b < bands; b++) features[c][b] += filteredLogSpectrogram[n][b];
            }
            for (int b = 0; b < bands; b++) features[c][b] /= window;
        }

        for (int b = 0; b < bands; b++) {
            double mean = 0;
            for (int c = 0; c < frames; c++) mean += features[c][b];
            mean /= frames;
            double variance = 0;
            for (int c = 0; c < frames; c++) variance += (features[c][b] - mean) * (features[c][b] - mean);
            double std = Math.sqrt(variance / frames);
            for (int c = 0; c < frames; c++) {
                features[c][b] = std > 0 ? (features[c][b] - mean) / std : 0;
            }
        }

        for (int c = 0; c < frames; c++) {
            double norm = 0;
            for (double v : features[c]) norm += v * v;
            norm = Math.sqrt(norm);
            if (norm > 0) {
                for (int b = 0; b < bands; b++) features[c][b] /= norm;
            }
        }
        return features;
    }

    /** Cosine self-similarity matrix of the normalized feature vectors (values in [-1, 1]). */
    private static double[][] selfSimilarity(double[][] features) {
        int n = features.length;
        double[][] ssm = new double[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = i; j < n; j++) {
                double dot = 0;
                for (int b = 0; b < features[i].length; b++) dot += features[i][b] * features[j][b];
                ssm[i][j] = dot;
                ssm[j][i] = dot;
            }
        }
        return ssm;
    }

    /**
     * Slides a Gaussian-tapered checkerboard kernel along the SSM diagonal (FMP C4S4).
     * The result is half-wave rectified and normalized by the kernel's total absolute mass, so
     * 1.0 means a transition between two internally identical, mutually anticorrelated blocks
     * and a structure-free song stays near 0 (no per-song max scaling that would amplify
     * noise). The first and last half-kernel frames stay 0 (incomplete context).
     */
    private static double[] checkerboardNovelty(double[][] ssm, double kernelSeconds) {
        int n = ssm.length;
        double[] novelty = new double[n];
        int half = Math.max(2, (int) Math.round(kernelSeconds / 2 / COARSE_SECONDS));
        double sigma = half / 2.0;

        double[][] kernel = new double[2 * half + 1][2 * half + 1];
        double totalMass = 0;
        for (int i = -half; i <= half; i++) {
            for (int j = -half; j <= half; j++) {
                double taper = Math.exp(-(i * (double) i + j * (double) j) / (2 * sigma * sigma));
                kernel[i + half][j + half] = Math.signum(i) * Math.signum(j) * taper;
                totalMass += Math.abs(kernel[i + half][j + half]);
            }
        }

        for (int c = half; c < n - half; c++) {
            double sum = 0;
            for (int i = -half; i <= half; i++) {
                for (int j = -half; j <= half; j++) {
                    sum += kernel[i + half][j + half] * ssm[c + i][c + j];
                }
            }
            novelty[c] = Math.max(0, sum / totalMass);
        }
        return novelty;
    }

    /**
     * Local maxima above the threshold, at least minSectionSeconds apart
     * (exposed for the benchmark parameter sweep, which reuses one novelty curve).
     */
    public static ArrayList<Double> pickBoundaries(double[] novelty, double[] times,
                                                   double minSectionSeconds, double noveltyThreshold) {
        ArrayList<Double> boundaries = new ArrayList<>();
        int minGapFrames = Math.max(1, (int) Math.round(minSectionSeconds / COARSE_SECONDS));

        double lastAccepted = -1e9;
        for (int c = 0; c < novelty.length; c++) {
            double value = novelty[c];
            if (value < noveltyThreshold) continue;

            boolean isLocalMax = true;
            for (int j = Math.max(0, c - minGapFrames); j <= Math.min(novelty.length - 1, c + minGapFrames); j++) {
                if (novelty[j] > value) {
                    isLocalMax = false;
                    break;
                }
            }
            if (!isLocalMax) continue;
            if (times[c] - lastAccepted < minSectionSeconds) continue;

            boundaries.add(times[c]);
            lastAccepted = times[c];
        }
        return boundaries;
    }

    private static double zScore(double[] values, int index) {
        double mean = 0;
        for (double v : values) mean += v;
        mean /= values.length;
        double variance = 0;
        for (double v : values) variance += (v - mean) * (v - mean);
        double std = Math.sqrt(variance / values.length);
        return std > 0 ? (values[index] - mean) / std : 0;
    }
}
