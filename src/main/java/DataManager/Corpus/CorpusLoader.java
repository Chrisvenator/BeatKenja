package DataManager.Corpus;

import DataManager.Records.QualityTier;
import MapGeneration.GenerationElements.Exceptions.NoteNotValidException;
import MapGeneration.GenerationElements.HigherOrderPattern;
import MapGeneration.GenerationElements.Pattern;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Walks a train folder with quality-tier subfolders (Ranked / Curated / Verified / Normal)
 * and builds a quality-weighted Pattern by merging transition counts.
 *
 * <p>Weight per map is determined by its tier: Ranked > Curated > Verified > Normal.
 * Each map's transition counts are merged {@code QualityTier.weight} times so high-quality
 * maps dominate the learned probabilities without discarding any data.
 *
 * <p>SpeedCategory from MapPackage.DiffInfo drives complexity constraints:
 * FAST diffs (high NPS, short reaction time) should use simpler patterns, which callers
 * can enforce by requesting a FAST-filtered Pattern variant.
 */
public class CorpusLoader {

    private static final Logger logger = LogManager.getLogger(CorpusLoader.class);

    public record LoadResult(
        Pattern patternRed,
        Pattern patternBlue,
        HigherOrderPattern higherOrderBlue,
        HigherOrderPattern higherOrderRed,
        int mapsLoaded,
        int diffsLoaded,
        int mapsSkipped
    ) {}

    /**
     * Loads all maps in the train root and merges them into two Patterns (red / blue).
     * Each map folder must be a direct child of a quality-tier subfolder.
     *
     * @param trainRoot  path to the train/ directory containing tier subfolders
     */
    public static LoadResult loadAll(String trainRoot) {
        File root = new File(trainRoot);
        if (!root.isDirectory()) throw new IllegalArgumentException("Not a directory: " + trainRoot);

        Pattern bluePattern = new Pattern();
        Pattern redPattern  = new Pattern();
        HigherOrderPattern hoBlue = new HigherOrderPattern();
        HigherOrderPattern hoRed  = new HigherOrderPattern();
        int mapsLoaded = 0, diffsLoaded = 0, mapsSkipped = 0;

        File[] tierFolders = root.listFiles(File::isDirectory);
        if (tierFolders == null) return new LoadResult(redPattern, bluePattern, hoBlue, hoRed, 0, 0, 0);

        for (File tierFolder : tierFolders) {
            QualityTier tier = QualityTier.fromFolderName(tierFolder.getName());
            logger.info("Processing tier {} (weight={}) from {}", tier, tier.weight, tierFolder.getName());

            File[] mapFolders = tierFolder.listFiles(f -> f.isDirectory() && !f.getName().endsWith(".zip"));
            if (mapFolders == null) continue;

            for (File mapFolder : mapFolders) {
                try {
                    MapPackage pkg = MapPackage.fromFolder(mapFolder);
                    if (pkg.difficulties.isEmpty()) {
                        mapsSkipped++;
                        continue;
                    }

                    for (MapPackage.DiffInfo diff : pkg.difficulties) {
                        // Build single-diff patterns for both colors
                        Pattern blue, red;
                        try {
                            blue = new Pattern(diff.map()._notes, 1);
                            red  = new Pattern(diff.map()._notes, 0);
                        } catch (NoteNotValidException e) {
                            logger.warn("Invalid notes in {} / {}: {}", mapFolder.getName(), diff.difficulty(), e.getMessage());
                            mapsSkipped++;
                            continue;
                        }

                        // Apply quality weight: merge 'weight' times so high-quality maps
                        // contribute proportionally more to the transition counts
                        for (int w = 0; w < tier.weight; w++) {
                            bluePattern.merge(blue);
                            redPattern.merge(red);
                        }
                        hoBlue.trainFrom(diff.map()._notes, 1, tier.weight);
                        hoRed.trainFrom(diff.map()._notes, 0, tier.weight);
                        diffsLoaded++;
                    }
                    mapsLoaded++;

                } catch (Exception e) {
                    logger.warn("Skipping map {}: {}", mapFolder.getName(), e.getMessage());
                    mapsSkipped++;
                }
            }
        }

        logger.info("Corpus loaded: {} maps ({} diffs, {} skipped)", mapsLoaded, diffsLoaded, mapsSkipped);
        return new LoadResult(redPattern, bluePattern, hoBlue, hoRed, mapsLoaded, diffsLoaded, mapsSkipped);
    }

    /**
     * Returns a list of all map folders found under the tier subfolders, with their tier.
     * Useful for inspection without full Pattern-building overhead.
     */
    public static List<MapEntry> listMapEntries(String trainRoot) {
        File root = new File(trainRoot);
        List<MapEntry> entries = new ArrayList<>();

        File[] tierFolders = root.listFiles(File::isDirectory);
        if (tierFolders == null) return entries;

        for (File tierFolder : tierFolders) {
            QualityTier tier = QualityTier.fromFolderName(tierFolder.getName());
            File[] mapFolders = tierFolder.listFiles(f -> f.isDirectory() && !f.getName().endsWith(".zip"));
            if (mapFolders == null) continue;

            Arrays.stream(mapFolders).map(f -> new MapEntry(f, tier)).forEach(entries::add);
        }

        return entries;
    }

    public record MapEntry(File folder, QualityTier tier) {}
}
