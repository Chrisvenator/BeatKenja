package UserInterface.Elements.Buttons.ButtonTypes.TimingNoteGenerator;

import BeatSaberObjects.Objects.BeatSaberMap;
import UserInterface.Elements.Buttons.MyButton;
import UserInterface.Elements.Buttons.MySubButton;
import UserInterface.Elements.ElementTypes;

import static DataManager.Parameters.logger;
import static DataManager.Parameters.verbose;

public class ToBlueOnlyTimingNotes extends MySubButton {
    public ToBlueOnlyTimingNotes(MyButton parent) {
        super(ElementTypes.TIMING_NOTES_TO_BLUE_ONLY_TIMING_NOTES, parent);
        logger.debug("ToBlueOnlyTimingNotes button initialized.");
    }

    @Override
    public void onClick() {
        ui.map.toBlueLeftBottomRowDotTimings();
        logger.info("Successfully converted Map to only blue timing notes");
        logger.debug("Created Blue-Only-Timing map: {}", ui.map.exportAsMap());
    }
}
