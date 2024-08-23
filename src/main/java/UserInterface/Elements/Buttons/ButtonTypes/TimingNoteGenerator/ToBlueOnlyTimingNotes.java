package UserInterface.Elements.Buttons.ButtonTypes.TimingNoteGenerator;

import BeatSaberObjects.Objects.BeatSaberMap;
import UserInterface.Elements.Buttons.MyButton;
import UserInterface.Elements.Buttons.MySubButton;
import UserInterface.Elements.ElementTypes;
import UserInterface.UserInterface;

import java.util.Arrays;

import static DataManager.Parameters.logger;

/**
 * The `ToBlueOnlyTimingNotes` class is a specialized sub-button that extends `MySubButton`.
 * This button converts all the notes in each `BeatSaberMap` loaded in the user interface into blue timing notes placed on the left bottom row.
 * When the button is clicked, the conversion is applied, and the process is logged.
 * It also verifies that the conversion was successful by checking the number of timing notes.
 */
public class ToBlueOnlyTimingNotes extends MySubButton {
    public ToBlueOnlyTimingNotes(MyButton parent) {
        super(ElementTypes.TIMING_NOTES_TO_BLUE_ONLY_TIMING_NOTES, parent);
        logger.debug("ToBlueOnlyTimingNotes button initialized.");
    }

    @Override
    public void onClick() {
        ui.manageMap();
        for (BeatSaberMap uiMap : ui.map) {
            UserInterface.currentDiff = uiMap.difficultyFileName;
            uiMap.toBlueLeftBottomRowDotTimings();

            //Check, if there are only timing notes:
            if (Arrays.stream(uiMap._notes).filter(n -> n._cutDirection == 8 && n._lineLayer == 0).count() <= uiMap._notes.length - 20) {
                logger.error("Could not convert map to timing notes");
                System.err.println("Could not convert map to timing notes");
            }
            else {
                logger.info("Successfully converted Map to only blue timing notes");
                logger.debug("Created Blue-Only-Timing map: {}", uiMap.exportAsMap());
                System.out.println("Normal timing notes: " + uiMap.exportAsMap());
            }
        }
    }
}
