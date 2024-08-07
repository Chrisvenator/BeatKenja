package UserInterface.Elements.Buttons.ButtonTypes.GlobalButtons.Buttons;

import BeatSaberObjects.Objects.BeatSaberMap;
import DataManager.Parameters;
import UserInterface.Elements.Buttons.ButtonTypes.GlobalButtons.Exceptions.WrongFileException;
import UserInterface.Elements.Buttons.ButtonTypes.GlobalButtons.GlobalButton;
import UserInterface.Elements.ElementTypes;
import UserInterface.UserInterface;

import java.awt.*;
import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;

import static DataManager.Parameters.AUTOLOAD_DEFAULT_MAP_for_testing;
import static DataManager.Parameters.DEFAULT_PATH_FOR_AUTOLOAD_MAP;
import static DataManager.Parameters.FILE_CHOOSER;
import static DataManager.Parameters.MAP_FILE_FORMAT;
import static DataManager.Parameters.filePath;
import static DataManager.Parameters.logger;

public class GlobalOpenMapButton extends GlobalButton {
    //TODO: Diese Methode
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

            loadMap(f);
            filePath = f.getParent();

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
            ui.map.clear();
            ui.statusCheck.clear();
            Parameters.PARITY_ERRORS_LIST.clear();

            if (path.isDirectory()) {
                File [] files = path.listFiles(MAP_FILE_FORMAT);
                if (files == null || files.length == 0) {
                    logger.error("No files found in path: {}", path);
                    System.err.println("No files found in path: " + path);
                    errorWhileLoading(new WrongFileException(path.getAbsolutePath(), "Could not find valid files in folder!"));
                    return;
                }

                //Change filePath because before, we set it to the parent dir and not the current (working) dir
                filePath = path.getAbsolutePath();
                for (File f : files) {
                    System.out.println("Found file: " + f.getName());
                    loadMap(f);
                }
            } else {
                if (path.getAbsolutePath().contains("Info.dat") || !path.getAbsolutePath().contains(".dat")) {
                    logger.error("Wrong file type While loading difficulty: {}", path.getName());
                    throw new WrongFileException(path.getName(), "Wrong file type!");
                } else loadMap(path);
            }
            successfullyLoaded(FILE_CHOOSER.getSelectedFile().getAbsolutePath());
        }
        catch (WrongFileException e) {
            errorWhileLoading(e);
        }
        catch (Exception e) {
            errorWhileLoading(e);
            printException(e);
        }
    }

    private void loadMap(File path){
        ui.map.add(BeatSaberMap.newMapFromJSON(path.getAbsolutePath()));
        Parameters.PARITY_ERRORS_LIST.put(path.getName(), new ArrayList<>());

        logger.info("Successfully loaded: {}/{}", path.getParent(), path.getName());
        System.out.println("Successfully loaded: " + path.getParent() + "/" + path.getName());
    }

    private void errorWhileLoading(Exception e) {
        logger.error("Error while loading Map. Map probably has the wrong format: {}", e.getMessage());
        System.err.println("[ERROR]: Map probably has the wrong format: \n" + e);
        ui.labelMapDiff.setText("There was an error while importing the map!");
        ui.labelMapDiff.setBounds(100, 20, 300, 30);
        this.setBounds(320, 20, 300, 30);
        this.setBackground(Color.RED);
        ui.mapSuccessfullyLoaded = false;
    }

    private void successfullyLoaded(String absolutePath) {
        this.setText("load another diff");
        this.setBounds(270, 20, 200, 30);
        this.setBackground(Color.GREEN);
        String successMessage = "Successfully loaded difficulty: \"" + absolutePath + "\"";
        logger.info(successMessage);
        ui.statusCheck.append(successMessage);
        ui.mapSuccessfullyLoaded = true;
    }
}
