import com.google.gson.Gson;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.util.*;
import java.util.List;

public class UserInterface extends JFrame {

    private String filePath;
    private BeatSaberMap map;
    private Pattern pattern;
    private float bpm = 120;

    //config.txt:
    private boolean verbose = true; //For debugging purposes. It prints EVERYTHING
    private String DEFAULT_PATH = "C:/Program Files/Steam/steamapps/common/Beat Saber/Beat Saber_Data/CustomWIPLevels";

    private final JLabel labelMapDiff;
    private final JButton openMapButton;
    private final TextArea statusCheck;
    private boolean mapSuccessfullyLoaded = false;


    // Redirect the standard error stream to the custom PrintStream
    private final PrintStream ORIGINAL_ERR = System.err;
    private final ByteArrayOutputStream OUTPUT_STREAM = new ByteArrayOutputStream();
    private final PrintStream ERROR_PRINT_STREAM = new PrintStream(OUTPUT_STREAM);

    public static void main(String[] args) {
        CreateAllNecessaryDIRsAndFiles.main();

        UserInterface ui = new UserInterface();
        ui.setVisible(true);
    }

    public UserInterface() {
        //loading config:
        readConfig();
        if (verbose) System.setErr(ERROR_PRINT_STREAM);

        // Einstellungen für das Fenster
        setTitle("Beat Kenja");
        setSize(1200, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(null);


        labelMapDiff = new JLabel("Choose map difficulty file: ");
        labelMapDiff.setBounds(50, 20, 200, 30);
        add(labelMapDiff);

        //Load Patterns from file
        loadPatterns();


        openMapButton = new JButton("click here and select your desired difficulty");
        openMapButton.setBounds(200, 20, 100, 30);
        openMapButton.addActionListener(e -> loadMap());
        openMapButton.setBackground(Color.cyan);
        add(openMapButton);


        //BPM Buttons are currently disabled!
        JTextField bpmTextField = new JFormattedTextField("BPM");
        bpmTextField.setBounds(50, 70, 100, 30);
        bpmTextField.setVisible(false);
        add(bpmTextField);


        // Button erstellen und positionieren
        //BPM Buttons are currently disabled!
        JButton submitBPM = new JButton("save BPM");
        submitBPM.setBounds(160, 70, 100, 30);
        submitBPM.setVisible(false);
        submitBPM.addActionListener(e -> {
            try {
                bpm = Float.parseFloat(bpmTextField.getText());
            } catch (NumberFormatException ex) {
                System.err.println("The BPM you typed in is not a number!");
            }
        });
        add(submitBPM);


        //Status Bar:
        statusCheck = new TextArea("config: \nverbose: " + verbose + "\npath: " + DEFAULT_PATH + "\n");
        statusCheck.setBounds(50, 235, 1090, 510);
        statusCheck.setBackground(Color.WHITE);
        statusCheck.setEditable(false);
        add(statusCheck);


        //TODO: Here is an error!
        //Save Map Button:
        JButton saveMap = new JButton("SAVE MAP");
        saveMap.setBounds(750, 50, 150, 50);
        saveMap.setBackground(Color.green);
        saveMap.setVisible(false);
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

                statusCheck.setText(statusCheck.getText() + "\nINFO: Map saved successfully: " + filePath);
                System.out.println("Map saved successfully: " + map.exportAsMap());
                if (verbose)
                    statusCheck.setText(statusCheck.getText() + "\n" + "VERBOSE: " + "Map saved successfully: " + map.exportAsMap());
            } catch (IOException ioException) {
                statusCheck.setText(statusCheck.getText() + "\nERROR: There was an error while saving the map " + filePath + "!");
                ioException.printStackTrace();
            }
        });
        add(saveMap);

        JButton mapChecks = new JButton("Map Checks");
        mapChecks.setBounds(50, 200, 190, 30);
        mapChecks.setVisible(false);
        mapChecks.addActionListener(e -> checkMap());
        add(mapChecks);


        //
        //
        //
        //
        //////////////
        //MAP UTILS///
        //////////////

        JButton mapUtils = new JButton("Map Utilities");
        mapUtils.setBounds(250, 200, 190, 30);
        mapUtils.setVisible(false);
        add(mapUtils);

        JButton mapUtilsFixPlacements = new JButton("Fix Placements");
        JTextField fixPlacementTextField = new JFormattedTextField(16);
        mapUtilsFixPlacements.setBounds(250, 180, 145, 15);
        fixPlacementTextField.setBounds(400, 180, 40, 15);
        mapUtilsFixPlacements.setVisible(false);
        fixPlacementTextField.setVisible(false);
        add(mapUtilsFixPlacements);
        add(fixPlacementTextField);

        JButton mapUtilsMakeOneHanded = new JButton("Delete Note Type");
        JTextField makeOneHandDeleteType = new JFormattedTextField(0);
        mapUtilsMakeOneHanded.setBounds(250, 160, 145, 15);
        makeOneHandDeleteType.setBounds(400, 160, 40, 15);
        mapUtilsMakeOneHanded.setVisible(false);
        makeOneHandDeleteType.setVisible(false);
        add(mapUtilsMakeOneHanded);
        add(makeOneHandDeleteType);

        JButton mapUtilsConvertAllFlashingLight = new JButton("Convert All FlashingLight");
        mapUtilsConvertAllFlashingLight.setBounds(250, 140, 190, 15);
        mapUtilsConvertAllFlashingLight.setVisible(false);
        add(mapUtilsConvertAllFlashingLight);

        JButton mapUtilsMakeIntoNoArrowMap = new JButton("Make into no arrow map");
        mapUtilsMakeIntoNoArrowMap.setBounds(250, 120, 190, 15);
        mapUtilsMakeIntoNoArrowMap.setVisible(false);
        add(mapUtilsMakeIntoNoArrowMap);


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
            map.fixPlacements((double) 1 / Integer.parseInt(fixPlacementTextField.getText()));
            statusCheck.setText(statusCheck.getText() + "\nINFO: Fixed Note Placement with a precision of 1/" + fixPlacementTextField.getText() + " of a beat.");
            System.out.println("Placements fixed: " + new BeatSaberMap(map._notes).exportAsMap());
            if (verbose)
                statusCheck.setText(statusCheck.getText() + "\n" + "VERBOSE: " + "Placements fixed: " + new BeatSaberMap(map._notes).exportAsMap());

        });
        mapUtilsMakeOneHanded.addActionListener(e -> {
            map.makeOneHanded(Integer.parseInt(makeOneHandDeleteType.getText()));
            statusCheck.setText(statusCheck.getText() + "\nINFO: Removed All Notes with type: " + makeOneHandDeleteType.getText());
            System.out.println("One handed diff: : " + new BeatSaberMap(map._notes).exportAsMap());
            if (verbose)
                statusCheck.setText(statusCheck.getText() + "\n" + "VERBOSE: " + "One handed diff: : " + new BeatSaberMap(map._notes).exportAsMap());
        });
        mapUtilsConvertAllFlashingLight.addActionListener(e -> {
            map.convertAllFlashLightsToOnLights();
            statusCheck.setText(statusCheck.getText() + "\nINFO: Removed flashing lights");
            System.out.println("flashing lights removed: " + new BeatSaberMap(map._notes).exportAsMap());
            if (verbose)
                statusCheck.setText(statusCheck.getText() + "\n" + "VERBOSE: " + "flashing lights removed: " + new BeatSaberMap(map._notes).exportAsMap());
        });
        mapUtilsMakeIntoNoArrowMap.addActionListener(e -> {
            map.makeNoArrows();
            BeatSaberMap m = new BeatSaberMap(map._notes);
            m._obstacles = map._obstacles;
            m._events = map._events;
            statusCheck.setText(statusCheck.getText() + "\nINFO: Map is now a no arrows map");
            System.out.println("No Arrow Map: " + m.exportAsMap());
            if (verbose)
                statusCheck.setText(statusCheck.getText() + "\n" + "VERBOSE: " + "No Arrow Map: " + m.exportAsMap());
        });

        //
        //
        //
        //
        //
        //
        /////////////////////////
        //Timing Note Generator//
        /////////////////////////

        JButton toTimingNotes = new JButton("Map to timing Notes");
        toTimingNotes.setBounds(450, 200, 190, 30);
        toTimingNotes.setVisible(false);
        add(toTimingNotes);

        JButton toBlueOnlyTimingNotes = new JButton("To 1 color timing notes");
        toBlueOnlyTimingNotes.setBounds(450, 180, 190, 15);
        toBlueOnlyTimingNotes.setVisible(false);
        add(toBlueOnlyTimingNotes);

        JButton toStackedTimingNotes = new JButton("To 2 color timing notes");
        toStackedTimingNotes.setBounds(450, 160, 190, 15);
        toStackedTimingNotes.setVisible(false);
        add(toStackedTimingNotes);

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
            BeatSaberMap b = new BeatSaberMap(map._notes);
            b.toBlueLeftBottomRowDotTimings();
            map = b;
            System.out.println("Normal timing notes: " + b.exportAsMap());
            statusCheck.setText(statusCheck.getText() + "\nINFO: Successfully converted Map to only blue timing notes");
            if (verbose)
                statusCheck.setText(statusCheck.getText() + "\n" + "VERBOSE: " + "Normal timing notes: " + b.exportAsMap());
        });
        toStackedTimingNotes.addActionListener(e -> {
            map.toTimingNotes();
            System.out.println();
            statusCheck.setText(statusCheck.getText() + "\nNOTE: It is very likely that this feature is broken! Use at your own risk!");
            statusCheck.setText(statusCheck.getText() + "\nINFO: Successfully converted Map to timing notes");
            if (verbose)
                statusCheck.setText(statusCheck.getText() + "\n" + "VERBOSE: " + "Stacked timing notes: " + map.exportAsMap());
        });


        //
        //
        //
        //
        //
        //
        ///////////////
        //Map Creator//
        ///////////////

        JButton mapCreator = new JButton("Map creator");
        mapCreator.setBounds(650, 200, 190, 30);
        mapCreator.setVisible(false);
        add(mapCreator);

        JButton mapCreatorCreateMap = new JButton("Create Map");
        mapCreatorCreateMap.setBounds(650, 180, 190, 15);
        mapCreatorCreateMap.setVisible(false);
        add(mapCreatorCreateMap);

        JButton mapCreatorCreateComplexMap = new JButton("Create Complex Map");
        mapCreatorCreateComplexMap.setBounds(650, 160, 190, 15);
        mapCreatorCreateComplexMap.setVisible(false);
        add(mapCreatorCreateComplexMap);

        JButton mapCreatorCreateLinearMap = new JButton("Create Linear Map");
        mapCreatorCreateLinearMap.setBounds(650, 140, 190, 15);
        mapCreatorCreateLinearMap.setVisible(false);
        add(mapCreatorCreateLinearMap);

        JButton mapCreatorCreateBlueLinearMap = new JButton("one handed simpl linear");
        mapCreatorCreateBlueLinearMap.setBounds(650, 120, 90, 15);
        mapCreatorCreateBlueLinearMap.setVisible(false);
        add(mapCreatorCreateBlueLinearMap);

        JButton mapCreatorCreateBlueComplexMap = new JButton("complex");
        mapCreatorCreateBlueComplexMap.setBounds(750, 120, 90, 15);
        mapCreatorCreateBlueComplexMap.setVisible(false);
        add(mapCreatorCreateBlueComplexMap);


        mapCreator.addActionListener(e -> {
            mapCreatorCreateMap.setVisible(!mapCreatorCreateMap.isVisible());
            mapCreatorCreateComplexMap.setVisible(!mapCreatorCreateComplexMap.isVisible());
            mapCreatorCreateLinearMap.setVisible(!mapCreatorCreateLinearMap.isVisible());
            mapCreatorCreateBlueLinearMap.setVisible(!mapCreatorCreateBlueLinearMap.isVisible());
            mapCreatorCreateBlueComplexMap.setVisible(!mapCreatorCreateBlueComplexMap.isVisible());
        });

        mapCreatorCreateMap.addActionListener(e -> {
            manageMap();
            map.toBlueLeftBottomRowDotTimings();

            try {
                // Redirect the standard error stream to the custom PrintStream
                System.setErr(ERROR_PRINT_STREAM);


                String exported = map.exportAsMap();
                System.out.println("og: " + exported);
                if (verbose) statusCheck.setText(statusCheck.getText() + "\n" + "VERBOSE: " + "og: " + exported);
                map = CreatePatterns.createMap(map, pattern, false, false);

                if (exported.equals(map.exportAsMap()) || map.exportAsMap().split("\"_cutDirection\":8").length >= 20) {
                    statusCheck.setText(statusCheck.getText() + "\n" + "ERROR! Something went wrong while creating the map... Try another diff. If this error still continues then contact the creator of this tool");
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
                if (verbose)
                    statusCheck.setText(statusCheck.getText() + "\n" + "VERBOSE: " + "Created Map: " + map.exportAsMap());
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
                calculateNewMap.stop();
                throw new IllegalArgumentException("Took too long lol");
            });

            try {
                watchForInfiniteLoop.start();
                calculateNewMap.start();
                watchForInfiniteLoop.stop();
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
                calculateNewMap.stop();
                throw new IllegalArgumentException("Took too long lol");
            });

            try {
                watchForInfiniteLoop.start();
                calculateNewMap.start();
                watchForInfiniteLoop.stop();
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
                if (verbose)
                    statusCheck.setText(statusCheck.getText() + "\n" + "VERBOSE: " + "Created Map: " + new BeatSaberMap(map._notes).exportAsMap());
                checkMap();
            } catch (IllegalArgumentException ex) {
                statusCheck.setText(statusCheck.getText() + "\nThere was an error while creating. Please try again!");
            }
        });


        // Monitor changes in the variable and update frame visibility accordingly
        new Thread(() -> {
            while (true) {
                if (mapSuccessfullyLoaded) {
//                    bpmTextField.setVisible(true);
//                    submitBPM.setVisible(true);

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
            statusCheck.setText(statusCheck.getText() + "\nINFO: Patterns have not been specified. Proceeding with default patterns");
            BeatSaberMap patterMap = new Gson().fromJson(FileManager.readFile("PatternTemplates/Template--ISeeFire.txt").get(0), BeatSaberMap.class);
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

    public void loadPatterns() {
        JButton loadPatternButton = new JButton("Load Patterns File");
        loadPatternButton.setBounds(270, 70, 200, 30);
        loadPatternButton.addActionListener(action -> {

            JFileChooser fileChooser = new JFileChooser(DEFAULT_PATH);
            int option = fileChooser.showOpenDialog(this);

            if (option == JFileChooser.APPROVE_OPTION) {
                try {
                    Scanner scanner = new Scanner(fileChooser.getSelectedFile());
                    String mapAsString = scanner.nextLine();

                    BeatSaberMap beatSaberMap = new Gson().fromJson(mapAsString, BeatSaberMap.class);
                    pattern = new Pattern(beatSaberMap._notes, 1);

                    statusCheck.setText(statusCheck.getText() + "\nINFO: Successfully loaded Patterns");
                    loadPatternButton.setBackground(Color.green);
                } catch (FileNotFoundException e) {
                    labelMapDiff.setText(statusCheck.getText() + "\nERROR: File Not found!");
                    System.err.println(e.getMessage());
                } catch (Exception e) {
                    System.err.println("ERROR: Map probably has the wrong format: \n" + e);
                    labelMapDiff.setText(statusCheck.getText() + "\nERROR: There was an error while importing the patterns!");
                }
            }
        });
        add(loadPatternButton);
    }

    public void loadMap() {
        JFileChooser fileChooser = new JFileChooser(DEFAULT_PATH);
        int option = fileChooser.showOpenDialog(this);

        if (option == JFileChooser.APPROVE_OPTION) {
            filePath = fileChooser.getCurrentDirectory().toString();
            try {
                Scanner scanner = new Scanner(fileChooser.getSelectedFile());

                String mapAsString = scanner.nextLine();

                this.map = new Gson().fromJson(mapAsString, BeatSaberMap.class);
                this.map.originalJSON = mapAsString;

                statusCheck.setText("Successfully loaded Map");
                mapSuccessfullyLoaded = true;
            } catch (FileNotFoundException e) {
                System.err.println(e.getMessage());
            } catch (Exception e) {
                System.err.println("ERROR: Map probably has the wrong format: \n" + e);
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
        if (errorOutput.length() == 0) statusCheck.setText(statusCheck.getText() + "\nINFO: No Errors detected");
        statusCheck.setText(statusCheck.getText() + "\n" + errorOutput + "\n");
        if (verbose) System.setErr(ERROR_PRINT_STREAM);
    }

    //If you want to add more configs:
    public void readConfig() {
        List<String> config = FileManager.readFile("./config.txt");
        if (config != null && config.size() >= 1) {
            for (String s : config) {
                String[] splits = s.split(":");
                if (s.contains("verbose")) verbose = splits[1].contains("true");
                if (s.contains("defaultPath")) DEFAULT_PATH = splits[1] + ":" + splits[2];
            }
        }
    }
}
