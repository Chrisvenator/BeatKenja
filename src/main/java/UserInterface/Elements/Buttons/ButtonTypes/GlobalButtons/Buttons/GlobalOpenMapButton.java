package UserInterface.Elements.Buttons.ButtonTypes.GlobalButtons.Buttons;

import BeatSaberObjects.Objects.BeatSaberMap;
import BeatSaberObjects.Objects.Note;
import DataManager.FileManager;
import DataManager.Parameters;
import MapGeneration.PatternGeneration.CommonMethods.FixSwingTimings;
import MapGeneration.PatternGeneration.CommonMethods.NpsBpmConverter;
import MapGeneration.PatternGeneration.CommonMethods.Parser;
import UserInterface.Elements.Buttons.ButtonTypes.GlobalButtons.Exceptions.WrongFileException;
import UserInterface.Elements.Buttons.ButtonTypes.GlobalButtons.GlobalButton;
import UserInterface.Elements.ElementTypes;
import UserInterface.UserInterface;

import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static DataManager.Parameters.AUTOLOAD_DEFAULT_MAP_for_testing;
import static DataManager.Parameters.BPM;
import static DataManager.Parameters.DEFAULT_PATH_FOR_AUTOLOAD_MAP;
import static DataManager.Parameters.FILE_CHOOSER;
import static DataManager.Parameters.MAP_FILE_FORMAT;
import static DataManager.Parameters.filePath;
import static DataManager.Parameters.logger;

/**
 * A button in the user interface responsible for opening and loading Beat Saber map files.
 * This button provides functionality for selecting and loading a map file, either from a specific file
 * or an entire directory containing multiple map files. It also handles the automatic loading of a default map for testing purposes.
 * <p>
 * The class includes error handling for incorrect file types and ensures that only valid map files are processed.
 * Once a map is successfully loaded, the button updates the UI and initiates any necessary post-loading processes, such as plotting NPS distribution graphs.
 */
public class GlobalOpenMapButton extends GlobalButton {

    /**
     * Constructs a new GlobalOpenMapButton with the specified user interface context.
     * If the AUTOLOAD_DEFAULT_MAP_for_testing parameter is set, it automatically loads the default map.
     *
     * @param ui The user interface context to which this button belongs.
     * @throws WrongFileException If the autoload default map file is invalid or cannot be read.
     */
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
                errorWhileLoading(new WrongFileException(DEFAULT_PATH_FOR_AUTOLOAD_MAP, "Wrong file type!"));
                return;
            }

            loadMap(f);
            filePath = f.getParent();

            logger.info("Successfully created map from Json");
            successfullyLoaded();
        }
    }

    /**
     * Handles the onClick event for the button, opening a file chooser dialog for the user to select a map file or directory.
     * The method validates the selected file, processes it, and updates the UI accordingly.
     */
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
                extractAndSetGlobalBPM(path);
                for (File f : files) {
                    System.out.println("Found file: " + f.getName());
                    loadMap(f);
                }
            } else {
                if (path.getAbsolutePath().contains("Info.dat") || !path.getAbsolutePath().contains(".dat")) {
                    logger.error("Wrong file type While loading difficulty: {}", path.getName());
                    throw new WrongFileException(path.getName(), "Wrong file type!");
                } else loadMap(path);
                extractAndSetGlobalBPM(path.getParentFile());
            }

            successfullyLoaded();
        }
        catch (WrongFileException e) {
            errorWhileLoading(e);
        }
        catch (Exception e) {
            errorWhileLoading(e);
            printException(e);
        }
    }

    /**
     * Loads a map from the specified file path and adds it to the user interface's map collection.
     * Also initializes the parity error list for the loaded map.
     *
     * @param path The file path from which to load the map.
     */
    public void loadMap(File path){
        ui.map.add(BeatSaberMap.newMapFromJSON(path.getAbsolutePath()));
        Parameters.PARITY_ERRORS_LIST.put(path.getName(), new ArrayList<>());

        logger.info("Successfully loaded: {}/{}", path.getParent(), path.getName());
        System.out.println("Successfully loaded: " + path.getParent() + "/" + path.getName());

        extractAndSetGlobalBPM(path);
    }

    /**
     * Extracts the global BPM (beats per minute) value from the map's info.dat file and updates the user interface.
     * This method reads the BPM from the info.dat file located in the parent directory of the provided path.
     *
     * @param path The file or directory path from which to extract the global BPM.
     */
    private void extractAndSetGlobalBPM (File path){
        File info = new File(path.getParentFile().getAbsolutePath() + "/info.dat");
        if (!info.exists() || !info.isFile() || !info.canRead()) {return;}

        List<String> infoFile = FileManager.readFile(info.getAbsolutePath());
        String searchString = "\"_beatsPerMinute\": ";
        for (String line : infoFile) {
            if (line.contains(searchString)){
                Parameters.BPM = Parser.parseValue(
                        line.substring(line.indexOf(searchString) + searchString.length(), line.lastIndexOf(",")),
                        "bpm according to info.at",
                        Double::parseDouble,
                        BPM
                );
                ui.globalButton.globalBPMField.setBPM(BPM);
            }
        }
    }

    /**
     * Handles errors that occur during the map loading process, logging the error and updating the UI to indicate failure.
     *
     * @param e The exception that occurred during the map loading process.
     */
    private void errorWhileLoading(Exception e) {
        logger.error("Error while loading Map. Map probably has the wrong format: {}", e.getMessage());
        System.err.println("[ERROR]: Map probably has the wrong format: \n" + e);
        ui.labelMapDiff.setText("There was an error while importing the map!");
        ui.labelMapDiff.setBounds(60, 20, 300, 30);
        this.setBounds(320, 20, 300, 30);
        this.setBackground(Color.RED);
        ui.mapSuccessfullyLoaded = false;
    }

    /**
     * Finalizes the successful loading of a map by updating the UI and initiating any necessary processes such as plotting the NPS distribution.
     * If the FIX_INCONSISTENT_TIMINGS parameter is enabled, the method plots the note timings as graphs.
     */
    private void successfullyLoaded() {
        // Plot nps distribution
        if (Parameters.FIX_INCONSISTENT_TIMINGS) ui.map.forEach(map -> {
            List<Note> notes = new ArrayList<>(Arrays.asList(map._notes));
            NpsBpmConverter.convertBeatsToSeconds(notes);
            FixSwingTimings.plotAsGraphs(map.difficultyFileName, Arrays.asList(map._notes));
            NpsBpmConverter.convertSecondsToBeats(notes);
        });

        this.setText("load another diff");
        this.setBounds(270, 20, 200, 30);
        this.setBackground(Color.GREEN);
        ui.mapSuccessfullyLoaded = true;
    }
}
