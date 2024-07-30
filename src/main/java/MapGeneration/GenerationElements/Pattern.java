package MapGeneration.GenerationElements;

import BeatSaberObjects.Objects.BeatSaberMap;
import BeatSaberObjects.Objects.Note;
import DataManager.Database.DatabaseEntities.*;
import DataManager.Database.DatabaseOperations.*;
import DataManager.FileManager;
import DataManager.Parameters;
import DataManager.Records.PatMetadata;
import MapAnalysation.PatternVisualisation.DirichletMultinomialDistributionVisualizer;
import MapAnalysation.PatternVisualisation.PatternVisualisationHeatMap;
import MapGeneration.GenerationElements.Exceptions.MalformattedFileException;
import MapGeneration.GenerationElements.Exceptions.NoteNotValidException;
import UserInterface.UserInterface;
import com.google.gson.Gson;

import javax.persistence.*;
import java.awt.*;
import java.io.*;
import java.util.*;
import java.util.List;
import java.util.logging.Level;
import java.util.stream.Collectors;


import static DataManager.Parameters.*;
import static MapAnalysation.Distributions.DirichletMultinomialDistribution.*;
import static MapAnalysation.Distributions.InverseDirichletMultinomialDistribution.estimateDirichletParameters;
import static MapAnalysation.Distributions.InverseDirichletMultinomialDistribution.estimateMultinomialProbabilities;

public class Pattern implements Iterable<PatternProbability>, Serializable {
    private static final int MAX_ARRAY_SIZE = 109; // lines * layers * cut directions = 4 * 3 * 9 = 108 + 1 (base note)

    // In this variable, all the possible notes are stored as patterns
    public Note[][] patterns = new Note[MAX_ARRAY_SIZE][MAX_ARRAY_SIZE];

    // This array stores how often a certain block follows another block. It contains the values of "patterns" array.
    public int[][] count = new int[MAX_ARRAY_SIZE][MAX_ARRAY_SIZE]; //for example, the Note from patterns[0][0] is followed by patterns[0][1] count[0][1] times
    public float[][] probabilities = new float[MAX_ARRAY_SIZE][MAX_ARRAY_SIZE];

    public PatMetadata metadata = new PatMetadata("default", -1.0, -1.0, Collections.singletonList("NULL"), new ArrayList<>(), new ArrayList<>());

    public static void main(String[] args) {
        java.util.logging.Logger.getLogger("org.hibernate").setLevel(Level.WARNING);

        Pattern p = new Pattern(Parameters.DEFAULT_PATTERN_METADATA);
        System.out.println(p.exportInPatFormat());
        System.out.println(p.getProbabilityOf(new Note(0, 2, 0, 1, 1)));
    }

    /**
     * Visualizes the current pattern as a heatmap without normalization or truncation.
     * <p>
     * This method calls the static `visualize` method of the `PatternVisualisationHeatMap` class
     * with normalization and truncation both set to false. The resulting heatmap will display
     * the raw count values as they are stored in the pattern.
     * </p>
     * <p>
     * A heatmap is a graphical representation of data where individual values are represented
     * by colors. In this case, each cell in the heatmap corresponds to a value in the count array.
     * </p>
     * <p>
     * Usage:
     * <pre>
     * {@code
     * Pattern pattern = new Pattern(metadata);
     * pattern.visualizeAsHeatmap();
     * }
     * </pre>
     * This will create a Swing window displaying the heatmap of the pattern's count array.
     */
    public void visualizeAsHeatmap(String... name) {
        PatternVisualisationHeatMap.visualizeAsHeatmap(this, name == null ? null : name[0]);
    }

    /**
     * Visualizes the current pattern as a normalized heatmap.
     * <p>
     * This method calls the static `visualize` method of the `PatternVisualisationHeatMap` class
     * with normalization set to true and truncation set to false. The count values in the pattern
     * will be normalized to the range 0-255 before being visualized. This ensures that the minimum
     * value is displayed as black and the maximum value is displayed as blue.
     * </p>
     * <p>
     * A normalized heatmap scales the values in the count array so that the smallest value becomes 0
     * and the largest value becomes 255. This preserves the relative differences between values while
     * fitting them into a standard color range.
     * </p>
     * <p>
     * Usage:
     * <pre>
     * {@code
     * Pattern pattern = new Pattern(metadata);
     * pattern.visualizeAsHeatmapNormalized();
     * }
     * </pre>
     * This will create a Swing window displaying the normalized heatmap of the pattern's count array.
     */
    public void visualizeAsHeatmapNormalized(String... name) {
        PatternVisualisationHeatMap.visualizeAsHeatmapNormalized(this, name == null || name.length == 0 ? null : name[0]);
    }

    /**
     * Visualizes the current pattern as a logarithmically normalized heatmap.
     * <p>
     * This method calls the static `visualize` method of the `PatternVisualisationHeatMap` class
     * with normalization set to true and truncation set to false. The count values in the pattern
     * will be normalized to the range 0-255 before being visualized. This ensures that the minimum
     * value is displayed as black and the maximum value is displayed as blue.
     * </p>
     * <p>
     * A normalized heatmap scales the values in the count array so that the smallest value becomes 0
     * and the largest value becomes 255. This preserves the relative differences between values while
     * fitting them into a standard color range.
     * </p>
     * <p>
     * Usage:
     * <pre>
     * {@code
     * Pattern pattern = new Pattern(metadata);
     * pattern.visualizeAsHeatmapLogarithmicNormalized();
     * }
     * </pre>
     * This will create a Swing window displaying the normalized heatmap of the pattern's count array.
     */
    public void visualizeAsHeatmapNormalizedLogarithmically(String... name) {
        PatternVisualisationHeatMap.visualizeAsHeatmapLogarithmicNormalized(this, name == null ? null : name[0]);
    }

