package AudioAnalysis;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link BPMDetector} tempo estimation on synthetic onset detection functions.
 * Tests use the explicit-prior overload so they stay valid if the corpus-tuned default
 * prior constants change.
 */
class BPMDetectorTest {

    /** Matches the production frame advance (hop 256 at 44.1 kHz). */
    private static final double FRAME_ADVANCE = 256.0 / 44100.0;
    private static final double PRIOR_CENTER = 240.0;
    private static final double PRIOR_SIGMA = 0.8;

    /** Impulse train ODF: one unit spike per beat at the given BPM. */
    private static double[] pulseTrain(double bpm, double durationSeconds) {
        int frames = (int) (durationSeconds / FRAME_ADVANCE);
        double[] odf = new double[frames];
        double period = 60.0 / bpm;
        for (double t = 0; t < durationSeconds; t += period) {
            int frame = (int) Math.round(t / FRAME_ADVANCE);
            if (frame < frames) odf[frame] = 1.0;
        }
        return odf;
    }

    private static void assertWithinFourPercent(double expected, double actual) {
        assertTrue(Math.abs(actual - expected) <= 0.04 * expected,
                "expected " + expected + " ±4%, got " + actual);
    }

    @Test
    void detects120BpmPulseTrain() {
        double bpm = BPMDetector.estimateTempo(pulseTrain(120, 60), FRAME_ADVANCE, PRIOR_CENTER, PRIOR_SIGMA);
        assertWithinFourPercent(120, bpm);
    }

    @Test
    void detects300BpmBeyondOldCap() {
        // The old detector was hard-capped at 200 BPM and folded speedcore tempos.
        double bpm = BPMDetector.estimateTempo(pulseTrain(300, 60), FRAME_ADVANCE, PRIOR_CENTER, PRIOR_SIGMA);
        assertWithinFourPercent(300, bpm);
    }

    @Test
    void flatOdfReturnsDefault() {
        double[] flat = new double[20000];
        java.util.Arrays.fill(flat, 0.5);
        assertEquals(120.0, BPMDetector.estimateTempo(flat, FRAME_ADVANCE, PRIOR_CENTER, PRIOR_SIGMA));
    }

    @Test
    void shortOdfReturnsDefault() {
        assertEquals(120.0, BPMDetector.estimateTempo(new double[10], FRAME_ADVANCE, PRIOR_CENTER, PRIOR_SIGMA));
    }

    @Test
    void autocorrelationOfPeriodicSignalPeaksAtPeriod() {
        int period = 50;
        double[] signal = new double[5000];
        for (int i = 0; i < signal.length; i += period) signal[i] = 1.0;

        double[] autocorr = BPMDetector.computeAutocorrelation(signal, 200);
        assertEquals(1.0, autocorr[0], 1e-9, "lag 0 normalized to 1");
        assertTrue(autocorr[period] > 0.8, "strong correlation at the true period, got " + autocorr[period]);
        assertTrue(autocorr[period / 2] < 0.2, "no correlation at half period, got " + autocorr[period / 2]);
    }

    @Test
    void autocorrelationOfFlatSignalIsZero() {
        double[] flat = new double[1000];
        java.util.Arrays.fill(flat, 3.0);
        double[] autocorr = BPMDetector.computeAutocorrelation(flat, 100);
        for (double v : autocorr) assertEquals(0.0, v);
    }
}
