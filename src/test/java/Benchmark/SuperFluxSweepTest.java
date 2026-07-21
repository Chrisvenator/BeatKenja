package Benchmark;

import AudioAnalysis.SpectrogramCalculator;
import AudioAnalysis.SuperFluxOnsetDetector;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Parameter sweep for the SuperFlux onset detector against the ground-truth corpus
 * (Expert+ tier only). Sweeps mu (reference-frame distance), delta (threshold as multiple
 * of the ODF positive mean) and a constant time shift; reports corpus-mean P/R/F@50ms.
 * <p>
 * Run with: {@code mvn test -Dtest=SuperFluxSweepTest -Dsurefire.excludedGroups=none}
 * Results land in docs/research/benchmark_results/sweep_superflux_<date>.md.
 */
@Tag("benchmark")
class SuperFluxSweepTest {

    private static final double SAMPLE_RATE = 44100.0;
    private static final int FFT_SIZE = 2048;
    private static final int HOP_SIZE = 256;
    private static final double FRAME_ADVANCE = HOP_SIZE / SAMPLE_RATE;
    private static final double TOLERANCE = 0.050;
    private static final double CHORD_DEDUPE_GAP = 0.025;
    private static final double EXPERT_PLUS_BASE_GAP = 0.065; // seconds at 120 BPM

    private static final int[] MU_VALUES = {1, 2, 3, 4};
    private static final double[] DELTA_RELATIVE_VALUES = {0.4, 0.6, 0.8, 1.0, 1.2, 1.5, 2.0, 2.5};
    private static final double[] TIME_SHIFT_VALUES = {0.000, 0.012, 0.023};

    /** Per-map data reused across all parameter combinations. */
    private record PreparedMap(String id, double[] reference, double minGapSeconds,
                               double[][] odfPerMu, double[] positiveMeanPerMu) {}

    private record SweepResult(int mu, double deltaRel, double timeShift,
                               double precision, double recall, double fMeasure) {}

    @Test
    void sweepExpertPlus() throws Exception {
        List<GroundTruthCorpus.CorpusMap> corpus = GroundTruthCorpus.load();
        assumeTrue(!corpus.isEmpty(), "No corpus at " + GroundTruthCorpus.corpusDirectory());

        List<PreparedMap> prepared = new ArrayList<>();
        for (GroundTruthCorpus.CorpusMap map : corpus) {
            if (map.variableBpm()) continue;
            File wav = GroundTruthCorpus.decodeToWav(map);
            double[][] spec = SpectrogramCalculator.calculateSpectrogram(
                    wav.getAbsolutePath(), FFT_SIZE, FFT_SIZE - HOP_SIZE);

            double[][] odfPerMu = new double[MU_VALUES.length][];
            double[] posMeanPerMu = new double[MU_VALUES.length];
            for (int m = 0; m < MU_VALUES.length; m++) {
                odfPerMu[m] = SuperFluxOnsetDetector.computeODF(spec, SAMPLE_RATE, MU_VALUES[m]);
                posMeanPerMu[m] = SuperFluxOnsetDetector.positiveMean(odfPerMu[m]);
            }
            double[] reference = OnsetEvaluator.dedupe(map.noteTimesSeconds(), CHORD_DEDUPE_GAP);
            double minGap = EXPERT_PLUS_BASE_GAP * 120.0 / map.bpm();
            prepared.add(new PreparedMap(map.id(), reference, minGap, odfPerMu, posMeanPerMu));
            System.out.printf("prepared %s (%s)%n", map.id(), map.songName());
        }

        List<SweepResult> results = new ArrayList<>();
        for (int m = 0; m < MU_VALUES.length; m++) {
            for (double deltaRel : DELTA_RELATIVE_VALUES) {
                for (double shift : TIME_SHIFT_VALUES) {
                    double sumP = 0;
                    double sumR = 0;
                    double sumF = 0;
                    for (PreparedMap map : prepared) {
                        double delta = deltaRel * map.positiveMeanPerMu()[m];
                        ArrayList<Double> peaks = SuperFluxOnsetDetector.pickPeaks(
                                map.odfPerMu()[m], FRAME_ADVANCE, shift, delta, map.minGapSeconds());
                        double[] estimated = peaks.stream().mapToDouble(Double::doubleValue).toArray();
                        OnsetEvaluator.Result r = OnsetEvaluator.evaluate(map.reference(), estimated, TOLERANCE);
                        sumP += r.precision();
                        sumR += r.recall();
                        sumF += r.fMeasure();
                    }
                    int n = prepared.size();
                    results.add(new SweepResult(MU_VALUES[m], deltaRel, shift,
                            sumP / n, sumR / n, sumF / n));
                }
            }
        }

        results.sort(Comparator.comparingDouble(SweepResult::fMeasure).reversed());

        StringBuilder report = new StringBuilder();
        report.append("# SuperFlux parameter sweep — ").append(LocalDate.now()).append("\n\n");
        report.append("Expert+ tier, ").append(prepared.size())
                .append(" constant-BPM maps, F-measure @50ms, tempo-scaled min-gap ")
                .append(EXPERT_PLUS_BASE_GAP).append("s@120BPM. Sorted by F.\n\n");
        report.append("| mu | deltaRel | shift(ms) | P@50 | R@50 | F@50 |\n");
        report.append("|---|---|---|---|---|---|\n");
        for (SweepResult r : results) {
            report.append(String.format("| %d | %.2f | %.0f | %.3f | %.3f | %.3f |%n",
                    r.mu(), r.deltaRel(), r.timeShift() * 1000, r.precision(), r.recall(), r.fMeasure()));
        }

        Path resultsDir = Path.of("docs", "research", "benchmark_results");
        Files.createDirectories(resultsDir);
        Path outFile = resultsDir.resolve("sweep_superflux_" + LocalDate.now() + ".md");
        Files.writeString(outFile, report.toString(), StandardCharsets.UTF_8,
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        System.out.println("\n=== Sweep results written to " + outFile + " ===\n");

        for (int i = 0; i < Math.min(15, results.size()); i++) {
            SweepResult r = results.get(i);
            System.out.printf("mu=%d deltaRel=%.2f shift=%.0fms  P=%.3f R=%.3f F=%.3f%n",
                    r.mu(), r.deltaRel(), r.timeShift() * 1000, r.precision(), r.recall(), r.fMeasure());
        }
    }
}
