package UserInterface.Elements.Buttons.ButtonTypes.GlobalButtons.Buttons;

import DataManager.Parameters;
import UserInterface.Elements.Buttons.ButtonTypes.GlobalButtons.GlobalButton;
import UserInterface.Elements.ElementTypes;
import UserInterface.Elements.Frames.ShowPatternsFrame;
import UserInterface.UserInterface;

import javax.swing.*;
import java.awt.*;
import java.io.File;

import static DataManager.Parameters.DEFAULT_PATTERN_PATH;
import static DataManager.Parameters.logger;

public class GlobalShowPatterns extends GlobalButton {
    public GlobalShowPatterns(UserInterface ui) {
        super(ElementTypes.GLOBAL_SHOW_PATTERNS_BUTTON, ui);
        if (ui.pattern == null || (!new File(Parameters.DEFAULT_PATTERN_PATH).exists() && !Parameters.useDatabase)) {
            setBackground(Color.RED);
            logger.warn("Pattern is null or the default pattern path does not exist, and the database is not in use: " + DEFAULT_PATTERN_PATH);
        }
        logger.debug("GlobalShowPatterns button initialized.");
    }

    @Override
    public void onClick() {
        logger.info("Opening pattern visualization window.");
        SwingUtilities.invokeLater(() -> new ShowPatternsFrame(ui).setVisible(true));
    }
}
