package DataManager.BeatSaverOperations;

import DataManager.FileManager;
import DataManager.Parameters;
import DataManager.Records.PatMetadata;
import MapGeneration.GenerationElements.Pattern;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.util.*;
import java.util.stream.IntStream;

public class ImportDownloadedMapsIntoDatabase {
    public static void main(String[] args) {
//        ImportDownloadedMapsIntoDatabase.importAllMaps("C:\\Program Files (x86)\\Steam\\steamapps\\common\\Beat Saber\\BeatSaberMaps\\_toAdd\\", "test");
        ImportDownloadedMapsIntoDatabase.importAllMaps("C:\\Program Files (x86)\\Steam\\steamapps\\common\\Beat Saber\\BeatSaberMaps\\test\\", "testPattern");
//        System.out.println("successful: " + ImportDownloadedMapsIntoDatabase.importMap(new File("C:\\Program Files (x86)\\Steam\\steamapps\\common\\Beat Saber\\BeatSaberMaps\\_toAdd\\1"), "test"));
//        System.out.println("successful: " + ImportDownloadedMapsIntoDatabase.createPatternFromMapDirectory(new File("C:\\Program Files (x86)\\Steam\\steamapps\\common\\Beat Saber\\BeatSaberMaps\\_toAdd\\11300"), "test"));
    }

    public static void importAllMaps(String MAPS_DIRECTORY, String patternName) {
        if (!MAPS_DIRECTORY.endsWith("/")) MAPS_DIRECTORY += "/";

        List<File> maps = Arrays.stream(Objects.requireNonNull(new File(MAPS_DIRECTORY).listFiles())).toList();
        List<Pattern> patterns = new ArrayList<>();

        for (File map : maps) {
            if (map.isDirectory()) {
                mergeIntoPattern(patterns,
                        createPatternsFromMapDirectory(map, patternName));
            } else
                throw new IllegalArgumentException("Map is not a directory: " + map);
        }
        patterns.forEach(p -> System.out.println(p.exportInPatFormat()));

        patterns.forEach(pattern -> {
            Pattern databasePattern = new Pattern(pattern.metadata);
            databasePattern.merge(pattern);
            databasePattern.saveOrUpdateInDatabase();
        });

        //TODO: Use a lot of different patterns to fill the database
    }

    /**
     * Merges the new patterns into one of the existing patterns (if it exists).
     *
     * @param patterns    the existing patterns
     * @param newPatterns the new patterns
     */
    private static void mergeIntoPattern(List<Pattern> patterns, List<Pattern> newPatterns) {
        for (Pattern p : newPatterns) {
            if (patterns.stream().noneMatch(pattern -> shouldItMerge(pattern.metadata, p.metadata))) {
                patterns.add(p);
            } else {
                Pattern existingPattern = patterns.stream().filter(pattern -> shouldItMerge(pattern.metadata, p.metadata)).findFirst().get();
                existingPattern.merge(p);
            }
        }
    }

    /**
     * Should it merge the two patterns based on their metadata?
     *
     * @param m1 metadata 1
     * @param m2 metadata 2
     * @return true if it should merge, false if not
     */
    private static boolean shouldItMerge(PatMetadata m1, PatMetadata m2) {
        //TODO: should I also group by bpm?
        return m1.name().equals(m2.name()) &&
                (int) Math.round(m1.nps()) == (int) Math.round(m2.nps()) &&
                new HashSet<>(m1.difficulty()).containsAll(m2.difficulty()) &&
                new HashSet<>(m1.genre()).containsAll(m2.genre()) &&
                new HashSet<>(m1.tags()).containsAll(m2.tags());
    }

