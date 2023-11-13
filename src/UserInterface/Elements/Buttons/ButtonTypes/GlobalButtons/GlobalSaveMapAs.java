package UserInterface.Elements.Buttons.ButtonTypes.GlobalButtons;

import BeatSaberObjects.BeatSaberMap;
import UserInterface.Elements.Buttons.MyButton;
import UserInterface.Elements.ElementTypes;
import UserInterface.UserInterface;

import javax.swing.*;
import java.awt.*;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

import static DataManager.Parameters.*;
import static DataManager.Parameters.filePath;

public class GlobalSaveMapAs extends MyButton {
    public GlobalSaveMapAs(UserInterface ui) {
        super(ElementTypes.GLOBAL_SAVE_MAP_AS, ui);
        setBackground(Color.green);
    }

    @Override
    public void onClick() {
        JFileChooser fileChooser = new JFileChooser(filePath);
        int option = fileChooser.showSaveDialog(this);
        if (option != 0) return;
        try {
            String filePath = fileChooser.getSelectedFile().getAbsolutePath();
            filePath += filePath.contains(".dat") ? "" : ".dat";

            BufferedWriter bw = new BufferedWriter(new FileWriter(filePath));
            bw.write(new BeatSaberMap(ui.map._notes).exportAsMap());
            bw.close();

            ui.statusCheck.setText(ui.statusCheck.getText() + "\n[INFO]: Map saved successfully: " + filePath);
            System.out.println("Map saved successfully: " + ui.map.exportAsMap());
            if (verbose) ui.statusCheck.setText(ui.statusCheck.getText() + "\n" + "VERBOSE: " + "Map saved successfully: " + ui.map.exportAsMap());
        } catch (IOException ioException) {
            ui.statusCheck.setText(ui.statusCheck.getText() + "\n[ERROR]: There was an error while saving the map " + filePath + "!");
            ioException.printStackTrace();
        }
    }
}
