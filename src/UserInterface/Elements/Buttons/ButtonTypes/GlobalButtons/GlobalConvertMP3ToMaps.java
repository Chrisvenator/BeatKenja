package UserInterface.Elements.Buttons.ButtonTypes.GlobalButtons;

import DataManager.CreateAllNecessaryDIRsAndFiles;
import MapGeneration.BatchWavToMaps;
import UserInterface.Elements.Buttons.MyButton;
import UserInterface.Elements.ElementTypes;
import UserInterface.UserInterface;

import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static DataManager.Parameters.*;

public class GlobalConvertMP3ToMaps extends MyButton {
    public GlobalConvertMP3ToMaps(UserInterface ui) {
        super(ElementTypes.GLOBAL_CONVERT_MP3s, ui);
        setBackground(Color.orange);
    }

    @Override
    public void onClick() {
        if (!CreateAllNecessaryDIRsAndFiles.isFFMpegInstalled()) {
            ui.statusCheck.setText(ui.statusCheck.getText() + "\n[ERROR]: FFMpeg is not installed. Please install it and try again!");
            return;
        }

        if (!CreateAllNecessaryDIRsAndFiles.isPythonInstalled()) {
            ui.statusCheck.setText(ui.statusCheck.getText() + "\n[ERROR]: Python could not be found please ensure that it is installed and added to the PATH variable!");
            return;
        }


        try {
            ui.statusCheck.setBackground(Color.gray);
            this.setText("In Progress...");
            ui.statusCheck.setText(ui.statusCheck.getText() + "\n[INFO]: Converting all Songs from \"" + ONSET_GENERATION_FOLDER_PATH_INPUT + "\" to timing maps... This might take a while if there are a lot of songs.\n");
            ui.statusCheck.setText(ui.statusCheck.getText() + "[INFO]: You can always check the progress when heading to \"" + ONSET_GENERATION_FOLDER_PATH_OUTPUT + "\"\n");

            try {
                List<File> files = new ArrayList<>(Arrays.stream(Objects.requireNonNull(new File(ONSET_GENERATION_FOLDER_PATH_INPUT).listFiles())).toList());
                files.removeIf(f -> !f.getName().endsWith(".mp3"));
                if (files.size() == 0) throw new Exception();
                ui.statusCheck.setText(ui.statusCheck.getText() + "[INFO]: Found " + files.size() + " MP3 Files in \"" + ONSET_GENERATION_FOLDER_PATH_INPUT + "\"\n\n");
            } catch (Exception e) {
                ui.statusCheck.setText(ui.statusCheck.getText() + "\n[INFO]: Found 0 MP3 Files! Please put your mp3 Files into th folder \"" + ONSET_GENERATION_FOLDER_PATH_INPUT + "\"\n\n");
                ui.statusCheck.setBackground(darkMode ? Color.BLACK : Color.WHITE);
                this.setText("Convert MP3s to timing maps");
                return;
            }

            //generate Onsets
            Thread.sleep(1000);
            if (BatchWavToMaps.generateOnsets(ONSET_GENERATION_FOLDER_PATH_INPUT, ONSET_GENERATION_FOLDER_PATH_OUTPUT, true)) {
                ui.statusCheck.setText(ui.statusCheck.getText() + "\n[INFO]: Successfully created Map. You can find your map in \"" + ONSET_GENERATION_FOLDER_PATH_OUTPUT + "/\"\n\n");

                //Install dependencies if not already installed
            } else {
                ui.statusCheck.setText(ui.statusCheck.getText() + "\n[ERROR]: There was an error while creating the onsets. It is possible that a dependency is not installed. Please ensure that they are all installed and then try again!");
                ui.statusCheck.setText(ui.statusCheck.getText() + "\n[INFO]: Trying installing dependencies...\n\n");
                if (CreateAllNecessaryDIRsAndFiles.installDependencies()) {
                    ui.statusCheck.setText(ui.statusCheck.getText() + "\n[INFO]: Finished installing dependencies... Please press the button again.\n\n");
                } else ui.statusCheck.setText(ui.statusCheck.getText() + "\n[ERROR]: error while installing dependencies...");
            }

            ui.statusCheck.setBackground(darkMode ? Color.BLACK : Color.WHITE);
            this.setText("Convert MP3s to timing maps");
        } catch (Exception e) {
            System.err.println("[ERROR]: Something went wrong during conversion. Is it the right file extension?\n" + e);
            ui.statusCheck.setText("[ERROR]: Something went wrong during conversion. Is it the right file extension?\n" + e + "\n\n");
            this.setBounds(320, 20, 300, 30);
            this.setBackground(Color.RED);
        }

    }
}
