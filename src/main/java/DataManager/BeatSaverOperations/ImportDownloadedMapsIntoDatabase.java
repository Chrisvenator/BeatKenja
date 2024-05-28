package DataManager.BeatSaverOperations;

import DataManager.FileManager;
import DataManager.Parameters;
import DataManager.Records.PatMetadata;
import MapGeneration.GenerationElements.Exceptions.NoteNotValidException;
import MapGeneration.GenerationElements.Pattern;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.*;
import java.util.concurrent.*;
import java.io.File;
import java.util.List;
import java.util.ArrayList;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ImportDownloadedMapsIntoDatabase {
//    public static void main(String[] args) {
//        ImportDownloadedMapsIntoDatabase.importAllMaps("C:\\Program Files (x86)\\Steam\\steamapps\\common\\Beat Saber\\BeatSaberMaps\\test\\", "AllMapsGroupedV1");
//        ImportDownloadedMapsIntoDatabase.importAllMaps("C:\\Program Files (x86)\\Steam\\steamapps\\common\\Beat Saber\\BeatSaberMaps\\" +
//                "test\\", "test123");
//    }

    public static void importAllMaps(String MAPS_DIRECTORY, String patternName) {
        if (!MAPS_DIRECTORY.endsWith("/")) MAPS_DIRECTORY += "/";

        File[] mapFolders = new File(MAPS_DIRECTORY).listFiles();
        if (mapFolders == null) {
            System.err.println("No files found in the directory.");
            return;
        }

        List<File> maps = Arrays.stream(mapFolders).filter(File::isDirectory).toList();
        List<Pattern> patterns = Collections.synchronizedList(new ArrayList<>());

        ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        List<Future<?>> futures = new ArrayList<>();

        int count = 0;
        for (File map : maps) {
//            if (count > 0) break;
            final int index = ++count;  // Use final variable for thread-safe operations
            futures.add(executor.submit(() -> {
                System.out.println(index + "/" + maps.size() + " Importing map: " + map.getName());
                try {
                    List<Pattern> patternsFromMap = createPatternsFromMapDirectory(map, patternName);
                    mergeIntoPattern(patterns, patternsFromMap);
                } catch (Exception e) {
                    System.err.println("Failed to import map: " + map.getName() + " due to " + e.getMessage());
                    e.printStackTrace();
                    throw new RuntimeException(e);
                }
            }));
        }

        // Wait for all tasks to complete
        for (Future<?> future : futures) {
            try {
                future.get();  // Will block until the task is completed
            } catch (InterruptedException | ExecutionException e) {
                System.err.println("Error waiting for map import task to complete: " + e.getMessage());
            }
        }
        executor.shutdown();  // Shutdown the executor

        try {
            if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
        }

        if (Parameters.verbose) sortPattern(patterns);
        if (Parameters.verbose) patterns.forEach(p -> System.out.println(p.metadata.toString().replaceAll("\n", "")));

        String patFolderName = new File(MAPS_DIRECTORY).getParent() + "\\" + new File(MAPS_DIRECTORY).getName() + "Pat\\";
        // Save patterns to the _toAddPat folder so that they can be added later to the database if something goes wrong
        boolean saved = saveAllPatternsIntoFolder(patFolderName, patterns);
        if (!saved) System.err.println("[ERROR] Couldn't save .pat Files! ");

//        savePatternsIntoDatabase(patterns);
    }

    private static void savePatternsIntoDatabase(List<Pattern> patterns) {
        AtomicInteger i = new AtomicInteger();
        patterns.forEach(pattern -> {
            savePatternsIntoDatabase(pattern);
            System.out.println("[INFO]: saved " + i.getAndIncrement() + "/" + patterns.size() + " " + pattern.metadata.toString());
        });
    }


    private static boolean savePatternsIntoDatabase(Pattern pattern) {
        Pattern databasePattern = new Pattern(pattern.metadata);
        System.out.println(pattern.exportInPatFormat());
        System.out.println("\n\n\n\n\n" + databasePattern.exportInPatFormat());
        databasePattern.merge(pattern);
        return databasePattern.saveOrUpdateInDatabase();
    }

    public static void main(String[] args) {
        savePatternIntoDatabaseFromFolder("C:\\Program Files (x86)\\Steam\\steamapps\\common\\Beat Saber\\BeatSaberMaps\\testPat\\");
    }

    /**
     * Saves all patterns from the specified folder into the database.
     * The folder should contain .pat files.
     * This function is used, for example, to save patterns that were not successfully imported into the database.
     *
     * @param path the path to the folder containing .pat files
     */
    private static void savePatternIntoDatabaseFromFolder(String path) {
        File folder = new File(path);
        if (!folder.isDirectory()) throw new IllegalArgumentException("Path is not a directory: " + path);

        File[] files = folder.listFiles();
        if (files == null) {
            System.err.println("No files found in the directory.");
            return;
        }

        List<File> patFiles = Arrays.stream(files).filter(file -> file.getName().endsWith(".pat")).toList();

        for (File patFile : patFiles) {
            try {
                if (savePatternsIntoDatabase(new Pattern(patFile.getAbsolutePath())))
                    System.out.println("Successfully saved pattern from file into database: " + patFile.getName());
                else System.err.println("Failed to import pattern from file: " + patFile.getName());

                break;
            } catch (Exception e) {
                System.err.println("Failed to import pattern from file: " + patFile.getName() + " due to " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    /**
     * Saves all patterns into the specified folder.
     *
     * @param path     the path to the folder where the patterns should be saved
     * @param patterns the patterns to save
     * @return true if all patterns were saved successfully. False, if the folder could not be created.
     */
    private static boolean saveAllPatternsIntoFolder(String path, List<Pattern> patterns) {
        File folder = new File(path);
        if (!folder.exists() && !folder.mkdirs()) return false;
        int threadCount = Runtime.getRuntime().availableProcessors(); // Use a thread pool based on available processors
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        for (Pattern pattern : patterns) {
            executorService.submit(() -> FileManager.overwriteFile(
                    path + pattern.metadata.toString().replaceAll("\n", "") + ".pat",
                    pattern.exportInPatFormat()));
        }
        executorService.shutdown();

        try {
            if (!executorService.awaitTermination(60, TimeUnit.MINUTES)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
        }

        return true;
    }

    /**
     * Merges the new patterns into one of the existing patterns (if it exists).
     * Optimized to use a single stream operation for checking and merging.
     *
     * @param patterns    the existing patterns
     * @param newPatterns the new patterns
     */
    private static void mergeIntoPattern(List<Pattern> patterns, List<Pattern> newPatterns) {
        for (Pattern newPattern : newPatterns) {
            if (newPattern.patterns[0][0] == null) continue;  // Skip empty initial patterns

            Optional<Pattern> match = patterns.stream()
                    .filter(existingPattern -> shouldItMerge(existingPattern.metadata, newPattern.metadata))
                    .findFirst();

            if (match.isPresent()) {
                match.get().merge(newPattern);  // Merge with the first matching pattern
            } else {
                patterns.add(newPattern);  // No match found, add as a new pattern
            }
        }
    }

    /**
     * Should it merge the two patterns based on their metadata?
     * Currently only checks if the name, NPS, difficulty, and tags are the same.
     *
     * @param m1 metadata 1
     * @param m2 metadata 2
     * @return true if it should merge, false if not
     */
    private static boolean shouldItMerge(PatMetadata m1, PatMetadata m2) {
        return m1.name().equals(m2.name()) &&
                (int) Math.round(m1.nps()) == (int) Math.round(m2.nps()) &&
                new HashSet<>(m1.difficulty()).containsAll(m2.difficulty()) &&
//                new HashSet<>(m1.genre()).containsAll(m2.genre()) &&
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
            System.out.println("[INFO]: Map is missing info.dat or metadata.json: " + mapDir + " Skipping...");
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

        Set<String> lowerCaseMapTags = Parameters.MAP_TAGS.stream().map(String::toLowerCase).collect(Collectors.toSet());
        Set<String> lowerCaseMusicGenres = Parameters.MUSIC_GENRES.stream().map(String::toLowerCase).collect(Collectors.toSet());

        // Collect to a mutable list to allow removal of unwanted tags.
        final List<String> tags = mapTags.stream().filter(tag -> Parameters.MAP_TAGS.stream().anyMatch(acceptedTag -> acceptedTag.equalsIgnoreCase(tag))).collect(Collectors.toCollection(ArrayList::new));
        final List<String> genres = mapTags.stream().filter(genre -> Parameters.MUSIC_GENRES.stream().anyMatch(acceptedGenre -> acceptedGenre.equalsIgnoreCase(genre))).collect(Collectors.toCollection(ArrayList::new));

        //remove tags and genres that are not in the list
        tags.removeIf(tag -> !lowerCaseMapTags.contains(tag.toLowerCase()));
        genres.removeIf(genre -> !lowerCaseMusicGenres.contains(genre.toLowerCase()));


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
            PatMetadata meta = new PatMetadata(patternName, (int) bpm, (int) Math.round(nps), Collections.singletonList(difficultyName),
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
        if (tags.size() > 2 || genres.size() > 2) {
            System.err.println("[INFO] Too many tags or genres for map: " + mapDir + ": " + tags + " " + genres + ". Please try to limit it to only two.");
        }


        List<Pattern> patterns = new ArrayList<>();

        // Create pattern objects for each difficulty, include metadata.
        for (String diff : difficulties.keySet()) {
            File f = new File(mapDir.getAbsolutePath() + "/" + diff);
            try {
                Pattern p = new Pattern(f.getAbsolutePath(), difficulties.get(diff));
                patterns.add(p);
            } catch (NoteNotValidException e) {
                System.err.println("[INFO]: Failed to create pattern for difficulty because a note is not valid: " + diff + " in map: " + mapDir);
                continue;
            }
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

    private static void sortPattern(List<Pattern> noteList) {
        noteList.sort((p1, p2) -> {
            // Extract metadata for ease of use
            PatMetadata meta1 = p1.metadata;
            PatMetadata meta2 = p2.metadata;

            // First, compare by the first difficulty if available
            if (!meta1.difficulty().isEmpty() && !meta2.difficulty().isEmpty()) {
                HashMap<String, Integer> difficultyOrder = new HashMap<>();
                difficultyOrder.put("StandardEasy", 4);
                difficultyOrder.put("StandardNormal", 3);
                difficultyOrder.put("StandardHard", 2);
                difficultyOrder.put("StandardExpert", 1);
                difficultyOrder.put("StandardExpertPlus", 0);

                int difficultyCompare = difficultyOrder.get(meta1.difficulty().get(0)) - difficultyOrder.get(meta2.difficulty().get(0));
                if (difficultyCompare != 0) {
                    return difficultyCompare;
                }
            } else if (meta1.difficulty().isEmpty() && !meta2.difficulty().isEmpty()) {
                return -1; // No difficulty is considered lesser
            } else if (!meta1.difficulty().isEmpty()) {
                return 1;
            }

            // Then compare by nps
            int npsCompare = Double.compare(meta1.nps(), meta2.nps());
            if (npsCompare != 0) {
                return npsCompare;
            }

            // Finally, compare by tags
            if (!meta1.tags().isEmpty() && !meta2.tags().isEmpty()) {
                return meta1.tags().get(0).compareTo(meta2.tags().get(0));
            } else if (meta1.tags().isEmpty() && !meta2.tags().isEmpty()) {
                return -1; // Empty tags are considered lesser
            } else if (!meta1.tags().isEmpty()) {
                return 1;
            }

            return 0; // All fields are equal or both are empty
        });
    }

}
