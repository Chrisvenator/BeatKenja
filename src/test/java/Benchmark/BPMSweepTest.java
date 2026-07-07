package Benchmark;

import AudioAnalysis.BPMDetector;
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
 * Parameter sweep for the tempo-prior of {@link BPMDetector} against the ground-truth corpus
 * (all maps, including variable-BPM ones — BPM eval uses the map's initial BPM metadata).
 * The ODF autocorrelation is computed once per map; only the prior (center, sigma) is swept.
 * <p>
 * Run with: {@code mvn test -Dtest=BPMSweepTest -Dsurefire.excludedGroups=none}
 * Results land in docs/research/benchmark_results/sweep_bpm_<date>.md.
 */
@Tag("benchmark")
class BPMSweepTest {

    private static final double SAMPLE_RATE = 44100.0;
    private static final int FFT_SIZE = 2048;
    private static final int HOP_SIZE = 256;
    private static final double FRAME_ADVANCE = HOP_SIZE / SAMPLE_RATE;
    private static final int MU = 2;
    /** 4 comb harmonics x beat period of the slowest tempo (50 BPM), mirroring BPMDetector. */
    private static final int MAX_LAG = (int) Math.ceil(4 * 60.0 / (50.0 * FRAME_ADVANCE)) + 1;

    private static final double[] CENTER_VALUES = {180, 200, 220, 240, 260, 280};
    private static final double[] SIGMA_VALUES = {0.4, 0.5, 0.65, 0.8, 1.0, 1.2};

    private record PreparedMap(String id, String song, double trueBpm, double[] autocorr) {}

    private record SweepResult(double center, double sigma, int acc1, int acc2) {}

    @Test
    void sweepTempoPrior() throws Exception {
        List<GroundTruthCorpus.CorpusMap> corpus = GroundTruthCorpus.load();
        assumeTrue(!corpus.isEmpty(), "No corpus at " + GroundTruthCorpus.corpusDirectory());

        List<PreparedMap> prepared = new ArrayList<>();
        for (GroundTruthCorpus.CorpusMap map : corpus) {
            File wav = GroundTruthCorpus.decodeToWav(map);
            double[][] spec = SpectrogramCalculator.calculateSpectrogram(
                    wav.getAbsolutePath(), FFT_SIZE, FFT_SIZE - HOP_SIZE);
            double[] odf = SuperFluxOnsetDetector.computeODF(spec, SAMPLE_RATE, MU);
            double[] autocorr = BPMDetector.computeAutocorrelation(
                    odf, Math.min(MAX_LAG, odf.length - 1));
            prepared.add(new PreparedMap(map.id(), map.songName(), map.bpm(), autocorr));
            System.out.printf("prepared %s (%s)%n", map.id(), map.songName());
        }

        List<SweepResult> results = new ArrayList<>();
        for (double center : CENTER_VALUES) {
            for (double sigma : SIGMA_VALUES) {
                int acc1 = 0;
                int acc2 = 0;
                for (PreparedMap map : prepared) {
                    double detected = BPMDetector.pickTempo(map.autocorr(), FRAME_ADVANCE, center, sigma);
                    if (OnsetEvaluator.bpmAccuracy1(map.trueBpm(), detected)) acc1++;
                    if (OnsetEvaluator.bpmAccuracy2(map.trueBpm(), detected)) acc2++;
                }
                results.add(new SweepResult(center, sigma, acc1, acc2));
            }
        }

        results.sort(Comparator.comparingInt(SweepResult::acc1)
                .thenComparingInt(SweepResult::acc2).reversed());
        SweepResult best = results.get(0);

        int n = prepared.size();
        StringBuilder report = new StringBuilder();
        report.append("# BPM tempo-prior sweep — ").append(LocalDate.now()).append("\n\n");
        report.append("SuperFlux ODF autocorrelation + harmonic comb (1/k, k=1..4), ")
                .append(n).append(" maps, range 50-420 BPM. Sorted by Acc1, then Acc2.\n\n");
        report.append("| priorCenter | priorSigma | Acc1 | Acc2 |\n");
        report.append("|---|---|---|---|\n");
        for (SweepResult r : results) {
            report.append(String.format("| %.0f | %.2f | %d/%d (%.1f%%) | %d/%d (%.1f%%) |%n",
                    r.center(), r.sigma(), r.acc1(), n, 100.0 * r.acc1() / n,
                    r.acc2(), n, 100.0 * r.acc2() / n));
        }

        report.append(String.format("%n## Per-map results at best combo (center=%.0f, sigma=%.2f)%n%n",
                best.center(), best.sigma()));
        report.append("| id | song | BPM | detBPM | Acc1 | Acc2 |\n");
        report.append("|---|---|---|---|---|---|\n");
        for (PreparedMap map : prepared) {
            double detected = BPMDetector.pickTempo(map.autocorr(), FRAME_ADVANCE, best.center(), best.sigma());
            report.append(String.format("| %s | %s | %.1f | %.1f | %s | %s |%n",
                    map.id(), map.song().replace("|", "/"), map.trueBpm(), detected,
                    OnsetEvaluator.bpmAccuracy1(map.trueBpm(), detected) ? "✓" : "✗",
                    OnsetEvaluator.bpmAccuracy2(map.trueBpm(), detected) ? "✓" : "✗"));
        }

        Path resultsDir = Path.of("docs", "research", "benchmark_results");
        Files.createDirectories(resultsDir);
        Path outFile = resultsDir.resolve("sweep_bpm_" + LocalDate.now() + ".md");
        Files.writeString(outFile, report.toString(), StandardCharsets.UTF_8,
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        System.out.println("\n=== Sweep results written to " + outFile + " ===\n");

        for (int i = 0; i < Math.min(10, results.size()); i++) {
            SweepResult r = results.get(i);
            System.out.printf("center=%.0f sigma=%.2f  Acc1=%d/%d Acc2=%d/%d%n",
                    r.center(), r.sigma(), r.acc1(), n, r.acc2(), n);
        }
    }
}
