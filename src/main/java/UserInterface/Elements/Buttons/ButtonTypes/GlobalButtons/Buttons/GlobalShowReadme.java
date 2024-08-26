package UserInterface.Elements.Buttons.ButtonTypes.GlobalButtons.Buttons;

import DataManager.Parameters;
import UserInterface.Elements.Buttons.ButtonTypes.GlobalButtons.GlobalButton;
import UserInterface.Elements.ElementTypes;
import UserInterface.Elements.Frames.MarkdownViewer;
import UserInterface.UserInterface;

import javax.swing.*;

import java.awt.*;

import static DataManager.Parameters.logger;

public class GlobalShowReadme extends GlobalButton {
    public GlobalShowReadme(UserInterface ui) {
        super(ElementTypes.GLOBAL_SHOW_README_BUTTON, ui);
        setBackground(Color.PINK);
    }

    @Override
    public void onClick(){
        logger.info("Opening Readme in new window.");
        SwingUtilities.invokeLater(() -> new MarkdownViewer(Parameters.README_FILE_LOCATION));
    }


}
