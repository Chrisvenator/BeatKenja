package UserInterface;

import DataManager.CreateAllNecessaryDIRsAndFiles;

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

    public JLabel labelMapDiff() {
        JLabel labelMapDiff = new JLabel("Choose map difficulty file: ");
        labelMapDiff.setBounds(50, 20, 200, 30);
        if (darkMode) labelMapDiff.setForeground(Color.white);
        userInterface.add(labelMapDiff);

        return labelMapDiff;
    }


    public JCheckBox ignoreDDsCheckbox() {
        JCheckBox ignoreDDsCheckbox = new JCheckBox("Ignore DDs");
        ignoreDDsCheckbox.setBounds(957, 45, 100, 20);
        if (darkMode) ignoreDDsCheckbox.setForeground(Color.white);
        userInterface.add(ignoreDDsCheckbox);

        return ignoreDDsCheckbox;
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
}
