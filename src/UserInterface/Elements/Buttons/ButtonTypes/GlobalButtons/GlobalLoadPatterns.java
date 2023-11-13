package UserInterface.Elements.Buttons.ButtonTypes.GlobalButtons;

import BeatSaberObjects.BeatSaberMap;
import MapGeneration.GenerationElements.Pattern;
import UserInterface.Elements.Buttons.MyButton;
import UserInterface.Elements.ElementTypes;
import UserInterface.UserInterface;
import com.google.gson.Gson;

import javax.swing.*;
import java.awt.*;
import java.io.FileNotFoundException;
import java.util.Scanner;

import static DataManager.Parameters.*;

public class GlobalLoadPatterns extends MyButton {
    public GlobalLoadPatterns(UserInterface ui) {
        super(ElementTypes.GLOBAL_LOAD_PATTERNS_BUTTON, ui);
    }

    @Override
    public void onClick() {
        JFileChooser fileChooser = new JFileChooser(DEFAULT_PATH);
        if (darkMode) fileChooser.setForeground(Color.white);
        int option = fileChooser.showOpenDialog(this);

        if (option == JFileChooser.APPROVE_OPTION) {
            try {
                Scanner scanner = new Scanner(fileChooser.getSelectedFile());
                String mapAsString = scanner.nextLine();

                BeatSaberMap beatSaberMap = new Gson().fromJson(mapAsString, BeatSaberMap.class);
                ui.pattern = new Pattern(beatSaberMap._notes, 1);

                ui.statusCheck.setText(ui.statusCheck.getText() + "\n[INFO]: Successfully loaded Patterns");
                this.setBackground(Color.green);
            } catch (FileNotFoundException e) {
                ui.labelMapDiff.setText(ui.statusCheck.getText() + "\n[ERROR]: File Not found!");
                System.err.println(e.getMessage());
            } catch (Exception e) {
                System.err.println("ERROR: Map probably has the wrong format: \n" + e);
                ui.labelMapDiff.setText(ui.statusCheck.getText() + "\n[ERROR]: There was an error while importing the patterns!");
            }
        }
    }
}