    /**
     * Creates a list of patterns based on the map files located in a specified directory.
     * Each pattern corresponds to a difficulty level in the rhythm game map, containing metadata
     * like BPM, tags, and NPS (notes per second).
     * <br>
     * !The nps has been rounded to an integer value!
     *
     * @param mapDir      The directory containing map files.
     * @param patternName The name of how it should be saved in the database.
     * @return A list of patterns for each difficulty found in the map directory.
     * @throws IllegalArgumentException If the provided mapDir is not a directory.
     */
    private static List<Pattern> createPatternsFromMapDirectory(File mapDir, String patternName) {
        if (!mapDir.isDirectory()) throw new IllegalArgumentException("Map is not a directory: " + mapDir);

        // Define files for map information and metadata.
        File infoFile = new File(mapDir.getAbsolutePath() + "/info.dat");
        File metadataFile = new File(mapDir.getAbsolutePath() + "/" + mapDir.getName() + ".json");

        // Check if essential map files exist, log error and return empty if not.
        if (!infoFile.exists() || !metadataFile.exists()) {
            System.err.println("Map is missing info.dat or metadata.json: " + mapDir);
            return new ArrayList<>();
        }

        // Parse JSON data from files.
        JSONObject info = new JSONObject(FileManager.readFile(infoFile.getAbsolutePath()).stream().reduce("", (a, b) -> a + b + "\n"));
        JSONObject metadata = new JSONObject(FileManager.readFile(metadataFile.getAbsolutePath()).stream().reduce("", (a, b) -> a + b + "\n"));

        // Extract tags if available, handle absence gracefully by assigning an empty list.
        JSONArray tagsArray = metadata.has("tags") ? metadata.getJSONArray("tags") : null;
        JSONArray diffs = info.getJSONArray("_difficultyBeatmapSets").getJSONObject(0).getJSONArray("_difficultyBeatmaps");

        final List<String> mapTags = tagsArray != null ? IntStream.range(0, tagsArray.length()).mapToObj(tagsArray::getString).toList() : new ArrayList<>();

        // Filter and normalize tags to include only those predefined in Parameters, ignoring case sensitivity.
        final double bpm = info.getDouble("_beatsPerMinute");
        final List<String> tags = mapTags.stream().filter(tag -> Parameters.MAP_TAGS.stream().anyMatch(acceptedTag -> acceptedTag.equalsIgnoreCase(tag))).toList();
        final List<String> genres = mapTags.stream().filter(genre -> Parameters.MUSIC_GENRES.stream().anyMatch(acceptedGenre -> acceptedGenre.equalsIgnoreCase(genre))).toList();
        final HashMap<String, PatMetadata> difficulties = new HashMap<>();

        // Iterate through each difficulty, extracting relevant data and constructing metadata objects.
        for (int i = 0; i < diffs.length(); i++) {
            JSONObject diff = diffs.getJSONObject(i);

            // Extract difficulty-specific data.
            String diffFileName = diff.getString("_beatmapFilename");
            String difficultyName = diff.getString("_difficulty");
            double nps = -1;

            // Attempt to retrieve NPS value from metadata, handle errors by logging and skipping. nps fallback value is -1.
            JSONArray metadataArray = metadata.getJSONArray("versions").getJSONObject(0).getJSONArray("diffs");
            for (int j = 0; j < metadataArray.length(); j++) {
                try {
                    if (metadataArray.getJSONObject(j).getString("difficulty").equalsIgnoreCase(difficultyName)) {
                        nps = metadataArray.getJSONObject(j).getDouble("nps");
                        break;
                    }
                } catch (Exception e) {
                    System.err.println("Failed to get NPS for difficulty: " + difficultyName + ". Skipping...");
                    break;
                }
            }

            // Rename difficulty names to match the naming convention.
            difficultyName = renamePatternDifficulty(difficultyName);

            // Create and store metadata for each difficulty.
            PatMetadata meta = new PatMetadata(patternName, bpm, Math.round(nps), Collections.singletonList(difficultyName),
                    tags.stream().map(name -> Character.toUpperCase(name.charAt(0)) + name.substring(1)).toList(),
                    genres.stream().map(name -> Character.toUpperCase(name.charAt(0)) + name.substring(1)).toList());
            difficulties.put(diffFileName, meta);
        }


        // Check if something went wrong during metadata extraction, log error and return empty if so.
        if (difficulties.isEmpty()) {
            System.err.println("Failed to get difficulties for mapDir: " + mapDir);
            return new ArrayList<>();
        }
        if (difficulties.size() > 5) System.err.println("Too many difficulties for mapDir: " + mapDir + ": " + difficulties);
        if (tags.size() > 2 || genres.size() > 2) System.err.println("Too many tags or genres for mapDir: " + mapDir + ": " + tags + " " + genres);


        List<Pattern> patterns = new ArrayList<>();

        // Create pattern objects for each difficulty, include metadata.
        for (String diff : difficulties.keySet()) {
            File f = new File(mapDir.getAbsolutePath() + "/" + diff);
            Pattern p = new Pattern(f.getAbsolutePath(), difficulties.get(diff));
            patterns.add(p);
        }

        if (Parameters.verbose) System.out.println("Analyzed Difficulties: " + difficulties);

        return patterns;
    }

    public static String renamePatternDifficulty(String diffName) {
        switch (diffName.toLowerCase()) {
            case "easy", "easystandard" -> {
                return "StandardEasy";
            }
            case "normal", "normalstandard" -> {
                return "StandardNormal";
            }
            case "hard", "hardstandard" -> {
                return "StandardHard";
            }
            case "expert", "expertstandard" -> {
                return "StandardExpert";
            }
            case "expertplus", "expertplusstandard" -> {
                return "StandardExpertPlus";
            }
        }
        return "NULL";
    }
}
