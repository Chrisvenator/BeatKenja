package UserInterface;

import BeatSaberObjects.*;
import DataManager.*;
import MapGeneration.CreatePatterns;
import MapGeneration.GenerationElements.*;

import static DataManager.Parameters.*;

import UserInterface.Elements.Buttons.*;
import UserInterface.Elements.Buttons.ButtonTypes.GlobalButtons.*;
import UserInterface.Elements.Buttons.ButtonTypes.MapChecks.*;
import UserInterface.Elements.Buttons.ButtonTypes.MapCreator.*;
import UserInterface.Elements.Buttons.ButtonTypes.*;
import UserInterface.Elements.Buttons.ButtonTypes.MapUtilities.*;
import UserInterface.Elements.Buttons.ButtonTypes.TimingNoteGenerator.*;
import UserInterface.Elements.Buttons.ButtonTypes.WaveGeneration.WaveGenerationGenerateWave;
import UserInterface.Elements.TextFields.*;
import UserInterface.Elements.TextFields.GlobalTextFields.*;
import UserInterface.Elements.TextFields.TextFieldTypes.MapUtils.*;
import com.google.gson.Gson;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class UserInterface extends JFrame {

    public BeatSaberMap map;
    public Pattern pattern;


    //GUI:

    public final JLabel labelMapDiff;
    public final TextArea statusCheck; //essentially the log
    public boolean mapSuccessfullyLoaded = false;


    // Redirect the standard error stream to the custom PrintStream
    public final PrintStream ORIGINAL_ERR = System.err;
    public final ByteArrayOutputStream OUTPUT_STREAM = new ByteArrayOutputStream();
    public final PrintStream ERROR_PRINT_STREAM = new PrintStream(OUTPUT_STREAM);

    public UserInterface() {
        //loading config:
        loadConfig();
        if (verbose) System.setErr(ERROR_PRINT_STREAM);

        //////////////////////////////
        //  Initialize UI Elements  //
        //////////////////////////////

        //Global Elements
        final UIElements uiElements = new UIElements(darkMode, this);

        uiElements.initialize();

        labelMapDiff = uiElements.labelMapDiff();
        statusCheck = uiElements.statusTextArea();
        JCheckBox ignoreDDsCheckBox = uiElements.ignoreDDsCheckbox();


        //Global Buttons
        MyButton saveMapButton = new GlobalSaveMapAs(this);
        MyButton openMapInBrowserButton = new GlobalOpenMapInBrowser(this);
        MyButton openMapButton = new GlobalOpenMapButton(this);
        MyButton convertMP3ToMapsButton = new GlobalConvertMP3ToMaps(this);
        MyButton openSongFolderButton = new GlobalOpenFolder(this);
        MyButton loadPatternsButton = new GlobalLoadPatterns(this);
        MyButton openMapChecksButton = new MapChecks(this);
        MyGlobalTextField globalSeedFrame = new GlobalSeedFrame(this);


        //Map Creator
        MyButton showMapCreatorButton = new MapCreatorButton(this);
        MyButton createMapButton = new CreateMapButton(showMapCreatorButton);
        MyButton createComplexMapButton = new CreateComplexMap(showMapCreatorButton);
        MyButton createRandomV2MapButton = new CreateRandomV2Map(showMapCreatorButton);
        MyButton createLinearMapButton = new CreateLinearMap(showMapCreatorButton);
        MyButton createBlueLinearMap = new CreateBlueLinearMap(showMapCreatorButton);
        MyButton createBlueComplexMap = new CreateBlueComplexMap(showMapCreatorButton);
        MyButton createRandomMap = new CreateRandomMap(showMapCreatorButton);

        //Timing Note Generator
        MyButton toTimingNotes = new ToTimingNotesButton(this);
        MyButton toBlueOnlyTimingNotes = new ToBlueOnlyTimingNotes(toTimingNotes);
        MyButton toTwoColorTimingNotes = new ToTwoColorTimingNotes(toTimingNotes);

        //Map Utilities
        MyButton utilsMapUtilsButton = new MapUtilitiesButton(this);
        MyTextField utilsFixPlacementTextField = new UtilsFixPlacementsTextField(utilsMapUtilsButton);
        MyButton utilsFixPlacementButton = new UtilsFixPlacements(utilsMapUtilsButton, utilsFixPlacementTextField);
        MyButton utilsDeleteNoteTypeButton = new UtilsDeleteNoteType(utilsMapUtilsButton, utilsFixPlacementTextField);
        MyButton utilsConvertAllFlashingLightButton = new UtilsConvertAllFlashingLight(utilsMapUtilsButton);
        MyTextField utilsDeleteNoteTypeTextField = new UtilsDeleteNoteTypeTextField(utilsMapUtilsButton);
        MyButton utilsMakeNoArrowMapButton = new UtilsMakeIntoNoArrowMap(utilsMapUtilsButton);

        //Wave Generator
        MyButton waveGeneratorButton = new WaveGenerationButton(this);
        MyButton waveGeneratorCreateWaveButton = new WaveGenerationGenerateWave(waveGeneratorButton);

        /////////////////////
        //  Event Listener //
        /////////////////////

        //global
        statusCheck.setText(statusCheck.getText() + "config: \nverbose: " + verbose + "\npath: " + DEFAULT_PATH + "\ndark mode:" + darkMode + "\nsave new maps to WIP folder (default path): " + saveNewMapsToDefaultPath + "\n\n");
        ignoreDDsCheckBox.addActionListener(e -> {
            ignoreDDs = ignoreDDsCheckBox.isSelected();
            ignoreDDs = ignoreDDsCheckBox.isSelected();
            statusCheck.setText(statusCheck.getText() + "\n[INFO]: ignore DDs: " + ignoreDDs);
        });

        new Thread(() -> {
            while (true) {
                if (mapSuccessfullyLoaded) {
                    labelMapDiff.setText("Successfully loaded difficulty");
                    labelMapDiff.setBackground(Color.GREEN);


                    showMapCreatorButton.setVisible(true);
                    toTimingNotes.setVisible(true);
                    utilsMapUtilsButton.setVisible(true);
                    waveGeneratorButton.setVisible(true);

                    saveMapButton.setVisible(true);
                    openMapInBrowserButton.setVisible(true);
                }
                try {
                    Thread.sleep(1000); // Check for changes every second
                } catch (InterruptedException e) {
                    System.err.println("Interrupted Thread LUL");
                    e.printStackTrace();
                }
            }
        }).start();
    }


    public void manageMap() {

        PrintStream ORIGINAL_ERR = System.err;
        ByteArrayOutputStream OUTPUT_STREAM = new ByteArrayOutputStream();
        PrintStream ERROR_PRINT_STREAM = new PrintStream(OUTPUT_STREAM);
        // Redirect the standard error stream to the custom PrintStream
        System.setErr(ERROR_PRINT_STREAM);

        if (pattern == null) {
            statusCheck.setText(statusCheck.getText() + "\n[INFO]: Patterns have not been specified. Proceeding with default patterns");
            BeatSaberMap patterMap = new Gson().fromJson(FileManager.readFile(DEFAULT_PATTERN_TEMPLATE).get(0), BeatSaberMap.class);
            pattern = new Pattern(patterMap._notes, 1);
            if (verbose) statusCheck.setText(statusCheck.getText() + "\n patterns: " + pattern.toString());
        }
        map._obstacles = new Obstacle[0];
        map._events = new Events[0];


        String errorOutput = OUTPUT_STREAM.toString();
        ERROR_PRINT_STREAM.close();

        System.setErr(ORIGINAL_ERR);
        System.err.println(errorOutput);
        statusCheck.setText(statusCheck.getText() + "\n" + errorOutput);
    }

    public void checkMap() {
        List<Note> notes = new ArrayList<>();
        Collections.addAll(notes, map._notes);

        System.setErr(ERROR_PRINT_STREAM);
        CreatePatterns.checkForMappingErrors(notes, false);
        changeBackOutput();
    }

    public void changeBackOutput() {
        String errorOutput = OUTPUT_STREAM.toString();

        ERROR_PRINT_STREAM.close();

        System.setErr(ORIGINAL_ERR);
        System.err.println(errorOutput);
        errorOutput = errorOutput.replaceAll("\n\n", "\n");
        if (errorOutput.length() == 0) statusCheck.setText(statusCheck.getText() + "[INFO]: No Errors detected");
        statusCheck.setText(statusCheck.getText() + "\n" + errorOutput + "\n");
        if (verbose) System.setErr(ERROR_PRINT_STREAM);
    }

    //If you want to add more configs:
    public static void loadConfig() {
        List<String> config = FileManager.readFile(CONFIG_FILE_LOCATION);
        if (config != null && config.size() >= 1) {
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
