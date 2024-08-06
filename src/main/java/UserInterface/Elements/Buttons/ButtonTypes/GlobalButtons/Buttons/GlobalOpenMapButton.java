package UserInterface.Elements.Buttons.ButtonTypes.GlobalButtons.Buttons;

import BeatSaberObjects.Objects.BeatSaberMap;
import UserInterface.Elements.Buttons.ButtonTypes.GlobalButtons.Exceptions.WrongFileException;
import UserInterface.Elements.Buttons.ButtonTypes.GlobalButtons.GlobalButton;
import UserInterface.Elements.ElementTypes;
import UserInterface.UserInterface;

import java.awt.*;
import java.io.File;

import static DataManager.Parameters.*;

public class GlobalOpenMapButton extends GlobalButton {
    @lombok.SneakyThrows
    public GlobalOpenMapButton(UserInterface ui) {
        super(ElementTypes.GLOBAL_OPEN_MAP, ui);
        setBackground(Color.cyan);
        logger.debug("GlobalOpenMapButton initialized.");

        // Autoload default map for testing purposes
        if (AUTOLOAD_DEFAULT_MAP_for_testing) {
            File f = new File(DEFAULT_PATH_FOR_AUTOLOAD_MAP);
            if (!f.exists() || f.isDirectory() || !f.isFile() || !f.canRead()) {
                logger.error("Wrong file type while initializing GlobalOpenMapButton: {}", DEFAULT_PATH_FOR_AUTOLOAD_MAP);
                throw new WrongFileException(DEFAULT_PATH_FOR_AUTOLOAD_MAP, "Wrong file type!");
            }

            ui.map = BeatSaberMap.newMapFromJSON(f.getAbsolutePath());
            logger.info("Successfully created map from Json");
            successfullyLoaded(f.getAbsolutePath());
        }
    }

    @Override
    public void onClick() {
        int option = FILE_CHOOSER.showOpenDialog(this);
        logger.debug("File chooser opened with option: {}", option);

        if (!approveFileLoading(option)) {
            logger.info("File loading not approved.");
            return;
        }

        filePath = FILE_CHOOSER.getCurrentDirectory().toString();
        try {
            File path = FILE_CHOOSER.getSelectedFile();
            if (path.isDirectory() || path.getAbsolutePath().contains("Info.dat") || !path.getAbsolutePath().contains(".dat")) {
                logger.error("Wrong file type While loading difficulty: {}", path.getName());
                throw new WrongFileException(path.getName(), "Wrong file type!");
            }

            ui.map = BeatSaberMap.newMapFromJSON(path.getAbsolutePath());
            successfullyLoaded(FILE_CHOOSER.getSelectedFile().getAbsolutePath());
        }catch (WrongFileException e){
            errorWhileLoading(e);
        } catch (Exception e) {
            errorWhileLoading(e);
            printException(e);
        }
    }

    private void errorWhileLoading(Exception e) {
        logger.error("Error while loading Map. Map probably has the wrong format: {}", e.getMessage());
        System.err.println("[ERROR]: Map probably has the wrong format: \n" + e);
        ui.labelMapDiff.setText("There was an error while importing the map!");
        ui.labelMapDiff.setBounds(100, 20, 300, 30);
        setBounds(320, 20, 300, 30);
        setBackground(Color.RED);
        ui.mapSuccessfullyLoaded = false;
    }

    private void successfullyLoaded(String absolutePath) {
        setText("load another diff");
        setBounds(270, 20, 200, 30);
        setBackground(Color.GREEN);
        String successMessage = "Successfully loaded difficulty: \"" + absolutePath + "\"";
        logger.info(successMessage);
        ui.statusCheck.setText(successMessage);
        ui.mapSuccessfullyLoaded = true;
    }
}
