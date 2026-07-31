package MapGeneration.StyleSpace;

import BeatSaberObjects.Objects.Note;
import DataManager.Corpus.CorpusLoader;
import DataManager.Corpus.MapPackage;
import DataManager.Records.QualityTier;
import MapGeneration.GenerationElements.HigherOrderPattern;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.*;

/**
 * Dev-side only. Builds {@link StyleArchetype}s from the corpus via K-Means clustering
 * over per-map {@link StyleVector}s, then serialises the result to a file bundled
 * in {@code src/main/resources/}.
 *
 * <p>Run once after adding new training maps. Outputs a serialised
 * {@code List<StyleArchetype>} that {@link StyleSpace} loads at jar startup.
 *
 * <p>Usage (from IntelliJ run config or CLI):
 * <pre>
 *   StyleSpaceTrainer.train("train/", "src/main/resources/style_archetypes.ser", 20, 100)
 * </pre>
 */
public class StyleSpaceTrainer {

    private static final Logger logger = LogManager.getLogger(StyleSpaceTrainer.class);

    /**
     * Full training pipeline: load corpus → compute style vectors → K-Means →
     * build per-cluster HigherOrderPatterns → serialise.
     *
     * @param trainRoot    path to the train/ folder (tier subfolders inside)
     * @param outputPath   where to write the serialised List<StyleArchetype>
     * @param k            number of archetypes (clusters)
     * @param kMeansIter   K-Means iterations
     */
    public static List<StyleArchetype> train(String trainRoot, String outputPath, int k, int kMeansIter)
            throws IOException {

        logger.info("StyleSpaceTrainer: loading corpus from {}", trainRoot);

        // ---- 1. Gather per-diff (style vector, notes, tier) entries ----
        List<MapEntry> entries = new ArrayList<>();
        for (CorpusLoader.MapEntry me : CorpusLoader.listMapEntries(trainRoot)) {
            try {
                MapPackage pkg = MapPackage.fromFolder(me.folder());
                for (MapPackage.DiffInfo diff : pkg.difficulties) {
                    StyleVector sv = StyleVector.compute(diff.map()._notes, 1); // blue
                    entries.add(new MapEntry(sv, diff.map()._notes, me.tier()));
                }
            } catch (Exception e) {
                logger.warn("Skipping {}: {}", me.folder().getName(), e.getMessage());
            }
        }
        logger.info("Collected {} diff entries for clustering", entries.size());

        if (entries.size() < k) {
            logger.warn("Fewer entries ({}) than k ({}); reducing k", entries.size(), k);
            k = Math.max(1, entries.size());
        }

        // ---- 2. K-Means over StyleVectors ----
        int[] assignments = kMeans(entries, k, kMeansIter);

        // ---- 3. Build HigherOrderPattern per cluster ----
        List<StyleArchetype> archetypes = new ArrayList<>();
        for (int clusterId = 0; clusterId < k; clusterId++) {
            List<MapEntry> members = new ArrayList<>();
            for (int i = 0; i < entries.size(); i++) {
                if (assignments[i] == clusterId) members.add(entries.get(i));
            }
            if (members.isEmpty()) continue;

            HigherOrderPattern hopBlue = new HigherOrderPattern();
            HigherOrderPattern hopRed  = new HigherOrderPattern();
            StyleVector[] memberVecs  = new StyleVector[members.size()];
            float[] memberWeights     = new float[members.size()];

            for (int j = 0; j < members.size(); j++) {
                MapEntry me = members.get(j);
                int w = me.tier().weight;
                memberWeights[j] = w;
                memberVecs[j]    = me.styleVector();
                try {
                    hopBlue.trainFrom(me.notes(), 1, w);
                    hopRed.trainFrom(me.notes(), 0, w);
                } catch (Exception e) {
                    logger.warn("Training error in cluster {}: {}", clusterId, e.getMessage());
                }
            }

            StyleVector centroid = StyleVector.blend(memberVecs, memberWeights);
            archetypes.add(new StyleArchetype(clusterId, "archetype-" + clusterId, centroid, hopBlue, hopRed));
            logger.info("Cluster {} — {} members, {} blue obs", clusterId, members.size(),
                    hopBlue.getTotalObservations());
        }

        // ---- 4. Serialise ----
        if (outputPath != null && !outputPath.isEmpty()) {
            try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(outputPath))) {
                oos.writeObject(archetypes);
            }
            logger.info("Wrote {} archetypes to {}", archetypes.size(), outputPath);
        }

        return archetypes;
    }

    // -----------------------------------------------------------------------
    // K-Means
    // -----------------------------------------------------------------------

    private static int[] kMeans(List<MapEntry> entries, int k, int maxIter) {
        int n = entries.size();
        Random rng = new Random(42);

        // Init centroids: pick k random entries
        StyleVector[] centroids = new StyleVector[k];
        List<Integer> chosen = new ArrayList<>();
        while (chosen.size() < k) {
            int idx = rng.nextInt(n);
            if (!chosen.contains(idx)) { chosen.add(idx); centroids[chosen.size() - 1] = entries.get(idx).styleVector(); }
        }

        int[] assignments = new int[n];
        for (int iter = 0; iter < maxIter; iter++) {
            // Assign
            boolean changed = false;
            for (int i = 0; i < n; i++) {
                int best = 0;
                float bestDist = entries.get(i).styleVector().distanceTo(centroids[0]);
                for (int c = 1; c < k; c++) {
                    float d = entries.get(i).styleVector().distanceTo(centroids[c]);
                    if (d < bestDist) { bestDist = d; best = c; }
                }
                if (assignments[i] != best) { assignments[i] = best; changed = true; }
            }
            if (!changed) { logger.info("K-Means converged at iteration {}", iter); break; }

            // Update centroids
            for (int c = 0; c < k; c++) {
                List<StyleVector> members = new ArrayList<>();
                List<Float> weights = new ArrayList<>();
                for (int i = 0; i < n; i++) {
                    if (assignments[i] == c) {
                        members.add(entries.get(i).styleVector());
                        weights.add((float) entries.get(i).tier().weight);
                    }
                }
                if (members.isEmpty()) continue;
                float[] wArr = new float[weights.size()];
                for (int j = 0; j < weights.size(); j++) wArr[j] = weights.get(j);
                centroids[c] = StyleVector.blend(members.toArray(new StyleVector[0]), wArr);
            }
        }
        return assignments;
    }

    // -----------------------------------------------------------------------
    // Inner record
    // -----------------------------------------------------------------------

    private record MapEntry(StyleVector styleVector, Note[] notes, QualityTier tier) {}

    // -----------------------------------------------------------------------
    // Dev-side entry point
    // -----------------------------------------------------------------------

    public static void main(String[] args) throws IOException {
        String trainRoot  = args.length > 0 ? args[0] : "train";
        String outputPath = args.length > 1 ? args[1] : "src/main/resources/style_archetypes.ser";
        int k             = args.length > 2 ? Integer.parseInt(args[2]) : 20;
        int iters         = args.length > 3 ? Integer.parseInt(args[3]) : 100;
        List<StyleArchetype> result = train(trainRoot, outputPath, k, iters);
        System.out.println("Done. " + result.size() + " archetypes written to " + outputPath);
    }
}
