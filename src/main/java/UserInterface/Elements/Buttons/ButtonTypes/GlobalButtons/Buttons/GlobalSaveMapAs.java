package UserInterface.Elements.Buttons.ButtonTypes.GlobalButtons.Buttons;

import BeatSaberObjects.Objects.BeatSaberMap;
import UserInterface.Elements.Buttons.ButtonTypes.GlobalButtons.GlobalButton;
import UserInterface.Elements.ElementTypes;
import UserInterface.UserInterface;

import java.awt.*;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

import static DataManager.Parameters.*;

public class GlobalSaveMapAs extends GlobalButton {
    public GlobalSaveMapAs(UserInterface ui) {
        super(ElementTypes.GLOBAL_SAVE_MAP_AS, ui);
        setBackground(Color.green);
    }

    @Override
    public void onClick() {
        int option = FILE_CHOOSER.showSaveDialog(this);

        if (!approveFileloading(option)) return;
        try {
            String filePath = FILE_CHOOSER.getSelectedFile().getAbsolutePath();
            filePath += filePath.contains(".dat") ? "" : ".dat";

            BufferedWriter bw = new BufferedWriter(new FileWriter(filePath));
            bw.write(new BeatSaberMap(ui.map._notes).exportAsMap());
            bw.close();

            ui.statusCheck.append("\n[INFO]: Map saved successfully: " + filePath);
            System.out.println("Map saved successfully: " + ui.map.exportAsMap());
            if (verbose) ui.statusCheck.setText(ui.statusCheck.getText() + "\n" + "VERBOSE: " + "Map saved successfully: " + ui.map.exportAsMap());
        } catch (IOException e) {
            printException(new IOException("There was an error while saving the map " + filePath + "!"));
        }
    }
}
