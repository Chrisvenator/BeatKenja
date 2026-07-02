package UserInterface.Elements.Buttons.ButtonTypes.GlobalButtons.Buttons;

import AppLogic.AppState;
import BeatSaberObjects.Objects.Note;
import DataManager.Parameters;
import MapGeneration.PatternGeneration.CommonMethods.FixSwingTimings;
import MapGeneration.PatternGeneration.CommonMethods.NpsBpmConverter;
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
import static DataManager.Parameters.DEFAULT_PATH_FOR_AUTOLOAD_MAP;
import static DataManager.Parameters.FILE_CHOOSER;
import static DataManager.Parameters.filePath;
import static DataManager.Parameters.logger;

/**
 * A button in the user interface responsible for opening and loading Beat Saber map files.
 * The actual loading (single file or whole folder, BPM extraction, parity list setup) lives
 * in the AppController; this class only handles the file chooser and the visual feedback
 * (button color/text), and plots the NPS distribution after a successful load.
 */
public class GlobalOpenMapButton extends GlobalButton {

    /**
     * Constructs a new GlobalOpenMapButton with the specified user interface context.
     * If the AUTOLOAD_DEFAULT_MAP_for_testing parameter is set, it automatically loads the default map.
     *
     * @param ui The user interface context to which this button belongs.
     */
    public GlobalOpenMapButton(UserInterface ui) {
        super(ElementTypes.GLOBAL_OPEN_MAP, ui);
        setBackground(Color.cyan);
        logger.debug("GlobalOpenMapButton initialized.");

        // Autoload default map for testing purposes
        if (AUTOLOAD_DEFAULT_MAP_for_testing) {
            File f = new File(DEFAULT_PATH_FOR_AUTOLOAD_MAP);
            loadAndShowResult(f);
            if (ui.controller.state() != AppState.EMPTY) filePath = f.getParent();
        }
    }

    /**
     * Handles the onClick event for the button, opening a file chooser dialog for the user to select a map file or directory.
     * The selected path is loaded through the AppController; the UI is updated accordingly.
     */
    @Override
    public void onClick() {
        FILE_CHOOSER.setCurrentDirectory(new File(filePath));
        int option = FILE_CHOOSER.showOpenDialog(this);
        logger.debug("File chooser opened with option: {}", option);

        if (!approveFileLoading(option)) {
            logger.info("File loading not approved.");
            return;
        }

        filePath = FILE_CHOOSER.getCurrentDirectory().toString();
        ui.statusCheck.clear();
        loadAndShowResult(FILE_CHOOSER.getSelectedFile());
    }

    /** Loads the file/folder via the AppController and updates the button visuals for success or failure. */
    private void loadAndShowResult(File path) {
        try {
            ui.controller.loadMapFileOrFolder(path);
            filePath = ui.controller.session().getMapFolderPath();
            plotNpsDistribution();
            successfullyLoaded();
        } catch (Exception e) {
            errorWhileLoading(e);
            printException(new WrongFileException(path.getAbsolutePath(), e.getMessage()));
        }
    }

    /** Plots the note timings of every loaded diff as graphs, if enabled in the config. */
    private void plotNpsDistribution() {
        if (Parameters.FIX_INCONSISTENT_TIMINGS) ui.map.forEach(map -> {
            List<Note> notes = new ArrayList<>(Arrays.asList(map._notes));
            if (Parameters.PLOT_NPS_DISTRIBUTION) {
                NpsBpmConverter.convertBeatsToSeconds(notes);
                FixSwingTimings.plotAsGraphs(map.difficultyFileName, Arrays.asList(map._notes));
                NpsBpmConverter.convertSecondsToBeats(notes);
            }
        });
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
    }

    /** Updates the button visuals after a successful load. */
    private void successfullyLoaded() {
        this.setText("load another diff");
        this.setBounds(270, 20, 200, 30);
        this.setBackground(Color.GREEN);
    }
}