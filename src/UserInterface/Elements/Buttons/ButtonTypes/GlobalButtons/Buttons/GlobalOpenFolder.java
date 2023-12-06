package UserInterface.Elements.Buttons.ButtonTypes.GlobalButtons.Buttons;

import UserInterface.Elements.Buttons.ButtonTypes.GlobalButtons.GlobalButton;
import UserInterface.Elements.Buttons.MyButton;
import UserInterface.Elements.ElementTypes;
import UserInterface.UserInterface;

import java.awt.*;
import java.io.File;
import java.io.IOException;

import static DataManager.Parameters.*;

public class GlobalOpenFolder extends GlobalButton {
    public GlobalOpenFolder(UserInterface ui) {
        super(ElementTypes.GLOBAL_OPEN_FOLDER, ui);
        setBackground(darkMode ? new Color(175, 140, 59) : new Color(255, 212, 123));
    }

    @Override
    public void onClick() {
        try {
            Desktop.getDesktop().open(new File(ONSET_GENERATION_FOLDER_PATH_INPUT));
        } catch (IOException ex) {
            printException(new IOException("\n[ERROR]: Couldn't open the folder: " + ONSET_GENERATION_FOLDER_PATH_INPUT + "!", ex));
        }
    }
}
