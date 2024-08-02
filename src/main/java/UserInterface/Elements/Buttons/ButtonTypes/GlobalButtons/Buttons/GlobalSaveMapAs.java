package UserInterface.Elements.Buttons.ButtonTypes.GlobalButtons.Buttons;

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
        logger.debug("GlobalSaveMapAs button initialized.");
    }

    @Override
    public void onClick() {
        int option = FILE_CHOOSER.showSaveDialog(this);
        logger.info("File chooser opened with option: {}", option);

        if (!approveFileLoading(option)) {
            logger.info("File loading not approved.");
            return;
        }

        String filePath = FILE_CHOOSER.getSelectedFile().getAbsolutePath();
        filePath += filePath.contains(".dat") ? "" : ".dat";

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(filePath))) {
            bw.write(ui.map.exportAsMap());
            logger.info("Map saved successfully at: {}", filePath);
            logger.debug("Map saved successfully: {}", ui.map.exportAsMap());
        } catch (IOException e) {
            logger.error("There was an error while saving the map at {}: {}", filePath, e.getMessage());
            printException(new IOException("There was an error while saving the map " + filePath + "!", e));
        }
    }
}
