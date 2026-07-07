package Benchmark;

import AudioAnalysis.AudioAnalysis;
import AudioAnalysis.BPMDetector;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Baseline benchmark of the current audio-sync pipeline against the ground-truth corpus
 * (hand-mapped ranked/curated maps in data/ground_truth).
 * <p>
 * Excluded from normal test runs (slow, needs local corpus + ffmpeg). Run with:
 * <pre>mvn test -Dtest=BaselineBenchmarkTest -Dsurefire.excludedGroups=none</pre>
 * or via the IDE. Results are appended to docs/research/benchmark_results/.
 * <p>
 * Protocol: see docs/research/SYNC_RESEARCH_LOG.md §6. Onset ground truth = note times of the
 * hardest Standard difficulty, chords deduped (25 ms), bombs excluded. Onset estimates = the
 * Expert+ list of {@link AudioAnalysis#getPeaksFromAudio} with the map's true BPM and offset=0
 * (raw audio-time comparison; the grid-alignment offset would shift all estimates uniformly).
 * Mappers undermap, so Recall matters more than Precision here (H4).
 */
@Tag("benchmark")
class BaselineBenchmarkTest {

    private static final double TOLERANCE_PRIMARY = 0.050;
    private static final double TOLERANCE_STRICT = 0.025;
    private static final double CHORD_DEDUPE_GAP = 0.025;
    private static final int EXPERT_PLUS = 4;

    @Test
    void baselineOnsetAndBpm() throws Exception {
        List<GroundTruthCorpus.CorpusMap> corpus = GroundTruthCorpus.load();
        assumeTrue(!corpus.isEmpty(), "No corpus at " + GroundTruthCorpus.corpusDirectory()
                + " — set -Dbk.corpus=<path>");

        String label = System.getProperty("bk.reportLabel", "baseline");

        StringBuilder report = new StringBuilder();
        report.append("# Benchmark \"").append(label).append("\" — ").append(LocalDate.now()).append("\n\n");
        report.append("Pipeline: current AudioAnalysis, true BPM, offset=0.\n\n");
        report.append("| id | song | BPM | varBPM | P@50 | R@50 | F@50 | F@25 | detBPM | Acc1 | Acc2 |\n");
        report.append("|---|---|---|---|---|---|---|---|---|---|---|\n");

        List<Double> fScores = new ArrayList<>();
        List<Double> precisions = new ArrayList<>();
        List<Double> recalls = new ArrayList<>();
        int bpmAcc1 = 0;
        int bpmAcc2 = 0;
        int bpmTotal = 0;

        for (GroundTruthCorpus.CorpusMap map : corpus) {
            File wav;
            try {
                wav = GroundTruthCorpus.decodeToWav(map);
            } catch (IOException | InterruptedException e) {
                report.append(String.format("| %s | %s | %.1f | - | decode FAILED: %s |%n",
                        map.id(), map.songName(), map.bpm(), e.getMessage()));
                continue;
            }

            // --- BPM detection (all maps) ---
            double detectedBpm;
            try {
                detectedBpm = BPMDetector.detectBPM(wav.getAbsolutePath());
            } catch (Exception e) {
                detectedBpm = -1;
            }
            boolean acc1 = detectedBpm > 0 && OnsetEvaluator.bpmAccuracy1(map.bpm(), detectedBpm);
            boolean acc2 = detectedBpm > 0 && OnsetEvaluator.bpmAccuracy2(map.bpm(), detectedBpm);
            bpmTotal++;
            if (acc1) bpmAcc1++;
            if (acc2) bpmAcc2++;

            // --- Onsets (constant-BPM maps only) ---
            String onsetCells = "excluded | excluded | excluded | excluded";
            if (!map.variableBpm()) {
                ArrayList<ArrayList<Double>> peaks =
                        AudioAnalysis.getPeaksFromAudio(wav.getAbsolutePath(), map.bpm(), 0.0);
                double[] estimated = peaks.get(EXPERT_PLUS).stream().mapToDouble(Double::doubleValue).toArray();
                double[] reference = OnsetEvaluator.dedupe(map.noteTimesSeconds(), CHORD_DEDUPE_GAP);

                OnsetEvaluator.Result primary = OnsetEvaluator.evaluate(reference, estimated, TOLERANCE_PRIMARY);
                OnsetEvaluator.Result strict = OnsetEvaluator.evaluate(reference, estimated, TOLERANCE_STRICT);
                fScores.add(primary.fMeasure());
                precisions.add(primary.precision());
                recalls.add(primary.recall());
                onsetCells = String.format("%.3f | %.3f | %.3f | %.3f",
                        primary.precision(), primary.recall(), primary.fMeasure(), strict.fMeasure());
            }

            report.append(String.format("| %s | %s | %.1f | %s | %s | %.1f | %s | %s |%n",
                    map.id(), map.songName().replace("|", "/"), map.bpm(),
                    map.variableBpm() ? "yes" : "no", onsetCells,
                    detectedBpm, acc1 ? "✓" : "✗", acc2 ? "✓" : "✗"));

            System.out.printf("done %s (%s)%n", map.id(), map.songName());
        }

        report.append("\n## Aggregate\n\n");
        report.append(String.format("- Onset maps evaluated: %d (constant BPM only)%n", fScores.size()));
        report.append(String.format("- Mean P@50ms: %.3f | Mean R@50ms: %.3f | Mean F@50ms: %.3f%n",
                mean(precisions), mean(recalls), mean(fScores)));
        report.append(String.format("- BPM Accuracy1: %d/%d (%.1f%%) | Accuracy2: %d/%d (%.1f%%)%n",
                bpmAcc1, bpmTotal, 100.0 * bpmAcc1 / bpmTotal,
                bpmAcc2, bpmTotal, 100.0 * bpmAcc2 / bpmTotal));

        Path resultsDir = Path.of("docs", "research", "benchmark_results");
        Files.createDirectories(resultsDir);
        Path outFile = resultsDir.resolve(label + "_" + LocalDate.now() + ".md");
        Files.writeString(outFile, report.toString(), StandardCharsets.UTF_8,
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        System.out.println("\n=== Results written to " + outFile + " ===\n");
        System.out.println(report);
    }

    private static double mean(List<Double> values) {
        return values.isEmpty() ? 0.0 : values.stream().mapToDouble(Double::doubleValue).average().orElse(0);
    }
}
