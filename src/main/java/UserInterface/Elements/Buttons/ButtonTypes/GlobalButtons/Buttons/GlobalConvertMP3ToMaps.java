package UserInterface.Elements.Buttons.ButtonTypes.GlobalButtons.Buttons;

import AudioAnalysis.Mp3ToWavConverter;
import DataManager.CreateAllNecessaryDIRsAndFiles;
import MapGeneration.BatchWavToMaps;
import UserInterface.Elements.Buttons.ButtonTypes.GlobalButtons.Exceptions.ConvertMP3Exception;
import UserInterface.Elements.Buttons.ButtonTypes.GlobalButtons.GlobalButton;
import UserInterface.Elements.ElementTypes;
import UserInterface.UserInterface;
import javazoom.jl.decoder.JavaLayerException;

import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
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
    }

    @Override
    public void onClick() {
        if (!CreateAllNecessaryDIRsAndFiles.isFFMpegInstalled()) {
            ui.statusCheck.append("\n[ERROR]: FFMpeg is not installed. Please install it and try again!");
            return;
        }

        if (!CreateAllNecessaryDIRsAndFiles.isPythonInstalled()) {
            ui.statusCheck.append("\n[ERROR]: Python could not be found please ensure that it is installed and added to the PATH variable!");
            return;
        }


        try {
            ui.statusCheck.setBackground(Color.gray);
            this.setText("In Progress...");
            ui.statusCheck.append("\n[INFO]: Converting all Songs from \"" + ONSET_GENERATION_FOLDER_PATH_INPUT + "\" to timing maps... This might take a while if there are a lot of songs.\n");
            ui.statusCheck.append("[INFO]: You can always check the progress when heading to \"" + ONSET_GENERATION_FOLDER_PATH_OUTPUT + "\"\n");

            try {
                //Convert mp3s to wav
                new ArrayList<>(Arrays.stream(Objects.requireNonNull(new File(ONSET_GENERATION_FOLDER_PATH_INPUT).listFiles())).toList()).stream().filter(f -> f.getName().endsWith(".mp3")).forEach(f -> {
                    try {
                        Mp3ToWavConverter.convert(f.getAbsolutePath(), f.getAbsolutePath().replace(".mp3", ".wav"));
                        System.out.println("mp3 to wav conversion completed successfully for: " + f.getName() + "\n");
                        ui.statusCheck.append("mp3 to wav conversion completed successfully for: " + f.getName() + "\n");
                    } catch (UnsupportedAudioFileException | IOException | LineUnavailableException | JavaLayerException e) {
                        e.printStackTrace();
                        System.err.println("Error while converting mp3 to wav: " + f.getName() + "\n");
                    }
                });

                //Update the file list to see if there are any wav files generated
                List<File> files = new ArrayList<>(Arrays.stream(Objects.requireNonNull(new File(ONSET_GENERATION_FOLDER_PATH_INPUT).listFiles())).toList());
                //Remove any file that is not a wav file. Only wav files are supported for the onset generation
                files.removeIf(f -> !f.getName().endsWith(".wav"));
                if (files.size() == 0) throw new Exception();
                ui.statusCheck.append("[INFO]: Found " + files.size() + " MP3 Files in \"" + ONSET_GENERATION_FOLDER_PATH_INPUT + "\"\n\n");
            } catch (Exception e) {
                ui.statusCheck.setBackground(darkMode ? Color.BLACK : Color.WHITE);
                this.setText("Convert MP3s to timing maps");
                throw new ConvertMP3Exception("Found 0 MP3 Files! Please put your mp3 Files into th folder: \"" + ONSET_GENERATION_FOLDER_PATH_INPUT);
            }

            //generate Onsets
            Thread.sleep(1000);
            if (BatchWavToMaps.generateOnsets(ONSET_GENERATION_FOLDER_PATH_INPUT, ONSET_GENERATION_FOLDER_PATH_OUTPUT, true, null)) {
                ui.statusCheck.append("\n[INFO]: Successfully created Map. You can find your map in \"" + ONSET_GENERATION_FOLDER_PATH_OUTPUT + "/\"\n\n");
            } else { //Install dependencies if not already installed
                ui.statusCheck.append("\n[ERROR]: There was an error while creating the onsets. It is possible that a dependency is not installed. Please ensure that they are all installed and then try again!");
                ui.statusCheck.append("\n[INFO]: Trying installing dependencies...\n\n");
                if (CreateAllNecessaryDIRsAndFiles.installDependencies()) ui.statusCheck.append("\n[INFO]: Finished installing dependencies... Please press the button again.\n\n");
                else ui.statusCheck.append("\n[ERROR]: error while installing dependencies...");
            }

            ui.statusCheck.setBackground(darkMode ? Color.BLACK : Color.WHITE);
            this.setText("Convert MP3s to timing maps");
        } catch (ConvertMP3Exception e) {
            printException(e);
        } catch (Exception e) {
            this.setBounds(320, 20, 300, 30);
            this.setBackground(Color.RED);
            printException(new ConvertMP3Exception("Something went wrong during conversion. Is it the right file extension?"));
            e.printStackTrace();
        }

    }
}