    /**
     * Visualizes the current pattern as a truncated heatmap.
     * <p>
     * This method calls the static `visualize` method of the `PatternVisualisationHeatMap` class
     * with normalization set to false and truncation set to true. The count values in the pattern
     * will be truncated to the range 0-255 before being visualized. This means that any values
     * above 255 will be displayed as blue.
     * </p>
     * <p>
     * A truncated heatmap limits (or truncates) the values in the count array to a specified range,
     * in this case, 0-255. Values above 255 are capped at 255. This approach ensures that extreme
     * values do not skew the color representation excessively.
     * </p>
     * <p>
     * Usage:
     * <pre>
     * {@code
     * Pattern pattern = new Pattern(metadata);
     * pattern.visualizeAsHeatmapTruncated();
     * }
     * </pre>
     * This will create a Swing window displaying the truncated heatmap of the pattern's count array.
     */
    public void visualizeAsHeatmapTruncated(String... name) {
        PatternVisualisationHeatMap.visualizeAsHeatmapTruncated(this, name == null ? null : name[0]);
    }

    /**
     * Normalizes the count array values to the range 0-255 for each row separately.
     * If all values in a row are the same, they are set to 0 to avoid log(0).
     * If logarithmic is true, the values are normalized logarithmically.
     * <p>
     * The count must be a square matrix with a size of MAX_ARRAY_SIZE!
     */
    public static void normalizeCountArray(int[][] count, boolean logarithmic) {
        if (count == null) return;
        if (count.length != MAX_ARRAY_SIZE) throw new IllegalArgumentException("The count array must have a size of " + MAX_ARRAY_SIZE);

        // Iterate over each row
        for (int i = 0; i < count.length; i++) {
            int min = Integer.MAX_VALUE;
            int max = Integer.MIN_VALUE;

            // Find min and max values in the current row
            for (int j = 0; j < count.length; j++) {
                if (count[i][j] < min) {
                    min = count[i][j];
                }
                if (count[i][j] > max) {
                    max = count[i][j];
                }
            }

            // If all values in the row are the same, set them to 0 to avoid log(0)
            if (min == max) {
                for (int j = 0; j < count.length; j++) {
                    count[i][j] = 0;
                }
            } else {
                // Normalize the values in the current row to the range 0-255
                for (int j = 0; j < count.length; j++) {
                    if (logarithmic) count[i][j] = (int) ((Math.log(count[i][j] - min + 1) / Math.log(max - min + 1)) * 255);
                    else count[i][j] = (int) ((count[i][j] - min) / (double) (max - min) * 255);
                }
            }
        }
    }

    // Inverse of logarithmic scaling
    public static void inverseNormalizeCountArray(int[][] count, boolean logarithmic, int N) {
        if (count == null) return;
        if (count.length != MAX_ARRAY_SIZE) throw new IllegalArgumentException("The count array must have a size of " + MAX_ARRAY_SIZE);

        // Iterate over each row
        for (int i = 0; i < count.length; i++) {
            int min = Integer.MAX_VALUE;
            int max = Integer.MIN_VALUE;

            // Find min and max values in the current row
            for (int j = 0; j < count[i].length; j++) {
                if (count[i][j] < min) {
                    min = count[i][j];
                }
                if (count[i][j] > max) {
                    max = count[i][j];
                }
            }

            if (min != max) {
                // Reverse the normalization of the values in the current row
                for (int j = 0; j < count[i].length; j++) {
                    if (logarithmic) count[i][j] = (int) Math.exp(((double) count[i][j] / (N * N)) * Math.log((max - min + 1) * N)) + min - 1;
                    else count[i][j] = (int) ((count[i][j] - min) * (double) (max - min) * 255);
                }
            }
        }
    }


    // Constructor that analyzes the patterns based on the provided notes and type
    public Pattern(Note[] notes, int type) throws NoteNotValidException {
        if (type != 0 && type != 1 || notes == null) return;

        if (!checkIfNotesAreValid(notes)) throw new NoteNotValidException("The notes are not valid!");

        // Analyze the patterns based on the provided notes and type
        analyzePattern(notes, type);

        // Compute the probabilities of each following note
        computeProbabilities();

        //remove all timings to make it look better
        for (Note n : notes) n._time = 0;
    }

    private boolean checkIfNotesAreValid(Note[] notes) {
        for (Note n : notes) {
            if (n == null) return false;
            if (n._lineIndex < 0 || n._lineIndex > 3) return false;
            if (n._lineLayer < 0 || n._lineLayer > 2) return false;
//            if (n._type < 0 || n._type > 1) return false;
            if (n._cutDirection < 0 || n._cutDirection > 8) return false;

        }
        return true;
    }

    /**
     * Create a default empty pattern object. This can be further modified
     *
     * @implNote keep in mind to update the metadata variable!
     */
    public Pattern() {
    }

    /**
     * Create a new pattern object based on a pattern file.
     * If the file is a .pat file or a folder, then It will be processed as a .pat file. Otherwise, it will be processed as a .json file (standard BeatSaberV2 format).
     * This is used for importing maps into patterns to be saved in the database.
     *
     * @param pathToPatternFile The path to the pattern file
     * @param metadata          The PatMetadata record of the pattern
     */
    public Pattern(String pathToPatternFile, PatMetadata metadata) throws NoteNotValidException {
        Pattern p = new Pattern(pathToPatternFile);
        this.metadata = metadata;
        this.patterns = p.patterns;
        this.count = p.count;
        this.probabilities = p.probabilities;
    }

