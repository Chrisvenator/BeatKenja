package UserInterface.Elements.Buttons.ButtonTypes.GlobalButtons.Buttons;

import BeatSaberObjects.Objects.BeatSaberMap;
import MapGeneration.GenerationElements.Pattern;
import UserInterface.Elements.Buttons.ButtonTypes.GlobalButtons.GlobalButton;
import UserInterface.Elements.ElementTypes;
import UserInterface.Elements.Buttons.ButtonTypes.GlobalButtons.Exceptions.MapHasWrongFormatException;
import UserInterface.UserInterface;

import java.awt.*;
import java.io.FileNotFoundException;

import static DataManager.Parameters.*;

public class GlobalLoadPatterns extends GlobalButton {
    public GlobalLoadPatterns(UserInterface ui) {
        super(ElementTypes.GLOBAL_LOAD_PATTERNS_BUTTON, ui);
    }

    @Override
    public void onClick() {
        int option = FILE_CHOOSER.showOpenDialog(this);
        if (!approveFileloading(option)) return;
        try {
            BeatSaberMap beatSaberMap = convertToMap(FILE_CHOOSER.getSelectedFile());

            ui.pattern = new Pattern(beatSaberMap._notes, 1);

            ui.statusCheck.append("\n[INFO]: Successfully loaded Patterns");
            this.setBackground(Color.green);
        } catch (FileNotFoundException e) {
            printException(e);
        } catch (Exception e) {
            printException(new MapHasWrongFormatException("There was an error while importing the patterns! Map probably has the wrong format!"));
        }

        if (verbose) System.out.println(ui.pattern);
    }
}
