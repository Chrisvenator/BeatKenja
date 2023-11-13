package UserInterface.Elements.Buttons.ButtonTypes.GlobalButtons;

import UserInterface.Elements.Buttons.MyButton;
import UserInterface.Elements.ElementTypes;
import UserInterface.UserInterface;

import java.awt.*;
import java.io.File;
import java.io.IOException;

import static DataManager.Parameters.*;

public class GlobalOpenFolder extends MyButton {
    public GlobalOpenFolder(UserInterface ui) {
        super(ElementTypes.GLOBAL_OPEN_FOLDER, ui);
        setBackground(darkMode ? new Color(175, 140, 59) : new Color(255, 212, 123));
    }

    @Override
    public void onClick() {
        try {
            Desktop.getDesktop().open(new File(ONSET_GENERATION_FOLDER_PATH_INPUT));
        } catch (IOException ex) {
            ui.statusCheck.append("\n[ERROR]: Couldn't open the folder: " + ONSET_GENERATION_FOLDER_PATH_INPUT);
        }
    }
}