    /**
     * Create a new pattern object based on a pattern file.
     * If the file is a .pat file or a folder, then It will be processed as a .pat file. Otherwise, it will be processed as a .json file (standard BeatSaberV2 format).
     *
     * @param pathToPatternFile The path to the pattern file
     */
    public Pattern(String pathToPatternFile) throws NoteNotValidException {
        //If it's not in the database, then check if it's a .pat file
        File f = new File(pathToPatternFile);
        if (f.exists() && (f.isDirectory() || pathToPatternFile.endsWith(".pat"))) {
            try {
                readFromPatFile(pathToPatternFile);
            } catch (MalformattedFileException e) {
                throw new RuntimeException(e);
            }
            return;
        }

        //If it's not a .pat file, then it's a .json (V2) or a .dat(V3+) file that is the standard BeatSaberV2 format
        // Read the pattern file and convert it to a BeatSaberMap object
        BeatSaberMap patterns = BeatSaberMap.newMapFromJSON(pathToPatternFile);

        // Create a new Pattern object based on the BeatSaberObjects.Objects.BeatSaberMap
        Pattern p = new Pattern(patterns._notes, 1);

        // Copy the patterns, count, and probabilities from the created Pattern object
        this.count = p.count;
        this.patterns = p.patterns;
        this.probabilities = p.probabilities;
    }


    /**
     * Constructs a Pattern object from the database based on the provided metadata. Validates the tags, genres, and difficulties
     * against the predefined parameters, retrieves or creates the pattern description, and initializes the pattern
     * from the database.
     *
     * @param metadata the metadata containing tags, genres, difficulties, and other relevant information
     * @throws IllegalArgumentException if any of the tags, genres, or difficulties are not found in the database,
     *                                  or if the pattern is not found in the database
     */
    public Pattern(PatMetadata metadata) {
        if (!useDatabase && metadata.equals(Parameters.DEFAULT_PATTERN_METADATA)) {
            //TODO:
        }

        this.metadata = metadata;

        // Convert predefined parameters to lowercase sets for case-insensitive comparison
        Set<String> lowerCaseMapTags = Parameters.MAP_TAGS.stream().map(String::toLowerCase).collect(Collectors.toSet());
        Set<String> lowerCaseMusicGenres = Parameters.MUSIC_GENRES.stream().map(String::toLowerCase).collect(Collectors.toSet());
        Set<String> lowerCaseDifficulties = Parameters.DIFFICULTIES.stream().map(String::toLowerCase).collect(Collectors.toSet());

        // Validate tags, genres, and difficulties against the predefined parameters
        if (!lowerCaseMapTags.containsAll(metadata.tags().stream().map(String::toLowerCase).toList()) && !lowerCaseMapTags.containsAll(metadata.tags()))
            throw new IllegalArgumentException("Tag(s) not found in database: " + metadata.tags());
        if (!lowerCaseMusicGenres.containsAll(metadata.genre().stream().map(String::toLowerCase).toList()) && !lowerCaseMusicGenres.containsAll(metadata.genre()))
            throw new IllegalArgumentException("Genre(s) not found in database: " + metadata.genre());
        if (!lowerCaseDifficulties.containsAll(metadata.difficulty().stream().map(String::toLowerCase).toList()) && !lowerCaseDifficulties.containsAll(metadata.difficulty()))
            throw new IllegalArgumentException("Difficulty not found in database: " + metadata.difficulty());


        PatternDescriptionEntity desc;
        try {
            // Attempt to retrieve the pattern description from the database
            desc = PatternDescriptionEntityOperations.getPatternDescription(metadata);
            if (desc == null) throw new NoResultException("Pattern not found in the database");
        } catch (NoResultException e) {
            // If the pattern is not found, throw an exception (could alternatively create a new pattern)
            //The Pattern has not been found in the database, so we create a new one:
            throw new IllegalArgumentException("Pattern not found in the database: " + metadata);
        }

        // Retrieve pattern entities based on the pattern description
        List<PatternEntity> databasePatterns = PatternEntityOperations.getPatternByDescription(desc);
        for (PatternEntity p : databasePatterns) {
            // Convert database entities to notes and initialize the pattern
            Note base = Objects.requireNonNull(NoteEntityOperations.getNoteById(p.getNoteId())).toNote();
            Note follower = Objects.requireNonNull(NoteEntityOperations.getNoteById(p.getFollowedByNoteId())).toNote();
            int count = p.getCount();

            // Create and merge the pattern
            Pattern pattern = new Pattern();
            pattern.patterns[0][0] = base;
            pattern.patterns[0][1] = follower;
            pattern.count[0][1] = count;

            this.merge(pattern);
        }
    }

    /**
     * Constructs a Pattern object based on the pattern description ID. Retrieves the pattern description
     * from the database, initializes metadata, and creates the pattern using the retrieved metadata.
     *
     * @param patternDescriptionId the ID of the pattern description
     * @throws IllegalArgumentException if the pattern description is not found in the database
     */
    public Pattern(int patternDescriptionId) {
        // Retrieve the pattern description from the database
        PatternDescriptionEntity description = PatternDescriptionEntityOperations.getPatternDescription(patternDescriptionId);
        if (description == null) {
            System.err.println("[WARN]: Pattern not found in database: " + patternDescriptionId);
            throw new IllegalArgumentException("Pattern not found in database: " + patternDescriptionId);
        }

        // Initialize metadata from the pattern description entity
        this.metadata = new PatMetadata(
                description.getName(),
                description.getBpm(),
                description.getNps(),
                DifficultyAssignmentEntityOperations.getDifficultiesForPatternID(description.getId()),
                TagAssignmentEntityOperations.getTagsForPattern(description.getId()),
                GenreAssignmentEntityOperations.getGenresForPatternID(description.getId())
        );

        // Create a new pattern using the retrieved metadata
        Pattern p = new Pattern(metadata);

        // Initialize the pattern properties with the newly created pattern
        this.patterns = p.patterns;
        this.count = p.count;
        this.probabilities = p.probabilities;
    }


    /**
     * Saves or updates the pattern in the database.
     *
     * @return true if the operation was successful, false otherwise
     */
    public boolean saveOrUpdateInDatabase() {
        return databaseOperation("save");
    }

