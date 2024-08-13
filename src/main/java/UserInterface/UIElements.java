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
        labelMapDiff.setBounds(60, 20, 200, 30);
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

    public StatusCheckTextPane statusTextArea() {
        logger.debug("Creating statusTextArea...");
        StatusCheckTextPane statusCheck = new StatusCheckTextPane();

        userInterface.add(statusCheck);

        logger.debug("statusTextArea created successfully.");
        return statusCheck;
    }
}
