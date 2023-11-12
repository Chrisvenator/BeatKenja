package UserInterface;

import BeatSaberObjects.*;
import DataManager.*;
import MapGeneration.*;
import MapGeneration.GenerationElements.*;

import static DataManager.Parameters.*;

import UserInterface.Elements.Buttons.*;
import UserInterface.Elements.Buttons.ButtonTypes.MapCreator.*;
import UserInterface.Elements.Buttons.ButtonTypes.*;
import UserInterface.Elements.Buttons.ButtonTypes.MapUtilities.*;
import UserInterface.Elements.Buttons.ButtonTypes.TimingNoteGenerator.*;
import UserInterface.Elements.Buttons.ButtonTypes.WaveGeneration.WaveGenerationGenerateWave;
import UserInterface.Elements.TextFields.*;
import UserInterface.Elements.TextFields.TextFieldTypes.MapUtils.*;
import com.google.gson.Gson;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class UserInterface extends JFrame {

    public BeatSaberMap map;
    public Pattern pattern;


    //GUI:

    private final JLabel labelMapDiff;
    private final JButton openMapButton;
    private final JButton openSongButton;
    public final TextArea statusCheck; //essentially the log
    private boolean mapSuccessfullyLoaded = false;


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
        openMapButton = uiElements.openMapButton();
        openSongButton = uiElements.openSongButton();
        JButton openSongFolderButton = uiElements.openSongFolderButton();
        statusCheck = uiElements.statusTextArea();

        JButton saveMap = uiElements.saveMapButton();
        JButton mapChecks = uiElements.mapChecks();
        JButton loadPatternButton = uiElements.loadPatternsButton();
        TextField seedFrame = uiElements.seedFrame();
        JCheckBox ignoreDDsCheckBox = uiElements.ignoreDDsCheckbox();
        JButton openMapInBrowser = uiElements.openMapInBrowser();

        ///////////////////OBJEKTORIENTIERTE BUTTONS////////////////////////

        //Map Creator //TODO: Objektorientiert machen
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
        openMapButton.addActionListener(e -> loadMap());
        openSongButton.addActionListener(e -> convertMp3ToMap());
        openSongFolderButton.addActionListener(e -> {
            try {
                Desktop.getDesktop().open(new File(ONSET_GENERATION_FOLDER_PATH_INPUT));
            } catch (IOException ex) {
                statusCheck.append("\n[ERROR]: Couldn't open the folder: " + ONSET_GENERATION_FOLDER_PATH_INPUT);
            }
        });
        statusCheck.setText(statusCheck.getText() + "config: \nverbose: " + verbose + "\npath: " + DEFAULT_PATH + "\ndark mode:" + darkMode + "\nsave new maps to WIP folder (default path): " + saveNewMapsToDefaultPath + "\n\n");
        saveMap.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser(filePath);
            int option = fileChooser.showSaveDialog(saveMap);
            if (option != 0) return;
            try {
                String filePath = fileChooser.getSelectedFile().getAbsolutePath();
                filePath += filePath.contains(".dat") ? "" : ".dat";

                BufferedWriter bw = new BufferedWriter(new FileWriter(filePath));
                bw.write(new BeatSaberMap(map._notes).exportAsMap());
                bw.close();

                statusCheck.setText(statusCheck.getText() + "\n[INFO]: Map saved successfully: " + filePath);
                System.out.println("Map saved successfully: " + map.exportAsMap());
                if (verbose) statusCheck.setText(statusCheck.getText() + "\n" + "VERBOSE: " + "Map saved successfully: " + map.exportAsMap());
            } catch (IOException ioException) {
                statusCheck.setText(statusCheck.getText() + "\n[ERROR]: There was an error while saving the map " + filePath + "!");
                ioException.printStackTrace();
            }
        });
        mapChecks.addActionListener(e -> checkMap());
        loadPatternButton.addActionListener(action -> {

            JFileChooser fileChooser = new JFileChooser(DEFAULT_PATH);
            if (darkMode) fileChooser.setForeground(Color.white);
            int option = fileChooser.showOpenDialog(this);

            if (option == JFileChooser.APPROVE_OPTION) {
                try {
                    Scanner scanner = new Scanner(fileChooser.getSelectedFile());
                    String mapAsString = scanner.nextLine();

                    BeatSaberMap beatSaberMap = new Gson().fromJson(mapAsString, BeatSaberMap.class);
                    pattern = new Pattern(beatSaberMap._notes, 1);

                    statusCheck.setText(statusCheck.getText() + "\n[INFO]: Successfully loaded Patterns");
                    loadPatternButton.setBackground(Color.green);
                } catch (FileNotFoundException e) {
                    labelMapDiff.setText(statusCheck.getText() + "\n[ERROR]: File Not found!");
                    System.err.println(e.getMessage());
                } catch (Exception e) {
                    System.err.println("ERROR: Map probably has the wrong format: \n" + e);
                    labelMapDiff.setText(statusCheck.getText() + "\n[ERROR]: There was an error while importing the patterns!");
                }
            }
        });
        seedFrame.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {
                //Autogenerated. Not needed
            }

            @Override
            public void keyPressed(KeyEvent e) {
                //Autogenerated. Not needed
            }

            @Override
            public void keyReleased(KeyEvent e) {
                try {
                    if (seedFrame.getText().length() >= 18) throw new IllegalArgumentException();
                    SEED = Long.parseLong(seedFrame.getText().replace(" ", ""));
                    System.out.println("Current Seed: " + SEED);
                } catch (NumberFormatException ex) {
                    statusCheck.setText(statusCheck.getText() + "\n[ERROR]: Seed is not a number!");
                } catch (IllegalArgumentException ex) {
                    statusCheck.setText(statusCheck.getText() + "\n[ERROR]: Seed is too long! It must be at most 18 digits long!");
                }
            }
        });
        ignoreDDsCheckBox.addActionListener(e -> {
            ignoreDDs = ignoreDDsCheckBox.isSelected();
            ignoreDDs = ignoreDDsCheckBox.isSelected();
            statusCheck.setText(statusCheck.getText() + "\n[INFO]: ignore DDs: " + ignoreDDs);
        });
        openMapInBrowser.addActionListener(e -> {
//            String url = "https://allpoland.github.io/ArcViewer/";
//            String url = "https://skystudioapps.com/bs-viewer/";

            try {
                URI uri = new URI(mapViewerURL);

                if (Desktop.isDesktopSupported()) {
                    Desktop desktop = Desktop.getDesktop();

                    if (filePath != null && !filePath.equals("")) {
                        desktop.browse(uri);
                        desktop.open(new File(filePath));
                        createZipFileFromCurrentDifficulty();
                    }
                } else {
                    System.out.println("Map preview viewing is not supported on this platform.");
                    statusCheck.setText(statusCheck.getText() + "\n[ERROR]: Map preview viewing is not supported on this platform.");
                }
            } catch (IOException | URISyntaxException ex) {
                ex.printStackTrace();
                statusCheck.setText(statusCheck.getText() + "\n[ERROR]: Map preview viewing encountered an error! This feature is currently not available :/");
            }
        });

        new Thread(() -> {
            while (true) {
                if (mapSuccessfullyLoaded) {
                    labelMapDiff.setText("Successfully loaded difficulty");
                    labelMapDiff.setBackground(Color.GREEN);

                    openMapButton.setText("load an other diff");
                    openMapButton.setBounds(270, 20, 200, 30);
                    openMapButton.setBackground(Color.GREEN);

                    showMapCreatorButton.setVisible(true);
                    toTimingNotes.setVisible(true);
                    utilsMapUtilsButton.setVisible(true);
                    waveGeneratorButton.setVisible(true);

                    mapChecks.setVisible(true);
                    toTimingNotes.setVisible(true);
                    saveMap.setVisible(true);
                    openMapInBrowser.setVisible(true);
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

    private void createZipFileFromCurrentDifficulty() {
        String sourceDir = new File(filePath).getAbsolutePath();
        System.out.println(sourceDir);

        String zipFileName = sourceDir + "/output.zip";

        try {
            FileOutputStream fos = new FileOutputStream(zipFileName);
            ZipOutputStream zipOut = new ZipOutputStream(fos);

            byte[] buffer = new byte[1024];

            // Get a list of files in the source directory
            File dir = new File(sourceDir);
            File[] files = dir.listFiles();

            if (files == null || files.length <= 3) {
                statusCheck.setText(statusCheck.getText() + "\n[ERROR]: Something went wrong...");
                return;
            }

            for (File file : files) {
                if (file.getName().contains(".zip") || file.isDirectory()) continue;
                ZipEntry zipEntry = new ZipEntry(file.getName());
                zipOut.putNextEntry(zipEntry);

                FileInputStream fis = new FileInputStream(file);
                int length;
                while ((length = fis.read(buffer)) > 0) {
                    zipOut.write(buffer, 0, length);
                }
                fis.close();
            }

            zipOut.close();

            System.out.println("The files have been successfully added to " + zipFileName);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private void convertMp3ToMap() {
        if (!CreateAllNecessaryDIRsAndFiles.isFFMpegInstalled()) {
            statusCheck.setText(statusCheck.getText() + "\n[ERROR]: FFMpeg is not installed. Please install it and try again!");
            return;
        }

        if (!CreateAllNecessaryDIRsAndFiles.isPythonInstalled()) {
            statusCheck.setText(statusCheck.getText() + "\n[ERROR]: Python could not be found please ensure that it is installed and added to the PATH variable!");
            return;
        }


        try {
            statusCheck.setBackground(Color.gray);
            openSongButton.setText("In Progress...");
            statusCheck.setText(statusCheck.getText() + "\n[INFO]: Converting all Songs from \"" + ONSET_GENERATION_FOLDER_PATH_INPUT + "\" to timing maps... This might take a while if there are a lot of songs.\n");
            statusCheck.setText(statusCheck.getText() + "[INFO]: You can always check the progress when heading to \"" + ONSET_GENERATION_FOLDER_PATH_OUTPUT + "\"\n");

            try {
                List<File> files = new ArrayList<>(Arrays.stream(Objects.requireNonNull(new File(ONSET_GENERATION_FOLDER_PATH_INPUT).listFiles())).toList());
                files.removeIf(f -> !f.getName().endsWith(".mp3"));
                if (files.size() == 0) throw new Exception();
                statusCheck.setText(statusCheck.getText() + "[INFO]: Found " + files.size() + " MP3 Files in \"" + ONSET_GENERATION_FOLDER_PATH_INPUT + "\"\n\n");
            } catch (Exception e) {
                statusCheck.setText(statusCheck.getText() + "\n[INFO]: Found 0 MP3 Files! Please put your mp3 Files into th folder \"" + ONSET_GENERATION_FOLDER_PATH_INPUT + "\"\n\n");
                statusCheck.setBackground(darkMode ? Color.BLACK : Color.WHITE);
                openSongButton.setText("Convert MP3s to timing maps");
                return;
            }

            //generate Onsets
            Thread.sleep(1000);
            if (BatchWavToMaps.generateOnsets(ONSET_GENERATION_FOLDER_PATH_INPUT, ONSET_GENERATION_FOLDER_PATH_OUTPUT, true)) {
                statusCheck.setText(statusCheck.getText() + "\n[INFO]: Successfully created Map. You can find your map in \"" + ONSET_GENERATION_FOLDER_PATH_OUTPUT + "/\"\n\n");

                //Install dependencies if not already installed
            } else {
                statusCheck.setText(statusCheck.getText() + "\n[ERROR]: There was an error while creating the onsets. It is possible that a dependency is not installed. Please ensure that they are all installed and then try again!");
                statusCheck.setText(statusCheck.getText() + "\n[INFO]: Trying installing dependencies...\n\n");
                if (CreateAllNecessaryDIRsAndFiles.installDependencies()) {
                    statusCheck.setText(statusCheck.getText() + "\n[INFO]: Finished installing dependencies... Please press the button again.\n\n");
                } else statusCheck.setText(statusCheck.getText() + "\n[ERROR]: error while installing dependencies...");
            }

            statusCheck.setBackground(darkMode ? Color.BLACK : Color.WHITE);
            openSongButton.setText("Convert MP3s to timing maps");
        } catch (Exception e) {
            System.err.println("[ERROR]: Something went wrong during conversion. Is it the right file extension?\n" + e);
            statusCheck.setText("[ERROR]: Something went wrong during conversion. Is it the right file extension?\n" + e + "\n\n");
            openSongButton.setBounds(320, 20, 300, 30);
            openSongButton.setBackground(Color.RED);
        }
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

    public void loadMap() {
        JFileChooser fileChooser = new JFileChooser(DEFAULT_PATH.trim());
        if (darkMode) fileChooser.setForeground(Color.white);
        int option = fileChooser.showOpenDialog(this);

        if (option == JFileChooser.APPROVE_OPTION) {
            filePath = fileChooser.getCurrentDirectory().toString();
            try {
                Scanner scanner = new Scanner(fileChooser.getSelectedFile());

                String mapAsString = scanner.nextLine();

                this.map = new Gson().fromJson(mapAsString, BeatSaberMap.class);
                this.map.originalJSON = mapAsString;

                statusCheck.setText("Successfully loaded difficulty: \"" + fileChooser.getSelectedFile().getAbsolutePath() + "\"");
                mapSuccessfullyLoaded = true;
            } catch (FileNotFoundException e) {
                System.err.println(e.getMessage());
            } catch (Exception e) {
                System.err.println("[ERROR]: Map probably has the wrong format: \n" + e);
                labelMapDiff.setText("There was an error while importing the map!");
                openMapButton.setBounds(320, 20, 300, 30);
                labelMapDiff.setBounds(100, 20, 300, 30);
                openMapButton.setBackground(Color.RED);
                mapSuccessfullyLoaded = false;
            }
        }
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
                if (s.contains("defaultPath")) DEFAULT_PATH = splits[1] + ":" + splits[2];
                if (s.contains("defaultPath") && s.contains("//")) DEFAULT_PATH = splits[1] + ":" + splits[2].substring(0, splits[2].indexOf("//"));
            }
            verbose = config.toString().contains("verbose:true");
            darkMode = config.toString().contains("dark-mode:true");
            saveNewMapsToDefaultPath = config.toString().contains("save_new_maps_to_default_path:true") && new File(DEFAULT_PATH).exists() && new File(DEFAULT_PATH).isDirectory();
        }
    }
}
