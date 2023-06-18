import java.io.*;
import java.lang.reflect.Array;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

public class BatchWavToMaps {
    public static void main(String[] args) {
        generateOnsets("./OnsetGeneration/wavFiles", "./OnsetGeneration/output");

    }

    /**
     * @param inputPath Path to where the .wav files are located
     * @param out       Path to where the outputted maps will be located. This can be the WIP folder
     */
    public static void generateOnsets(String inputPath, String out) {
        PrintStream originalOut = System.out;
        PrintStream printStream = new PrintStream(new NullOutputStream());

        System.out.println("Checking if there are some illegal file names...");
        renameAllIllegalFileNames(inputPath);

        File folder = new File(inputPath);
        File[] files = folder.listFiles();

        System.out.println();
        System.out.println("Creating maps... ");
        if (files != null) {
            for (File file : files) {
                if (file.isFile() && file.getName().contains(".wav")) {
                    String filename = file.getName().replaceAll(".wav", "");
                    String destinationFolderPath = out + "/" + filename;

                    // Disable prints while generating, so that the console isn't spammed with bulls**t
                    System.setOut(printStream);

                    try {
                        createFolderAndMoveItems(filename, file, destinationFolderPath);
                        executePythonScript(filename, file, inputPath, destinationFolderPath);
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
    }

    /**
     * rename all files so that there are no illegal characters or japanese Kanji etc.
     *
     * @param inputPath Path where all the .wav files are located
     */
    private static void renameAllIllegalFileNames(String inputPath) {
        File folder = new File(inputPath);

        File[] files = folder.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isFile()) {
                    String fileName = file.getName();
                    String sanitizedFileName = fileName
                            .replaceAll(".wav", "")
                            .replaceAll("[^a-zA-Z0-9-_]", "")
                            + ".wav";

                    if (!fileName.equals(sanitizedFileName)) {
                        String newFilePath = file.getParent() + File.separator + sanitizedFileName;
                        File newFile = new File(newFilePath);

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
     * creates the output folder and moves all renamed .wav files there. The Files MUST have been renamed before!
     *
     * @param filename              Name of the current file
     * @param file                  the File itself
     * @param destinationFolderPath Destination Folder where everything is saved. It doesn't have to exist!
     * @throws IOException If one of the folders is missing
     */
    private static void createFolderAndMoveItems(String filename, File file, String destinationFolderPath) throws IOException {
        File outFolder = new File(destinationFolderPath);
        if (!outFolder.exists())
            if (!outFolder.mkdir()) System.out.println("Failed to create parent folder: " + outFolder.getAbsolutePath());

        FileWriter writer;
        writer = new FileWriter(destinationFolderPath + "/info.dat");
        writer.write(createDatFile(filename));
        writer.close();


        Path sourceFile = Path.of(file.getAbsolutePath());
        Path destinationFolder = Path.of(destinationFolderPath);

        Path destinationFile = destinationFolder.resolve(sourceFile.getFileName());
        Files.copy(sourceFile, destinationFile, StandardCopyOption.REPLACE_EXISTING);
    }

    /**
     * @param inputPath             path where the file is located
     * @param filename              Name of the current file
     * @param file                  the File itself
     * @param destinationFolderPath Destination Folder where everything is saved. It doesn't have to exist!
     * @throws IOException          If one of the folders is missing
     * @throws InterruptedException No idea what this is lol
     */
    private static void executePythonScript(String filename, File file, String inputPath, String destinationFolderPath) throws IOException, InterruptedException {
        ProcessBuilder processBuilder = new ProcessBuilder("python", "./OnsetGeneration/SongToOnsets.py", inputPath + "/" + file.getName(), "--output", destinationFolderPath + "/" + filename + ".txt");
        Process process = processBuilder.start();

        int exitCode = process.waitFor();
        if (exitCode != 0) {
            System.out.println("Fehler beim Ausführen des Skripts. Exit-Code: " + exitCode);

            // Erfasse die Fehlerausgabe des Skripts
            InputStream errorStream = process.getErrorStream();
            BufferedReader errorReader = new BufferedReader(new InputStreamReader(errorStream));
            String line;
            while ((line = errorReader.readLine()) != null) {
                System.out.println(line);
            }
        }
    }

    /**
     * Ths function creates the timings
     *
     * @param destinationFolderPath Destination Folder where everything is saved. It doesn't have to exist!
     * @param filename              Name of the current file
     */
    private static void createDiffFromTimings(String destinationFolderPath, String filename) {
        String timingsFromSong = CreateTimings.makeMap(120, destinationFolderPath + "/" + filename + ".txt", (double) 1 / 16);
        CreateTimings.overwriteFile(destinationFolderPath + "/" + "ExpertPlusNoArrows.dat", timingsFromSong);
    }

    /**
     * Outsources the info.dat file generation
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
     * required so that the console is not spammed by unnecessary things
     */
    static class NullOutputStream extends java.io.OutputStream {
        @Override
        public void write(int b) {
            // Do nothing, effectively discarding the output
        }
    }
}
