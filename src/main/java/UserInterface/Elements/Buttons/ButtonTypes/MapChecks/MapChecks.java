package UserInterface.Elements.Buttons.ButtonTypes.MapChecks;

import BeatSaberObjects.Objects.Note;
import MapGeneration.PatternGeneration.CommonMethods.CheckParity;
import UserInterface.Elements.Buttons.MyButton;
import UserInterface.Elements.ElementTypes;
import UserInterface.UserInterface;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static DataManager.Parameters.logger;

public class MapChecks extends MyButton {
    public MapChecks(UserInterface ui) {
        super(ElementTypes.MAP_CHECKS_MAP_CHECKS, ui);
    }

    @Override
    public void onClick() {
        List<Note> notes = new ArrayList<>();
        Collections.addAll(notes, ui.map._notes);

        logger.info("Checking for mapping errors...");
        CheckParity.checkForMappingErrors(notes, false);
    }
}