    /**
     * Deletes the pattern from the database.
     *
     * @return true if the operation was successful, false otherwise
     */
    public boolean deleteFromDatabase() {
        if (databaseOperation("delete")) {
            if (verbose) System.out.println("[INFO]: Successfully deleted pattern from database: " + metadata);
            return true;
        } else {
            System.err.println("[WARN]: Failed to delete pattern: " + metadata);
            return false;
        }
    }

    /**
     * Performs the specified database operation (save or delete) on the pattern.
     *
     * @param operation the operation to perform ("save" or "delete")
     * @return true if the operation was successful, false otherwise
     */
    private boolean databaseOperation(String operation) {
        if (operation == null || operation.isEmpty() || Objects.equals(operation, "update") || Objects.equals(operation, "save")) {
            operation = "save";
        } else if (Objects.equals(operation, "delete") || Objects.equals(operation, "remove")) {
            operation = "delete";
        }

        PatternDescriptionEntity description;
        if (operation.equals("save")) {
            description = PatternDescriptionEntityOperations.savePatternDescription(metadata);
        } else if (operation.equals("delete")) {
            description = PatternDescriptionEntityOperations.getPatternDescription(metadata);
        } else {
            return true;
        }

        System.out.println("Pattern to be " + operation + "d: " + description);

        for (int i = 0; i < patterns.length; i++) {
            Note base = patterns[i][0];
            if (base == null) break;
            for (int j = 1; j < patterns[i].length; j++) {
                Note follower = patterns[i][j];
                if (follower == null) break;
                int count = this.count[i][j];

                NoteEntity baseEntity;
                NoteEntity followerEntity;
                try {
                    baseEntity = NoteEntityOperations.getNote(base);
                    followerEntity = NoteEntityOperations.getNote(follower);
                    if (baseEntity == null || followerEntity == null) throw new NoResultException("Note not found in database: " + base + " or " + follower);
                } catch (NoResultException e) {
                    System.err.println("Note not found in database: " + base + " or " + follower);
                    continue;
                }

                PatternEntity pattern = new PatternEntity();
                pattern.setPatternDescriptionId(description.getId());
                pattern.setNoteId(baseEntity.getId());
                pattern.setFollowedByNoteId(followerEntity.getId());
                pattern.setCount(count);

                boolean success = false;
                if (Objects.equals(operation, "save")) success = PatternEntityOperations.saveOrUpdatePattern(pattern, entityManager);
                if (Objects.equals(operation, "delete")) {
                    pattern = PatternEntityOperations.getPattern(description.getId(), baseEntity.getId(), followerEntity.getId());
                    success = PatternEntityOperations.deletePattern(pattern, entityManager);
                }

                if (success && Parameters.verbose) System.out.println("Successfully " + operation + "d pattern: " + pattern);
                if (!success && Parameters.verbose) System.err.println("Failed to " + operation + " pattern: " + pattern);
            }
        }

        if (operation.equals("delete")) {
            // Perform additional delete operations for related entities
            boolean success = true;
            success &= DifficultyAssignmentEntityOperations.deleteAssignmentEntity(metadata, description, entityManager);
            success &= TagAssignmentEntityOperations.deleteTagAssignmentEntity(metadata, description, entityManager);
            success &= GenreAssignmentEntityOperations.deleteGenreAssignmentEntity(metadata, description, entityManager);
            success &= PatternDescriptionEntityOperations.deletePatternDescriptionEntity(metadata, description, entityManager);

            if (success) {
                System.out.println("[INFO]: Successfully deleted PatternDescription: " + description);
            } else {
                System.err.println("[WARN]: Failed to delete PatternDescription: " + description);
            }
            return success;
        }

        return true;
    }


    /**
     * Create a new pattern object based on a pattern file.
     * A pattern file is a file that contains a list of patterns in the .pat file format.
     * A line always represents the probabilities that a certain note will follow a given note.<br>
     * All notes are separated by a semicolon;<br>
     * The pat format is as follows: <br>
     * <br>
     * _lineIndex,_lineLayer,_type,_cutDirection,count ; _time,_lineIndex,_lineLayer,_type,_cutDirection,count ; ... (If there are more than one notes in the pattern) <br>
     * Example: 0.0,2.0,2.0,1,0;0.0,2.0,0.0,1,1,2
     *
     * @param pathToPatternFile The path to the pattern file
     */
    private void readFromPatFile(String pathToPatternFile) throws MalformattedFileException {
        List<String> lines = FileManager.readFile(pathToPatternFile);

        String[] metadata = lines.get(0).split(";");
        for (int i = 0; i < metadata.length; i++) {
            if (metadata[i].contains("[")) metadata[i] = metadata[i].replaceAll("\\[", "");
            if (metadata[i].contains("]")) metadata[i] = metadata[i].replaceAll("]", "");
        }

        if (metadata.length == 5) {
            this.metadata = new PatMetadata(
                    pathToPatternFile.contains("/") ? pathToPatternFile.substring(pathToPatternFile.lastIndexOf("/")) : pathToPatternFile, //set the filename as the name of the Pattern
                    Float.parseFloat(metadata[0].replaceAll(" ", "")),
                    Float.parseFloat(metadata[1].replaceAll(" ", "")),
                    Collections.singletonList(metadata[2]),
                    metadata[3].contains(",") ? List.of(metadata[3].split(",")) : List.of(metadata[3]),
                    metadata[4].contains(",") ? List.of(metadata[4].split(",")) : List.of(metadata[4])
            );
        } else if (metadata.length == 6) {
            this.metadata = new PatMetadata(
                    metadata[0],
                    Float.parseFloat(metadata[1].replaceAll(" ", "")),
                    Float.parseFloat(metadata[2].replaceAll(" ", "")),
                    Collections.singletonList(metadata[3]),
                    metadata[4].contains(",") ? List.of(metadata[4].split(",")) : List.of(metadata[4]),
                    metadata[5].contains(",") ? List.of(metadata[5].split(",")) : List.of(metadata[5])
            );
        } else {
            throw new MalformattedFileException("The file is not in the correct format. The metadata is not correct.");
        }

        this.metadata.tags().stream().filter(tag -> !Parameters.MAP_TAGS.contains(tag)).forEach(tag -> System.err.println("Unknown tag: " + tag));
        this.metadata.genre().stream().filter(genre -> !Parameters.MUSIC_GENRES.contains(genre)).forEach(genre -> System.err.println("Unknown genre: " + genre));

        for (int lineIndex = 1, i = 0; lineIndex < lines.size(); lineIndex++, i++) {
            if (lines.get(lineIndex).contains(".")) throw new MalformattedFileException("The file contains a dot (.) in line " + lineIndex + ". This is not allowed in the .pat file format.");

            String[] split = lines.get(lineIndex).split(";");
            patterns[i][0] = new Note(0,
                    Integer.parseInt(String.valueOf(split[0].charAt(0))),
                    Integer.parseInt(String.valueOf(split[0].charAt(1))),
                    Integer.parseInt(String.valueOf(split[0].charAt(2))),
                    Integer.parseInt(String.valueOf(split[0].charAt(3))));

            for (int j = 1; j < split.length; j++) {
                if (split[j] == null) break;
                int count = Integer.parseInt(split[j].substring(4));
                patterns[i][j] = new Note(0,
                        Integer.parseInt(String.valueOf(split[j].charAt(0))),
                        Integer.parseInt(String.valueOf(split[j].charAt(1))),
                        Integer.parseInt(String.valueOf(split[j].charAt(2))),
                        Integer.parseInt(String.valueOf(split[j].charAt(3))));
                this.count[i][j] = count;
            }
        }

        computeProbabilities();
    }

