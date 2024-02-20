package MapGeneration.GenerationElements;

import DataManager.FileManager;
import DataManager.Parameters;
import DataManager.Records.PatMetadata;
import MapGeneration.GenerationElements.Exceptions.MalformedFileExtensionException;
import MapGeneration.GenerationElements.Exceptions.MalformedSequenceException;
import com.google.gson.JsonSyntaxException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;

public class Patterns {
    public static void main(String[] args) {
//        Patterns patterns = new Patterns();
//        System.out.println(patterns.sequences);
//        System.out.println(patterns.patterns);
//        System.out.println(patterns.patterns.get(0).exportInPatFormat());
//        System.out.println(patterns.patterns.get(1).exportInPatFormat());
//        Pattern p = new Pattern("C:\\Program Files (x86)\\Steam\\steamapps\\common\\Beat Saber\\_SongsToTimings\\Patterns\\PatternProbabilities\\test1.pat");
//        System.out.println(p);
//        System.out.println(p.exportInPatFormat());


        String patFilePath = "./BeatSaberMaps/good_pat/";

        Patterns patterns2 = new Patterns();
        patterns2.folderToPat(new File("./BeatSaberMaps/good/"), patFilePath);
        Pattern p = patterns2.mergePatternsInFolder(new File(patFilePath));

        System.out.println(p);
        FileManager.overwriteFile(patFilePath + "_merged.pat", p.exportInPatFormat());
    }

    private final List<Sequence> sequences = new ArrayList<>();
    private final List<Pattern> patterns = new ArrayList<>();


    public Patterns(HashMap<String, String> type) {
        for (String path : type.keySet()) {
            initialize(path, type.get(path));
        }
    }

    public Patterns() {
        HashMap<String, String> map = new HashMap<>();
        map.put(Parameters.DEFAULT_SEQUENCES_FOLDER, Sequence.class.toString());
        map.put(Parameters.DEFAULT_PATTERN_PROBABILITIES_FOLDER, Pattern.class.toString());

        Patterns p = new Patterns(map);
        this.sequences.addAll(p.sequences);
        this.patterns.addAll(p.patterns);
    }


    public Pattern mergePatternsInFolder(File folder) {
        Pattern p = new Pattern();

        for (File file : Objects.requireNonNull(folder.listFiles()))
            p.merge(new Pattern(file.getPath()));

        return p;
    }

    /**
     * Converts all difficulties of all folders in the given folder to .pat files.
     *
     * @param folder The path to the folder containing the folders
     * @warning V3 FILES ARE NOT SUPPORTED YET!
     * @warning This method is very slow. It is recommended to use the other constructor instead.
     */
    public void folderToPat(File folder, String patFilePath) {
        int i = 0;
        for (File subFolder : Objects.requireNonNull(folder.listFiles())) {
            System.out.println("Processing " + subFolder.getName() + " (" + ++i + "/" + Objects.requireNonNull(folder.listFiles()).length + ")");
            Thread t = new Thread(() -> mapFolderToPat(subFolder, patFilePath));
            t.start();
        }

    }

