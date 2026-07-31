package MapGeneration.StyleSpace;

import BeatSaberObjects.Objects.Note;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StyleVectorTest {

    private static Note blue(float time, int li, int ll, int cd) {
        return new Note(time, li, ll, 1, cd);
    }

    @Test
    void compute_returnsZeroForTooFewNotes() {
        Note[] notes = { blue(0, 1, 0, 1), blue(1, 2, 0, 0) };
        StyleVector sv = StyleVector.compute(notes, 1);
        for (float v : sv.axes) assertEquals(0f, v, 1e-6f);
    }

    @Test
    void compute_dimensionCount() {
        Note[] notes = new Note[20];
        for (int i = 0; i < 20; i++) notes[i] = blue(i, i % 4, i % 3, i % 8);
        StyleVector sv = StyleVector.compute(notes, 1);
        assertEquals(StyleVector.DIMENSIONS, sv.axes.length);
    }

    @Test
    void compute_dotRatioCorrect() {
        // All notes have cutDirection=8 (dot)
        Note[] notes = new Note[10];
        for (int i = 0; i < 10; i++) notes[i] = blue(i, 1, 0, 8);
        StyleVector sv = StyleVector.compute(notes, 1);
        assertEquals(1.0f, sv.axes[4], 1e-5f, "dotRatio should be 1 when all notes are dots");
    }

    @Test
    void compute_streamRatioCorrect() {
        // All gaps = 0.1 beats → all STREAM
        Note[] notes = new Note[10];
        for (int i = 0; i < 10; i++) notes[i] = blue(i * 0.1f, 1, 0, 1);
        StyleVector sv = StyleVector.compute(notes, 1);
        assertEquals(1.0f, sv.axes[11], 1e-5f, "streamRatio should be 1 when all gaps ≤ 0.25");
    }

    @Test
    void compute_sparseRatioCorrect() {
        // All gaps = 2 beats → all SPARSE
        Note[] notes = new Note[10];
        for (int i = 0; i < 10; i++) notes[i] = blue(i * 2.0f, 1, 0, 1);
        StyleVector sv = StyleVector.compute(notes, 1);
        assertEquals(1.0f, sv.axes[12], 1e-5f, "sparseRatio should be 1 when all gaps > 1.0");
    }

    @Test
    void distanceTo_sameVectorIsZero() {
        Note[] notes = new Note[10];
        for (int i = 0; i < 10; i++) notes[i] = blue(i, i % 4, i % 3, i % 8);
        StyleVector sv = StyleVector.compute(notes, 1);
        assertEquals(0f, sv.distanceTo(sv), 1e-5f);
    }

    @Test
    void blend_equalWeightsIsAverage() {
        float[] a = new float[StyleVector.DIMENSIONS];
        float[] b = new float[StyleVector.DIMENSIONS];
        for (int i = 0; i < StyleVector.DIMENSIONS; i++) { a[i] = 0f; b[i] = 1f; }
        StyleVector va = new StyleVector(a);
        StyleVector vb = new StyleVector(b);
        StyleVector blended = StyleVector.blend(new StyleVector[]{va, vb}, new float[]{1f, 1f});
        for (float v : blended.axes) assertEquals(0.5f, v, 1e-5f);
    }

    @Test
    void lerp_midpointIsAverage() {
        float[] a = new float[StyleVector.DIMENSIONS];
        float[] b = new float[StyleVector.DIMENSIONS];
        for (int i = 0; i < StyleVector.DIMENSIONS; i++) { a[i] = 0f; b[i] = 1f; }
        StyleVector va = new StyleVector(a);
        StyleVector vb = new StyleVector(b);
        StyleVector mid = StyleVector.lerp(va, vb, 0.5f);
        for (float v : mid.axes) assertEquals(0.5f, v, 1e-5f);
    }
}
