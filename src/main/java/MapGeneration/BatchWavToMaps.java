package MapGeneration;

import AudioAnalysis.AudioAnalysis;
import AudioAnalysis.BPMDetector;
import AudioAnalysis.TimingOffsetDetector;
import AudioAnalysis.SpectrogramCalculator;
import AudioAnalysis.SpectrogramDisplay;
import BeatSaberObjects.Objects.BeatSaberMap;
import BeatSaberObjects.Objects.Note;
import DataManager.FileManager;
import DataManager.Parameters;
import MapGeneration.PatternGeneration.CommonMethods.NpsBpmConverter;
import lombok.Cleanup;

import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static DataManager.FileManager.createZipPackage;
import static DataManager.Parameters.*;

/**
 * This class is used to generate Beat Saber maps from .wav files. It is used to generate maps in bulk.
 */
public class BatchWavToMaps {
    
    /**
     * Generates Beat Saber maps based on .wav files located in the specified input path. The generated maps will be located in the specified output path.
     *
     * @param inputPath Path to where the .wav files are located.
     * @param out       Path to where the outputted maps will be located. This can be the WIP folder.
     */
    public static boolean generateOnsets(String inputPath, String out, boolean verbose, String pythonScript) {
        if (pythonScript == null) pythonScript = "SongToOnsets.py";
        logger.info("Checking if there are some illegal file names...");
        System.out.println("Checking if there are some illegal file names...");
        
        renameAllIllegalFileNames(inputPath, verbose);
        
        File folder = new File(inputPath);
        File[] files = folder.listFiles();
        logger.info("Creating maps...");
        System.out.println();
        System.out.println("Creating maps...");
        
        
        if (files != null) {
            for (File file : files) {
                if (file.isFile() && file.getName().contains(".mp3") &&
                        !new File(file.getName().replace("mp3", "wav")).exists()) { //Check if the song has already been converted
                    //If there is an error generating the map, then dependencies are probably missing
                    if (!executeConvertSongsPY(file, verbose)) return false;
                }
            }
        }
        files = folder.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isFile() && (file.getName().contains(".wav"))) {
                    String filename = file.getName().replaceAll(".wav", "");
                    String destinationFolderPath = out + "/[BeatKenja]_" + filename;
                    
                    // Disable prints while generating the map to avoid console spam
                    try {
                        createFolderAndMoveItems(filename, file, destinationFolderPath, verbose);
                        
                        //Try to execute the python script. If unsuccessful, try installing all dependencies
//                        if (!executePythonScript(filename, file, inputPath, destinationFolderPath, pythonScript)) return false;
//                        List<String> peaks = FileManager.readFile(destinationFolderPath + "/" + filename + ".txt");
                        
                        double bpm;
                        if (extractBpm(filename) == null) {
                            bpm = BPMDetector.detectBPM(file.getAbsolutePath());
                        } else {
                            bpm = extractBpm(filename);
                        }
                        
                        Double offset = TimingOffsetDetector.detectTimingOffset(file.getAbsolutePath(), bpm);
                        
                        logger.info("Detected BPM: {}", bpm);
                        logger.info("Detected offset: {}", offset);
                        System.out.println("Detected BPM: " + bpm);
                        System.out.println("Detected offset: " + offset);
                        
                        ArrayList<ArrayList<Double>> peaks = AudioAnalysis.getPeaksFromAudio(file.getAbsolutePath(), bpm, offset);
                        String[] difficulties = {"EasyNoArrows", "NormalNoArrows", "HardNoArrows", "ExpertNoArrows", "ExpertPlusNoArrows"};
                        
                        int i = 0;
                        for (ArrayList<Double> timingsDiff : peaks) {
                            if (i == 5) throw new IllegalArgumentException("Too many difficulties. Please adjust the difficulties array in the code.");
                            createDiffFromTimings(destinationFolderPath, difficulties[i], timingsDiff);
                            
                            
                            if (peaks.get(i).isEmpty()) {
                                logger.error("No peaks found for difficulty {} in the audio file. Please adjust the thresholds in the code.", i);
                                System.err.println("No peaks found for difficulty " + i + " in the audio file. Please adjust the thresholds in the code.");
                                i++;
                                continue;
                            }
                            double duration = peaks.get(i).getLast(); // Assuming the last peak time gives approximate duration
                            double[][] spectrogram = SpectrogramCalculator.calculateSpectrogram(file.getAbsolutePath(), 1024, 512);
                            
                            if (SHOW_SPECTOGRAM_WHEN_GENERATING_ONSETS) {
                                final int finalI = i;
                                SwingUtilities.invokeLater(() -> {
                                    SpectrogramDisplay frame = new SpectrogramDisplay(spectrogram, peaks.get(finalI), duration, difficulties[finalI], true); // Example uses the first difficulty level
                                    frame.setVisible(true);
                                });
                            }
                            i++;
                        }
                        convertWavToOgg(file.getAbsolutePath(), destinationFolderPath + "/" + filename + ".ogg");
                        createZipPackage(destinationFolderPath, filename, verbose);
                    } catch (IOException e) {
                        logger.error("IO Exception: {}", e.getMessage());
                        e.printStackTrace();
                    } catch (UnsupportedAudioFileException e) {
                        logger.error("Error while generating the map: {}\n{}", file.getName(), e.getMessage());
                        System.err.println("Error while generating the map: " + file.getName() + "\n" + e.getMessage());
                    }
                    
                    // Enable prints after the generation
                    logger.info("Created Beat Saber Map: {}", file.getName());
                    System.out.println("Created Beat Saber Map: " + file.getName());
                }
            }
        }
        return true;
    }
    
    private static void convertWavToOgg(String inputFilePath, String outputFilePath) throws IOException {// Path to the input .wav file
        ProcessBuilder processBuilder = new ProcessBuilder(
                "ffmpeg", "-i", inputFilePath, outputFilePath
        );
        
        logger.info("Converting {} to {}", new File(inputFilePath).getName(), outputFilePath.substring(outputFilePath.lastIndexOf("/")));
        System.out.println("Converting " + new File(inputFilePath).getName() + " to " + outputFilePath.substring(outputFilePath.lastIndexOf("/")));
        processBuilder.redirectErrorStream(true);
        Process process = processBuilder.start();
        int exitCode;
        try {
            exitCode = process.waitFor();
            logger.info("Converted {} to {}", new File(inputFilePath).getName(), new File(outputFilePath).getName());
            System.out.println("Converted " + new File(inputFilePath).getName() + " to " + new File(outputFilePath).getName());
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        
        if (exitCode != 0) {
            throw new RuntimeException("FFmpeg conversion failed with exit code: " + exitCode);
        }
        
        // Delete the original .wav file
        File inputFile = new File(inputFilePath);
        if (Parameters.DELETE_WAV_AFTER_CONVERSION) {
            logger.info("Attempting to delete the original .wav file: {}", inputFilePath);
            System.out.println("Attempting to delete the original .wav file: " + inputFilePath);
            if (inputFile.delete()) {
                logger.info("Deleted the original .wav file: {}", inputFilePath);
                System.out.println("Deleted the original .wav file: " + inputFilePath);
            } else {
                logger.info("Failed to delete the original .wav file: {}", inputFilePath);
                System.out.println("Failed to delete the original .wav file: " + inputFilePath);
            }
        }
    }
    
    /**
     * Renames all files in the specified input path, removing illegal characters and Japanese Kanji, ensuring file names comply with the naming rules.
     * If this step isn't done, then the python script will throw an error
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
                    
                    //Converting the filename to CamelCase:
                    for (int i = 0; i < fileName.length() - 2; i++)
                        if (fileName.charAt(i) == ' ') fileName = fileName.substring(0, i) + (String.valueOf(fileName.charAt(i + 1))).toUpperCase() + fileName.substring(i + 2);
                    
                    String sanitizedFileName = sanitizeFilename(file, fileName);
                    
                    if (!fileName.isEmpty() && !fileName.equals(sanitizedFileName)) {
                        String newFilePath = file.getParent() + File.separator + sanitizedFileName;
                        File newFile = new File(newFilePath);
                        
                        if (file.renameTo(newFile)) {
                            if (verbose)
                                logger.info("File renamed successfully: {} -> {}", fileName, sanitizedFileName);
                        } else {
                            logger.info("Failed to rename the file: {}", fileName);
                            System.out.println("Failed to rename the file: " + fileName);
                        }
                    }
                }
            }
        }
    }
    
    /**
     * Renames the file to remove illegal characters and add the correct file extension.<br>
     * Helper Method for renameAllIllegalFileNames
     *
     * @param file     The File object representing the file to be renamed.
     * @param fileName The name of the current file.
     * @return The sanitized file name.
     */
    private static String sanitizeFilename(File file, String fileName) {
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
        if (sanitizedFileName.isEmpty()) sanitizedFileName = "UNDEFINED";
        return sanitizedFileName;
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
            if (!outFolder.mkdirs()) {
                if (verbose)
                    logger.info("Failed to create parent folder: {}", outFolder.getAbsolutePath());
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
     * It converts mp3 to wav
     * !! OGG IS BROKEN !!
     *
     * @param file File that should be converted
     */
    @SuppressWarnings("GrazieInspection")
    private static boolean executeConvertSongsPY(File file, boolean verbose) {
        //Command to do it manually:
        //python ConvertSong.py mp3Files/input.mp3 output.wav wav
        
        try {
            ProcessBuilder processBuilder = new ProcessBuilder(
                    "python",
                    DEFAULT_ONSET_GENERATION_FOLDER + "ConvertSong.py",
                    ONSET_GENERATION_FOLDER_PATH_INPUT + file.getName(),
                    ONSET_GENERATION_FOLDER_PATH_INPUT + file.getName().replace(".mp3", "." + "wav"),
                    "wav");
            Process process = processBuilder.start();
            
            int exitCode = process.waitFor();
            if (exitCode == 0) {
                if (verbose)
                    logger.info("Converted {} to wav format", file.getName());
            } else {
                logger.info("Python script execution failed with exit code: {}", exitCode);
                System.out.println("Python script execution failed with exit code: " + exitCode);
                
                // Capture and print the error output of the script
                @Cleanup InputStream errorStream = process.getErrorStream();
                @Cleanup BufferedReader errorReader = new BufferedReader(new InputStreamReader(errorStream));
                String line;
                while ((line = errorReader.readLine()) != null) {
                    logger.info(line);
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
     * This function executes a Python script to create timings from a .wav file. <br>
     * If the option madmom is being used, then the madmom_certainty and madmom_proximity flags can be set in the Parameters class.
     *
     * @param inputPath             The path where the input file is located.
     * @param filename              The name of the current file.
     * @param file                  The File object representing the input file.
     * @param destinationFolderPath The destination folder where everything will be saved. It does not have to exist.
     * @param pythonScript          The name of the Python script that will be executed. It must be located in the DEFAULT_ONSET_GENERATION_FOLDER.
     * @throws IOException          If there is an issue with input/output operations or missing folders.
     * @throws InterruptedException If the execution of the Python script is interrupted.
     */
    @Deprecated
    private static boolean executePythonScript(String filename, File file, String inputPath, String destinationFolderPath, String pythonScript) throws IOException, InterruptedException {
        File pythonScriptFile = new File(DEFAULT_ONSET_GENERATION_FOLDER + pythonScript);
        File songFIle = new File(inputPath + "/" + file.getName());
        ProcessBuilder processBuilder = new ProcessBuilder("python",
                pythonScriptFile.getAbsolutePath(), songFIle.getAbsolutePath(),
                "--output", destinationFolderPath + "/" + filename + ".txt",
                "--madmom_certainty", MADMOM_ONSET_GENERATION_ONSET_CERTAINTY + "",
                "--madmom_proximity", MADMOM_ONSET_GENERATION_MINIMUM_PROXIMITY + "");
        Process process = processBuilder.start();
        
        int exitCode = process.waitFor();
        
        if (exitCode != 0) {
            logger.info("Error while Executing the script. Exit-Code: {}", exitCode);
            System.out.println("Error while Executing the script. Exit-Code: " + exitCode);
            
            @Cleanup InputStream errorStream = process.getErrorStream();
            @Cleanup BufferedReader errorReader = new BufferedReader(new InputStreamReader(errorStream));
            String line;
            while ((line = errorReader.readLine()) != null) {
                logger.info(line);
            }
        } else {
            logger.info("Python script execution finished with exit code: {}", exitCode);
            System.out.println("Python script execution finished with exit code: " + exitCode);
        }
        return exitCode != -4;
    }
    
    /**
     * This function creates the timings for a song.
     *
     * @param destinationFolderPath The destination folder where everything will be saved. It does not have to exist.
     * @param difficultyName        The name of the current file.
     */
    private static void createDiffFromTimings(String destinationFolderPath, String difficultyName, ArrayList<Double> timings) {
        List<Note> notes = timings.stream().map(t -> new Note(t.floatValue())).collect(Collectors.toList());
        
        NpsBpmConverter.convertSecondsToBeats(notes);
        
        BeatSaberMap map = new BeatSaberMap(notes);
        if (FIX_PLACEMENTS) map.fixPlacements(PLACEMENT_PRECISION);
        
        FileManager.overwriteFile(destinationFolderPath + "/" + difficultyName + ".dat", map.exportAsMap());
    }
    
    
    /**
     * Outsources the info.dat file generation so that the code isn't cluttered
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
                "  \"_beatsPerMinute\" : " + BPM + ",\n" +
                "  \"_previewStartTime\" : 30,\n" +
                "  \"_previewDuration\" : 20,\n" +
                "  \"_songTimeOffset\" : 0,\n" +
                "  \"_shuffle\" : 0,\n" +
                "  \"_shufflePeriod\" : 0.5,\n" +
                "  \"_coverImageFilename\" : \"\",\n" +
                "  \"_songFilename\" : \"" + songName + ".ogg\",\n" +
                "  \"_environmentName\" : \"DefaultEnvironment\",\n" +
                "  \"_allDirectionsEnvironmentName\" : \"GlassDesertEnvironment\",\n" +
                "  \"_customData\" : {\n" +
                "  },\n" +
                "\"_difficultyBeatmapSets\" : [\n" +
                "    {\n" +
                "      \"_beatmapCharacteristicName\" : \"NoArrows\",\n" +
                "      \"_difficultyBeatmaps\" : [\n" +
                "        {\n" +
                "          \"_difficulty\" : \"Easy\",\n" +
                "          \"_difficultyRank\" : 1,\n" +
                "          \"_beatmapFilename\" : \"EasyNoArrows.dat\",\n" +
                "          \"_noteJumpMovementSpeed\" : 16,\n" +
                "          \"_noteJumpStartBeatOffset\" : 0,\n" +
                "          \"_beatmapColorSchemeIdx\" : 0,\n" +
                "          \"_environmentNameIdx\" : 0\n" +
                "        },\n" +
                "        {\n" +
                "          \"_difficulty\" : \"Normal\",\n" +
                "          \"_difficultyRank\" : 3,\n" +
                "          \"_beatmapFilename\" : \"NormalNoArrows.dat\",\n" +
                "          \"_noteJumpMovementSpeed\" : 16,\n" +
                "          \"_noteJumpStartBeatOffset\" : 0,\n" +
                "          \"_beatmapColorSchemeIdx\" : 0,\n" +
                "          \"_environmentNameIdx\" : 0\n" +
                "        },\n" +
                "        {\n" +
                "          \"_difficulty\" : \"Hard\",\n" +
                "          \"_difficultyRank\" : 5,\n" +
                "          \"_beatmapFilename\" : \"HardNoArrows.dat\",\n" +
                "          \"_noteJumpMovementSpeed\" : 16,\n" +
                "          \"_noteJumpStartBeatOffset\" : 0,\n" +
                "          \"_beatmapColorSchemeIdx\" : 0,\n" +
                "          \"_environmentNameIdx\" : 0\n" +
                "        },\n" +
                "        {\n" +
                "          \"_difficulty\" : \"Expert\",\n" +
                "          \"_difficultyRank\" : 7,\n" +
                "          \"_beatmapFilename\" : \"ExpertNoArrows.dat\",\n" +
                "          \"_noteJumpMovementSpeed\" : 16,\n" +
                "          \"_noteJumpStartBeatOffset\" : 0,\n" +
                "          \"_beatmapColorSchemeIdx\" : 0,\n" +
                "          \"_environmentNameIdx\" : 0\n" +
                "        },\n" +
                "        {\n" +
                "          \"_difficulty\" : \"ExpertPlus\",\n" +
                "          \"_difficultyRank\" : 9,\n" +
                "          \"_beatmapFilename\" : \"ExpertPlusNoArrows.dat\",\n" +
                "          \"_noteJumpMovementSpeed\" : 16,\n" +
                "          \"_noteJumpStartBeatOffset\" : 0,\n" +
                "          \"_beatmapColorSchemeIdx\" : 0,\n" +
                "          \"_environmentNameIdx\" : 0,\n" +
                "          \"_customData\" : {\n" +
                "            \"_difficultyLabel\" : \"Timings\"\n" +
                "          }\n" +
                "        }\n" +
                "      ]\n" +
                "    }\n" +
                "  ]  " +
                "}";
    }
    
    public static Integer extractBpm(String input) {
        Pattern pattern = Pattern.compile("(\\d{2,4})bpm", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(input);
        
        if (matcher.find()) {
            return Integer.parseInt(matcher.group(1));
        }
        return -1; // no BPM found
    }
}
