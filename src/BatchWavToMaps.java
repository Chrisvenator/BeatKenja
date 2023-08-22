import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

public class BatchWavToMaps {
    /**
     * Generates Beat Saber maps based on .wav files located in the specified input path. The generated maps will be located in the specified output path.
     *
     * @param inputPath Path to where the .wav files are located.
     * @param out       Path to where the outputted maps will be located. This can be the WIP folder.
     */
    public static boolean generateOnsets(String inputPath, String out, boolean verbose) {
        PrintStream originalOut = System.out;
        PrintStream printStream = new PrintStream(new NullOutputStream());

        System.out.println("Checking if there are some illegal file names...");

        renameAllIllegalFileNames(inputPath, verbose);

        File folder = new File(inputPath);
        File[] files = folder.listFiles();
        System.out.println();
        System.out.println("Creating maps...");


        if (files != null) {
            for (File file : files) {
                if (file.isFile() && file.getName().contains(".mp3")) {
                    if (!executeConvertSongsPY(file, verbose)) return false;
                }
            }
        }

        files = folder.listFiles();


        if (files != null) {
            for (File file : files) {
                if (file.isFile() && (file.getName().contains(".wav"))) {
                    String filename = file.getName().replaceAll(".wav", "");
                    String destinationFolderPath = out + "/" + filename;

                    // Disable prints while generating the map to avoid console spam
                    if (verbose) System.setOut(printStream);

                    try {
                        createFolderAndMoveItems(filename, file, destinationFolderPath, verbose);

                        //Try to execute the python script. If unsuccessful, try installing all dependencies
                        if (!executePythonScript(filename, file, inputPath, destinationFolderPath)) return false;

                        createDiffFromTimings(destinationFolderPath, filename);
                    } catch (IOException | InterruptedException e) {
                        e.printStackTrace();
                    }

                    // Enable prints after the generation
                    System.setOut(originalOut);
                    System.out.println("Created Beat Saber Map: " + file.getName());
                }
            }
        }
        return true;
    }


    /**
     * Renames all files in the specified input path, removing illegal characters and Japanese Kanji, ensuring file names comply with the naming rules.
     * If this step isn't done then the python script will throw an error
     *
     * @param inputPath The path where all the .wav files are located.
     */
    private static void renameAllIllegalFileNames(String inputPath, boolean verbose) {
        File folder = new File(inputPath);
        // Get the list of files in the folder
        File[] files = folder.listFiles();

        if (files != null) {
            for (File file : files) {
                if (file.isFile()) {
                    String fileName = file.getName();
                    String sanitizedFileName = "";
                    if (file.getName().contains(".wav")) {
                        sanitizedFileName = fileName
                                .replaceAll(".wav", "")
                                .replaceAll("[^a-zA-Z0-9-_]", "")
                                + ".wav";
                    } else if (file.getName().contains(".mp3")) {
                        sanitizedFileName = fileName
                                .replaceAll(".mp3", "")
                                .replaceAll("[^a-zA-Z0-9-_]", "")
                                + ".mp3";
                    }
                    if (sanitizedFileName.equals("")) sanitizedFileName = "UNDEFINED";

                    if (!fileName.equals("") && !fileName.equals(sanitizedFileName)) {
                        String newFilePath = file.getParent() + File.separator + sanitizedFileName;
                        File newFile = new File(newFilePath);

                        if (file.renameTo(newFile)) {
                            if (verbose) System.out.println("File renamed successfully: " + fileName + " -> " + sanitizedFileName);
                        } else {
                            System.out.println("Failed to rename the file: " + fileName);
                        }
                    }
                }
            }
        }
    }

