package UserInterface;

import BeatSaberObjects.BeatSaberMap;
import BeatSaberObjects.Events;
import BeatSaberObjects.Note;
import BeatSaberObjects.Obstacle;
import DataManager.CreateAllNecessaryDIRsAndFiles;
import DataManager.FileManager;
import MapGeneration.BatchWavToMaps;
import MapGeneration.CreatePatterns;
import MapGeneration.GenerationElements.Pattern;

import static DataManager.Parameters.*;

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

@SuppressWarnings("ALL")
public class UserInterface extends JFrame {

    private BeatSaberMap map;
    private Pattern pattern;


    //GUI:

    private final JLabel labelMapDiff;
    private final JButton openMapButton;
    private final JButton openSongButton;
    public final TextArea statusCheck; //essentially the log
    private boolean mapSuccessfullyLoaded = false;


    // Redirect the standard error stream to the custom PrintStream
    private final PrintStream ORIGINAL_ERR = System.err;
    private final ByteArrayOutputStream OUTPUT_STREAM = new ByteArrayOutputStream();
    private final PrintStream ERROR_PRINT_STREAM = new PrintStream(OUTPUT_STREAM);

    public UserInterface() {
        final UIElements uiElements;

        //loading config:
        loadConfig();
        if (verbose) System.setErr(ERROR_PRINT_STREAM);

        //////////////////////////////
        //  Initialize UI Elements  //
        //////////////////////////////

        //Global Elements
        uiElements = new UIElements(darkMode, this);
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


        //Map Utilities
        JButton mapUtils = uiElements.mapUtils();
        JButton mapUtilsFixPlacements = uiElements.mapUtilsFixPlacements();
        JTextField fixPlacementTextField = uiElements.fixPlacementTextField(PLACEMENT_PRECISION);
        JButton mapUtilsMakeOneHanded = uiElements.mapUtilsMakeOneHanded();
        JTextField makeOneHandDeleteType = uiElements.makeOneHandDeleteType();
        JButton mapUtilsConvertAllFlashingLight = uiElements.mapUtilsConvertAllFlashingLight();
        JButton mapUtilsMakeIntoNoArrowMap = uiElements.mapUtilsMakeIntoNoArrowMap();

        //Timing Note Generator
        JButton toTimingNotes = uiElements.toTimingNotes();
        JButton toBlueOnlyTimingNotes = uiElements.toBlueOnlyTimingNotes();
        JButton toStackedTimingNotes = uiElements.toStackedTimingNotes();

        //Map Creator
        JButton mapCreator = uiElements.mapCreator();
        JButton mapCreatorCreateMap = uiElements.mapCreatorCreateMap();
        JButton mapCreatorCreateComplexMap = uiElements.mapCreatorCreateComplexMap();
        JButton mapCreatorCreateRandomV2Map = uiElements.mapCreatorCreateRandomV2Map();
        JButton mapCreatorCreateLinearMap = uiElements.mapCreatorCreateLinearMap();
        JButton mapCreatorCreateBlueLinearMap = uiElements.mapCreatorCreateBlueLinearMap();
        JButton mapCreatorCreateBlueComplexMap = uiElements.mapCreatorCreateBlueComplexMap();
        JButton mapCreatorCreateRandomMap = uiElements.mapCreatorCreateRandomMap();


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

        //Map Utilities
        mapUtils.addActionListener(e -> {
            if (mapUtilsFixPlacements.isVisible()) {
                mapUtilsFixPlacements.setVisible(false);
                mapUtilsMakeOneHanded.setVisible(false);
                mapUtilsConvertAllFlashingLight.setVisible(false);
                fixPlacementTextField.setVisible(false);
                makeOneHandDeleteType.setVisible(false);
                mapUtilsMakeIntoNoArrowMap.setVisible(false);
            } else {
                mapUtilsFixPlacements.setVisible(true);
                mapUtilsMakeOneHanded.setVisible(true);
                mapUtilsConvertAllFlashingLight.setVisible(true);
                fixPlacementTextField.setVisible(true);
                makeOneHandDeleteType.setVisible(true);
                mapUtilsMakeIntoNoArrowMap.setVisible(true);
            }
        });
        mapUtilsFixPlacements.addActionListener(e -> {
            map.fixPlacements((double) 1 / Integer.parseInt(fixPlacementTextField.getText().replaceAll("[^\\d.]", "")));
            statusCheck.setText(statusCheck.getText() + "\n[INFO]: Fixed Note Placement with a precision of 1/" + fixPlacementTextField.getText() + " of a beat.");
            System.out.println("Placements fixed: " + new BeatSaberMap(map._notes).exportAsMap());
            if (verbose)
                statusCheck.setText(statusCheck.getText() + "\n" + "VERBOSE: " + "Placements fixed: " + new BeatSaberMap(map._notes).exportAsMap());

        });
        mapUtilsMakeOneHanded.addActionListener(e -> {
            map.makeOneHanded(Integer.parseInt(makeOneHandDeleteType.getText()));
            statusCheck.setText(statusCheck.getText() + "\n[INFO]: Removed All Notes with type: " + makeOneHandDeleteType.getText());
            System.out.println("One handed diff: : " + new BeatSaberMap(map._notes).exportAsMap());
            if (verbose) statusCheck.setText(statusCheck.getText() + "\n" + "VERBOSE: " + "One handed diff: : " + new BeatSaberMap(map._notes).exportAsMap());
        });
        mapUtilsConvertAllFlashingLight.addActionListener(e -> {
            map.convertAllFlashLightsToOnLights();
            statusCheck.setText(statusCheck.getText() + "\n[INFO]: Removed flashing lights");
            System.out.println("flashing lights removed: " + new BeatSaberMap(map._notes).exportAsMap());
            if (verbose) statusCheck.setText(statusCheck.getText() + "\n" + "VERBOSE: " + "flashing lights removed: " + new BeatSaberMap(map._notes).exportAsMap());
        });
        mapUtilsMakeIntoNoArrowMap.addActionListener(e -> {
            map.makeNoArrows();
            statusCheck.setText(statusCheck.getText() + "\n[INFO]: Map is now a no arrows map");
            System.out.println("No Arrow Map: " + map.exportAsMap());
            if (verbose) statusCheck.setText(statusCheck.getText() + "\n" + "VERBOSE: " + "No Arrow Map: " + map.exportAsMap());
        });

        //Timing Note Generator
        toTimingNotes.addActionListener(e -> {
            if (toBlueOnlyTimingNotes.isVisible()) {
                toBlueOnlyTimingNotes.setVisible(false);
                toStackedTimingNotes.setVisible(false);
            } else {
                toBlueOnlyTimingNotes.setVisible(true);
                toStackedTimingNotes.setVisible(true);
            }
        });
        toBlueOnlyTimingNotes.addActionListener(e -> {
            map = new BeatSaberMap(map._notes);
            System.out.println("Normal timing notes: " + map.exportAsMap());
            statusCheck.setText(statusCheck.getText() + "\n[INFO]: Successfully converted Map to only blue timing notes");
            if (verbose) statusCheck.setText(statusCheck.getText() + "\n" + "VERBOSE: " + "Normal timing notes: " + map.exportAsMap());
        });
        toStackedTimingNotes.addActionListener(e -> {
            map.toTimingNotes();
            System.out.println();
            statusCheck.setText(statusCheck.getText() + "\nNOTE: It is very likely that this feature is broken! Use at your own risk!");
            statusCheck.setText(statusCheck.getText() + "\n[INFO]: Successfully converted Map to timing notes");
            if (verbose) statusCheck.setText(statusCheck.getText() + "\n" + "VERBOSE: " + "Stacked timing notes: " + map.exportAsMap());
        });

        //Map Creator
        mapCreator.addActionListener(e -> {
            mapCreatorCreateMap.setVisible(!mapCreatorCreateMap.isVisible());
            mapCreatorCreateComplexMap.setVisible(!mapCreatorCreateComplexMap.isVisible());
            mapCreatorCreateLinearMap.setVisible(!mapCreatorCreateLinearMap.isVisible());
            mapCreatorCreateBlueLinearMap.setVisible(!mapCreatorCreateBlueLinearMap.isVisible());
            mapCreatorCreateBlueComplexMap.setVisible(!mapCreatorCreateBlueComplexMap.isVisible());
            mapCreatorCreateRandomMap.setVisible(!mapCreatorCreateRandomMap.isVisible());
            mapCreatorCreateRandomV2Map.setVisible(!mapCreatorCreateRandomV2Map.isVisible());

        });
        mapCreatorCreateMap.addActionListener(e -> {
            manageMap();
            map.toBlueLeftBottomRowDotTimings();

            try {
                // Redirect the standard error stream to the custom PrintStream so that errors can be printed to the UI
                System.setErr(ERROR_PRINT_STREAM);


                String exported = map.exportAsMap();
                System.out.println("og: " + exported);
                if (verbose) statusCheck.setText(statusCheck.getText() + "\n" + "VERBOSE: " + "og: " + exported);
                map = CreatePatterns.createMap(map, pattern, false, false);

                if (exported.equals(map.exportAsMap()) || map.exportAsMap().split("\"_cutDirection\":8").length >= 20) {
                    statusCheck.setText(statusCheck.getText() + "\n" + "[ERROR]! Something went wrong while creating the map... Try another diff. If this error still continues then contact the creator of this tool");
                }

                //change back the error outputs
                changeBackOutput();

                statusCheck.setText(statusCheck.getText() + "\nMap creation finished");
                System.out.println("Created map: " + new BeatSaberMap(map._notes).exportAsMap());
                if (verbose)
                    statusCheck.setText(statusCheck.getText() + "\n" + "VERBOSE: " + "Created map: " + new BeatSaberMap(map._notes).exportAsMap());
            } catch (IllegalArgumentException ex) {
                statusCheck.setText(statusCheck.getText() + "\nThere was an error while creating. Please try again!");
                System.err.println(ex.getMessage());
                changeBackOutput();
            }


        });
        mapCreatorCreateComplexMap.addActionListener(e -> {
            manageMap();
            map.toBlueLeftBottomRowDotTimings();

            try {
                String ogJson = map.originalJSON;
                map = new BeatSaberMap(CreatePatterns.complexPatternFromTemplate(map._notes, pattern, false, false, null, null));
                map.originalJSON = ogJson;
                statusCheck.setText(statusCheck.getText() + "\nMap creation finished");
                System.out.println("Created Map: " + map.exportAsMap());
                if (verbose) statusCheck.setText(statusCheck.getText() + "\n" + "VERBOSE: " + "Created Map: " + map.exportAsMap());
                checkMap();
            } catch (IllegalArgumentException ex) {
                statusCheck.setText(statusCheck.getText() + "\nThere was an error while creating. Please try again!");
            }


        });
        mapCreatorCreateLinearMap.addActionListener(e -> {
            //DO NOT QUESTION THIS SECTION
            //IT WAS NECESSARY TO ENSURE THAT THERE IS NO INFINITE LOOP
            manageMap();
            map.toBlueLeftBottomRowDotTimings();

            Thread calculateNewMap = new Thread(() -> {
                String ogJson = map.originalJSON;

                map = new BeatSaberMap(CreatePatterns.linearSlowPattern(map._notes, false, null, null));
                map.originalJSON = ogJson;
                statusCheck.setText(statusCheck.getText() + "\nMap creation finished");
                System.out.println("Created Map: " + map.exportAsMap());
                if (verbose) statusCheck.setText(statusCheck.getText() + "\n" + "VERBOSE: " + "Created Map: " + map.exportAsMap());
                checkMap();
            });
            Thread watchForInfiniteLoop = new Thread(() -> {
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
                calculateNewMap.interrupt();
                throw new IllegalArgumentException("Took too long lol");
            });

            try {
                watchForInfiniteLoop.start();
                calculateNewMap.start();
                watchForInfiniteLoop.interrupt();
            } catch (IllegalArgumentException ex) {
                statusCheck.setText(statusCheck.getText() + "\nThere was an error while creating. Please try again!");
            }
        });
        mapCreatorCreateBlueLinearMap.addActionListener(e -> {
            //DO NOT QUESTION THIS SECTION
            //IT WAS NECESSARY TO ENSURE THAT THERE IS NO INFINITE LOOP
            manageMap();
            map.toBlueLeftBottomRowDotTimings();

            Thread calculateNewMap = new Thread(() -> {
                String ogJson = map.originalJSON;
                map = new BeatSaberMap(CreatePatterns.linearSlowPattern(map._notes, true, null, null));
                map.originalJSON = ogJson;
                statusCheck.setText(statusCheck.getText() + "\nMap creation finished");
                System.out.println("Created Map: " + map.exportAsMap());
                if (verbose)
                    statusCheck.setText(statusCheck.getText() + "\n" + "VERBOSE: " + "Created Map: " + map.exportAsMap());
                checkMap();
            });
            Thread watchForInfiniteLoop = new Thread(() -> {
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
                calculateNewMap.interrupt();
                throw new IllegalArgumentException("Took too long lol");
            });

            try {
                watchForInfiniteLoop.start();
                calculateNewMap.start();
                watchForInfiniteLoop.interrupt();
            } catch (IllegalArgumentException ex) {
                statusCheck.setText(statusCheck.getText() + "\nThere was an error while creating. Please try again!");
            }
        });
        mapCreatorCreateBlueComplexMap.addActionListener(e -> {
            manageMap();
            map.toBlueLeftBottomRowDotTimings();

            try {
                String ogJson = map.originalJSON;
                map = new BeatSaberMap(CreatePatterns.complexPatternFromTemplate(map._notes, pattern, true, false, null, null));
                map.originalJSON = ogJson;

                statusCheck.setText(statusCheck.getText() + "\nMap creation finished");
                System.out.println("Created Map: " + new BeatSaberMap(map._notes).exportAsMap());
                if (verbose) statusCheck.setText(statusCheck.getText() + "\n" + "VERBOSE: " + "Created Map: " + new BeatSaberMap(map._notes).exportAsMap());
                checkMap();
            } catch (IllegalArgumentException ex) {
                statusCheck.setText(statusCheck.getText() + "\nThere was an error while creating. Please try again!");
            }
        });
        mapCreatorCreateRandomMap.addActionListener(e -> {
            manageMap();
            map.toBlueLeftBottomRowDotTimings();
            String ogJson = map.originalJSON;

            try {
                map = new BeatSaberMap(CreatePatterns.createRandomPattern(map._notes, false));
                map.originalJSON = ogJson;

                System.out.println("Created Map: " + new BeatSaberMap(map._notes).exportAsMap());
                statusCheck.setText(statusCheck.getText() + "\nMap creation finished");
                if (verbose) statusCheck.setText(statusCheck.getText() + "\n" + "VERBOSE: " + "Created Map: " + new BeatSaberMap(map._notes).exportAsMap());
                statusCheck.setText(statusCheck.getText() + "\nThere will be a lot of errors. But that's what you wanted lol");
            } catch (IllegalArgumentException ex) {
                statusCheck.setText(statusCheck.getText() + "\nThere was an error while creating. Please try again!");
            }
        });
        mapCreatorCreateRandomV2Map.addActionListener(e -> {
            manageMap();
            map.toBlueLeftBottomRowDotTimings();

            try {
                String ogJson = map.originalJSON;
                map = new BeatSaberMap(CreatePatterns.randomV2FromTemplate(map._notes, pattern, false, null, null));
                map.originalJSON = ogJson;
                statusCheck.setText(statusCheck.getText() + "\nMap creation finished");
                System.out.println("Created Map: " + map.exportAsMap());
                if (verbose) statusCheck.setText(statusCheck.getText() + "\n" + "VERBOSE: " + "Created Map: " + map.exportAsMap());
                checkMap();
            } catch (IllegalArgumentException ex) {
                statusCheck.setText(statusCheck.getText() + "\nThere was an error while creating. Please try again!");
            }
        });


        // Monitor changes in the variable and update frame visibility accordingly
        new Thread(() -> {
            while (true) {
                if (mapSuccessfullyLoaded) {
                    labelMapDiff.setText("Successfully loaded difficulty");
                    labelMapDiff.setBackground(Color.GREEN);

                    openMapButton.setText("load an other diff");
                    openMapButton.setBounds(270, 20, 200, 30);
                    openMapButton.setBackground(Color.GREEN);

                    mapChecks.setVisible(true);
                    mapUtils.setVisible(true);
                    toTimingNotes.setVisible(true);
                    mapCreator.setVisible(true);
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
        JFileChooser fileChooser = new JFileChooser(DEFAULT_PATH);
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