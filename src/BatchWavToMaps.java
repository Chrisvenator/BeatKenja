import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;

public class BatchWavToMaps {
    public static void main(String[] args) {
        generateOnsets("./OnsetGeneration/mp3Files", "./OnsetGeneration/output");
    }

    /**
     * Generates Beat Saber maps based on .wav files located in the specified input path. The generated maps will be located in the specified output path.
     *
     * @param inputPath Path to where the .wav files are located.
     * @param out       Path to where the outputted maps will be located. This can be the WIP folder.
     */
    public static void generateOnsets(String inputPath, String out) {
        // Save the original System.out to restore it later
        PrintStream originalOut = System.out;

        // Create a PrintStream with a NullOutputStream to discard the output
        PrintStream printStream = new PrintStream(new NullOutputStream());

        // Print initial message
        System.out.println("Checking if there are some illegal file names...");

        // Rename files with illegal characters in their names
        renameAllIllegalFileNames(inputPath);

        // Create a File object representing the input path
        File folder = new File(inputPath);

        // Get the list of files in the input path
        File[] files = folder.listFiles();

        // Print separator line
        System.out.println();

        // Print message for creating maps
        System.out.println("Creating maps...");


        if (files != null) {
            for (File file : files) {
                if (file.isFile() && file.getName().contains(".mp3")) {
                    executeConvertSongsPY(file, "./OnsetGeneration/mp3Files/", "wav");
                }
            }
        }

        //Update files.
        files = folder.listFiles();


        if (files != null) {
            for (File file : files) {
                // Check if the current item is a file and has the ".wav" extension
                if (file.isFile() && (file.getName().contains(".wav"))) {
                    String filename = file.getName().replaceAll(".wav", "");
                    String destinationFolderPath = out + "/" + filename;

                    // Disable prints while generating the map to avoid console spam
//                    System.setOut(printStream);

                    try {
                        // Create the output folder and move the file to it
                        createFolderAndMoveItems(filename, file, destinationFolderPath);

                        // Execute the Python script to generate timings
                        executePythonScript(filename, file, inputPath, destinationFolderPath);

                        // Create the diff file from the timings
                        String timings = createDiffFromTimings(destinationFolderPath, filename);
                    } catch (IOException | InterruptedException e) {
                        e.printStackTrace();
                    }

                    // Enable prints after the generation
                    System.setOut(originalOut);

                    // Print success message for the created map
                    System.out.println("Created Beat Saber Map: " + file.getName());
                }
            }
        }
    }


