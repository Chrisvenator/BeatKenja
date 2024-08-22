package UserInterface;

import BeatSaberObjects.Objects.BeatSaberMap;
import BeatSaberObjects.Objects.Bookmark;
import BeatSaberObjects.Objects.Enums.ParityErrorEnum;
import BeatSaberObjects.Objects.Note;
import DataManager.Logger.GuiAppender;
import MapGeneration.GenerationElements.Pattern;
import MapGeneration.PatternGeneration.CommonMethods.CheckParity;
import UserInterface.Elements.Buttons.ButtonTypes.AdvancedMapCreatorButton;
import UserInterface.Elements.Buttons.ButtonTypes.GlobalButton;
import UserInterface.Elements.Buttons.ButtonTypes.GlobalButtons.Buttons.GlobalOpenMapInBrowser;
import UserInterface.Elements.Buttons.ButtonTypes.GlobalButtons.Buttons.GlobalSaveMapAs;
import UserInterface.Elements.Buttons.ButtonTypes.MapCreatorButton;
import UserInterface.Elements.Buttons.ButtonTypes.MapUtilitiesButton;
import UserInterface.Elements.Buttons.ButtonTypes.ToTimingNotesButton;
import UserInterface.Elements.Buttons.MyButton;
import UserInterface.Elements.JSlider.GlobalJSlider.GlobalPatternVarianceJSlider;
import UserInterface.Elements.JSlider.MyGlobalJSlider;
import javafx.util.Pair;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static DataManager.Parameters.DARK_MODE;
import static DataManager.Parameters.DEFAULT_PATH;
import static DataManager.Parameters.DEFAULT_PATTERN_METADATA;
import static DataManager.Parameters.PARITY_ERRORS_COLORS_MAP;
import static DataManager.Parameters.PARITY_ERRORS_LIST;
import static DataManager.Parameters.SAVE_PARITY_ERRORS_AS_BOOKMARKS_WILL_OVERWRITE_BOOKMARKS;
import static DataManager.Parameters.ignoreDDs;
import static DataManager.Parameters.logger;
import static DataManager.Parameters.saveNewMapsToDefaultPath;
import static DataManager.Parameters.verbose;

/**
 * The `UserInterface` class is responsible for initializing and managing the graphical user interface (GUI) components of the application.
 * It provides common methods that are used by various buttons and elements within the GUI, and it handles the overall interaction with the user.
 * The class also manages map loading, error checking, and event listeners related to the UI.
 */
@SuppressWarnings("BusyWait")
public class UserInterface extends JFrame {

    /** A list of all map difficulties, stored as `BeatSaberMap` objects. These maps are manipulated and displayed within the UI.*/
    public List<BeatSaberMap> map = new ArrayList<>();
    /** The pattern used to generate the map. If no pattern is specified, a default pattern is used.*/
    public Pattern pattern;
    /** The topmost, invisible button that serves as a parent for all global buttons. This button cannot be pressed and provides a category for "default/global" buttons. All child buttons of this button should be visible from the start.*/
    public GlobalButton globalButton;
    /** A label that displays the current status of whether the map is successfully loaded.*/
    public final JLabel labelMapDiff;
    /** A text pane that acts as a log, displaying important events within the GUI.*/
    public final StatusCheckTextPane statusCheck;
    /** A flag representing the loading status of the map. When set to true, all button categories will be set to visible.*/
    public boolean mapSuccessfullyLoaded = false;
    /** A slider used to set the variance of the pattern. It should only be applied to a deep-cloned pattern when generating a map.*/
    public static int patternVariance = 0;
    /** The current difficulty used by the algorithm to determine where to place parity breaks.*/
    public static String currentDiff;

