package AppLogic;

import AudioAnalysis.Mp3ToWavConverter;
import DataManager.CreateAllNecessaryDIRsAndFiles;
import MapGeneration.BatchWavToMaps;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiConsumer;

import static DataManager.Parameters.ONSET_GENERATION_FOLDER_PATH_INPUT;
import static DataManager.Parameters.ONSET_GENERATION_FOLDER_PATH_OUTPUT;
import static DataManager.Parameters.logger;

/**
 * Batch "MP3 → timing maps" conversion, UI-free.
 * (Formerly the Swing GlobalConvertMP3ToMaps button.)
 *
 * Converts every mp3 in the onset input folder to wav (skipping already converted ones),
 * then runs the onset generation over all wav files, writing one timing map per song to
 * the output folder. Progress is reported per file through a callback.
 */
public class OnsetGenerationService {

    /** Files in the onset input folder that can take part in the conversion (.mp3/.wav). */
    public static List<File> listConvertibleFiles() {
        File[] files = new File(ONSET_GENERATION_FOLDER_PATH_INPUT).listFiles();
        if (files == null) return List.of();
        return Arrays.stream(files)
                .filter(f -> f.isFile() && (f.getName().endsWith(".mp3") || f.getName().endsWith(".wav")))
                .toList();
    }

    /**
     * Runs the full conversion. Synchronous — UI callers should run it in a background
     * task and marshal the progress callback to their UI thread.
     *
     * @param progress called with (file name, status) per processed file and with
     *                 ("", message) for global phases
     * @throws IllegalStateException when a precondition fails (ffmpeg missing, no files, …)
     */
    public static void convertMp3FolderToTimingMaps(BiConsumer<String, String> progress) {
        if (!CreateAllNecessaryDIRsAndFiles.isFFMpegInstalled()) {
            throw new IllegalStateException("FFMpeg is not installed. Please install it and try again!");
        }

        File inputFolder = new File(ONSET_GENERATION_FOLDER_PATH_INPUT);
        File[] allFiles = inputFolder.listFiles();
        if (allFiles == null) throw new IllegalStateException("Input folder not found: " + ONSET_GENERATION_FOLDER_PATH_INPUT);

        logger.info("Converting all Songs from \"{}\" to timing maps... This might take a while if there are a lot of songs.", ONSET_GENERATION_FOLDER_PATH_INPUT);

        // Convert mp3s to wav; already converted songs are skipped
        for (File f : allFiles) {
            if (!f.getName().endsWith(".mp3")) continue;

            File wavFile = new File(f.getAbsolutePath().replace(".mp3", ".wav"));
            if (wavFile.exists()) {
                logger.info("Skipping conversion for: {} as the .wav file already exists.", f.getName());
                progress.accept(f.getName(), "skipped — wav exists");
                continue;
            }
            try {
                progress.accept(f.getName(), "converting to wav…");
                Mp3ToWavConverter.convert(f.getAbsolutePath(), wavFile.getAbsolutePath());
                logger.info("mp3 to wav conversion completed successfully for: {}", f.getName());
                progress.accept(f.getName(), "converted");
            } catch (IOException e) {
                logger.error("Error while converting mp3 to wav: {}", f.getName(), e);
                progress.accept(f.getName(), "failed: " + e.getMessage());
            }
        }

        File[] refreshed = inputFolder.listFiles();
        if (refreshed == null) throw new IllegalStateException("Input folder not found: " + ONSET_GENERATION_FOLDER_PATH_INPUT);

        boolean hasMp3 = Arrays.stream(refreshed).anyMatch(f -> f.isFile() && f.getName().endsWith(".mp3"));
        List<File> wavFiles = Arrays.stream(refreshed).filter(f -> f.isFile() && f.getName().endsWith(".wav")).toList();
        if (wavFiles.isEmpty()) {
            throw new IllegalStateException(hasMp3
                    ? "No .wav files were produced from the input .mp3 files (conversion failed?). Check the log."
                    : "Found no .mp3 or .wav files in: " + ONSET_GENERATION_FOLDER_PATH_INPUT);
        }

        progress.accept("", "Generating onsets… (this can take a while)");
        if (BatchWavToMaps.generateOnsets(ONSET_GENERATION_FOLDER_PATH_INPUT, ONSET_GENERATION_FOLDER_PATH_OUTPUT, true, null)) {
            logger.info("Successfully created Map. You can find your map in \"{}/\"", ONSET_GENERATION_FOLDER_PATH_OUTPUT);
            progress.accept("", "✓ Done — maps are in " + ONSET_GENERATION_FOLDER_PATH_OUTPUT);
        } else {
            // A failed run usually means missing python dependencies — try installing them once
            logger.error("There was an error while creating the onsets. It is possible that a dependency is not installed.");
            progress.accept("", "Onset generation failed — trying to install dependencies…");
            if (CreateAllNecessaryDIRsAndFiles.installDependencies()) {
                throw new IllegalStateException("Dependencies were just installed — please run the conversion again.");
            }
            throw new IllegalStateException("Onset generation failed and dependencies could not be installed. Check the log.");
        }
    }
}
