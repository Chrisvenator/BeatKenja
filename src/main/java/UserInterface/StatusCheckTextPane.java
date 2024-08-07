package UserInterface;

import DataManager.Parameters;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;

import static DataManager.Parameters.logger;

public class StatusCheckTextPane extends JPanel {
    private final JTextPane textPane;
    private final StyledDocument doc;
    private Style fatalStyle;
    private Style errorStyle;
    private Style warnStyle;
    private Style infoStyle;
    private Style debugStyle;

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

        infoStyle = textPane.addStyle("InfoStyle", null);
        StyleConstants.setForeground(infoStyle, Parameters.STATUS_TEXT_INFO_STYLE);

        debugStyle = textPane.addStyle("DebugStyle", null);
        StyleConstants.setForeground(debugStyle, Parameters.STATUS_TEXT_DEBUG_STYLE);
    }

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

    public void append(String... strings) {
        for (String s : strings) {
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

    private Style determineStyle(String text) {
        if (text.contains("FATAL")) return fatalStyle;
        if (text.contains("ERROR")) return errorStyle;
        if (text.contains("WARN")) return warnStyle;
        if (text.contains("INFO")) return infoStyle;
        if (text.contains("DEBUG")) return debugStyle;
        return infoStyle; // Default style
    }
}
