package UserInterface.Elements.Buttons.ButtonTypes.TimingNoteGenerator;

import BeatSaberObjects.Objects.BeatSaberMap;
import UserInterface.Elements.Buttons.MyButton;
import UserInterface.Elements.Buttons.MySubButton;
import UserInterface.Elements.ElementTypes;
import UserInterface.UserInterface;

import java.util.Arrays;

import static DataManager.Parameters.logger;

public class ToTwoColorTimingNotes extends MySubButton {
    public ToTwoColorTimingNotes(MyButton parent) {
        super(ElementTypes.TIMING_NOTES_TO_2_COLOR_TIMING_NOTES, parent);
    }

    @Override
    public void onClick() {
        ui.manageMap();
        for (BeatSaberMap uiMap : ui.map) {
            UserInterface.currentDiff = uiMap.difficultyFileName;
            logger.warn("NOTE: It is very likely that this feature is broken! Use at your own risk!");
            uiMap.toTimingNotes();

            if (Arrays.stream(uiMap._notes).filter(n -> n._cutDirection == 8).count() <= uiMap._notes.length - 20) {
                logger.error("Could not convert map to two-color timing notes");
                System.err.println("Could not convert map to two-color timing notes");
            }
            else {
                logger.info("Successfully converted Map to two-color timing notes");
                logger.debug("Successfully converted Map to two-color timing notes: {}", uiMap.exportAsMap());
            }
        }
    }
}
