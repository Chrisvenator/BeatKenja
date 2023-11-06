package UserInterface;

import DataManager.CreateAllNecessaryDIRsAndFiles;
import DataManager.Parameters;
import UserInterface.Elements.WaveFunctions;

import static DataManager.Parameters.*;
import static DataManager.Parameters.darkMode;

import javax.swing.*;
import java.awt.*;

/**
 * This class contains all the UI element definitions that are used in the program.
 * It is used to keep the UI class clean and readable.
 * There is no logic contained in this class.
 */
public class UIElements {
    private final boolean darkMode;
    protected final UserInterface userInterface;

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

    private void initializeVariables() {
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
        mapChecks.setBounds(850, 200, 190, 30);
        mapChecks.setVisible(false);
        userInterface.add(mapChecks);

        return mapChecks;
    }

    public JButton loadPatternsButton() {
        JButton loadPatternButton = new JButton("Load Patterns File");
        loadPatternButton.setBounds(270, 70, 200, 30);
        userInterface.add(loadPatternButton);

        return loadPatternButton;
    }
}
