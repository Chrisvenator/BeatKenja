package UserInterface.Elements.Buttons.ButtonTypes.GlobalButtons.Buttons;

import BeatSaberObjects.Objects.BeatSaberMap;
import DataManager.Parameters;
import UserInterface.Elements.Buttons.ButtonTypes.GlobalButtons.GlobalButton;
import UserInterface.Elements.ElementTypes;
import UserInterface.UserInterface;

import javax.swing.*;
import java.awt.*;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import static DataManager.Parameters.FILE_CHOOSER;
import static DataManager.Parameters.logger;

public class GlobalSaveMapAs extends GlobalButton {
    public GlobalSaveMapAs(UserInterface ui) {
        super(ElementTypes.GLOBAL_SAVE_MAP_AS, ui);
        setBackground(Color.green);
        logger.debug("GlobalSaveMapAs button initialized.");
    }

    @Override
    public void onClick() {
        int confirmationPopUp = -1;
        for (BeatSaberMap uiMap : ui.map) {
            if (uiMap.difficultyFileName == null || uiMap.difficultyFileName.isEmpty() || uiMap.difficultyFileName.equals("NULL")) continue;
            String filePath = Parameters.filePath;

            if (ui.map.isEmpty()) return;
            else if (ui.map.size() == 1) {
                int option = FILE_CHOOSER.showSaveDialog(this);
                logger.info("File chooser opened with option: {}", option);

                if (!approveFileLoading(option)) {
                    logger.info("File loading not approved.");
                    return;
                }
                filePath = FILE_CHOOSER.getSelectedFile().getAbsolutePath();
            } else filePath += "/" + uiMap.difficultyFileName;

            filePath += filePath.contains(".dat") ? "" : ".dat";
            System.out.println(filePath);

            //Confirmation
            if (confirmationPopUp == -1) {
                confirmationPopUp = confirmationPupUp();
                if (confirmationPopUp == 2) return;
            }
            if (confirmationPopUp == 1) if (!this.createBackup(filePath)) {logger.error("Something went wrong");continue;}


            //Write
            try (BufferedWriter bw = new BufferedWriter(new FileWriter(filePath))) {
                bw.write(uiMap.exportAsMap());
//                logger.info("Map saved successfully at: {}", filePath);
//                logger.debug("Map saved successfully: {}", uiMap.exportAsMap());
                System.out.println("Map saved successfully: " + uiMap.exportAsMap());
            }
            catch (IOException e) {
                logger.error("There was an error while saving the map at {}: {}", filePath, e.getMessage());
                printException(new IOException("There was an error while saving the map " + filePath + "!", e));
            }
        }
    }

    private boolean createBackup(String filePath) {
        File f = new File(filePath);
        if (!f.exists()) return true;

        int i = 1;
        while (f.exists()){
            f = new File(filePath + i);
            i++;
        }
        return new File(filePath).renameTo(f);
    }

    private int confirmationPupUp(){
        String[] options = { "YES", "Create Backup", "NO" };

        return JOptionPane.showOptionDialog(
                ui,
                "Are you sure you want to save? This will overwrite all files! You can make a backup of your old files with the option \"make backup\"",
                "Confirmation",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[2]
        );
    }
}
