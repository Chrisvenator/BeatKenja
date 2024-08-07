package UserInterface.Elements.Buttons.ButtonTypes.TimingNoteGenerator;

import BeatSaberObjects.Objects.BeatSaberMap;
import UserInterface.Elements.Buttons.MyButton;
import UserInterface.Elements.Buttons.MySubButton;
import UserInterface.Elements.ElementTypes;

import java.util.Arrays;

import static DataManager.Parameters.logger;
import static DataManager.Parameters.verbose;

public class ToBlueOnlyTimingNotes extends MySubButton {
    public ToBlueOnlyTimingNotes(MyButton parent) {
        super(ElementTypes.TIMING_NOTES_TO_BLUE_ONLY_TIMING_NOTES, parent);
        logger.debug("ToBlueOnlyTimingNotes button initialized.");
    }

    @Override
    public void onClick() {
        ui.manageMap();
        ui.map.toBlueLeftBottomRowDotTimings();

        //Check, if there are only timing notes:
        if (Arrays.stream(ui.map._notes).filter(n -> n._cutDirection == 8 && n._lineLayer == 0).count() <= ui.map._notes.length - 20){
            logger.error("Could not convert map to timing notes");
            System.err.println("Could not convert map to timing notes");
        } else {
            logger.info("Successfully converted Map to only blue timing notes");
            logger.debug("Created Blue-Only-Timing map: {}", ui.map.exportAsMap());
            System.out.println("Normal timing notes: " + ui.map.exportAsMap());
        }
    }
}
