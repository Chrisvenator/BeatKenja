package UserInterface;

import BeatSaberObjects.Objects.BeatSaberMap;
import BeatSaberObjects.Objects.Bookmark;
import BeatSaberObjects.Objects.Enums.ParityErrorEnum;
import BeatSaberObjects.Objects.Note;
import BeatSaberObjects.Objects.Obstacle;
import DataManager.Logger.GuiAppender;
import MapGeneration.GenerationElements.Exceptions.NoteNotValidException;
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
import static DataManager.Parameters.DEFAULT_PATTERN_PATH;
import static DataManager.Parameters.PARITY_ERRORS_COLORS_MAP;
import static DataManager.Parameters.PARITY_ERRORS_LIST;
import static DataManager.Parameters.SAVE_PARITY_ERRORS_AS_BOOKMARKS_WILL_OVERWRITE_BOOKMARKS;
import static DataManager.Parameters.ignoreDDs;
import static DataManager.Parameters.logger;
import static DataManager.Parameters.saveNewMapsToDefaultPath;
import static DataManager.Parameters.useDatabase;
import static DataManager.Parameters.verbose;

/**
 * UserInterface is the class that is responsible for initializing all graphical elements.
 * It is also contains common methods that are being used by its buttons.
 */
@SuppressWarnings("BusyWait")
public class UserInterface extends JFrame {

    /** "map" are all map difficulties. They are stored as BeatSaberMaps in a List. */
    public List<BeatSaberMap> map = new ArrayList<>();
    /** "pattern" is the pattern with which the map will be created.*/
    public Pattern pattern;

    /** This is the most topmost button that exists. It can not be pressed and is invisible. It is used so that every "global" button has a button to inherit from. It is also used so that the "default/global" buttons have a "category". Every child of this button should be visible from the start.*/
    public GlobalButton globalButton;
    /** "labelMapDiff" is used to display the current status of if the map is successfully loaded. */
    public final JLabel labelMapDiff;
    /** "statusCheck" is essentially the log. It logs important events into the Graphical User Interface. */
    public final StatusCheckTextPane statusCheck;
    /** "mapSuccessfullyLoaded" represents the loading status. When set to true, all button categories will be set to visible. */
    public boolean mapSuccessfullyLoaded = false;
    /** "patternVariance" is used to set the variance of the pattern. It should only be applied on a deep-cloned pattern when generating a map. */
    public static int patternVariance = 0;
    /** "currentDiff" is used to tell the algorithm in which Difficulty to place the parity breaks. */
    public static String currentDiff;

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
     * Redirects the error stream to the statusCheck text area, <br>
     * Removes all Obstacles and Events from the map, <br>
     * Checks if the pattern is set and if not, loads the default pattern.<br>
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
     * Checks the map for errors and prints them to the statusCheck text area.
     */
    public static void checkMap(BeatSaberMap map) {
        List<Note> notes = new ArrayList<>();
        Collections.addAll(notes, map._notes);

        CheckParity.checkAndFixBasicMappingErrors(notes, false);
        logger.warn("There have been {} mapping errors", PARITY_ERRORS_LIST.get(UserInterface.currentDiff).size());
    }
}