    /**
     * Exports the pattern analysis results in a .pat file format.
     * A line always represents the probabilities that a certain note will follow a given note.
     * All notes are separated by a semicolon;<br>
     * The pat format is as follows: <br>
     * <br>
     * _lineIndex,_lineLayer,_type,_cutDirection,count ; _time,_lineIndex,_lineLayer,_type,_cutDirection,count ; ... (If there are more than one notes in the pattern) <br>
     * Example: 0.0,2.0,2.0,1,0;0.0,2.0,0.0,1,1,2
     *
     * @return a Pattern in the .pat file format.
     */
    public String exportInPatFormat() {
        StringBuilder s = new StringBuilder();
        int counter = 0;
        s.append(metadata.toString());

        // Iterate over the pattern array
        for (Note[] notes : this.patterns) {
            if (notes[0] == null) continue;
            // Append the string representation of the first note in the pattern
            int lineIndex = (int) Math.round(notes[0]._lineIndex);
            int lineLayer = (int) Math.round(notes[0]._lineLayer);
            int type = notes[0]._type;
            int cutDirection = notes[0]._cutDirection;

            s.append(lineIndex).append(lineLayer).append(type).append(cutDirection).append(";");

            // Iterate over the remaining notes in the pattern
            // Append the string representation of the note and its count
            for (int i = 1; i < notes.length; i++)
                if (notes[i] != null) s.append(notes[i].exportInPatFormat()).append(this.count[counter][i]).append(";");


            // Append the closing bracket for the pattern
            s.append("\n");
            s.delete(s.length() - 2, s.length() - 1);

            counter++;
        }

        return s.toString();
    }


    /**
     * Analyzes the pattern of notes in the given map for a specific type.
     * <p>
     * The analyzePattern method analyzes the pattern of notes in the given map for a specific type. It first removes all notes from the map that do not match the specified type. Then, it iterates through each note in the modified notes array, checking if the note is null or has a cut direction of 8. If it passes the checks, it searches for the note and its previous note in the 2-dimensional patterns array.
     * <p>
     * If the previous note is not found in the pattern list, it adds the previous note and the current note to the pattern list and initializes the count. If the previous note is found, it checks if the current note is already saved in the pattern list. If it is not, it adds the current note to the pattern list and initializes the count. If it is already saved, it increments the count.
     * <p>
     * The method uses a label (twoDimArr) to break out of the outer loop when a match is found, avoiding unnecessary iterations.
     *
     * @param map  The array of notes to analyze.
     * @param type The type of notes to consider in the analysis.
     */
    public void analyzePattern(Note[] map, int type) {
        Note[] notes = removeAllOtherTypes(map, type);

        for (int k = 1; k < notes.length; k++) {
            Note n = notes[k];
            Note prev = notes[k - 1];

            if (n == null || n._cutDirection == 8) continue;

            // Iterating over the 2-dimensional array
            twoDimArr:
            for (int i = 0; i < patterns.length; i++) {

                // If the previous Note was not found in the pattern list
                if ((patterns[i][0] == null)) {
                    // Add the previous Note and the current BeatSaberObjects.Objects.Note to the pattern list
                    patterns[i][0] = prev;
                    patterns[i][1] = n;
                    count[i][1] = 1;
                    break;

                    // If it was found, then check if n is already saved in the pattern list
                } else if (patterns[i][0].equalPlacement(prev)) {
                    for (int j = 1; j < patterns[i].length; j++) {
                        if (patterns[i][j] == null) {
                            // Add the current Note to the pattern list
                            patterns[i][j] = n;
                            count[i][j] = 1;
                            break twoDimArr; // Break out of the outer loop
                        } else if (patterns[i][j].equalPlacement(n)) {
                            // Increment the count if the current BeatSaberObjects.Objects.Note is already saved in the pattern list
                            count[i][j]++;
                            break twoDimArr; // Break out of the outer loop
                        }
                    }
                }
            }
        }
    }


