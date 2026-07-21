package AudioAnalysis;

import java.util.ArrayList;
import java.util.List;

/**
 * SuperFlux onset detection function and peak picking, ported from
 * Böck &amp; Widmer, "Maximum Filter Vibrato Suppression for Onset Detection" (DAFx 2013).
 * <p>
 * Pipeline: magnitude spectrogram → quarter-tone (24 bands/octave) triangular filterbank →
 * logarithmic compression → positive difference against a frequency-maximum-filtered
 * spectrogram {@code mu} frames earlier. The maximum filter widens each band's footprint in
 * the reference frame so that vibrato/portamento (energy wobbling between neighbouring bands)
 * no longer produces spurious flux; genuine onsets (new energy across bands) still do.
 * <p>
 * Peak picking follows the same paper: a frame is an onset if it is a local maximum within
 * ±{@code MAX_WINDOW_SECONDS}, exceeds the local mean over
 * [-{@code PRE_AVG_SECONDS}, +{@code POST_AVG_SECONDS}] by {@code delta}, and lies at least
 * {@code minGapSeconds} after the previously accepted onset.
 * <p>
 * Parameter defaults ({@code mu}, {@code delta}) were tuned on the local ground-truth corpus
 * (see docs/research/SYNC_RESEARCH_LOG.md §6 and benchmark_results/), not copied from the paper,
 * because our ground truth is "what mappers map", not annotated percussive onsets.
 */
public final class SuperFluxOnsetDetector {

    /** Lowest filterbank centre frequency in Hz (below ~30 Hz there is no useful onset energy). */
    private static final double MIN_FREQUENCY = 30.0;
    /** Highest filterbank centre frequency in Hz (paper uses 17 kHz). */
    private static final double MAX_FREQUENCY = 17000.0;
    /** Filterbank resolution: 24 bands per octave = quarter-tone spacing. */
    private static final int BANDS_PER_OCTAVE = 24;
    /** Width of the frequency maximum filter in bands (paper: 3 = one neighbour on each side). */
    private static final int MAX_FILTER_BANDS = 3;

    /** Local-maximum window for peak picking, seconds on each side (paper: 30 ms). */
    private static final double MAX_WINDOW_SECONDS = 0.030;
    /** Local-mean window before the candidate frame (paper: 100 ms). */
    private static final double PRE_AVG_SECONDS = 0.100;
    /** Local-mean window after the candidate frame (paper: 70 ms). */
    private static final double POST_AVG_SECONDS = 0.070;

    /** A triangular filter: {@code weights[i]} applies to FFT bin {@code startBin + i}. */
    private record Filter(int startBin, double[] weights) {}

    private SuperFluxOnsetDetector() {}

    /**
     * Computes the SuperFlux onset detection function of a magnitude spectrogram.
     *
     * @param spectrogram magnitude spectrogram, rows = time frames, columns = FFT bins
     *                    (as produced by {@link SpectrogramCalculator#calculateSpectrogram}).
     * @param sampleRate  the sample rate the spectrogram was computed at (Hz).
     * @param mu          how many frames back the maximum-filtered reference frame lies (&ge; 1).
     * @return one ODF value per frame; the first {@code mu} frames are 0.
     */
    public static double[] computeODF(double[][] spectrogram, double sampleRate, int mu) {
        if (mu < 1) throw new IllegalArgumentException("mu must be >= 1, got: " + mu);
        int frames = spectrogram.length;
        if (frames == 0) return new double[0];

        double[][] filtered = filteredLogSpectrogram(spectrogram, sampleRate);
        int bands = filtered[0].length;

        double[] odf = new double[frames];
        double[] maxFiltered = new double[bands];
        int halfWidth = MAX_FILTER_BANDS / 2;
        for (int n = mu; n < frames; n++) {
            double[] reference = filtered[n - mu];
            for (int b = 0; b < bands; b++) {
                double max = reference[b];
                for (int k = Math.max(0, b - halfWidth); k <= Math.min(bands - 1, b + halfWidth); k++) {
                    if (reference[k] > max) max = reference[k];
                }
                maxFiltered[b] = max;
            }
            double sum = 0;
            for (int b = 0; b < bands; b++) {
                double diff = filtered[n][b] - maxFiltered[b];
                if (diff > 0) sum += diff;
            }
            odf[n] = sum;
        }
        return odf;
    }

