package MapGeneration.StyleSpace;

import BeatSaberObjects.Objects.Note;

import java.io.Serializable;
import java.util.Arrays;

/**
 * A fixed-length feature vector describing the "feel" of a Beat Saber map diff.
 *
 * <p>Axes (all normalised 0–1 unless noted):
 * <ol>
 *   <li>cutDirectionEntropy   — Shannon entropy of cut-direction distribution (0=mono-dir, 1=uniform over 8)</li>
 *   <li>diagonalRatio         — fraction of notes with diagonal cuts (4,5,6,7)</li>
 *   <li>upDownRatio           — fraction with strictly vertical cuts (0=up, 1=down)</li>
 *   <li>horizontalRatio       — fraction with strictly horizontal cuts (2=left, 3=right)</li>
 *   <li>dotRatio              — fraction any-direction (cutDirection=8)</li>
 *   <li>topLayerRatio         — fraction of notes in top row (lineLayer=2)</li>
 *   <li>midLayerRatio         — fraction in middle row (lineLayer=1)</li>
 *   <li>bottomLayerRatio      — fraction in bottom row (lineLayer=0)</li>
 *   <li>leftColumnRatio       — fraction in left column (lineIndex 0–1)</li>
 *   <li>rightColumnRatio      — fraction in right column (lineIndex 2–3)</li>
 *   <li>crossColumnRatio      — fraction where consecutive notes cross the centre line</li>
 *   <li>streamRatio           — fraction of inter-note gaps ≤ 0.25 beats (stream density)</li>
 *   <li>sparseRatio           — fraction of inter-note gaps > 1.0 beats</li>
 *   <li>layerJumpRatio        — fraction where consecutive notes jump ≥2 layers</li>
 *   <li>resetRatio            — fraction where same cut direction repeats within 4 notes (anti-flow reset proxy)</li>
 * </ol>
 */
public class StyleVector implements Serializable {

    public static final int DIMENSIONS = 15;

    public final float[] axes;

    StyleVector(float[] axes) {
        this.axes = axes;
    }

    /**
     * Computes the style vector for one color (type 0=red, 1=blue) from a note array.
     * Returns a zero vector if fewer than 4 notes of the requested type are present.
     */
    public static StyleVector compute(Note[] notes, int type) {
        Note[] typed = filterType(notes, type);
        if (typed.length < 4) return zero();

        float[] axes = new float[DIMENSIONS];

        // --- cut-direction distribution ---
        int[] cdCount = new int[9];
        for (Note n : typed) if (n._cutDirection >= 0 && n._cutDirection <= 8) cdCount[n._cutDirection]++;

        float total = typed.length;

        // entropy over directions 0–7 (skip dot=8 for entropy, it's its own axis)
        float nonDot = 0;
        for (int i = 0; i < 8; i++) nonDot += cdCount[i];
        float entropy = 0;
        if (nonDot > 0) {
            for (int i = 0; i < 8; i++) {
                if (cdCount[i] <= 0) continue;
                float p = cdCount[i] / nonDot;
                entropy -= p * (float) (Math.log(p) / Math.log(8)); // normalised to [0,1]
            }
        }
        axes[0] = entropy;
        axes[1] = (cdCount[4] + cdCount[5] + cdCount[6] + cdCount[7]) / total; // diagonal
        axes[2] = (cdCount[0] + cdCount[1]) / total;                             // up+down
        axes[3] = (cdCount[2] + cdCount[3]) / total;                             // horizontal
        axes[4] = cdCount[8] / total;                                             // dot

        // --- layer / column distribution ---
        int[] llCount = new int[3];
        int[] liLeft = new int[1], liRight = new int[1];
        for (Note n : typed) {
            int ll = (int) Math.round(n._lineLayer);
            int li = (int) Math.round(n._lineIndex);
            if (ll >= 0 && ll <= 2) llCount[ll]++;
            if (li <= 1) liLeft[0]++; else liRight[0]++;
        }
        axes[5] = llCount[2] / total;  // top
        axes[6] = llCount[1] / total;  // mid
        axes[7] = llCount[0] / total;  // bottom
        axes[8] = liLeft[0]  / total;  // left columns
        axes[9] = liRight[0] / total;  // right columns

        // --- consecutive-note metrics ---
        int crossColumn = 0, streamGaps = 0, sparseGaps = 0, layerJumps = 0, resets = 0;
        for (int i = 1; i < typed.length; i++) {
            Note prev = typed[i - 1];
            Note curr = typed[i];

            // cross-centre: one note left half, next right half
            boolean prevLeft = prev._lineIndex <= 1;
            boolean currLeft = curr._lineIndex <= 1;
            if (prevLeft != currLeft) crossColumn++;

            // gap buckets
            float gap = curr._time - prev._time;
            if (gap <= 0.25f) streamGaps++;
            if (gap > 1.0f)   sparseGaps++;

            // layer jump ≥2
            if (Math.abs(curr._lineLayer - prev._lineLayer) >= 2) layerJumps++;
        }

        // reset: same cut direction within 4-note window
        for (int i = 0; i + 3 < typed.length; i++) {
            int cd = typed[i]._cutDirection;
            if (cd == 8) continue;
            for (int k = 1; k <= 3; k++) {
                if (typed[i + k]._cutDirection == cd) { resets++; break; }
            }
        }

        float pairs = typed.length - 1;
        axes[10] = crossColumn / pairs;
        axes[11] = streamGaps  / pairs;
        axes[12] = sparseGaps  / pairs;
        axes[13] = layerJumps  / pairs;
        axes[14] = resets      / (float) Math.max(1, typed.length - 3);

        return new StyleVector(axes);
    }

    /** Euclidean distance between two style vectors. */
    public float distanceTo(StyleVector other) {
        float sum = 0;
        for (int i = 0; i < DIMENSIONS; i++) {
            float d = axes[i] - other.axes[i];
            sum += d * d;
        }
        return (float) Math.sqrt(sum);
    }

    /** Weighted average of multiple vectors (weights need not sum to 1). */
    public static StyleVector blend(StyleVector[] vectors, float[] weights) {
        float[] result = new float[DIMENSIONS];
        float totalW = 0;
        for (float w : weights) totalW += w;
        for (int k = 0; k < vectors.length; k++) {
            float w = weights[k] / totalW;
            for (int i = 0; i < DIMENSIONS; i++) result[i] += vectors[k].axes[i] * w;
        }
        return new StyleVector(result);
    }

    public static StyleVector zero() {
        return new StyleVector(new float[DIMENSIONS]);
    }

    /** Linear interpolation between two vectors (t=0 → a, t=1 → b). */
    public static StyleVector lerp(StyleVector a, StyleVector b, float t) {
        float[] result = new float[DIMENSIONS];
        for (int i = 0; i < DIMENSIONS; i++) result[i] = a.axes[i] + t * (b.axes[i] - a.axes[i]);
        return new StyleVector(result);
    }

    private static Note[] filterType(Note[] notes, int type) {
        if (notes == null) return new Note[0];
        java.util.List<Note> out = new java.util.ArrayList<>();
        for (Note n : notes) if (n != null && n._type == type) out.add(n);
        return out.toArray(new Note[0]);
    }

    @Override
    public String toString() {
        return "StyleVector" + Arrays.toString(axes);
    }
}
