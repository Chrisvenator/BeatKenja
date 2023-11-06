package UserInterface;

import DataManager.CreateAllNecessaryDIRsAndFiles;
import DataManager.Parameters;
import static DataManager.Parameters.*;

import javax.swing.*;
import java.awt.*;

/**
 * This class contains all the UI element definitions that are used in the program.
 * It is used to keep the UI class clean and readable.
 * There is no logic contained in this class.
 */
public class UIElements {
    private final boolean darkMode;
    private final UserInterface userInterface;

    public UIElements(boolean darkMode, UserInterface userInterface) {
        this.darkMode = darkMode;
        this.userInterface = userInterface;
    }

    // Einstellungen für das Fenster
    public void initialize() {
        userInterface.setTitle("Beat Kenja");
        userInterface.setSize(1200, 800);
        userInterface.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        if (darkMode) userInterface.getContentPane().setBackground(darkModeBackgroundColor);
        if (darkMode) userInterface.getContentPane().setForeground(darkModeForegroundColor);
        userInterface.setLayout(null);
    }

    public JLabel labelMapDiff() {
        JLabel labelMapDiff = new JLabel("Choose map difficulty file: ");
        labelMapDiff.setBounds(50, 20, 200, 30);
        if (darkMode) labelMapDiff.setForeground(Color.white);
        userInterface.add(labelMapDiff);

        return labelMapDiff;
    }

    public JButton saveMapButton() {
        JButton saveMap = new JButton("SAVE MAP AS");
        saveMap.setBounds(750, 50, 150, 50);
        saveMap.setBackground(Color.green);
        saveMap.setVisible(false);
        userInterface.add(saveMap);


        return saveMap;
    }

    public TextField seedFrame() {
        JLabel seedLabel = new JLabel("Seed:");
        seedLabel.setBounds(960, 17, 40, 20);
        if (darkMode) seedLabel.setForeground(Color.white);
        userInterface.add(seedLabel);

        TextField seedFrame = new TextField(String.valueOf(Parameters.SEED));
        seedFrame.setBounds(1000, 20, 100, 20);
        if (darkMode) seedFrame.setBackground(Color.black);
        if (darkMode) seedFrame.setForeground(Color.white);
        userInterface.add(seedFrame);

        return seedFrame;
    }

    public JCheckBox ignoreDDsCheckbox() {
        JCheckBox ignoreDDsCheckbox = new JCheckBox("Ignore DDs");
        ignoreDDsCheckbox.setBounds(957, 45, 100, 20);
        if (darkMode) ignoreDDsCheckbox.setForeground(Color.white);
        userInterface.add(ignoreDDsCheckbox);

        return ignoreDDsCheckbox;
    }

    public JButton openMapInBrowser() {
        JButton openMapInBrowser = new JButton("Open Map in Browser");
        openMapInBrowser.setBounds(975, 70, 150, 50);
        openMapInBrowser.setBackground(Color.gray);
        openMapInBrowser.setVisible(false);
        userInterface.add(openMapInBrowser);

        return openMapInBrowser;
    }


    public JButton openMapButton() {
        JButton openMapButton = new JButton("click here and select your desired difficulty");
        openMapButton.setBounds(200, 20, 100, 30);
        openMapButton.setBackground(Color.cyan);
        userInterface.add(openMapButton);

        return openMapButton;
    }

    public JButton openSongButton() {
        JButton openSongButton = new JButton("Convert MP3s to timing maps");
        openSongButton.setBounds(500, 20, 200, 30);
        openSongButton.setBackground(Color.orange);
        openSongButton.setVisible(true);
        userInterface.add(openSongButton);

        return openSongButton;
    }

    public JButton openSongFolderButton() {
        JButton openSongFolderButton = new JButton("open folder");
        openSongFolderButton.setBounds(705, 22, 110, 26);
        openSongFolderButton.setBackground(darkMode ? new Color(175, 140, 59) : new Color(255, 212, 123));
        openSongFolderButton.setVisible(true);
        userInterface.add(openSongFolderButton);

        return openSongFolderButton;
    }