    /**
     * Returns a string representation of the pattern analysis results.
     * The string includes the notes in the patterns, their counts, and probabilities.
     *
     * @return The string representation of the pattern analysis results.
     */
    public String toString() {
        StringBuilder s = new StringBuilder();
        int counter = 0;

        // Iterate over the pattern array
        for (Note[] notes : this.patterns) {
            if (notes[0] != null) {
                // Append the string representation of the first note in the pattern
                s.append(notes[0].toString().replaceAll("\n", "")).append(": [\n");

                // Iterate over the remaining notes in the pattern
                for (int i = 1; i < notes.length; i++) {
                    if (notes[i] != null) {
                        // Append the string representation of the note, its count, and probability
                        s.append("  ").append(notes[i].toString().replaceAll("\n", "")).append(": ").append(this.count[counter][i]).append(" times = ").append(this.probabilities[counter][i]).append("% , \n");
                    }
                }

                // Append the closing bracket for the pattern
                s.append("] \n");
            }
            counter++;
        }

        return s.toString();
    }


    /**
     * Removes notes that occur fewer than the specified threshold number of times from the pattern analysis.
     * Returns a string representation of the removed notes.
     * <p>
     * The removeXTimes method removes notes from the pattern analysis that occur fewer than the specified threshold number of times.
     * The method starts by initializing variables and arrays to store intermediate results and modified patterns.
     * It splits the string representation of the pattern analysis into an array of strings.
     * It then iterates over each string in the array to check if it contains an occurrence count within the threshold.
     * If a string does not contain the occurrence count within the threshold, it adds it to the split list.
     * Next, it tries to remove unnecessary patterns and closing brackets from the split list.
     * After that, it resets the patterns, count, and probabilities arrays to their original size.
     * It initializes positionY and positionX variables to keep track of the current position in the arrays.
     * The method builds a new array without the notes that have been removed based on the threshold.
     * It parses the strings to extract note information, including probability and count.
     * The note, probability, and count are added to the corresponding arrays.
     * Finally, it recalculates the probabilities for the updated patterns using the computeProbabilities method.
     * The method returns the string representation of the removed notes.
     *
     * @param threshold The minimum number of occurrences for a note to be retained.
     */
    public void removeXTimes(int threshold) {
        String s = this.toString();
        List<String> split = getStrings(threshold, s);

        try {
            // Iterates over the split list to remove unnecessary patterns and closing brackets
            for (int i = 0; i < split.size(); i++) {
                if (split.get(i).contains("[")
                        && split.get(i + 1).contains("]")
                        && !split.get(i + 1).contains("time")) {
                    split.remove(i);
                    i--;
                }

                if (split.get(i).contains("]") && split.get(i + 1).contains("]")) {
                    split.remove(i + 1);
                    i--;
                }
            }
        } catch (IndexOutOfBoundsException e) {
            // Ignore any index out of bounds exceptions
        }

        // Resets the patterns, count, and probabilities arrays to the original size
        this.patterns = new Note[this.patterns.length][this.patterns[0].length];
        this.count = new int[this.patterns.length][this.patterns[0].length];
        this.probabilities = new float[this.patterns.length][this.patterns[0].length];

        int positionY = 0;
        int positionX = 0;

        // Builds a new array without notes that have been removed based on the threshold
        for (int i = 0; i < split.size() && this.patterns[positionY] != null; i++) {
            String note = split.get(i).split("}:")[0] + "}";

            if (split.get(i).contains("[")) {
                // If it is a note, initializes the positionX and adds the note to the patterns array
                positionX = 0;
                this.patterns[positionY][positionX] = new Gson().fromJson(note, Note.class);
                positionX++;
            } else if (split.get(i).contains("]")) {
                // If it is the end of a pattern, increments the positionY
                positionY++;
            } else {
                // Extracts information from the string to obtain the probability and count of the note
                float prob = Float.parseFloat(split.get(i).split("}:")[1].split(" = ")[1].split("%")[0].replaceAll(" ", ""));
                int count = Integer.parseInt(split.get(i).split("}:")[1].split(" times")[0].replaceAll(" ", ""));

                // Adds the note, probability, and count to the patterns, probabilities, and count arrays respectively
                this.patterns[positionY][positionX] = new Gson().fromJson(note, Note.class);
                this.probabilities[positionY][positionX] = prob;
                this.count[positionY][positionX] = count;

                positionX++;
            }
        }

        // Recalculates the probabilities for the updated patterns
        computeProbabilities();
    }

    //Helper method for removeXTimes
    private static List<String> getStrings(int threshold, String s) {
        String[] strings = s.split("\n");
        List<String> split = new ArrayList<>();

        // Iterates over each string in the array
        for (String ss : strings) {
            boolean contains = false;

            // Checks if the string contains the occurrence count within the threshold
            for (int i = 0; i <= threshold; i++) {
                if (ss.contains(" " + i + " time")) {
                    contains = true;
                    break;
                }
            }

            // If the string does not contain the occurrence count within the threshold, adds it to the split list
            if (!contains) {
                split.add(ss + "\n");
            }
        }
        return split;
    }


    /**
     * Computes the probabilities of each following note in the patterns array.
     * The probabilities are calculated based on the counts of each note occurrence.
     */
    public void computeProbabilities() {
        for (int i = 0; i < this.patterns.length; i++) {

            // Counts how many notes are present in the arrays
            int ct = 0;
            for (int j = 0; j < this.patterns[i].length; j++) {
                this.probabilities[i][j] = 0;
                if (this.patterns[i][j] != null) ct += this.count[i][j];
            }

            // Check if ct is zero to avoid division by zero
            if (ct == 0) {
                // Handle the case when no elements are present
                for (int j = 0; j < this.patterns[i].length; j++) {
                    this.probabilities[i][j] = 0; // You can assign any appropriate default value
                }
            } else {
                // Calculates the probability for every note
                for (int j = 0; j < this.patterns[i].length; j++) {
                    if (this.patterns[i][j] != null)
                        this.probabilities[i][j] = (float) this.count[i][j] / ct * 100;
                }
            }
        }

        if (containsNaN()) throw new RuntimeException("CONTAINS NAN!");
    }

