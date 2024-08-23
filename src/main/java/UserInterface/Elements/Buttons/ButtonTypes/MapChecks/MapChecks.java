package UserInterface.Elements.Buttons.ButtonTypes.MapChecks;

import BeatSaberObjects.Objects.BeatSaberMap;
import BeatSaberObjects.Objects.Note;
import MapGeneration.PatternGeneration.CommonMethods.CheckParity;
import UserInterface.Elements.Buttons.MyButton;
import UserInterface.Elements.ElementTypes;
import UserInterface.UserInterface;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static DataManager.Parameters.logger;

/**
 * The `MapChecks` class extends `MyButton` and is responsible for performing checks on maps within the user interface.
 * Specifically, it checks for basic mapping errors in the notes of each `BeatSaberMap` loaded in the UI.
 * When the button is clicked, it initiates the checking process and logs the results.
 */
public class MapChecks extends MyButton {
    public MapChecks(UserInterface ui) {
        super(ElementTypes.MAP_CHECKS_MAP_CHECKS, ui);
    }

    @Override
    public void onClick() {
        for (BeatSaberMap uiMap : ui.map) {

            List<Note> notes = new ArrayList<>();
            Collections.addAll(notes, uiMap._notes);

            logger.info("Checking for mapping errors...");
            CheckParity.checkAndFixBasicMappingErrors(notes, false);
        }
    }
}