    /**
     * Creates the output folder and moves all renamed .wav files there. The files must have been renamed before calling this function.
     *
     * @param filename              The name of the current file.
     * @param file                  The File object representing the file to be moved.
     * @param destinationFolderPath The destination folder where everything will be saved. It does not have to exist.
     * @throws IOException If there is an issue with input/output operations or missing folders.
     */
    private static void createFolderAndMoveItems(String filename, File file, String destinationFolderPath, boolean verbose) throws IOException {
        File outFolder = new File(destinationFolderPath);

        if (!outFolder.exists()) {
            if (!outFolder.mkdir()) {
                if (verbose) System.out.println("Failed to create parent folder: " + outFolder.getAbsolutePath());
            }
        }

        FileWriter writer = new FileWriter(destinationFolderPath + "/info.dat");
        writer.write(createDatFile(filename));
        writer.close();

        Path sourceFile = Path.of(file.getAbsolutePath());
        Path destinationFolder = Path.of(destinationFolderPath);
        Path destinationFile = destinationFolder.resolve(sourceFile.getFileName());

        Files.copy(sourceFile, destinationFile, StandardCopyOption.REPLACE_EXISTING);
    }

    /**
     * This function executes the python script ConvertSong.
     * It converts a mp3 to wav
     * !! OGG IS BROKEN !!
     *
     * @param file File that should be converted
     */
    private static boolean executeConvertSongsPY(File file, boolean verbose) {
        //Command to do it manually:
        //python ConvertSong.py mp3Files/input.mp3 output.wav wav

        try {
            ProcessBuilder processBuilder = new ProcessBuilder(
                    "python",
                    UserInterface.DEFAULT_ONSET_GENERATION_FOLDER + "ConvertSong.py",
                    UserInterface.ONSET_GENERATION_FOLDER_PATH_INPUT + file.getName(),
                    UserInterface.ONSET_GENERATION_FOLDER_PATH_INPUT + file.getName().replace(".mp3", "." + "wav"),
                    "wav");
            Process process = processBuilder.start();

            int exitCode = process.waitFor();
            if (exitCode == 0) {
                if (verbose) System.out.println("Converted " + file.getName() + " to " + "wav" + " format");
            } else {
                System.out.println("Python script execution failed with exit code: " + exitCode);

                // Capture and print the error output of the script
                InputStream errorStream = process.getErrorStream();
                BufferedReader errorReader = new BufferedReader(new InputStreamReader(errorStream));
                String line;
                while ((line = errorReader.readLine()) != null) {
                    System.out.println(line);
                }
                return exitCode != -4;
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

        return true;
    }

    /**
     * This function executes a Python script to create timings from a .wav file.
     *
     * @param inputPath             The path where the input file is located.
     * @param filename              The name of the current file.
     * @param file                  The File object representing the input file.
     * @param destinationFolderPath The destination folder where everything will be saved. It does not have to exist.
     * @throws IOException          If there is an issue with input/output operations or missing folders.
     * @throws InterruptedException If the execution of the Python script is interrupted.
     */
    private static boolean executePythonScript(String filename, File file, String inputPath, String destinationFolderPath) throws IOException, InterruptedException {
        ProcessBuilder processBuilder = new ProcessBuilder("python", UserInterface.DEFAULT_ONSET_GENERATION_FOLDER + "SongToOnsets.py", inputPath + "/" + file.getName(), "--output", destinationFolderPath + "/" + filename + ".txt");
        Process process = processBuilder.start();

        int exitCode = process.waitFor();
        System.out.println("Python script execution finished with exit code: " + exitCode);

        if (exitCode != 0) {
            System.out.println("Fehler beim Ausführen des Skripts. Exit-Code: " + exitCode);

            InputStream errorStream = process.getErrorStream();
            BufferedReader errorReader = new BufferedReader(new InputStreamReader(errorStream));
            String line;
            while ((line = errorReader.readLine()) != null) {
                System.out.println(line);
            }
        }
        return exitCode != -4;
    }

    /**
     * This function creates the timings for a song.
     *
     * @param destinationFolderPath The destination folder where everything will be saved. It does not have to exist.
     * @param filename              The name of the current file.
     */
    private static void createDiffFromTimings(String destinationFolderPath, String filename) {
        List<String> timings = FileManager.readFile(destinationFolderPath + "/" + filename + ".txt");
        List<Note> notes = new ArrayList<>();

        for (String s : timings) {

            float t = Float.parseFloat(s);
            double beat = t * UserInterface.BPM / 60;
            System.out.println(beat);

            notes.add(new Note((float) beat));
        }

        BeatSaberMap map = new BeatSaberMap(notes);
        if (UserInterface.FIX_PLACEMENTS) map.fixPlacements(UserInterface.PLACEMENT_PRECISION);

        FileManager.overwriteFile(destinationFolderPath + "/" + "ExpertPlusNoArrows.dat", map.exportAsMap());
    }


    /**
     * Outsources the info.dat file generation so that the code isn't clustered
     *
     * @param songName Name of the song lol
     * @return the complete info.dat File
     */
    private static String createDatFile(String songName) {
        return "{\n" +
                "  \"_version\" : \"2.0.0\",\n" +
                "  \"_songName\" : \"" + songName + "\",\n" +
                "  \"_songSubName\" : \"\",\n" +
                "  \"_songAuthorName\" : \"BeatKenja\",\n" +
                "  \"_levelAuthorName\" : \"BeatKenja\",\n" +
                "  \"_beatsPerMinute\" : 120,\n" +
                "  \"_previewStartTime\" : 30,\n" +
                "  \"_previewDuration\" : 20,\n" +
                "  \"_songTimeOffset\" : 0,\n" +
                "  \"_shuffle\" : 0,\n" +
                "  \"_shufflePeriod\" : 0.5,\n" +
                "  \"_coverImageFilename\" : \"\",\n" +
                "  \"_songFilename\" : \"" + songName + ".wav\",\n" +
                "  \"_environmentName\" : \"DefaultEnvironment\",\n" +
                "  \"_allDirectionsEnvironmentName\" : \"GlassDesertEnvironment\",\n" +
                "  \"_customData\" : {\n" +
                "  },\n" +
                "  \"_difficultyBeatmapSets\" : [\n" +
                "    {\n" +
                "      \"_beatmapCharacteristicName\" : \"NoArrows\",\n" +
                "      \"_difficultyBeatmaps\" : [\n" +
                "        {\n" +
                "          \"_difficulty\" : \"ExpertPlus\",\n" +
                "          \"_difficultyRank\" : 9,\n" +
                "          \"_beatmapFilename\" : \"ExpertPlusNoArrows.dat\",\n" +
                "          \"_noteJumpMovementSpeed\" : 16,\n" +
                "          \"_noteJumpStartBeatOffset\" : 0,\n" +
                "          \"_customData\" : {\n" +
                "            \"_difficultyLabel\" : \"Timings\"\n" +
                "          }\n" +
                "        }\n" +
                "      ]\n" +
                "    }\n" +
                "  ]\n" +
                "}";
    }

    /**
     * // This code defines a static class called NullOutputStream that extends the java.io.OutputStream class.
     * // The purpose of this class is to provide a stream that discards any output written to it.
     * <p>
     * // The class overrides the write(int b) method from the OutputStream class.
     * // The method takes an integer argument representing a byte of data to be written.
     * <p>
     * // Inside the overridden write() method, there is a comment indicating that the method does nothing.
     * // This means that when the write() method is called, it effectively discards the output without performing any action.
     * <p>
     * // This NullOutputStream class can be useful in situations where there is a need to suppress or ignore output.
     * // For example, it can be used to prevent certain data from being written to a stream or to suppress unnecessary output during testing or debugging.
     * required so that the console is not spammed by unnecessary things
     */
    static class NullOutputStream extends java.io.OutputStream {
        @Override
        public void write(int b) {
            // Do nothing, effectively discarding the output
        }
    }
}
