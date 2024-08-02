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
        logger.info("UIElements initialized with darkMode: {}", darkMode);
    }

    // Einstellungen für das Fenster
    public void initialize() {
        logger.info("Initializing UIElements...");
        userInterface.setTitle("Beat Kenja");
        userInterface.setSize(1200, 800);
        userInterface.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        if (darkMode) {
            userInterface.getContentPane().setBackground(darkModeBackgroundColor);
            userInterface.getContentPane().setForeground(darkModeForegroundColor);
        }
        userInterface.setLayout(null);
        logger.debug("UIElements initialized successfully.");
    }

    public JLabel labelMapDiff() {
        logger.debug("Creating labelMapDiff...");
        JLabel labelMapDiff = new JLabel("Choose map difficulty file: ");
        labelMapDiff.setBounds(50, 20, 200, 30);
        if (darkMode) labelMapDiff.setForeground(Color.white);
        userInterface.add(labelMapDiff);
        logger.debug("labelMapDiff created successfully.");

        return labelMapDiff;
    }

    public JCheckBox ignoreDDsCheckbox() {
        logger.debug("Creating ignoreDDsCheckbox...");
        JCheckBox ignoreDDsCheckbox = new JCheckBox("Ignore DDs");
        ignoreDDsCheckbox.setBounds(957, 45, 100, 20);
        if (darkMode) ignoreDDsCheckbox.setForeground(Color.white);
        userInterface.add(ignoreDDsCheckbox);
        logger.debug("ignoreDDsCheckbox created successfully.");

        return ignoreDDsCheckbox;
    }

    public TextArea statusTextArea() {
        logger.debug("Creating statusTextArea...");
        TextArea statusCheck = new TextArea();
        statusCheck.setBounds(50, 235, 1090, 510);
        statusCheck.setBackground(darkMode ? Color.BLACK : Color.WHITE);
        statusCheck.setForeground(darkMode ? Color.WHITE : Color.BLACK);
        statusCheck.setEditable(false);
        userInterface.add(statusCheck);

        if (!CreateAllNecessaryDIRsAndFiles.isPythonInstalled()) {
            String error = "[ERROR]: Python could not be found. Please ensure that it is installed and added to the PATH variable or else the Onset Generation will not work!\n";
            statusCheck.append(error);
            logger.error("Python not found.");
        }

        if (!CreateAllNecessaryDIRsAndFiles.isPipInstalled()) {
            String error = "[ERROR]: PIP could not be found. Please ensure that it is installed or else the Onset Generation will not work!\n";
            statusCheck.append(error);
            logger.error("PIP not found.");
        }

        if (!CreateAllNecessaryDIRsAndFiles.isFFMpegInstalled()) {
            String error = "[ERROR]: FFMPEG could not be found. Please ensure that it is installed and added to the PATH variable or else the Onset Generation will not work!\n";
            statusCheck.append(error);
            logger.error("FFMPEG not found.");
        }

        if (CreateAllNecessaryDIRsAndFiles.isPythonInstalled() && CreateAllNecessaryDIRsAndFiles.isPipInstalled() && CreateAllNecessaryDIRsAndFiles.isFFMpegInstalled()) {
            String info = "[INFO]: All necessary dependencies are installed. You can now convert MP3s to timing maps.\n\n";
            statusCheck.append(info);
            logger.info("All necessary dependencies are installed.");
        }

        logger.debug("statusTextArea created successfully.");
        return statusCheck;
    }
}
