package UserInterface.Elements.Buttons.ButtonTypes.GlobalButtons.Buttons;

import BeatSaberObjects.Objects.BeatSaberMap;
import DataManager.Parameters;
import UserInterface.Elements.Buttons.ButtonTypes.GlobalButtons.GlobalButton;
import UserInterface.Elements.ElementTypes;
import UserInterface.UserInterface;

import javax.swing.*;
import java.awt.*;

import static DataManager.Parameters.FILE_CHOOSER;
import static DataManager.Parameters.logger;

/**
 * Saves all generated difficulties to disk. The user is asked once whether existing files
 * may be overwritten or should be backed up first; the actual writing and backup handling
 * is done by the AppController.
 */
public class GlobalSaveMapAs extends GlobalButton {
    private static final int OPTION_OVERWRITE = 0;
    private static final int OPTION_BACKUP = 1;

    public GlobalSaveMapAs(UserInterface ui) {
        super(ElementTypes.GLOBAL_SAVE_MAP_AS, ui);
        setBackground(Color.green);
        logger.debug("GlobalSaveMapAs button initialized.");
    }

    @Override
    public void onClick() {
        if (ui.map.isEmpty()) return;

        int confirmation = -1;
        boolean allSucceeded = true;
        for (BeatSaberMap uiMap : ui.map) {
            if (uiMap.difficultyFileName == null || uiMap.difficultyFileName.isEmpty() || uiMap.difficultyFileName.equals("NULL")) continue;
            String filePath = Parameters.filePath;

            if (ui.map.size() == 1) {
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

            // Ask only once whether existing files may be overwritten or should be backed up
            if (confirmation == -1) {
                confirmation = confirmationPopUp();
                if (confirmation != OPTION_OVERWRITE && confirmation != OPTION_BACKUP) return;
            }

            if (!ui.controller.saveMap(uiMap, filePath, confirmation == OPTION_BACKUP)) {
                allSucceeded = false;
            }
        }

        if (allSucceeded && confirmation != -1) ui.controller.markSaved();
    }

    private int confirmationPopUp() {
        String[] options = {"YES", "Create Backup", "NO"};

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