    /**
     * SuperFlux-style peak picking on an onset detection function.
     *
     * @param odf                 the onset detection function (one value per frame).
     * @param frameAdvanceSeconds time between consecutive frames (hop / sample rate).
     * @param timeShiftSeconds    constant added to every reported peak time (compensates the
     *                            window-start vs. event-time bias of the STFT; tuned on corpus).
     * @param delta               absolute threshold above the local mean; use
     *                            {@link #positiveMean} to derive it from the ODF scale.
     * @param minGapSeconds       minimum time between two accepted onsets.
     * @return accepted onset times in seconds, ascending.
     */
    public static ArrayList<Double> pickPeaks(double[] odf, double frameAdvanceSeconds,
                                              double timeShiftSeconds, double delta, double minGapSeconds) {
        ArrayList<Double> peaks = new ArrayList<>();
        int maxWindow = Math.max(1, (int) Math.round(MAX_WINDOW_SECONDS / frameAdvanceSeconds));
        int preAvg = Math.max(1, (int) Math.round(PRE_AVG_SECONDS / frameAdvanceSeconds));
        int postAvg = Math.max(1, (int) Math.round(POST_AVG_SECONDS / frameAdvanceSeconds));

        double lastAccepted = -1e9;
        for (int n = 0; n < odf.length; n++) {
            double value = odf[n];
            if (value <= 0) continue;

            boolean isLocalMax = true;
            for (int j = Math.max(0, n - maxWindow); j <= Math.min(odf.length - 1, n + maxWindow); j++) {
                if (odf[j] > value) {
                    isLocalMax = false;
                    break;
                }
            }
            if (!isLocalMax) continue;

            int start = Math.max(0, n - preAvg);
            int end = Math.min(odf.length - 1, n + postAvg);
            double mean = 0;
            for (int j = start; j <= end; j++) mean += odf[j];
            mean /= (end - start + 1);
            if (value < mean + delta) continue;

            double time = n * frameAdvanceSeconds + timeShiftSeconds;
            if (time - lastAccepted < minGapSeconds) continue;

            peaks.add(time);
            lastAccepted = time;
        }
        return peaks;
    }

    /** Mean of the strictly positive ODF values; 0 if none. Used to scale {@code delta}. */
    public static double positiveMean(double[] odf) {
        double sum = 0;
        int count = 0;
        for (double v : odf) {
            if (v > 0) {
                sum += v;
                count++;
            }
        }
        return count == 0 ? 0 : sum / count;
    }

    /**
     * The log-compressed quarter-tone filterbank spectrogram the ODF is computed from
     * (rows = time frames, columns = filterbank bands). Also used as the feature basis
     * for structure segmentation ({@link FooteSectionDetector}).
     *
     * @param spectrogram magnitude spectrogram as produced by {@link SpectrogramCalculator}.
     * @param sampleRate  the sample rate the spectrogram was computed at (Hz).
     * @return one row per input frame with {@code log10(1 + bandEnergy)} per band.
     */
    public static double[][] filteredLogSpectrogram(double[][] spectrogram, double sampleRate) {
        if (spectrogram.length == 0) return new double[0][0];
        List<Filter> filterbank = buildFilterbank(spectrogram[0].length, sampleRate);
        return applyFilterbankLog(spectrogram, filterbank);
    }

    /**
     * Applies the filterbank and logarithmic compression ({@code log10(1 + x)}) to each frame.
     */
    private static double[][] applyFilterbankLog(double[][] spectrogram, List<Filter> filterbank) {
        int frames = spectrogram.length;
        int bands = filterbank.size();
        double[][] result = new double[frames][bands];
        for (int n = 0; n < frames; n++) {
            double[] frame = spectrogram[n];
            for (int b = 0; b < bands; b++) {
                Filter filter = filterbank.get(b);
                double sum = 0;
                for (int i = 0; i < filter.weights().length; i++) {
                    sum += frame[filter.startBin() + i] * filter.weights()[i];
                }
                result[n][b] = Math.log10(1.0 + sum);
            }
        }
        return result;
    }

    /**
     * Builds overlapping triangular filters with quarter-tone spaced centre frequencies.
     * Centres whose FFT bins collide at low frequencies are merged (like madmom's
     * {@code unique_filters}), so the effective band count depends on the FFT resolution.
     * Each filter is normalized to unit weight sum.
     */
    private static List<Filter> buildFilterbank(int numBins, double sampleRate) {
        double binWidth = sampleRate / 2.0 / numBins;

        // Quarter-tone centre frequencies covering [MIN_FREQUENCY, MAX_FREQUENCY], mapped to bins.
        List<Integer> centerBins = new ArrayList<>();
        int kMin = (int) Math.floor(BANDS_PER_OCTAVE * log2(MIN_FREQUENCY / 440.0));
        int kMax = (int) Math.ceil(BANDS_PER_OCTAVE * log2(MAX_FREQUENCY / 440.0));
        for (int k = kMin; k <= kMax; k++) {
            double freq = 440.0 * Math.pow(2.0, k / (double) BANDS_PER_OCTAVE);
            int bin = (int) Math.round(freq / binWidth);
            if (bin < 1 || bin >= numBins) continue;
            if (!centerBins.isEmpty() && centerBins.get(centerBins.size() - 1) == bin) continue;
            centerBins.add(bin);
        }

        List<Filter> filters = new ArrayList<>();
        for (int i = 0; i < centerBins.size(); i++) {
            int left = i == 0 ? Math.max(1, centerBins.get(0) - 1) : centerBins.get(i - 1);
            int center = centerBins.get(i);
            int right = i == centerBins.size() - 1
                    ? Math.min(numBins - 1, center + 1) : centerBins.get(i + 1);

            double[] weights = new double[right - left + 1];
            double sum = 0;
            for (int bin = left; bin <= right; bin++) {
                double w = bin <= center
                        ? (left == center ? 1.0 : (bin - left) / (double) (center - left))
                        : (right - bin) / (double) (right - center);
                weights[bin - left] = w;
                sum += w;
            }
            if (sum <= 0) { // degenerate single-bin filter
                weights = new double[]{1.0};
                filters.add(new Filter(center, weights));
                continue;
            }
            for (int j = 0; j < weights.length; j++) weights[j] /= sum;
            filters.add(new Filter(left, weights));
        }
        return filters;
    }

    private static double log2(double x) {
        return Math.log(x) / Math.log(2);
    }
}