    public boolean containsNaN() {
        for (float[] pat : this.probabilities)
            for (float p : pat)
                if (Double.isNaN(p))
                    return true;

        return false;
    }


    /**
     * Removes all notes from the given array that do not match the specified type.
     * <p>
     * The removeAllOtherTypes method takes an array of notes (notes) and an integer (type) as parameters.
     * It removes all notes from the notes array that do not match the specified type.
     * The method returns an array of notes that only contains notes of the specified type.
     * Inside the method, a List called noteList is created to store the filtered notes.
     * The method iterates over the notes array using an enhanced for loop.
     * For each note n in the notes array, it checks if the _type of the note matches the specified type.
     * If the types match, the note is added to the noteList.
     * After iterating over all the notes, the noteList contains only the notes that have the specified type.
     * Finally, the noteList is converted back to an array using the toArray method, specifying the array type as BeatSaberObjects.Objects.Note[], and returned.
     *
     * @param notes The array of notes to filter.
     * @param type  The type of notes to keep.
     * @return An array of notes that only contains notes of the specified type.
     */
    private static Note[] removeAllOtherTypes(Note[] notes, int type) {
        List<Note> noteList = new ArrayList<>();

        // Iterate over the notes array and add notes of the specified type to the noteList
        for (Note n : notes) {
            if (n._type == type)
                noteList.add(n);
        }

        // Convert the noteList back to an array and return it
        return noteList.toArray(Note[]::new);
    }

    /**
     * Retrieves the probability of a specific note pattern.
     * <p>
     * The getProbabilityOf method takes a BeatSaberObjects.Objects.Note object (n) as a parameter.
     * It retrieves the probability of the specified note pattern.
     * The method returns a PatternProbability object that represents the probability of the note pattern.
     * If the note pattern is not found, the method returns null.
     * Inside the method, a loop iterates over the patterns array.
     * The loop condition checks if the loop index is within the bounds of the patterns array and if the current pattern is not null.
     * Within the loop, three checks are performed:
     * - If the pattern array is unexpectedly null, the method returns null.
     * - If the first note in the pattern is unexpectedly null, the method returns null.
     * - If the placement of the first note in the pattern matches the placement of the specified note (n), a new PatternProbability object is created using the current pattern and its corresponding probability. This object is then returned.
     * - If no matching note pattern is found after iterating over all patterns, the method returns null.
     *
     * @param n The note for which to retrieve the probability.
     * @return A PatternProbability object that represents the probability of the note pattern, or null if the note pattern is not found.
     */
    public PatternProbability getProbabilityOf(Note n) {
//        Pattern p = adjustVariance(this);
        Pattern p = this;

        // Iterate over the patterns array and check for a matching note pattern
        for (int i = 0; i < p.patterns.length && p.patterns[i] != null; i++) {

            // Check if the pattern array is null (unexpected)
            if (p.patterns[i] == null) return null;

            // Check if the first note in the pattern is null (unexpected)
            if (p.patterns[i][0] == null) return null;

            // Check if the placement of the first note in the pattern matches the specified note
            if (p.patterns[i][0].equalPlacement(n))
                return new PatternProbability(p.patterns[i], p.probabilities[i]);
        }

        // If no matching note pattern is found, return null
        return null;
    }


    public Pattern clonePattern() {
        Pattern p = new Pattern();
        System.arraycopy(p.patterns, 0, patterns, 0, p.patterns.length);
        System.arraycopy(p.count, 0, count, 0, p.count.length);
        System.arraycopy(p.probabilities, 0, probabilities, 0, p.probabilities.length);
        p.metadata = metadata;
        return p;
    }

    public Pattern deepCopy() {
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(bos);
            oos.writeObject(this);
            oos.flush();
            oos.close();

            ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
            ObjectInputStream ois = new ObjectInputStream(bis);
            Pattern p = (Pattern) ois.readObject();
            bis.close();
            ois.close();

            return p;
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }


    /**
     * Returns an iterator over the pattern probabilities.
     * <p>
     * The iterator method is overridden to provide an iterator over the pattern probabilities.
     * The method returns an iterator object that allows iterating over the pattern probabilities.
     * Inside the method, a new iterator object is created as an anonymous inner class.
     * The iterator maintains an index i to keep track of the current position during iteration.
     * The hasNext method checks if there are more elements to iterate.
     * It verifies if the iterator index i is beyond the bounds of the patterns array or if the current pattern is null.
     * If the conditions are met, it returns false to indicate that there are no more elements to iterate; otherwise, it returns true.
     * The next method retrieves the next pattern probability.
     * It creates a new PatternProbability object using the current pattern (patterns[i]) and its corresponding probability (probabilities[i]).
     * It increments the iterator index i to move to the next position.
     * It returns the created PatternProbability object.
     *
     * @return An iterator that allows iterating over the pattern probabilities.
     */
    @Override
    public Iterator<PatternProbability> iterator() {
        // Create a new iterator object
        return new Iterator<>() {
            int i = 0; // Iterator index

            /**
             * Checks if there are more elements to iterate.
             *
             * @return true if there are more elements, false otherwise.
             */
            @Override
            public boolean hasNext() {
                // Check if the iterator index is beyond the bounds of the patterns array or if the current pattern is null
                if (i >= patterns.length) return false;
                return patterns[i] != null;
            }

            /**
             * Retrieves the next pattern probability.
             *
             * @return The next PatternProbability object.
             */
            @Override
            public PatternProbability next() {
                // Create a new PatternProbability object using the current pattern and its corresponding probability
                PatternProbability p = new PatternProbability(patterns[i], probabilities[i]);
                i++; // Increment the iterator index
                return p; // Return the PatternProbability object
            }
        };
    }

