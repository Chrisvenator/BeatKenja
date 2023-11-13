package UserInterface.Elements.Buttons.ButtonTypes.GlobalButtons;

import BeatSaberObjects.BeatSaberMap;
import UserInterface.Elements.Buttons.MyButton;
import UserInterface.Elements.ElementTypes;
import UserInterface.UserInterface;
import com.google.gson.Gson;

import javax.swing.*;
import java.awt.*;
import java.io.FileNotFoundException;
import java.util.Scanner;

import static DataManager.Parameters.*;
import static DataManager.Parameters.darkMode;

public class GlobalOpenMapButton extends MyButton {
    public GlobalOpenMapButton(UserInterface ui) {
        super(ElementTypes.GLOBAL_OPEN_MAP, ui);
        setBackground(Color.cyan);
    }

    @Override
    public void onClick() {
        JFileChooser fileChooser = new JFileChooser(DEFAULT_PATH.trim());
        if (darkMode) fileChooser.setForeground(Color.white);
        int option = fileChooser.showOpenDialog(this);

        if (option == JFileChooser.APPROVE_OPTION) {
            filePath = fileChooser.getCurrentDirectory().toString();
            try {
                Scanner scanner = new Scanner(fileChooser.getSelectedFile());

                String mapAsString = scanner.nextLine();

                ui.map = new Gson().fromJson(mapAsString, BeatSaberMap.class);
                ui.map.originalJSON = mapAsString;

                ui.statusCheck.setText("Successfully loaded difficulty: \"" + fileChooser.getSelectedFile().getAbsolutePath() + "\"");
                ui.mapSuccessfullyLoaded = true;
                successfullyLoaded();
            } catch (FileNotFoundException e) {
                System.err.println(e.getMessage());
            } catch (Exception e) {
                errorWhileLoading(e);
            }
        }
    }

    private void errorWhileLoading(Exception e) {
        System.err.println("[ERROR]: Map probably has the wrong format: \n" + e);
        ui.labelMapDiff.setText("There was an error while importing the map!");
        ui.labelMapDiff.setBounds(100, 20, 300, 30);
        setBounds(320, 20, 300, 30);
        setBackground(Color.RED);
        ui.mapSuccessfullyLoaded = false;
    }

    private void successfullyLoaded() {
        setText("load an other diff");
        setBounds(270, 20, 200, 30);
        setBackground(Color.GREEN);
    }
}
