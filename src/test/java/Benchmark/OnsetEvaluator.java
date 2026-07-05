package Benchmark;

import java.util.Arrays;

/**
 * Computes onset detection metrics (Precision, Recall, F-measure) following the
 * mir_eval convention: greedy one-to-one matching of estimated onsets to reference
 * onsets within a time tolerance window.
 * <p>
 * Reference onsets come from hand-mapped ranked maps (note times). Note that mappers
 * deliberately undermap (skip onsets), so Precision is expected to be unfairly
 * penalized — Recall is the more meaningful single number against this ground truth.
 * See docs/research/SYNC_RESEARCH_LOG.md §6 (benchmark protocol) and H4.
 */
public final class OnsetEvaluator {

    private OnsetEvaluator() {}

    public record Result(int truePositives, int falsePositives, int falseNegatives,
                         double precision, double recall, double fMeasure) {

        @Override
        public String toString() {
            return String.format("P=%.3f R=%.3f F=%.3f (TP=%d FP=%d FN=%d)",
                    precision, recall, fMeasure, truePositives, falsePositives, falseNegatives);
        }
    }

    /**
     * Matches estimated onsets against reference onsets one-to-one within {@code toleranceSeconds}.
     * Both arrays are sorted internally; duplicates within 1 ms should be removed by the caller
     * (e.g. chords/stacks deduped to one event).
     */
    public static Result evaluate(double[] reference, double[] estimated, double toleranceSeconds) {
        double[] ref = reference.clone();
        double[] est = estimated.clone();
        Arrays.sort(ref);
        Arrays.sort(est);

        // Greedy in-order matching: advance through both sorted lists, matching each
        // reference onset to the earliest unmatched estimate within tolerance.
        int tp = 0;
        int i = 0;
        int j = 0;
        while (i < ref.length && j < est.length) {
            double diff = est[j] - ref[i];
            if (Math.abs(diff) <= toleranceSeconds) {
                tp++;
                i++;
                j++;
            } else if (diff < 0) {
                j++; // estimate too early for this reference: discard estimate
            } else {
                i++; // reference has no estimate within tolerance: miss
            }
        }

        int fp = est.length - tp;
        int fn = ref.length - tp;
        double precision = est.length == 0 ? 0.0 : (double) tp / est.length;
        double recall = ref.length == 0 ? 0.0 : (double) tp / ref.length;
        double f = (precision + recall) == 0 ? 0.0 : 2 * precision * recall / (precision + recall);
        return new Result(tp, fp, fn, precision, recall, f);
    }

    /** Removes events closer than {@code minGapSeconds} to their predecessor (chord/stack dedupe). */
    public static double[] dedupe(double[] times, double minGapSeconds) {
        if (times.length == 0) return times;
        double[] sorted = times.clone();
        Arrays.sort(sorted);
        double[] out = new double[sorted.length];
        int n = 0;
        out[n++] = sorted[0];
        for (int i = 1; i < sorted.length; i++) {
            if (sorted[i] - out[n - 1] >= minGapSeconds) out[n++] = sorted[i];
        }
        return Arrays.copyOf(out, n);
    }

    /** BPM accuracy following common MIREX conventions. Accuracy1: within ±4% of reference. */
    public static boolean bpmAccuracy1(double referenceBpm, double detectedBpm) {
        return Math.abs(detectedBpm - referenceBpm) / referenceBpm <= 0.04;
    }

    /** Accuracy2: correct within ±4% allowing octave errors (1/3x, 1/2x, 1x, 2x, 3x). */
    public static boolean bpmAccuracy2(double referenceBpm, double detectedBpm) {
        for (double factor : new double[]{1.0 / 3, 0.5, 1.0, 2.0, 3.0}) {
            if (bpmAccuracy1(referenceBpm * factor, detectedBpm)) return true;
        }
        return false;
    }
}