    public List<List<Note>> asList() {
        List<List<Note>> list = new ArrayList<>();
        for (Note[] pat : this.patterns) {
            ArrayList<Note> l = new ArrayList<>();
            for (Note n : pat) {
                if (n == null) break;
                l.add(n);
            }
            if (l.isEmpty()) break;
            list.add(l);
        }

        return list;
    }


    /**
     * Merges the patterns from the specified {@code Pattern} object into this {@code Pattern} object.
     * <p>
     * This method integrates the notes, counts, and probabilities from the given pattern into the current pattern.
     * It follows these rules:
     * - If a note pattern in the given pattern does not exist in this pattern, it is added.
     * - If a note pattern exists, the counts for each note are updated. If a note in the given pattern is not present in the existing pattern, it is added.
     * - After merging, the probabilities are recalculated for the entire pattern.
     * <p>
     * The merging process involves checking each note pattern in the given {@code Pattern} object:
     * - If the key (first note in a pattern) does not exist in this pattern, the entire note pattern is added.
     * - If the key exists, the method checks each subsequent note in the pattern.
     * - If the note exists, its count is incremented by the count from the given pattern.
     * - If the note does not exist, it is added along with its count.
     * <p>
     * The method ensures that the merged patterns are properly integrated without duplication,
     * maintaining the integrity of the pattern sequences and their respective counts and probabilities.
     * * @param p the {@code Pattern} object to merge into this pattern. It should not be {@code null}.
     */
    public void merge(Pattern p) {
        int lastKey = 0;
        for (; lastKey < patterns.length; lastKey++) if (patterns[lastKey][0] == null) break;

        for (int i = 0; i < p.patterns.length; i++) { //Why did I do i=1 here? I don't know... If something breaks, check this!
            if (p.patterns[i][0] == null) break;

            if (lastKey >= patterns.length) throw new RuntimeException("ja mann"); //If the pattern is full, then stop (this should never happen, but just in case...)


            int key = containsKey(patterns, p.patterns[i][0]); //This contains they key where the Note is saved in this.patterns
            if (key == -1) {
                patterns[lastKey] = p.patterns[i];
                probabilities[lastKey] = p.probabilities[i];
                for (int j = 0; j < p.patterns[i].length; j++) {
                    if (p.patterns[i][j] == null) break;
                    patterns[lastKey][j] = p.patterns[i][j];
                    count[lastKey][j] = p.count[i][j];
                }
                lastKey++;

            } else {
                for (int j = 1; j < p.patterns[i].length; j++) { //The i=1 is needed because [0] equals the note that is being looked at

                    int value = containsValue(patterns[key], p.patterns[i][j]); //This contains the index where the Note is saved in this.patterns[key]
                    if (value == -1) {
                        for (int k = 1; k < patterns[key].length; k++) {
                            if (patterns[key][k] == null) {
                                patterns[key][k] = p.patterns[i][j];
                                count[key][k] = p.count[i][j];
                                break;
                            }
                        }
                    } else {
                        count[key][value] += p.count[i][j];
                    }
                }
            }
        }

        computeProbabilities();
    }

    private int containsKey(Note[][] patterns, Note n) {
        for (int i = 0; i < patterns.length; i++) {
            if (patterns[i][0] == null) return -1;
            if (patterns[i][0].equalPlacement(n)) return i;
        }
        return -1;
    }

    private int containsValue(Note[] values, Note n) {
        for (int i = 1; i < values.length; i++) {
            if (values[i] == null) return -1;
            if (values[i].equalPlacement(n)) return i;
        }
        return -1;
    }

    public static Pattern adjustVariance(Pattern pattern) {
        if (UserInterface.patternVariance == 0) {
            return pattern;
        }
        Pattern p = pattern.deepCopy();

        if (UserInterface.patternVariance < 0) {
            System.out.println("Variance: " + UserInterface.patternVariance);
            Pattern.inverseNormalizeCountArray(p.count, true, (UserInterface.patternVariance * -1));
            Pattern.normalizeCountArray(p.count, true);
        } else {
            p.applyDirichletMultinomial(UserInterface.patternVariance);
            Pattern.normalizeCountArray(p.count, true);
        }
        System.out.println("Applied Dirichlet Multinomial Distribution");

        p.computeProbabilities();
        return p;
    }


    public void applyDirichletMultinomial(int N) {
        for (int i = 0; i < patterns.length; i++) {
            if (patterns[i][0] == null) break; // Beende die Schleife, wenn keine weiteren Muster vorhanden sind
            double[] dirichletSample = sampleDirichlet(this.count[i]);
            int[] multinomialSample = sampleMultinomial(N, dirichletSample);
            int[] mle = estimateAlphaMLE(multinomialSample, count[i], N);
//            System.arraycopy(mle, 0, this.count[i], 0, mle.length);
            System.arraycopy(multinomialSample, 0, this.count[i], 0, multinomialSample.length);
        }
        computeProbabilities();
    }

    @Deprecated
    public void applyInverseDirichletMultinomial(int N) {
        for (int[] ints : count) {
            double[] dirichletSample = estimateDirichletParameters(ints, N);
            int[] multinomialSample = estimateMultinomialProbabilities(N, dirichletSample);
            int[] mle = estimateAlphaMLE(multinomialSample, ints, N);
            System.arraycopy(mle, 0, ints, 0, mle.length);
        }
        computeProbabilities();
    }


    /**
     * Visualizes the original and Dirichlet-Multinomial-Distributed pattern[][]
     *
     * @param name Displayed name
     */
    public void visualizeDirichletMultinomialDistribution(String name) {
        EventQueue.invokeLater(() -> {
            DirichletMultinomialDistributionVisualizer ex = new DirichletMultinomialDistributionVisualizer(this, UserInterface.patternVariance);
            ex.setVisible(true);
        });
    }
}
