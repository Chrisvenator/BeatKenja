package UserInterface;

import BeatSaberObjects.Objects.BeatSaberMap;
import BeatSaberObjects.Objects.Bookmark;
import BeatSaberObjects.Objects.Note;
import BeatSaberObjects.Objects.Obstacle;
import BeatSaberObjects.Objects.Parity.Enums.ParityErrorEnum;
import DataManager.Logger.GuiAppender;
import MapGeneration.GenerationElements.*;

import static DataManager.Parameters.*;

import MapGeneration.GenerationElements.Exceptions.NoteNotValidException;
import MapGeneration.PatternGeneration.CommonMethods.CheckParity;
import UserInterface.Elements.Buttons.*;
import UserInterface.Elements.Buttons.ButtonTypes.GlobalButtons.Buttons.*;
import UserInterface.Elements.Buttons.ButtonTypes.*;
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

@SuppressWarnings("BusyWait")
public class UserInterface extends JFrame {

    public BeatSaberMap map;
    public Pattern pattern;


    //GUI:
    public final JLabel labelMapDiff;
    public final TextArea statusCheck; //essentially the log
    public boolean mapSuccessfullyLoaded = false;
    public static int patternVariance = 0;


    public UserInterface() throws NoteNotValidException {
        //loading config:
        pattern = new Pattern(String.valueOf(useDatabase ? DEFAULT_PATTERN_METADATA : DEFAULT_PATTERN_PATH));


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

        new GlobalButton(this);
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
                        logger.info("Successfully loaded difficulty");
                        labelMapDiff.setText("Successfully loaded difficulty");
                        labelMapDiff.setBackground(Color.GREEN);
                        logger.info("Set all Buttons to visible");
                        success = true;
                    }
                } else success = false;


                showMapCreatorButton.setVisible(mapSuccessfullyLoaded);
                advancedMapCreatorButton.setVisible(mapSuccessfullyLoaded);
                toTimingNotes.setVisible(mapSuccessfullyLoaded);
                utilsMapUtilsButton.setVisible(mapSuccessfullyLoaded);

                saveMapButton.setVisible(mapSuccessfullyLoaded);
                openMapInBrowserButton.setVisible(mapSuccessfullyLoaded);
                globalPatternVarianceJSlider.setVisible(mapSuccessfullyLoaded);
                try {
                    Thread.sleep(1000); // Check for changes every second
                } catch (InterruptedException e) {
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
        PARITY_ERRORS_LIST.clear();
        if (pattern == null) {
            logger.info("Patterns have not been specified. Proceeding with default patterns");
            pattern = new Pattern(DEFAULT_PATTERN_METADATA);
        }
        map._obstacles = new Obstacle[0];
        // map._events = new Events[0]; //Don't remove events because bpm changes are stored in events
        // map._events = Arrays.stream(map._events).filter(event -> event._type == 1000).toArray(Events[]::new); //remove all events EXCEPT for the bpm-changes!
    }

    public List<Bookmark> parityErrorsAsBookmarks() {
        if (SAVE_PARITY_ERRORS_AS_BOOKMARKS_WILL_OVERWRITE_BOOKMARKS) this.map.bookmarks = new ArrayList<>();
        List<Bookmark> bookmarks = new ArrayList<>();

        System.out.println("Found the following Errors: ");
        for (Pair<Float, ParityErrorEnum> err : PARITY_ERRORS_LIST) {
            System.out.println(err.getKey() + ": " + err.getValue());

            float [] color = new float[3];
            color[0] = PARITY_ERRORS_COLORS_MAP.get(err.getValue()).getRed();
            color[1] = PARITY_ERRORS_COLORS_MAP.get(err.getValue()).getGreen();
            color[2] = PARITY_ERRORS_COLORS_MAP.get(err.getValue()).getBlue();

            Bookmark b = new Bookmark(err.getKey(), err.getValue().toString(), color);
            bookmarks.add(b);
        }

        PARITY_ERRORS_LIST.clear();
        return bookmarks;
    }


    /**
     * Checks the map for errors and prints them to the statusCheck text area.
     */
    public void checkMap() {
        List<Note> notes = new ArrayList<>();
        Collections.addAll(notes, map._notes);

        CheckParity.checkForMappingErrors(notes, false);
        logger.warn("There have been {} mapping errors", PARITY_ERRORS_LIST.size());
    }
}