    public TextArea statusTextArea() {
        TextArea statusCheck = new TextArea();
        statusCheck.setBounds(50, 235, 1090, 510);
        statusCheck.setBackground(darkMode ? Color.BLACK : Color.WHITE);
        statusCheck.setForeground(darkMode ? Color.WHITE : Color.BLACK);
        statusCheck.setEditable(false);
        userInterface.add(statusCheck);

        if (!CreateAllNecessaryDIRsAndFiles.isPythonInstalled())
            statusCheck.setText(statusCheck.getText() + "[ERROR]: Python could not be found please ensure that it is installed and added to the PATH variable or else the Onset Generation will not work!\n");

        if (!CreateAllNecessaryDIRsAndFiles.isPipInstalled())
            statusCheck.setText(statusCheck.getText() + "[ERROR]: PIP could not be found please ensure that it is installed or else the Onset Generation will not work!\n");

        if (!CreateAllNecessaryDIRsAndFiles.isFFMpegInstalled())
            statusCheck.setText(statusCheck.getText() + "[ERROR]: FFMPEG could not be found please ensure that it is installed and added to the PATH variable or else the Onset Generation will not work!\n");

        if (CreateAllNecessaryDIRsAndFiles.isPythonInstalled() && CreateAllNecessaryDIRsAndFiles.isPipInstalled() && CreateAllNecessaryDIRsAndFiles.isFFMpegInstalled())
            statusCheck.setText(statusCheck.getText() + "[INFO]: All necessary dependencies are installed. You can now convert MP3s to timing maps.\n\n");

        return statusCheck;
    }

    public JButton mapChecks() {
        JButton mapChecks = new JButton("Map Checks");
        mapChecks.setBounds(50, 200, 190, 30);
        mapChecks.setVisible(false);
        userInterface.add(mapChecks);

        return mapChecks;
    }

    public JButton mapUtils() {
        JButton mapUtils = new JButton("Map Utilities");
        mapUtils.setBounds(250, 200, 190, 30);
        mapUtils.setVisible(false);
        userInterface.add(mapUtils);

        return mapUtils;
    }

    public JButton mapUtilsFixPlacements() {
        JButton mapUtilsFixPlacements = new JButton("Fix Placements");
        mapUtilsFixPlacements.setBounds(250, 180, 145, 15);
        mapUtilsFixPlacements.setVisible(false);
        userInterface.add(mapUtilsFixPlacements);

        return mapUtilsFixPlacements;
    }

    public JTextField fixPlacementTextField(double placementPrecision) {
        JTextField fixPlacementTextField = new JFormattedTextField(1 / placementPrecision);
        fixPlacementTextField.setBounds(400, 180, 40, 15);
        fixPlacementTextField.setVisible(false);
        userInterface.add(fixPlacementTextField);

        return fixPlacementTextField;
    }

    public JButton mapUtilsMakeOneHanded() {
        JButton mapUtilsMakeOneHanded = new JButton("Delete Note Type");
        mapUtilsMakeOneHanded.setBounds(250, 160, 145, 15);
        mapUtilsMakeOneHanded.setVisible(false);
        userInterface.add(mapUtilsMakeOneHanded);

        return mapUtilsMakeOneHanded;
    }

    public JTextField makeOneHandDeleteType() {
        JTextField makeOneHandDeleteType = new JFormattedTextField(0);
        makeOneHandDeleteType.setBounds(400, 160, 40, 15);
        makeOneHandDeleteType.setVisible(false);
        userInterface.add(makeOneHandDeleteType);

        return makeOneHandDeleteType;
    }

    public JButton mapUtilsConvertAllFlashingLight() {
        JButton mapUtilsConvertAllFlashingLight = new JButton("Convert All FlashingLight");
        mapUtilsConvertAllFlashingLight.setBounds(250, 140, 190, 15);
        mapUtilsConvertAllFlashingLight.setVisible(false);
        userInterface.add(mapUtilsConvertAllFlashingLight);

        return mapUtilsConvertAllFlashingLight;
    }

    public JButton mapUtilsMakeIntoNoArrowMap() {
        JButton mapUtilsMakeIntoNoArrowMap = new JButton("Make into no arrow map");
        mapUtilsMakeIntoNoArrowMap.setBounds(250, 120, 190, 15);
        mapUtilsMakeIntoNoArrowMap.setVisible(false);
        userInterface.add(mapUtilsMakeIntoNoArrowMap);

        return mapUtilsMakeIntoNoArrowMap;
    }

