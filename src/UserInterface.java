import com.google.gson.Gson;

import javax.swing.*;
import javax.swing.text.StyledDocument;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.util.*;
import java.util.List;

public class UserInterface extends JFrame {

    private BeatSaberMap map;
    private float bpm = 120;

    private final JLabel labelMapDiff;
    private final JButton openMapButton;
    private final TextArea statusCheck;
    private boolean mapSuccessfullyLoaded = false;

    public static void main(String[] args) {
        UserInterface ui = new UserInterface();
        ui.setVisible(true);
    }

    public UserInterface() {
        // Einstellungen für das Fenster
        setTitle("Beat Kenja");
        setSize(1000, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(null);


        JLabel beatKenja = new JLabel("Beat Kenja!");
        beatKenja.setBounds(50, 20, 100, 30);
        add(beatKenja);
        beatKenja.getParent().setBackground(Color.lightGray);


        labelMapDiff = new JLabel("Choose map difficulty file: ");
        labelMapDiff.setBounds(50, 50, 200, 30);
        add(labelMapDiff);


        openMapButton = new JButton("click here and select your desired difficulty");
        openMapButton.setBounds(200, 50, 100, 30);
        openMapButton.addActionListener(e -> loadMap());
        openMapButton.setBackground(Color.cyan);
        add(openMapButton);


        JTextField bpmTextField = new JFormattedTextField("BPM");
        bpmTextField.setBounds(50, 100, 100, 30);
        bpmTextField.setVisible(false);
        add(bpmTextField);


        // Button erstellen und positionieren
        JButton submitBPM = new JButton("save BPM");
        submitBPM.setBounds(160, 100, 100, 30);
        submitBPM.setVisible(false);
        submitBPM.addActionListener(e -> {
            try {
                bpm = Float.parseFloat(bpmTextField.getText());
            } catch (NumberFormatException ex) {
                System.err.println("The BPM you typed in is not a number!");
            }
        });
        add(submitBPM);

        statusCheck = new TextArea("Status:Nothing here yet.");
        statusCheck.setBounds(50, 235, 890, 310);
        statusCheck.setEditable(false);
        add(statusCheck);

        JButton mapChecks = new JButton("Map Checks");
        mapChecks.setBounds(50, 200, 190, 30);
        add(mapChecks);

        //////////////
        //MAP UTILS///
        //////////////

        JButton mapUtils = new JButton("Map Utilities");
        mapUtils.setBounds(250, 200, 190, 30);
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


        mapUtils.addActionListener(e -> {
            if (mapUtilsFixPlacements.isVisible()) {
                mapUtilsFixPlacements.setVisible(false);
                mapUtilsMakeOneHanded.setVisible(false);
                mapUtilsConvertAllFlashingLight.setVisible(false);
                fixPlacementTextField.setVisible(false);
                makeOneHandDeleteType.setVisible(false);
            } else {
                mapUtilsFixPlacements.setVisible(true);
                mapUtilsMakeOneHanded.setVisible(true);
                mapUtilsConvertAllFlashingLight.setVisible(true);
                fixPlacementTextField.setVisible(true);
                makeOneHandDeleteType.setVisible(true);
            }
        });
        mapUtilsFixPlacements.addActionListener(e -> {
            map.fixPlacements((double) 1 / Integer.parseInt(fixPlacementTextField.getText()));
            statusCheck.setText(statusCheck.getText() + "\nINFO: Fixed Note Placement with a precision of 1/" + fixPlacementTextField.getText() + " of a beat.");
        });
        mapUtilsMakeOneHanded.addActionListener(e -> {
            map.makeOneHanded(Integer.parseInt(makeOneHandDeleteType.getText()));
            statusCheck.setText(statusCheck.getText() + "\nINFO: Removed All Notes with type: " + makeOneHandDeleteType.getText());
        });
        mapUtilsConvertAllFlashingLight.addActionListener(e -> {
            map.convertAllFlashLightsToOnLights();
            statusCheck.setText(statusCheck.getText() + "\nINFO: Removed flashing lights");
        });

        /////////////////////////
        //Timing Note Generator//
        /////////////////////////

        JButton toTimingNotes = new JButton("Map to timing Notes");
        toTimingNotes.setBounds(450, 200, 190, 30);
        add(toTimingNotes);
        //Both versions

        ///////////////
        //Map Creator//
        ///////////////

        JButton mapCreator = new JButton("Map creator");
        mapCreator.setBounds(650, 200, 190, 30);
        add(mapCreator);

        mapChecks.setVisible(false);
        mapUtils.setVisible(false);
        toTimingNotes.setVisible(false);
        mapCreator.setVisible(false);

        mapChecks.addActionListener(e -> {
            List<Note> notes = new ArrayList<>();
            Collections.addAll(notes, map._notes);

            PrintStream originalErr = System.err;

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            PrintStream errorPrintStream = new PrintStream(outputStream);

            // Redirect the standard error stream to the custom PrintStream
            System.setErr(errorPrintStream);

            CreatePatterns.checkForMappingErrors(notes, false);
            String errorOutput = outputStream.toString();

            errorPrintStream.close();

            System.setErr(originalErr);
            System.err.println(errorOutput);
            if (errorOutput.length() == 0) statusCheck.setText("No Errors detected");
            statusCheck.setText(statusCheck.getText() + "\n" + errorOutput);
        });


        // Monitor changes in the variable and update frame visibility accordingly
        new Thread(() -> {
            while (true) {
                if (mapSuccessfullyLoaded) {
                    bpmTextField.setVisible(true);
                    submitBPM.setVisible(true);

                    labelMapDiff.setText("Successfully loaded difficulty");
                    labelMapDiff.setBackground(Color.GREEN);

                    openMapButton.setText("load an other diff");
                    openMapButton.setBounds(270, 50, 200, 30);
                    openMapButton.setBackground(Color.GREEN);

                    mapChecks.setVisible(true);
                    mapUtils.setVisible(true);
                    toTimingNotes.setVisible(true);
                    mapCreator.setVisible(true);
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

    public void loadMap() {
        JFileChooser fileChooser = new JFileChooser();
        int option = fileChooser.showOpenDialog(this);

        if (option == JFileChooser.APPROVE_OPTION) {
            try {
                Scanner scanner = new Scanner(fileChooser.getSelectedFile());

                String mapAsString = scanner.nextLine();

                this.map = new Gson().fromJson(mapAsString, BeatSaberMap.class);
                this.map.originalJSON = mapAsString;
                System.out.println(map.exportAsMap());

                statusCheck.setText("");
                mapSuccessfullyLoaded = true;
            } catch (FileNotFoundException e) {
                System.err.println(e);
            } catch (Exception e) {
                System.err.println("ERROR: Map probably has the wrong format: \n" + e);
                labelMapDiff.setText("There was an error while importing the map!");
                openMapButton.setBounds(320, 50, 300, 30);
                labelMapDiff.setBounds(100, 50, 300, 30);
                openMapButton.setBackground(Color.RED);
                mapSuccessfullyLoaded = false;
            }
        }
    }
}
