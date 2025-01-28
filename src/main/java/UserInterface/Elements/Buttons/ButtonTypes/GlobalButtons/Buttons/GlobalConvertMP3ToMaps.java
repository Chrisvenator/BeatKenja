package UserInterface.Elements.Buttons.ButtonTypes.GlobalButtons.Buttons;

import AudioAnalysis.Mp3ToWavConverter;
import DataManager.CreateAllNecessaryDIRsAndFiles;
import MapGeneration.BatchWavToMaps;
import UserInterface.Elements.Buttons.ButtonTypes.GlobalButtons.Exceptions.ConvertMP3Exception;
import UserInterface.Elements.Buttons.ButtonTypes.GlobalButtons.GlobalButton;
import UserInterface.Elements.ElementTypes;
import UserInterface.UserInterface;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static DataManager.Parameters.*;

public class GlobalConvertMP3ToMaps extends GlobalButton {
    public GlobalConvertMP3ToMaps(UserInterface ui) {
        super(ElementTypes.GLOBAL_CONVERT_MP3s, ui);
        setBackground(Color.orange);
        logger.info("GlobalConvertMP3ToMaps button initialized.");
    }
    
    @Override
    public void onClick() {
        if (!CreateAllNecessaryDIRsAndFiles.isFFMpegInstalled()) {
            logger.error("FFMpeg is not installed. Please install it and try again!");
            return;
        }
        
        if (!CreateAllNecessaryDIRsAndFiles.isPythonInstalled()) {
            logger.error("Python could not be found. Please ensure that it is installed and added to the PATH-System-Variable!");
            return;
        }
        
        try {
            ui.statusCheck.setBackground(Color.gray);
            this.setText("In Progress...");
            logger.info("Converting all Songs from \"{}\" to timing maps... This might take a while if there are a lot of songs.", ONSET_GENERATION_FOLDER_PATH_INPUT);
            logger.info("You can always check the progress when heading to \"{}\"", ONSET_GENERATION_FOLDER_PATH_OUTPUT);
            
            try {
                // Convert mp3s to wav
                new ArrayList<>(Arrays.stream(Objects.requireNonNull(new File(ONSET_GENERATION_FOLDER_PATH_INPUT).listFiles())).toList()).stream().filter(f -> f.getName().endsWith(".mp3")).forEach(f -> {
                    String wavFilePath = f.getAbsolutePath().replace(".mp3", ".wav");
                    
                    File wavFile = new File(wavFilePath);
                    if (wavFile.exists()) {
                        System.out.println("Skipping conversion for: " + f.getName() + " as the .wav file already exists.\n");
                        logger.info("Skipping conversion for: {} as the .wav file already exists.", f.getName());
                    } else {
                        try {
                            
                            Mp3ToWavConverter.convert(f.getAbsolutePath(), f.getAbsolutePath().replace(".mp3", ".wav"));
                            System.out.println("mp3 to wav conversion completed successfully for: " + f.getName() + "\n");
                            logger.info("mp3 to wav conversion completed successfully for: {}", f.getName());
                        } catch (IOException e) {
                            logger.error("Error while converting mp3 to wav: {}", f.getName(), e);
                            System.err.println("Error while converting mp3 to wav: " + f.getName() + "\n");
                        }
                    }
                });
                
                // Update the file list to see if there are any wav files generated
                List<File> files = new ArrayList<>(Arrays.stream(Objects.requireNonNull(new File(ONSET_GENERATION_FOLDER_PATH_INPUT).listFiles())).toList());
                // Remove any file that is not a wav file. Only wav files are supported for the onset generation
                files.removeIf(f -> !f.getName().endsWith(".wav"));
                if (files.isEmpty()) throw new Exception();
                logger.info("Found {} MP3 Files in \"{}\"", files.size(), ONSET_GENERATION_FOLDER_PATH_INPUT);
            } catch (Exception e) {
                ui.statusCheck.setBackground(DARK_MODE ? Color.BLACK : Color.WHITE);
                this.setText("Convert MP3s to timing maps");
                throw new ConvertMP3Exception("Found 0 MP3 Files! Please put your mp3 Files into the folder: \"" + ONSET_GENERATION_FOLDER_PATH_INPUT + "\"");
            }
            
            // Generate Onsets
            Thread.sleep(1000);
            if (BatchWavToMaps.generateOnsets(ONSET_GENERATION_FOLDER_PATH_INPUT, ONSET_GENERATION_FOLDER_PATH_OUTPUT, true, null)) {
                logger.info("Successfully created Map. You can find your map in \"{}/\"", ONSET_GENERATION_FOLDER_PATH_OUTPUT);
            } else { // Install dependencies if not already installed
                logger.error("There was an error while creating the onsets. It is possible that a dependency is not installed. Please ensure that they are all installed and then try again!");
                logger.info("Trying installing dependencies...");
                if (CreateAllNecessaryDIRsAndFiles.installDependencies()) {
                    logger.info("Finished installing dependencies... Please press the button again.");
                } else {
                    logger.error("Error while installing dependencies...");
                }
            }
            
            ui.statusCheck.setBackground(DARK_MODE ? Color.BLACK : Color.WHITE);
            this.setText("Convert MP3s to timing maps");
        } catch (ConvertMP3Exception e) {
            logger.error("Conversion error: ", e);
            printException(e);
        } catch (Exception e) {
            this.setBounds(320, 20, 300, 30);
            this.setBackground(Color.RED);
            logger.error("Something went wrong during conversion. Is it the right file extension?", e);
            logger.error(Arrays.toString(e.getStackTrace()));
            printException(new ConvertMP3Exception("Something went wrong during conversion. Is it the right file extension?"));
        }
    }
}
