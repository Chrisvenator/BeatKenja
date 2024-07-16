package UserInterface;

import BeatSaberObjects.Objects.BeatSaberMap;
import BeatSaberObjects.Objects.Note;
import BeatSaberObjects.Objects.Obstacle;
import DataManager.*;
import MapGeneration.GenerationElements.*;

import static DataManager.Parameters.*;

import MapGeneration.GenerationElements.Exceptions.NoteNotValidException;
import MapGeneration.PatternGeneration.CommonMethods.CheckParity;
import UserInterface.Elements.Buttons.*;
import UserInterface.Elements.Buttons.ButtonTypes.GlobalButtons.Buttons.*;
import UserInterface.Elements.Buttons.ButtonTypes.*;
import UserInterface.Elements.JSlider.GlobalJSlider.GlobalPatternVarianceJSlider;
import UserInterface.Elements.JSlider.MyGlobalJSlider;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.util.ArrayList;
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
    public static float patternVariance;


    // Redirect the standard error stream to the custom PrintStream
    public final PrintStream ORIGINAL_ERR = System.err;
    public final ByteArrayOutputStream OUTPUT_STREAM = new ByteArrayOutputStream();
    public final PrintStream ERROR_PRINT_STREAM = new PrintStream(OUTPUT_STREAM);

    public UserInterface() throws NoteNotValidException {
        //loading config:
        loadConfig();
        if (verbose) System.setErr(ERROR_PRINT_STREAM);
        pattern = new Pattern(String.valueOf(useDatabase ? DEFAULT_PATTERN_METADATA : DEFAULT_PATTERN_PATH));
        System.out.println(pattern);


        //<editor-fold desc="Initialize UI Elements">
        //////////////////////////////
        //  Initialize UI Elements  //
        //////////////////////////////

        final UIElements uiElements = new UIElements(darkMode, this);
        uiElements.initialize();

        labelMapDiff = uiElements.labelMapDiff();
        statusCheck = uiElements.statusTextArea();
        JCheckBox ignoreDDsCheckBox = uiElements.ignoreDDsCheckbox();

        new GlobalButton(this);
        GlobalSaveMapAs saveMapButton = new GlobalSaveMapAs(this);
        GlobalOpenMapInBrowser openMapInBrowserButton = new GlobalOpenMapInBrowser(this);
        MyGlobalJSlider globalPatternVarianceJSlider = new GlobalPatternVarianceJSlider(this);

        MapCreatorButton showMapCreatorButton = new MapCreatorButton(this);
        AdvancedMapCreatorButton advancedMapCreatorButton = new AdvancedMapCreatorButton(this);
        MyButton toTimingNotes = new ToTimingNotesButton(this);
        MyButton utilsMapUtilsButton = new MapUtilitiesButton(this);
        MyButton waveGeneratorButton = new WaveGenerationButton(this);
        //</editor-fold desc="Initialize UI Elements">

        //<editor-fold desc="Event Listener">
        /////////////////////
        //  Event Listener //
        /////////////////////

        //global
        statusCheck.append("config: \nverbose: " + verbose + "\npath: " + DEFAULT_PATH + "\ndark mode:" + darkMode + "\nsave new maps to WIP folder (default path): " + saveNewMapsToDefaultPath + "\n\n");
        ignoreDDsCheckBox.addActionListener(e -> statusCheck.append("\n[INFO]: ignore DDs: " + (ignoreDDs = ignoreDDsCheckBox.isSelected())));
        //</editor-fold desc="Event Listener">

        //<editor-fold desc="Thread">
        new Thread(() -> {
            while (true) {
                if (mapSuccessfullyLoaded) {
                    labelMapDiff.setText("Successfully loaded difficulty");
                    labelMapDiff.setBackground(Color.GREEN);


                    showMapCreatorButton.setVisible(true);
                    advancedMapCreatorButton.setVisible(true);
                    toTimingNotes.setVisible(true);
                    utilsMapUtilsButton.setVisible(true);
                    waveGeneratorButton.setVisible(true);

                    saveMapButton.setVisible(true);
                    openMapInBrowserButton.setVisible(true);
                    globalPatternVarianceJSlider.setVisible(true);
                }
                try {
                    Thread.sleep(1000); // Check for changes every second
                } catch (InterruptedException e) {
                    System.err.println("Interrupted Thread LUL");
                    e.printStackTrace();
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

        PrintStream ORIGINAL_ERR = System.err;
        ByteArrayOutputStream OUTPUT_STREAM = new ByteArrayOutputStream();
        PrintStream ERROR_PRINT_STREAM = new PrintStream(OUTPUT_STREAM);
        // Redirect the standard error stream to the custom PrintStream
        System.setErr(ERROR_PRINT_STREAM);

        if (pattern == null) {
            statusCheck.setText(statusCheck.getText() + "\n[INFO]: Patterns have not been specified. Proceeding with default patterns");
            pattern = new Pattern(DEFAULT_PATTERN_METADATA);
            if (verbose) statusCheck.setText(statusCheck.getText() + "\n patterns: " + pattern.toString());
        }
        map._obstacles = new Obstacle[0];
        //map._events = new Events[0]; //Dont remove events because bpm changes are stored in events
        //map._events = Arrays.stream(map._events).filter(event -> event._type == 1000).toArray(Events[]::new); //remove all events EXCEPT for the bpm-changes!


        String errorOutput = OUTPUT_STREAM.toString();
        ERROR_PRINT_STREAM.close();

        System.setErr(ORIGINAL_ERR);
        System.err.println(errorOutput);
        statusCheck.append("\n" + errorOutput);
    }

    /**
     * Checks the map for errors and prints them to the statusCheck text area.
     */
    public void checkMap() {
        List<Note> notes = new ArrayList<>();
        Collections.addAll(notes, map._notes);

        System.setErr(ERROR_PRINT_STREAM);
        CheckParity.checkForMappingErrors(notes, false);
        changeBackOutput();
    }

    /**
     * Changes the error stream back to the original one and prints the error output to the statusCheck text area.
     */
    public void changeBackOutput() {
        String errorOutput = OUTPUT_STREAM.toString();
        ERROR_PRINT_STREAM.close();
        System.setErr(ORIGINAL_ERR);

        System.err.println(errorOutput);
        errorOutput = errorOutput.replaceAll("\n\n", "\n");
        if (errorOutput.isEmpty()) statusCheck.append("[INFO]: No Errors detected");
        statusCheck.append("\n" + errorOutput + "\n");
        if (verbose) System.setErr(ERROR_PRINT_STREAM);
    }

    //If you want to add more configs:
    public static void loadConfig() {
        List<String> config = FileManager.readFile(CONFIG_FILE_LOCATION);
        if (config != null && !config.isEmpty()) {
            for (String s : config) {
                String[] splits = s.split(":");
                if (s.contains("defaultPath")) DEFAULT_PATH = splits[1] + ":" + splits[2].trim();
                if (s.contains("defaultPath") && s.contains("//")) DEFAULT_PATH = splits[1] + ":" + splits[2].substring(0, splits[2].indexOf("//")).trim();
            }
            verbose = config.toString().contains("verbose:true");
            darkMode = config.toString().contains("dark-mode:true");
            saveNewMapsToDefaultPath = config.toString().contains("save_new_maps_to_default_path:true") && new File(DEFAULT_PATH).exists() && new File(DEFAULT_PATH).isDirectory();
        }
    }
}