    public JButton toTimingNotes() {
        JButton toTimingNotes = new JButton("Map to timing Notes");
        toTimingNotes.setBounds(450, 200, 190, 30);
        toTimingNotes.setVisible(false);
        userInterface.add(toTimingNotes);

        return toTimingNotes;
    }

    public JButton toBlueOnlyTimingNotes() {
        JButton toBlueOnlyTimingNotes = new JButton("To 1 color timing notes");
        toBlueOnlyTimingNotes.setBounds(450, 180, 190, 15);
        toBlueOnlyTimingNotes.setVisible(false);
        userInterface.add(toBlueOnlyTimingNotes);

        return toBlueOnlyTimingNotes;
    }

    public JButton toStackedTimingNotes() {
        JButton toStackedTimingNotes = new JButton("To 2 color timing notes");
        toStackedTimingNotes.setBounds(450, 160, 190, 15);
        userInterface.add(toStackedTimingNotes);
        toStackedTimingNotes.setVisible(false);

        return toStackedTimingNotes;
    }

    public JButton mapCreator() {
        JButton mapCreator = new JButton("Map creator");
        mapCreator.setBounds(650, 200, 190, 30);
        mapCreator.setVisible(false);
        userInterface.add(mapCreator);

        return mapCreator;
    }

    public JButton mapCreatorCreateMap() {
        JButton mapCreatorCreateMap = new JButton("Create Map");
        mapCreatorCreateMap.setBounds(650, 180, 190, 15);
        mapCreatorCreateMap.setVisible(false);
        userInterface.add(mapCreatorCreateMap);

        return mapCreatorCreateMap;
    }

    public JButton mapCreatorCreateComplexMap() {
        JButton mapCreatorCreateComplexMap = new JButton("Complex");
        mapCreatorCreateComplexMap.setBounds(650, 160, 190, 15);
        mapCreatorCreateComplexMap.setVisible(false);
        userInterface.add(mapCreatorCreateComplexMap);

        return mapCreatorCreateComplexMap;
    }


    public JButton mapCreatorCreateLinearMap() {
        JButton mapCreatorCreateLinearMap = new JButton("Create Linear Map");
        mapCreatorCreateLinearMap.setBounds(650, 140, 190, 15);
        mapCreatorCreateLinearMap.setVisible(false);
        userInterface.add(mapCreatorCreateLinearMap);

        return mapCreatorCreateLinearMap;
    }

    public JButton mapCreatorCreateBlueLinearMap() {
        JButton mapCreatorCreateBlueLinearMap = new JButton("one handed simpl linear");
        mapCreatorCreateBlueLinearMap.setBounds(650, 120, 90, 15);
        mapCreatorCreateBlueLinearMap.setVisible(false);
        userInterface.add(mapCreatorCreateBlueLinearMap);

        return mapCreatorCreateBlueLinearMap;
    }

    public JButton mapCreatorCreateBlueComplexMap() {
        JButton mapCreatorCreateBlueComplexMap = new JButton("complex");
        mapCreatorCreateBlueComplexMap.setBounds(750, 120, 90, 15);
        mapCreatorCreateBlueComplexMap.setVisible(false);
        userInterface.add(mapCreatorCreateBlueComplexMap);

        return mapCreatorCreateBlueComplexMap;
    }

    public JButton mapCreatorCreateRandomMap() {
        JButton mapCreatorCreateRandomMap = new JButton("random");
        mapCreatorCreateRandomMap.setBounds(650, 100, 90, 15);
        mapCreatorCreateRandomMap.setVisible(false);
        userInterface.add(mapCreatorCreateRandomMap);

        return mapCreatorCreateRandomMap;
    }

    public JButton mapCreatorCreateRandomV2Map() {
        JButton mapCreatorCreateRandomV2Map = new JButton("rand. V2");
        mapCreatorCreateRandomV2Map.setBounds(750, 100, 90, 15);
        mapCreatorCreateRandomV2Map.setVisible(false);
        userInterface.add(mapCreatorCreateRandomV2Map);

        return mapCreatorCreateRandomV2Map;
    }

    public JButton loadPatternsButton() {
        JButton loadPatternButton = new JButton("Load Patterns File");
        loadPatternButton.setBounds(270, 70, 200, 30);
        userInterface.add(loadPatternButton);

        return loadPatternButton;
    }


}
