package UserInterface.Elements.Buttons.ButtonTypes.TimingNoteGenerator;

import UserInterface.Elements.Buttons.MyButton;
import UserInterface.Elements.Buttons.MySubButton;
import UserInterface.Elements.ElementTypes;

import java.util.Arrays;

import static DataManager.Parameters.logger;
import static DataManager.Parameters.verbose;

public class ToTwoColorTimingNotes extends MySubButton {
    public ToTwoColorTimingNotes(MyButton parent) {
        super(ElementTypes.TIMING_NOTES_TO_2_COLOR_TIMING_NOTES, parent);
    }

    @Override
    public void onClick() {
        ui.manageMap();
        logger.warn("NOTE: It is very likely that this feature is broken! Use at your own risk!");
        ui.map.toTimingNotes();

        if (Arrays.stream(ui.map._notes).filter(n -> n._cutDirection == 8).count() <= ui.map._notes.length - 20) {
            logger.error("Could not convert map to two-color timing notes");
            System.err.println("Could not convert map to two-color timing notes");
        } else {
            logger.info("Successfully converted Map to two-color timing notes");
            logger.debug("Successfully converted Map to two-color timing notes: {}", ui.map.exportAsMap());
        }
    }
}
