package UserInterface.Elements.Buttons.ButtonTypes.GlobalButtons.Buttons;

import BeatSaberObjects.Objects.BeatSaberMap;
import DataManager.Parameters;
import UserInterface.Elements.Buttons.ButtonTypes.GlobalButtons.GlobalButton;
import UserInterface.Elements.ElementTypes;
import UserInterface.UserInterface;

import java.awt.*;
import java.io.BufferedWriter;
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

            try (BufferedWriter bw = new BufferedWriter(new FileWriter(filePath))) {
                bw.write(uiMap.exportAsMap());
                logger.info("Map saved successfully at: {}", filePath);
                logger.debug("Map saved successfully: {}", uiMap.exportAsMap());
                System.out.println("Map saved successfully: " + uiMap.exportAsMap());
            }
            catch (IOException e) {
                logger.error("There was an error while saving the map at {}: {}", filePath, e.getMessage());
                printException(new IOException("There was an error while saving the map " + filePath + "!", e));
            }
        }
    }
}
