package UserInterface;

import static DataManager.Parameters.*;

import javax.swing.*;
import java.awt.*;

/**
 * This class encapsulates all the UI element definitions used in the program, separating the user interface (UI) setup from the main logic.
 * It provides methods to create and initialize various UI components such as labels, checkboxes, and text areas.
 * This organization helps keep the `UserInterface` class clean and readable by moving UI element creation into a separate class.
 */
public class UIElements {
    /** Indicates whether dark mode is enabled for the UI.*/
    private final boolean darkMode;
    /** Reference to the main `UserInterface` instance that these UI elements belong to.*/
    protected final UserInterface userInterface;

    /**
     * Constructs a `UIElements` instance with the specified dark mode setting and associated `UserInterface`.
     *
     * @param darkMode       Whether dark mode is enabled.
     * @param userInterface  The `UserInterface` instance that these elements will be part of.
     */
    public UIElements(boolean darkMode, UserInterface userInterface) {
        this.darkMode = darkMode;
        this.userInterface = userInterface;
        logger.info("UIElements initialized with darkMode: {}", darkMode);
    }

    /**
     * Initializes the main settings for the user interface window, such as the title, size, and layout.
     * It also applies dark mode settings if enabled.
     */
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

    /**
     * Creates and returns a label for displaying map difficulty file selection status.
     * The label is configured with specific bounds and color settings based on the dark mode setting.
     *
     * @return The `JLabel` component for map difficulty selection.
     */
    public JLabel labelMapDiff() {
        logger.debug("Creating labelMapDiff...");
        JLabel labelMapDiff = new JLabel("Choose map difficulty file: ");
        labelMapDiff.setBounds(60, 20, 200, 30);
        if (darkMode) labelMapDiff.setForeground(Color.white);
        userInterface.add(labelMapDiff);
        logger.debug("labelMapDiff created successfully.");

        return labelMapDiff;
    }

    /**
     * Creates and returns a checkbox for the "Ignore DDs" option.
     * The checkbox is configured with specific bounds and color settings based on the dark mode setting.
     *
     * @return The `JCheckBox` component for the "Ignore DDs" option.
     */
    public JCheckBox ignoreDDsCheckbox() {
        logger.debug("Creating ignoreDDsCheckbox...");
        JCheckBox ignoreDDsCheckbox = new JCheckBox("Ignore DDs");
        ignoreDDsCheckbox.setBounds(957, 45, 100, 20);
        if (darkMode) ignoreDDsCheckbox.setForeground(Color.white);
        userInterface.add(ignoreDDsCheckbox);
        logger.debug("ignoreDDsCheckbox created successfully.");

        return ignoreDDsCheckbox;
    }

    /**
     * Creates and returns a text pane for displaying status and log messages.
     * This pane is used to show important events and logs in the GUI.
     *
     * @return The `StatusCheckTextPane` component for status and log messages.
     */
    public StatusCheckTextPane statusTextArea() {
        logger.debug("Creating statusTextArea...");
        StatusCheckTextPane statusCheck = new StatusCheckTextPane();

        userInterface.add(statusCheck);

        logger.debug("statusTextArea created successfully.");
        return statusCheck;
    }
}