    /**
     * Renames all files in the specified input path, removing illegal characters and Japanese Kanji, ensuring file names comply with the naming rules.
     * If this step isn't done then the python script will throw an error
     *
     * @param inputPath The path where all the .wav files are located.
     */
    private static void renameAllIllegalFileNames(String inputPath) {
        // Create a File object representing the input path
        File folder = new File(inputPath);

        // Get the list of files in the folder
        File[] files = folder.listFiles();

        // Check if the list of files is not null
        if (files != null) {
            // Iterate through each file in the folder
            for (File file : files) {
                // Check if the current item is a file
                if (file.isFile()) {
                    // Get the current file name
                    String fileName = file.getName();
                    // Sanitize the file name by removing the ".wav" extension and replacing illegal characters with empty strings
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

                    // Check if the file name needs to be changed
                    if (!fileName.equals("") && !fileName.equals(sanitizedFileName)) {
                        // Create the new file path by appending the sanitized file name to the parent folder path
                        String newFilePath = file.getParent() + File.separator + sanitizedFileName;
                        File newFile = new File(newFilePath);

                        // Attempt to rename the file
                        if (file.renameTo(newFile)) {
                            System.out.println("File renamed successfully: " + fileName + " -> " + sanitizedFileName);
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
    private static void createFolderAndMoveItems(String filename, File file, String destinationFolderPath) throws IOException {
        // Create a File object representing the destination folder
        File outFolder = new File(destinationFolderPath);

        // Check if the destination folder exists
        if (!outFolder.exists()) {
            // If it doesn't exist, attempt to create the folder
            if (!outFolder.mkdir()) {
                System.out.println("Failed to create parent folder: " + outFolder.getAbsolutePath());
            }
        }

        // Create a FileWriter to write the contents of the info.dat file
        FileWriter writer = new FileWriter(destinationFolderPath + "/info.dat");
        writer.write(createDatFile(filename));
        writer.close();

        // Get the source file path and destination folder path as Path objects
        Path sourceFile = Path.of(file.getAbsolutePath());
        Path destinationFolder = Path.of(destinationFolderPath);

        // Resolve the destination file path by appending the source file name to the destination folder path
        Path destinationFile = destinationFolder.resolve(sourceFile.getFileName());

        // Copy the source file to the destination file, replacing it if it already exists
        Files.copy(sourceFile, destinationFile, StandardCopyOption.REPLACE_EXISTING);
    }

    /**
     * This function executes the python script ConvertSong.
     * It converts a mp3 to wav
     * !! OGG IS BROKEN !!
     *
     * @param file      File that should be converted
     * @param convertTo File extension. Supported: wav
     */
    private static void executeConvertSongsPY(File file, String filePath, String convertTo) {
        //Command to do it manually:
        //python ConvertSong.py mp3Files/input.mp3 output.wav wav

        try {
            ProcessBuilder processBuilder = new ProcessBuilder(
                    "python",
                    "./OnsetGeneration/ConvertSong.py",
                    filePath + file.getName(),
                    filePath + file.getName().replace(".mp3", "." + convertTo),
                    "wav");
            Process process = processBuilder.start();

            int exitCode = process.waitFor();
            if (exitCode == 0) {
                System.out.println("Converted " + file.getName() + " to " + convertTo + " format");
            } else {
                System.out.println("Python script execution failed with exit code: " + exitCode);

                // Capture and print the error output of the script
                InputStream errorStream = process.getErrorStream();
                BufferedReader errorReader = new BufferedReader(new InputStreamReader(errorStream));
                String line;
                while ((line = errorReader.readLine()) != null) {
                    System.out.println(line);
                }
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }


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
    private static void executePythonScript(String filename, File file, String inputPath, String destinationFolderPath) throws IOException, InterruptedException {
        // Create a ProcessBuilder to execute the Python script
        ProcessBuilder processBuilder = new ProcessBuilder("python", "./OnsetGeneration/SongToOnsets.py", inputPath + "/" + file.getName(), "--output", destinationFolderPath + "/" + filename + ".txt");
        Process process = processBuilder.start();

        // Wait for the process to finish and retrieve the exit code
        int exitCode = process.waitFor();

        // Check if the process exited with an error
        if (exitCode != 0) {
            System.out.println("Fehler beim Ausführen des Skripts. Exit-Code: " + exitCode);
            // Capture and print the error output of the script
            InputStream errorStream = process.getErrorStream();
            BufferedReader errorReader = new BufferedReader(new InputStreamReader(errorStream));
            String line;
            while ((line = errorReader.readLine()) != null) {
                System.out.println(line);
            }
        }
    }

    /**
     * This function creates the timings for a song.
     *
     * @param destinationFolderPath The destination folder where everything will be saved. It does not have to exist.
     * @param filename              The name of the current file.
     * @return The timings generated for the song.
     */
    private static String createDiffFromTimings(String destinationFolderPath, String filename) {
        // Create timings for the song based on parameters and save it to timingsFromSong
        String timingsFromSong = FileManager.makeMap(120, destinationFolderPath + "/" + filename + ".txt", (double) 1 / 32);

        // Overwrite the timings file for the ExpertPlusNoArrows.dat with the timings generated for the song
        FileManager.overwriteFile(destinationFolderPath + "/" + "ExpertPlusNoArrows.dat", timingsFromSong);

        // Return the timings generated for the song
        return timingsFromSong;
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