    /**
     * Converts a folder containing a .json files and .dat files to a .pat file.
     * The .pat file will be saved in the patFilePath.
     * The folder must contain a .json file with the same name as the folder itself. You can get the json file from the beatsaver api
     *
     * @param subFolder   The folder containing the .json file and the .dat files
     * @param patFilePath The path to the folder where the .pat files will be saved
     * @warning This method is very slow. It is recommended to use the other constructor instead.
     */
    public void mapFolderToPat(File subFolder, String patFilePath) {

        if (!subFolder.isDirectory()) return;
        File jsonFile = new File(subFolder, subFolder.getName() + ".json");
        if (!jsonFile.exists()) return;

        try {
            String content = new String(Files.readAllBytes(Paths.get(jsonFile.getPath())));
            JSONObject jsonObject = new JSONObject(content);

            // Extract tags and bpm
//                        System.out.println(jsonObject.toString(4));
            String tags;
            try {
                tags = jsonObject.getJSONArray("tags").toString()
                        .replace("[", "")
                        .replace("]", "")
                        .replaceAll("\"", "")
                        .replace(" ", "")
                        .replace("&", "-");
            } catch (JSONException e) {
                tags = "NULL";
            }


            int bpm = jsonObject.getJSONObject("metadata").getInt("bpm");

            // Extract label-nps pairs from diffs
//                System.out.println(new JSONObject(jsonObject.getJSONArray("versions").get(0).toString()).getJSONArray("diffs"));
            JSONArray diffs = new JSONObject(jsonObject.getJSONArray("versions").get(0).toString()).getJSONArray("diffs");
            for (int i = 0; i < diffs.length(); i++) {
                JSONObject diff = diffs.getJSONObject(i);

                String characteristic = diff.getString("characteristic"); //Maybe use this in the future for analyzing different characteristics
                String label = diff.getString("difficulty");

                if (diff.getBoolean("me") || diff.getBoolean("ne") || !characteristic.equals("Standard")) {
                    System.err.println("Found noodles, mapping extension or non standard map. Skipping...");
                    continue;
                }


                double nps = diff.getDouble("nps");

                Pattern p;

                if (!new File(subFolder.getPath() + "/" + label + ".dat").exists()) label += characteristic;
                if (!new File(subFolder.getPath() + "/" + label + ".dat").exists()) {
                    System.err.println("No .dat file found in " + subFolder.getPath() + ". Skipping...");
                }

                if (!FileManager.readFile(subFolder.getPath() + "/" + label + ".dat").stream().filter(e -> e.contains("\"version\":\"3.")).toList().isEmpty()) {
                    System.err.println("Found a .dat file with version 3. Skipping...");
                    continue;
                }

                try {
                    p = new Pattern(subFolder.getPath() + "/" + label + ".dat");
                    if (diff.getBoolean("me")) throw new EOFException("test"); //I don't know why, but this is the only way to skip the rest of the code and continue the loop
                } catch (EOFException | JsonSyntaxException ex) {
                    continue;
                }

                ArrayList<String> metaGenre = new ArrayList<>();
                ArrayList<String> metaTags = new ArrayList<>();

                // Add genre & tags. But only those specified in Parameters.MUSIC_GENRE and Parameters.MAP_TAGS
                Arrays.stream(tags.split(",")).forEach(tag -> Parameters.MUSIC_GENRES.stream().filter(genre -> genre.equalsIgnoreCase(tag)).forEach(metaGenre::add));
                Arrays.stream(tags.split(",")).forEach(tag -> Parameters.MAP_TAGS.stream().filter(genre -> genre.equalsIgnoreCase(tag)).forEach(metaTags::add));

                p.metadata = new PatMetadata("default", bpm, nps, Collections.singletonList(label), metaTags, metaGenre);

                FileManager.overwriteFile(patFilePath + (!label.contains("Standard") ? label + "Standard" : label) + "_" + jsonFile.getName().replace(".json", "") + ".pat", p.exportInPatFormat());
            }


        } catch (IOException | NoSuchElementException e) {
            System.err.println("Error reading file: " + jsonFile.getPath() + ". Doesn't exist. Skipping...");
        } catch (JSONException e) {
            System.err.println("Error reading file: " + jsonFile.getPath() + ". JSON exception. Skipping...");
        }
    }

    /**
     * Initializes the sequences or patterns list with the files in the given folder path.
     * The type parameter is used to determine whether to initialize sequences or patterns.
     *
     * @param folderPath The path to the folder containing the sequences or patterns.
     * @param type       The type of the files in the folder. This will determine which list to initialize.
     */
    public void initialize(String folderPath, String type) {
        try {
            Path start = Paths.get(folderPath);

            // Use Files.walkFileTree to traverse the directory recursively
            Files.walkFileTree(start, EnumSet.noneOf(FileVisitOption.class), Integer.MAX_VALUE,
                    new SimpleFileVisitor<>() {
                        @Override
                        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                            try {
                                if (type.equals(Sequence.class.toString())) {
                                    sequences.add(new Sequence(file.toString()));
                                } else if (type.equals(Pattern.class.toString())) {
                                    patterns.add(new Pattern(file.toString()));
                                }
                            } catch (MalformedSequenceException | MalformedFileExtensionException e) {
                                throw new RuntimeException(e);
                            }

                            return FileVisitResult.CONTINUE;
                        }

                        @Override
                        public FileVisitResult visitFileFailed(Path file, IOException exc) {
                            System.err.println("Failed to visit file: " + file.toString());
                            return FileVisitResult.CONTINUE;
                        }
                    });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
