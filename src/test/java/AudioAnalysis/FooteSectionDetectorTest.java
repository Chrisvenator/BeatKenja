package AudioAnalysis;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link FooteSectionDetector} on synthetic filtered log spectrograms.
 */
class FooteSectionDetectorTest {

    private static final double FRAME_ADVANCE = 256.0 / 44100.0;
    private static final int BANDS = 24;

    private static final double KERNEL_SECONDS = 16;
    private static final double MIN_SECTION_SECONDS = 8;
    private static final double THRESHOLD = 0.3;

    /**
     * Builds a spectrogram whose active band set switches at the given section starts;
     * section i uses bands [pattern[i] * BANDS/2, ...) half of the spectrum.
     */
    private static double[][] blockSpectrogram(double durationSeconds, double[] sectionStarts, int[] pattern) {
        int frames = (int) (durationSeconds / FRAME_ADVANCE);
        double[][] spec = new double[frames][BANDS];
        Random random = new Random(42);
        for (int n = 0; n < frames; n++) {
            double t = n * FRAME_ADVANCE;
            int section = 0;
            for (int s = 0; s < sectionStarts.length; s++) {
                if (t >= sectionStarts[s]) section = s;
            }
            int offset = pattern[section] * BANDS / 2;
            for (int b = 0; b < BANDS / 2; b++) {
                spec[n][offset + b] = 1.0;
            }
            for (int b = 0; b < BANDS; b++) {
                spec[n][b] += 0.05 * random.nextDouble();
            }
        }
        return spec;
    }

    @Test
    void detectsBoundaryBetweenOrthogonalBlocks() {
        double[][] spec = blockSpectrogram(120, new double[]{0, 60}, new int[]{0, 1});
        FooteSectionDetector.Result result = FooteSectionDetector.detect(
                spec, FRAME_ADVANCE, KERNEL_SECONDS, MIN_SECTION_SECONDS, THRESHOLD);

        assertEquals(1, result.boundaries().size(), "one boundary, got: " + result.boundaries());
        assertEquals(60, result.boundaries().get(0), 2.0);
    }

    @Test
    void homogeneousAudioHasNoBoundaries() {
        double[][] spec = blockSpectrogram(120, new double[]{0}, new int[]{0});
        FooteSectionDetector.Result result = FooteSectionDetector.detect(
                spec, FRAME_ADVANCE, KERNEL_SECONDS, MIN_SECTION_SECONDS, THRESHOLD);

        assertTrue(result.boundaries().isEmpty(), "no boundaries, got: " + result.boundaries());
    }

    @Test
    void detectsBothBoundariesOfAbaStructure() {
        double[][] spec = blockSpectrogram(120, new double[]{0, 40, 80}, new int[]{0, 1, 0});
        FooteSectionDetector.Result result = FooteSectionDetector.detect(
                spec, FRAME_ADVANCE, KERNEL_SECONDS, MIN_SECTION_SECONDS, THRESHOLD);

        assertEquals(2, result.boundaries().size(), "two boundaries, got: " + result.boundaries());
        assertEquals(40, result.boundaries().get(0), 2.0);
        assertEquals(80, result.boundaries().get(1), 2.0);
    }

    @Test
    void ratesLoudDenseSectionAboveQuietSparseSection() {
        int frames = (int) (120 / FRAME_ADVANCE);
        double[][] spec = new double[frames][BANDS];
        for (int n = 0; n < frames; n++) {
            double level = n * FRAME_ADVANCE < 60 ? 0.2 : 2.0;
            for (int b = 0; b < BANDS; b++) spec[n][b] = level;
        }
        double[] onsets = new double[100];
        for (int i = 0; i < onsets.length; i++) onsets[i] = 60 + i * 0.5; // all in the loud half

        ArrayList<Double> boundaries = new ArrayList<>();
        boundaries.add(60.0);
        int[] tiers = FooteSectionDetector.rateIntensity(boundaries, 120, spec, FRAME_ADVANCE, onsets);

        assertEquals(2, tiers.length);
        assertTrue(tiers[1] > tiers[0], "loud+dense section must rank above quiet one, got: "
                + tiers[0] + " vs " + tiers[1]);
    }
}
