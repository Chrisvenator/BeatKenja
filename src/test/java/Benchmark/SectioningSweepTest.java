package Benchmark;

import AudioAnalysis.FooteSectionDetector;
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
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Parameter sweep for {@link FooteSectionDetector} against hand-placed mapper bookmarks
 * (H3: does Foote novelty suffice for section boundaries?).
 * <p>
 * Ground truth: bookmarks of the hardest Standard difficulty, but only from maps where they
 * plausibly mark sections — at least {@link #MIN_BOOKMARKS} of them and a median spacing of
 * at least {@link #MIN_MEDIAN_GAP_SECONDS} (Osu2MIR-style curation; dense bookmarks are
 * lyrics or choreography cues, e.g. SAtAN's word-per-beat markers). Variable-BPM maps are
 * excluded (beat→second conversion needs the base BPM).
 * <p>
 * Run with: {@code mvn test -Dtest=SectioningSweepTest -Dsurefire.excludedGroups=none}
 * Results land in docs/research/benchmark_results/sweep_sectioning_<date>.md.
 */
@Tag("benchmark")
class SectioningSweepTest {

    private static final double SAMPLE_RATE = 44100.0;
    private static final int FFT_SIZE = 2048;
    private static final int HOP_SIZE = 256;
    private static final double FRAME_ADVANCE = HOP_SIZE / SAMPLE_RATE;

    /** Boundary tolerance (seconds) — the protocol's primary sectioning metric (log §6). */
    private static final double TOLERANCE = 3.0;

    private static final int MIN_BOOKMARKS = 3;
    private static final double MIN_MEDIAN_GAP_SECONDS = 5.0;

    private static final double[] KERNEL_SECONDS_VALUES = {8, 16, 24, 32};
    private static final double[] THRESHOLD_VALUES = {0.00, 0.01, 0.02, 0.03, 0.05, 0.10, 0.15, 0.20, 0.30, 0.40};
    private static final double[] MIN_SECTION_VALUES = {6, 10};

    /** Novelty curves per kernel size, computed once per map. */
    private record PreparedMap(String id, String song, double[] reference,
                               double[][] noveltyPerKernel, double[] noveltyTimes) {}

    private record SweepResult(double kernel, double threshold, double minSection,
                               double precision, double recall, double fMeasure) {}

    @Test
    void sweepSectionBoundaries() throws Exception {
        List<GroundTruthCorpus.CorpusMap> corpus = GroundTruthCorpus.load();
        assumeTrue(!corpus.isEmpty(), "No corpus at " + GroundTruthCorpus.corpusDirectory());

        List<PreparedMap> prepared = new ArrayList<>();
        List<String> excluded = new ArrayList<>();
        for (GroundTruthCorpus.CorpusMap map : corpus) {
            if (map.variableBpm() || !hasCuratedBookmarks(map)) {
                if (map.bookmarkTimesSeconds().length > 0) {
                    excluded.add(map.id() + " (" + map.bookmarkTimesSeconds().length + " bookmarks"
                            + (map.variableBpm() ? ", varBPM" : ", spacing") + ")");
                }
                continue;
            }
            File wav = GroundTruthCorpus.decodeToWav(map);
            double[][] spec = SpectrogramCalculator.calculateSpectrogram(
                    wav.getAbsolutePath(), FFT_SIZE, FFT_SIZE - HOP_SIZE);
            double[][] filtered = SuperFluxOnsetDetector.filteredLogSpectrogram(spec, SAMPLE_RATE);

            double[][] noveltyPerKernel = new double[KERNEL_SECONDS_VALUES.length][];
            double[] noveltyTimes = null;
            for (int k = 0; k < KERNEL_SECONDS_VALUES.length; k++) {
                FooteSectionDetector.Result result = FooteSectionDetector.detect(
                        filtered, FRAME_ADVANCE, KERNEL_SECONDS_VALUES[k], 6, 1.1); // threshold 1.1 = pick nothing
                noveltyPerKernel[k] = result.novelty();
                noveltyTimes = result.noveltyTimesSeconds();
            }
            prepared.add(new PreparedMap(map.id(), map.songName(), map.bookmarkTimesSeconds(),
                    noveltyPerKernel, noveltyTimes));
            System.out.printf("prepared %s (%s), %d reference boundaries%n",
                    map.id(), map.songName(), map.bookmarkTimesSeconds().length);
        }
        assumeTrue(!prepared.isEmpty(), "No maps with curated bookmarks");

        List<SweepResult> results = new ArrayList<>();
        for (int k = 0; k < KERNEL_SECONDS_VALUES.length; k++) {
            for (double threshold : THRESHOLD_VALUES) {
                for (double minSection : MIN_SECTION_VALUES) {
                    double sumP = 0;
                    double sumR = 0;
                    double sumF = 0;
                    for (PreparedMap map : prepared) {
                        ArrayList<Double> boundaries = FooteSectionDetector.pickBoundaries(
                                map.noveltyPerKernel()[k], map.noveltyTimes(), minSection, threshold);
                        double[] estimated = boundaries.stream().mapToDouble(Double::doubleValue).toArray();
                        OnsetEvaluator.Result r = OnsetEvaluator.evaluate(map.reference(), estimated, TOLERANCE);
                        sumP += r.precision();
                        sumR += r.recall();
                        sumF += r.fMeasure();
                    }
                    int n = prepared.size();
                    results.add(new SweepResult(KERNEL_SECONDS_VALUES[k], threshold, minSection,
                            sumP / n, sumR / n, sumF / n));
                }
            }
        }

        results.sort(Comparator.comparingDouble(SweepResult::fMeasure).reversed());
        SweepResult best = results.get(0);

        StringBuilder report = new StringBuilder();
        report.append("# Foote sectioning sweep — ").append(LocalDate.now()).append("\n\n");
        report.append("Boundary P/R/F @ ±").append(TOLERANCE).append("s vs curated mapper bookmarks (")
                .append(prepared.size()).append(" maps: constant BPM, ≥").append(MIN_BOOKMARKS)
                .append(" bookmarks, median gap ≥").append(MIN_MEDIAN_GAP_SECONDS).append("s). Sorted by F.\n\n");
        report.append("Excluded despite having bookmarks: ").append(String.join(", ", excluded)).append("\n\n");
        report.append("| kernel(s) | threshold | minSection(s) | P | R | F |\n");
        report.append("|---|---|---|---|---|---|\n");
        for (SweepResult r : results) {
            report.append(String.format("| %.0f | %.2f | %.0f | %.3f | %.3f | %.3f |%n",
                    r.kernel(), r.threshold(), r.minSection(), r.precision(), r.recall(), r.fMeasure()));
        }

        report.append(String.format("%n## Per-map results at best combo (kernel=%.0fs, threshold=%.2f, minSection=%.0fs)%n%n",
                best.kernel(), best.threshold(), best.minSection()));
        report.append("| id | song | refBounds | detBounds | P | R | F |\n");
        report.append("|---|---|---|---|---|---|---|\n");
        int bestKernel = indexOf(KERNEL_SECONDS_VALUES, best.kernel());
        for (PreparedMap map : prepared) {
            ArrayList<Double> boundaries = FooteSectionDetector.pickBoundaries(
                    map.noveltyPerKernel()[bestKernel], map.noveltyTimes(), best.minSection(), best.threshold());
            double[] estimated = boundaries.stream().mapToDouble(Double::doubleValue).toArray();
            OnsetEvaluator.Result r = OnsetEvaluator.evaluate(map.reference(), estimated, TOLERANCE);
            report.append(String.format("| %s | %s | %d | %d | %.3f | %.3f | %.3f |%n",
                    map.id(), map.song().replace("|", "/"), map.reference().length, estimated.length,
                    r.precision(), r.recall(), r.fMeasure()));
        }

        Path resultsDir = Path.of("docs", "research", "benchmark_results");
        Files.createDirectories(resultsDir);
        Path outFile = resultsDir.resolve("sweep_sectioning_" + LocalDate.now() + ".md");
        Files.writeString(outFile, report.toString(), StandardCharsets.UTF_8,
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        System.out.println("\n=== Sweep results written to " + outFile + " ===\n");

        for (int i = 0; i < Math.min(10, results.size()); i++) {
            SweepResult r = results.get(i);
            System.out.printf("kernel=%.0fs threshold=%.2f minSection=%.0fs  P=%.3f R=%.3f F=%.3f%n",
                    r.kernel(), r.threshold(), r.minSection(), r.precision(), r.recall(), r.fMeasure());
        }
    }

    /** Osu2MIR-style curation: enough bookmarks, spaced like sections rather than lyrics. */
    private static boolean hasCuratedBookmarks(GroundTruthCorpus.CorpusMap map) {
        double[] bookmarks = map.bookmarkTimesSeconds();
        if (bookmarks.length < MIN_BOOKMARKS) return false;
        double[] gaps = new double[bookmarks.length - 1];
        for (int i = 1; i < bookmarks.length; i++) gaps[i - 1] = bookmarks[i] - bookmarks[i - 1];
        Arrays.sort(gaps);
        return gaps[gaps.length / 2] >= MIN_MEDIAN_GAP_SECONDS;
    }

    private static int indexOf(double[] values, double value) {
        for (int i = 0; i < values.length; i++) {
            if (values[i] == value) return i;
        }
        throw new IllegalArgumentException("value not in sweep grid: " + value);
    }
}
