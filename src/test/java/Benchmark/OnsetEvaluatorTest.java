package Benchmark;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class OnsetEvaluatorTest {

    @Test
    void perfectMatchGivesFOne() {
        double[] ref = {1.0, 2.0, 3.0};
        OnsetEvaluator.Result r = OnsetEvaluator.evaluate(ref, ref.clone(), 0.05);
        assertThat(r.fMeasure()).isEqualTo(1.0);
        assertThat(r.truePositives()).isEqualTo(3);
    }

    @Test
    void matchWithinToleranceCounts() {
        double[] ref = {1.0, 2.0};
        double[] est = {1.04, 2.06}; // second is 60ms off -> miss at 50ms tolerance
        OnsetEvaluator.Result r = OnsetEvaluator.evaluate(ref, est, 0.05);
        assertThat(r.truePositives()).isEqualTo(1);
        assertThat(r.falsePositives()).isEqualTo(1);
        assertThat(r.falseNegatives()).isEqualTo(1);
        assertThat(r.precision()).isEqualTo(0.5);
        assertThat(r.recall()).isEqualTo(0.5);
    }

    @Test
    void oneToOneMatchingNoDoubleCounting() {
        // two estimates near one reference: only one may match
        double[] ref = {1.0};
        double[] est = {0.98, 1.02};
        OnsetEvaluator.Result r = OnsetEvaluator.evaluate(ref, est, 0.05);
        assertThat(r.truePositives()).isEqualTo(1);
        assertThat(r.falsePositives()).isEqualTo(1);
    }

    @Test
    void emptyEstimatesGiveZero() {
        OnsetEvaluator.Result r = OnsetEvaluator.evaluate(new double[]{1.0}, new double[]{}, 0.05);
        assertThat(r.fMeasure()).isEqualTo(0.0);
        assertThat(r.recall()).isEqualTo(0.0);
    }

    @Test
    void dedupeRemovesChords() {
        double[] times = {1.0, 1.001, 1.002, 2.0}; // chord at 1.0
        double[] deduped = OnsetEvaluator.dedupe(times, 0.025);
        assertThat(deduped).containsExactly(1.0, 2.0);
    }

    @Test
    void bpmAccuracyOctaves() {
        assertThat(OnsetEvaluator.bpmAccuracy1(300, 150)).isFalse();
        assertThat(OnsetEvaluator.bpmAccuracy2(300, 150)).isTrue();  // half-tempo octave
        assertThat(OnsetEvaluator.bpmAccuracy2(300, 100)).isTrue();  // third-tempo
        assertThat(OnsetEvaluator.bpmAccuracy2(300, 130)).isFalse();
        assertThat(OnsetEvaluator.bpmAccuracy1(200, 205)).isTrue();  // within 4%
    }
}
