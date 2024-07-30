package UserInterface.Elements.Buttons.ButtonTypes.GlobalButtons.Buttons;

import BeatSaberObjects.Objects.BeatSaberMap;
import DataManager.Parameters;
import MapGeneration.GenerationElements.Pattern;
import UserInterface.Elements.Buttons.ButtonTypes.GlobalButtons.Exceptions.MapHasWrongFormatException;
import UserInterface.Elements.Buttons.ButtonTypes.GlobalButtons.GlobalButton;
import UserInterface.Elements.ElementTypes;
import UserInterface.UserInterface;

import java.awt.*;
import java.io.File;

import static DataManager.Parameters.DEFAULT_PATH;
import static DataManager.Parameters.FILE_CHOOSER;
import static DataManager.Parameters.verbose;

public class GlobalLoadPatterns extends GlobalButton {
    public GlobalLoadPatterns(UserInterface ui) {
        super(ElementTypes.GLOBAL_LOAD_PATTERNS_BUTTON, ui);
        if (ui.pattern == null || (!new File(Parameters.DEFAULT_PATTERN_PATH).exists() && !Parameters.useDatabase) ){
            setBackground(Color.RED);
        }
    }

    @Override
    public void onClick() {
        FILE_CHOOSER.setCurrentDirectory(new File("Patterns/PatternProbabilities"));
        int option = FILE_CHOOSER.showOpenDialog(this);
        FILE_CHOOSER.setCurrentDirectory(new File(DEFAULT_PATH));

        if (!approveFileLoading(option)) return;
        try {
            if (FILE_CHOOSER.getSelectedFile().getName().endsWith(".pat")) {
                ui.pattern = new Pattern(FILE_CHOOSER.getSelectedFile().getAbsolutePath());
            } else {
                File f = FILE_CHOOSER.getSelectedFile();
                if (!f.isFile() || (!f.getName().endsWith(".json") && !f.getName().endsWith(".dat"))) throw new MapHasWrongFormatException("Wrong file type!");

                BeatSaberMap beatSaberMap = BeatSaberMap.newMapFromJSON(f.getAbsolutePath());
                ui.pattern = new Pattern(beatSaberMap._notes, 1);
                System.out.println("[INFO]:Pattern loaded: " + ui.pattern.exportInPatFormat());
            }

            ui.statusCheck.append("\n[INFO]: Successfully loaded Patterns");
            this.setBackground(Color.green);
        } catch (Exception e) {
            e.printStackTrace();
            printException(new MapHasWrongFormatException("There was an error while importing the patterns! Map probably has the wrong format!"));
        }

        if (verbose) System.out.println(ui.pattern);
    }
}
