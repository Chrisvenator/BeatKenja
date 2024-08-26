package UserInterface;

import DataManager.Parameters;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;

import static DataManager.Parameters.logger;

/**
 * The `StatusCheckTextPane` class provides a custom text pane within a JPanel for displaying status messages with different styles.
 * It is designed to be used in a graphical user interface (GUI) to log messages with different levels of severity, such as fatal errors, warnings, and informational messages.
 * The text pane supports various styles, which are applied based on the content of the messages.
 */
public class StatusCheckTextPane extends JPanel {
    /** The main text pane where messages are displayed.*/
    private final JTextPane textPane;
    private final StyledDocument doc;
    private Style fatalStyle;
    private Style errorStyle;
    private Style warnStyle;
    private Style noticeStyle;
    private Style infoStyle;
    private Style debugStyle;
    private Style checkingMapStyle;

    /**
     * Constructs a `StatusCheckTextPane` with predefined styles and initializes the text pane within a scrollable panel.
     * The panel is configured to have specific dimensions and color settings based on the application's parameters.
     */
    public StatusCheckTextPane() {
        setLayout(new BorderLayout());

        textPane = new JTextPane();
        textPane.setBackground(Parameters.DARK_MODE ? Parameters.darkModeBackgroundColor : Parameters.lightModeBackgroundColor);
        textPane.setEditable(false);

        doc = textPane.getStyledDocument();
        initializeStyles();

        JScrollPane scrollPane = new JScrollPane(textPane);
        scrollPane.setPreferredSize(new Dimension(1090, 510));
        add(scrollPane, BorderLayout.CENTER);

        this.setBounds(50, 235, 1090, 510);

        logger.debug("statusTextArea created successfully.");
    }

    /**
     * Initializes the various text styles used in the text pane, such as styles for fatal errors, warnings, and informational messages.
     * Each style is configured with specific text attributes like color, boldness, and background color.
     */
    private void initializeStyles() {
        // Define styles
        fatalStyle = textPane.addStyle("FatalStyle", null);
        StyleConstants.setBackground(fatalStyle, Parameters.STATUS_TEXT_FATAL_STYLE_BACKGROUND);
        StyleConstants.setForeground(fatalStyle, Parameters.STATUS_TEXT_FATAL_STYLE_FOREGROUND);
        StyleConstants.setBold(fatalStyle, true);

        errorStyle = textPane.addStyle("ErrorStyle", null);
        StyleConstants.setBackground(errorStyle, Parameters.STATUS_TEXT_ERROR_STYLE_BACKGROUND);
        StyleConstants.setForeground(errorStyle, Parameters.STATUS_TEXT_ERROR_STYLE_FOREGROUND);
        StyleConstants.setBold(errorStyle, true);

        warnStyle = textPane.addStyle("WarnStyle", null);
        StyleConstants.setForeground(warnStyle, Parameters.STATUS_TEXT_WARN_STYLE);

        noticeStyle = textPane.addStyle("NoticeStyle", null);
        StyleConstants.setForeground(noticeStyle, Parameters.STATUS_TEXT_NOTICE_STYLE);

        infoStyle = textPane.addStyle("InfoStyle", null);
        StyleConstants.setForeground(infoStyle, Parameters.STATUS_TEXT_INFO_STYLE);

        debugStyle = textPane.addStyle("DebugStyle", null);
        StyleConstants.setForeground(debugStyle, Parameters.STATUS_TEXT_DEBUG_STYLE);

        checkingMapStyle = textPane.addStyle("CheckingMapStyle", null);
        StyleConstants.setBackground(checkingMapStyle, Parameters.STATUS_TEXT_CHECKING_MAP_STYLE_BACKGROUND);
        StyleConstants.setForeground(checkingMapStyle, Parameters.STATUS_TEXT_CHECKING_MAP_STYLE_FOREGROUND);
//        StyleConstants.setBold(checkingMapStyle, true);
        StyleConstants.setItalic(checkingMapStyle, true);
        StyleConstants.setUnderline(checkingMapStyle, true);

    }

    /**
     * Sets the entire text of the text pane to the specified string, replacing any existing content.
     * The method applies appropriate styles based on the content of the string.
     *
     * @param text The text to display in the text pane.
     */
    public void setText(String text) {
        try {
            // Clear existing text
            doc.remove(0, doc.getLength());
            // Insert new text
            append(text);
        } catch (BadLocationException e) {
            logger.error("Something went wrong with the Status Text: " + e);
            System.err.println("Something went wrong with the Status Text: " + e);
            throw new RuntimeException(e);
        }
    }

    /**
     * Appends one or more strings to the text pane, applying the appropriate style for each string.
     * The text pane automatically scrolls to the bottom after inserting new text.
     *
     * @param strings The strings to append to the text pane.
     */
    public void append(String... strings) {
        for (String s : strings) {
            if (s.contains("NOTICE")) s = s.substring(s.indexOf("[NOTICE]"));
            Style style = determineStyle(s);
            try {
                doc.insertString(doc.getLength(), s, style);
                // Scroll to the bottom after inserting text
                textPane.setCaretPosition(doc.getLength());
            } catch (BadLocationException e) {
                logger.error("Something went wrong with the Status Text: " + e);
                System.err.println("Something went wrong with the Status Text: " + e);
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * Determines the appropriate style to apply to a given text string based on its content.
     * For example, messages containing "ERROR" are styled with the error style.
     *
     * @param text The text for which to determine the style.
     * @return The `Style` to be applied to the text.
     */
    private Style determineStyle(String text) {
        if (text.contains("Checking map:")) return checkingMapStyle;
        if (text.contains("FATAL")) return fatalStyle;
        if (text.contains("ERROR")) return errorStyle;
        if (text.contains("WARN")) return warnStyle;
        if (text.contains("NOTICE")) return noticeStyle;
        if (text.contains("INFO")) return infoStyle;
        if (text.contains("DEBUG")) return debugStyle;
        return infoStyle; // Default style
    }

    /**
     * Clears all text from the text pane, leaving it empty.
     */
    public void clear() {
        setText("");
    }

    public String getText() {
        return this.textPane.getText();
    }
}
