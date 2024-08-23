package UserInterface.Elements.Buttons.ButtonTypes.GlobalButtons.Buttons;

import BeatSaberObjects.Objects.BeatSaberMap;
import DataManager.Parameters;
import MapGeneration.GenerationElements.Exceptions.NoteNotValidException;
import MapGeneration.GenerationElements.Pattern;
import UserInterface.Elements.Buttons.ButtonTypes.GlobalButtons.Exceptions.MapHasWrongFormatException;
import UserInterface.Elements.Buttons.ButtonTypes.GlobalButtons.GlobalButton;
import UserInterface.Elements.ElementTypes;
import UserInterface.UserInterface;

import java.awt.*;
import java.io.File;

import static DataManager.Parameters.DEFAULT_EASY_PATTERN_PATH;
import static DataManager.Parameters.DEFAULT_PATH;
import static DataManager.Parameters.DEFAULT_PATTERN_METADATA;
import static DataManager.Parameters.DEFAULT_PATTERN_PATH;
import static DataManager.Parameters.FILE_CHOOSER;
import static DataManager.Parameters.logger;
import static DataManager.Parameters.useDatabase;
import static DataManager.Parameters.verbose;

public class GlobalLoadPatterns extends GlobalButton {
    public GlobalLoadPatterns(UserInterface ui) {
        super(ElementTypes.GLOBAL_LOAD_PATTERNS_BUTTON, ui);
        if (Parameters.AUTOLOAD_DEFAULT_PATTERNS) {
            try {
                ui.pattern = new Pattern(String.valueOf(useDatabase ? DEFAULT_PATTERN_METADATA : DEFAULT_PATTERN_PATH));
            } catch (NoteNotValidException e) {
                setBackground(Color.RED);
                logger.error("Could not load default pattern. Please do it manually!");
            }
            try {
                UserInterface.easyPattern = new Pattern(String.valueOf(DEFAULT_EASY_PATTERN_PATH));
            } catch (NoteNotValidException e) {
                logger.error("Could not load default easy pattern. Skipping");
                UserInterface.easyPattern = null;
            }

        }

        if (ui.pattern == null || (!new File(Parameters.DEFAULT_PATTERN_PATH).exists() && !Parameters.useDatabase)) {
            setBackground(Color.RED);
            logger.warn("Pattern is null or the default pattern path does not exist, and the database is not in use. Pattern has to be loaded manually: " + new File(Parameters.DEFAULT_PATTERN_PATH).getAbsolutePath());
        }
        logger.debug("GlobalLoadPatterns button initialized.");
    }

    @Override
    public void onClick() {
        FILE_CHOOSER.setCurrentDirectory(new File(Parameters.DEFAULT_PATTERN_FOLDER_PATH));
        int option = FILE_CHOOSER.showOpenDialog(this);
        FILE_CHOOSER.setCurrentDirectory(new File(DEFAULT_PATH));
        logger.debug("File chooser opened with option: {}", option);

        if (!approveFileLoading(option)) {
            logger.info("File loading not approved.");
            return;
        } else logger.info("File loading approved.");

        try {
            File selectedFile = FILE_CHOOSER.getSelectedFile();
            if (selectedFile.getName().endsWith(".pat")) {
                ui.pattern = new Pattern(selectedFile.getAbsolutePath());
                logger.info("Pattern loaded from .pat file: {}", selectedFile.getAbsolutePath());
            } else {
                if (!selectedFile.isFile() || (!selectedFile.getName().endsWith(".json") && !selectedFile.getName().endsWith(".dat"))) {
                    logger.error("Map has wrong file type: {}", selectedFile.getAbsolutePath());
                    throw new MapHasWrongFormatException("Wrong file type!");
                }

                BeatSaberMap beatSaberMap = BeatSaberMap.newMapFromJSON(selectedFile.getAbsolutePath());
                ui.pattern = new Pattern(beatSaberMap._notes, 1);
                logger.info("Pattern loaded from map file: {}", selectedFile.getAbsolutePath());
                System.out.println("[INFO]:Pattern loaded: " + ui.pattern.exportInPatFormat());
            }

            String successMessage = "Successfully loaded Patterns";
            logger.info(successMessage);
            ui.statusCheck.append("\n[INFO]: " + successMessage);
            this.setBackground(Color.green);
        } catch (Exception e) {
            logger.error("Error while importing patterns: ", e);
            printException(new MapHasWrongFormatException("There was an error while importing the patterns! Map probably has the wrong format!"));
        }

        if (verbose) {
            logger.debug("Pattern: {}", ui.pattern);
        }
    }
}
