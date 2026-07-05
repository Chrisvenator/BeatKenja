package AudioAnalysis;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link SuperFluxOnsetDetector} using synthetic magnitude spectrograms
 * (no audio files needed, runs in the default test suite).
 */
class SuperFluxOnsetDetectorTest {

    private static final double SAMPLE_RATE = 44100.0;
    private static final int BINS = 1024; // FFT 2048
    private static final double FRAME_ADVANCE = 256.0 / SAMPLE_RATE;

    /** Broadband bursts must produce ODF peaks at the burst frames. */
    @Test
    void detectsBroadbandBursts() {
        int frames = 400;
        double[][] spec = quietSpectrogram(frames);
        // noisy floor so the ODF has a small positive baseline (like real audio);
        // otherwise positiveMean() is dominated by the burst frames themselves
        java.util.Random random = new java.util.Random(42);
        for (int n = 0; n < frames; n++) {
            for (int bin = 0; bin < BINS; bin++) {
                spec[n][bin] += 0.05 * random.nextDouble();
            }
        }
        int[] burstFrames = {50, 150, 250, 350};
        for (int f : burstFrames) {
            for (int bin = 10; bin < 800; bin++) {
                spec[f][bin] += 5.0;
                spec[f + 1][bin] += 4.0; // decay frame
            }
        }

        double[] odf = SuperFluxOnsetDetector.computeODF(spec, SAMPLE_RATE, 2);
        double delta = 1.2 * SuperFluxOnsetDetector.positiveMean(odf);
        ArrayList<Double> peaks = SuperFluxOnsetDetector.pickPeaks(odf, FRAME_ADVANCE, 0.0, delta, 0.05);

        assertEquals(burstFrames.length, peaks.size(), "one onset per burst, got: " + peaks);
        for (int i = 0; i < burstFrames.length; i++) {
            assertEquals(burstFrames[i] * FRAME_ADVANCE, peaks.get(i), 0.015,
                    "onset " + i + " should sit at the burst frame");
        }
    }

    /**
     * Vibrato: energy wobbling between two quarter-tone-adjacent bins. The frequency
     * maximum filter must suppress this — ODF stays near zero after the initial attack.
     */
    @Test
    void suppressesVibrato() {
        int frames = 300;
        double[][] spec = quietSpectrogram(frames);
        // bin 100 ≈ 2153 Hz; bin 103 is roughly one quarter-tone up (adjacent filter band)
        for (int n = 20; n < frames; n++) {
            int bin = (n / 3) % 2 == 0 ? 100 : 103; // wobble every 3 frames (~17 ms)
            spec[n][bin] += 8.0;
        }

        double[] odf = SuperFluxOnsetDetector.computeODF(spec, SAMPLE_RATE, 2);

        double attack = 0;
        for (int n = 20; n < 26; n++) attack = Math.max(attack, odf[n]);
        double sustainMax = 0;
        for (int n = 40; n < frames; n++) sustainMax = Math.max(sustainMax, odf[n]);

        assertTrue(attack > 0, "tone attack must register in the ODF");
        assertTrue(sustainMax < attack * 0.25,
                "vibrato must be suppressed: sustain max " + sustainMax + " vs attack " + attack);
    }

    /** pickPeaks must enforce the minimum gap between accepted onsets. */
    @Test
    void enforcesMinimumGap() {
        double[] odf = new double[500];
        for (int n = 20; n < 500; n += 10) odf[n] = 10.0; // peaks every ~58 ms

        ArrayList<Double> peaks = SuperFluxOnsetDetector.pickPeaks(odf, FRAME_ADVANCE, 0.0, 0.5, 0.150);
        for (int i = 1; i < peaks.size(); i++) {
            assertTrue(peaks.get(i) - peaks.get(i - 1) >= 0.150 - 1e-9,
                    "gap violated between " + peaks.get(i - 1) + " and " + peaks.get(i));
        }
        assertTrue(peaks.size() >= 2, "several peaks should survive the gap filter");
    }

    /** The time shift must move every reported onset uniformly. */
    @Test
    void appliesTimeShift() {
        double[] odf = new double[200];
        odf[100] = 10.0;

        ArrayList<Double> unshifted = SuperFluxOnsetDetector.pickPeaks(odf, FRAME_ADVANCE, 0.0, 0.5, 0.05);
        ArrayList<Double> shifted = SuperFluxOnsetDetector.pickPeaks(odf, FRAME_ADVANCE, 0.025, 0.5, 0.05);

        assertEquals(1, unshifted.size());
        assertEquals(1, shifted.size());
        assertEquals(unshifted.get(0) + 0.025, shifted.get(0), 1e-9);
    }

    @Test
    void positiveMeanIgnoresZeros() {
        double[] odf = {0.0, 2.0, 0.0, 4.0, 0.0};
        assertEquals(3.0, SuperFluxOnsetDetector.positiveMean(odf), 1e-9);
        assertEquals(0.0, SuperFluxOnsetDetector.positiveMean(new double[]{0, 0}), 1e-9);
    }

    private static double[][] quietSpectrogram(int frames) {
        double[][] spec = new double[frames][BINS];
        for (int n = 0; n < frames; n++) {
            for (int bin = 0; bin < BINS; bin++) {
                spec[n][bin] = 0.01; // constant noise floor, produces no flux
            }
        }
        return spec;
    }
}