    /**
     * Constructs and initializes the user interface, setting up all graphical elements and their respective event listeners.
     * A background thread is started to monitor the loading status of the map and update the visibility of UI components accordingly.
     */
    public UserInterface()  {

        //<editor-fold desc="Initialize UI Elements">
        //////////////////////////////
        //  Initialize UI Elements  //
        //////////////////////////////

        final UIElements uiElements = new UIElements(DARK_MODE, this);
        uiElements.initialize();

        labelMapDiff = uiElements.labelMapDiff();
        statusCheck = uiElements.statusTextArea();
        GuiAppender.setUserInterface(this);
        JCheckBox ignoreDDsCheckBox = uiElements.ignoreDDsCheckbox();

        globalButton = new GlobalButton(this);
        globalButton.init();
        GlobalSaveMapAs saveMapButton = new GlobalSaveMapAs(this);
        GlobalOpenMapInBrowser openMapInBrowserButton = new GlobalOpenMapInBrowser(this);
        MyGlobalJSlider globalPatternVarianceJSlider = new GlobalPatternVarianceJSlider(this);

        MapCreatorButton showMapCreatorButton = new MapCreatorButton(this);
        AdvancedMapCreatorButton advancedMapCreatorButton = new AdvancedMapCreatorButton(this);
        MyButton toTimingNotes = new ToTimingNotesButton(this);
        MyButton utilsMapUtilsButton = new MapUtilitiesButton(this);
        //</editor-fold desc="Initialize UI Elements">

        //<editor-fold desc="Event Listener">
        /////////////////////
        //  Event Listener //
        /////////////////////

        //global
        statusCheck.append("config: \nverbose: " + verbose + "\npath to WIP-Folder: " + DEFAULT_PATH + "\ndark mode:" + DARK_MODE + "\nsave new maps to WIP folder (default path): " + saveNewMapsToDefaultPath + "\n\n");
        ignoreDDsCheckBox.addActionListener(e -> statusCheck.append("\n[INFO]: ignore DDs: " + (ignoreDDs = ignoreDDsCheckBox.isSelected())));
        //</editor-fold desc="Event Listener">


        //<editor-fold desc="Thread">
        new Thread(() -> {
            boolean success = false;
            while (true) {
                if (mapSuccessfullyLoaded) {
                    // Make it so that the logger only logs once when the map was successfully loaded
                    if (!success) {
//                        logger.info("Successfully loaded difficulty");
                        labelMapDiff.setBackground(Color.GREEN);
                        logger.info("Set all Buttons to visible");
                        success = true;
                    }
                }
                else
                    success = false;

                showMapCreatorButton.setVisible(mapSuccessfullyLoaded);
                advancedMapCreatorButton.setVisible(mapSuccessfullyLoaded);
                toTimingNotes.setVisible(mapSuccessfullyLoaded);
                utilsMapUtilsButton.setVisible(mapSuccessfullyLoaded);

                saveMapButton.setVisible(mapSuccessfullyLoaded);
                openMapInBrowserButton.setVisible(mapSuccessfullyLoaded);
                globalPatternVarianceJSlider.setVisible(mapSuccessfullyLoaded);
                try {
                    Thread.sleep(1000); // Check for changes every second
                }
                catch (InterruptedException e) {
                    System.err.println("Interrupted Thread LUL");
                    logger.fatal("Interrupted Button-Active-Check Thread from the UserInterface! Shutting down... Cause: {}", e.getMessage());
                    logger.fatal(Arrays.toString(e.getStackTrace()));

                    this.dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
                    throw new RuntimeException("Interrupted Button-Active-Check Thread from the UserInterface!");
                }
            }
        }).start();
        //</editor-fold desc="Thread">
    }

    /**
     * Manages the map by performing initial setup tasks such as clearing obstacles and events,
     * loading the default pattern if none is specified, and resetting the current difficulty.
     * It also redirects the error stream to the `statusCheck` text area.
     */
    public void manageMap() {
        //Clear errors before loading processing another map
        PARITY_ERRORS_LIST.keySet().forEach(k-> PARITY_ERRORS_LIST.get(k).clear());
        if (pattern == null) {
            logger.info("Patterns have not been specified. Proceeding with default patterns");
            pattern = new Pattern(DEFAULT_PATTERN_METADATA);
        }
//        map.forEach(m -> m._obstacles = new Obstacle[0]);
        UserInterface.currentDiff = "NULL";
        // map._events = new Events[0]; //Don't remove events because bpm changes are stored in events
        // map._events = Arrays.stream(map._events).filter(event -> event._type == 1000).toArray(Events[]::new); //remove all events EXCEPT for the bpm-changes!
    }

    /**
     * Converts detected parity errors into bookmarks that can be used within the map editor.
     * If the `SAVE_PARITY_ERRORS_AS_BOOKMARKS_WILL_OVERWRITE_BOOKMARKS` flag is set, existing bookmarks will be cleared before adding new ones.
     *
     * @param diffName The name of the difficulty for which to create bookmarks.
     * @return A list of `Bookmark` objects representing the parity errors.
     */
    public List<Bookmark> parityErrorsAsBookmarks(String diffName) {
        if (SAVE_PARITY_ERRORS_AS_BOOKMARKS_WILL_OVERWRITE_BOOKMARKS) this.map.forEach(b -> b.bookmarks = new ArrayList<>());

        List<Bookmark> bookmarks = new ArrayList<>();

        for (Pair<Float, ParityErrorEnum> err : PARITY_ERRORS_LIST.get(diffName)) {
            float[] color = new float[3];
            color[0] = PARITY_ERRORS_COLORS_MAP.get(err.getValue()).getRed();
            color[1] = PARITY_ERRORS_COLORS_MAP.get(err.getValue()).getGreen();
            color[2] = PARITY_ERRORS_COLORS_MAP.get(err.getValue()).getBlue();

            Bookmark b = new Bookmark(err.getKey(), err.getValue().toString(), color);
            bookmarks.add(b);
        }

        PARITY_ERRORS_LIST.get(UserInterface.currentDiff).clear();
        return bookmarks;
    }

    /**
     * Checks the map for errors and prints any detected issues to the `statusCheck` text area.
     * This method is typically used to ensure the integrity of the map before saving or exporting it.
     *
     * @param map The `BeatSaberMap` to check for errors.
     */
    public static void checkMap(BeatSaberMap map) {
        List<Note> notes = new ArrayList<>();
        Collections.addAll(notes, map._notes);

        CheckParity.checkAndFixBasicMappingErrors(notes, false);
        logger.warn("There have been {} mapping errors", PARITY_ERRORS_LIST.get(UserInterface.currentDiff).size());
    }
}
