package MapGeneration.GenerationElements;

import BeatSaberObjects.Objects.Note;
import DataManager.Corpus.GapBucket;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 2nd-order Markov transition table with Katz backoff.
 *
 * <p>State: {@code (prevPrev index, prev index, GapBucket)} → weighted successor counts.
 * Lookup produces a {@link PatternProbability} ready for use in
 * {@link ComplexPattern#predictNextNote}.
 *
 * <p>Backoff chain: 2nd-order → 1st-order → supplied baseline {@link Pattern}.
 * Falls back when the 2nd-order state has fewer than {@value #BACKOFF_THRESHOLD} observations.
 *
 * <p>Training: call {@link #trainFrom} per map difficulty. The {@code weight} parameter
 * implements quality weighting (Ranked=10, Curated=5, Verified=3, Normal=1).
 */
public class HigherOrderPattern {

    private static final Logger logger = LogManager.getLogger(HigherOrderPattern.class);

    /** Min total weighted count in a state row before falling back. */
    private static final int BACKOFF_THRESHOLD = 5;

    /** Canonical note state indices: lineIndex*27 + lineLayer*9 + cutDirection → 0..107. */
    private static final int STATE_COUNT = 108;

    private record StateKey(int ppIdx, int pIdx, GapBucket gap) {}

    /** 2nd-order: state → successor counts. */
    private final Map<StateKey, int[]>  secondOrderCounts = new HashMap<>();
    /** Canonical Note for each state index (populated on first observation). */
    private final Note[] stateNotes = new Note[STATE_COUNT];

    /** 1st-order fallback counts[prevIdx][nextIdx]. */
    private final int[][] firstOrderCounts = new int[STATE_COUNT][STATE_COUNT];

    private int totalObservations = 0;

    // -----------------------------------------------------------------------
    // Training
    // -----------------------------------------------------------------------

    /**
     * Records (prevPrev → prev → next) with quality weight.
     *
     * @param prevPrev note two steps back (same color)
     * @param prev     note one step back (same color)
     * @param next     note being placed
     * @param beatGap  gap in beats between prev and next
     * @param weight   quality weight; counts added {@code weight} times
     */
    public void observe(Note prevPrev, Note prev, Note next, float beatGap, int weight) {
        if (prevPrev == null || prev == null || next == null) return;
        if (next._cutDirection == 8) return;

        int ppIdx = toIndex(prevPrev);
        int pIdx  = toIndex(prev);
        int nIdx  = toIndex(next);
        if (ppIdx < 0 || pIdx < 0 || nIdx < 0) return;

        // Store canonical notes for later lookup
        if (stateNotes[ppIdx] == null) stateNotes[ppIdx] = canonical(prevPrev);
        if (stateNotes[pIdx]  == null) stateNotes[pIdx]  = canonical(prev);
        if (stateNotes[nIdx]  == null) stateNotes[nIdx]  = canonical(next);

        // 2nd-order
        StateKey key = new StateKey(ppIdx, pIdx, GapBucket.fromBeatGap(beatGap));
        int[] row = secondOrderCounts.computeIfAbsent(key, k -> new int[STATE_COUNT]);
        row[nIdx] += weight;

        // 1st-order fallback
        firstOrderCounts[pIdx][nIdx] += weight;

        totalObservations += weight;
    }

    /** Trains all consecutive triples in {@code notes} for one color. */
    public void trainFrom(Note[] notes, int type, int weight) {
        Note[] typed = filterType(notes, type);
        for (int k = 2; k < typed.length; k++) {
            Note pp = typed[k - 2];
            Note p  = typed[k - 1];
            Note n  = typed[k];
            if (pp == null || p == null || n == null) continue;
            observe(pp, p, n._time - p._time, n, weight);
        }
    }

    // Overload with gap before next (cleaner call-site)
    private void observe(Note pp, Note p, float gap, Note next, int weight) {
        observe(pp, p, next, gap, weight);
    }

    // -----------------------------------------------------------------------
    // Lookup
    // -----------------------------------------------------------------------

    /**
     * Returns a {@link PatternProbability} for the given context, with backoff.
     *
     * <p>Priority: 2nd-order (if ≥ {@value #BACKOFF_THRESHOLD} obs) → 1st-order → baseline.
     *
     * @param prevPrev note two steps back; may be null (forces 1st-order backoff)
     * @param prev     note one step back; may be null (forces baseline)
     * @param beatGap  gap in beats from prev to current slot
     * @param baseline fallback Pattern from the original 1st-order corpus
     * @param time     beat time to stamp on the returned note candidates
     */
    public PatternProbability getProbability(Note prevPrev, Note prev, float beatGap, Pattern baseline, float time) {
        // 2nd-order attempt
        if (prevPrev != null && prev != null) {
            int ppIdx = toIndex(prevPrev);
            int pIdx  = toIndex(prev);
            if (ppIdx >= 0 && pIdx >= 0) {
                GapBucket gap = GapBucket.fromBeatGap(beatGap);
                int[] row = secondOrderCounts.get(new StateKey(ppIdx, pIdx, gap));
                if (row != null && sumRow(row) >= BACKOFF_THRESHOLD) {
                    PatternProbability pp2 = rowToPatternProbability(row, time, prev._type);
                    if (pp2 != null) return pp2;
                }
            }
        }

        // 1st-order backoff
        if (prev != null) {
            int pIdx = toIndex(prev);
            if (pIdx >= 0) {
                int[] row = firstOrderCounts[pIdx];
                if (sumRow(row) >= BACKOFF_THRESHOLD) {
                    PatternProbability pp1 = rowToPatternProbability(row, time, prev._type);
                    if (pp1 != null) return pp1;
                }
            }
        }

        // Baseline fallback — delegate to existing Pattern
        if (baseline != null && prev != null) {
            return baseline.getProbabilityOf(prev);
        }
        return null;
    }

    // -----------------------------------------------------------------------
    // Private helpers
    // -----------------------------------------------------------------------

    private PatternProbability rowToPatternProbability(int[] counts, float time, int type) {
        int total = sumRow(counts);
        if (total <= 0) return null;

        List<Note> notes = new ArrayList<>();
        List<Float> probs = new ArrayList<>();

        for (int j = 0; j < STATE_COUNT; j++) {
            if (counts[j] <= 0) continue;
            Note n = stateNotes[j];
            if (n == null) n = indexToNote(j, type);
            if (n == null) continue;
            notes.add(new Note(time, (int) n._lineIndex, (int) n._lineLayer, n._type, n._cutDirection));
            probs.add(counts[j] * 100.0f / total);
        }

        if (notes.isEmpty()) return null;

        Note[]  noteArr = notes.toArray(new Note[0]);
        float[] probArr = new float[probs.size()];
        for (int i = 0; i < probs.size(); i++) probArr[i] = probs.get(i);
        return new PatternProbability(noteArr, probArr);
    }

    private static int sumRow(int[] row) {
        int s = 0;
        for (int v : row) s += v;
        return s;
    }

    /**
     * Maps a Note to its state index: lineIndex*27 + lineLayer*9 + cutDirection.
     * Returns -1 for any out-of-range values.
     */
    public static int toIndex(Note n) {
        int li = (int) n._lineIndex;
        int ll = (int) n._lineLayer;
        int cd = n._cutDirection;
        if (li < 0 || li > 3 || ll < 0 || ll > 2 || cd < 0 || cd > 8) return -1;
        return li * 27 + ll * 9 + cd;
    }

    public static Note indexToNote(int idx, int type) {
        if (idx < 0 || idx >= STATE_COUNT) return null;
        int li = idx / 27;
        int ll = (idx % 27) / 9;
        int cd = idx % 9;
        return new Note(0, li, ll, type, cd);
    }

    private static Note canonical(Note n) {
        return new Note(0, (int) n._lineIndex, (int) n._lineLayer, n._type, n._cutDirection);
    }

    private static Note[] filterType(Note[] notes, int type) {
        if (notes == null) return new Note[0];
        List<Note> out = new ArrayList<>();
        for (Note n : notes) if (n != null && n._type == type) out.add(n);
        return out.toArray(new Note[0]);
    }

    // -----------------------------------------------------------------------
    // Stats
    // -----------------------------------------------------------------------

    public int getTotalObservations() { return totalObservations; }
    public int getSecondOrderStateCount() { return secondOrderCounts.size(); }
}
