package UserInterface.Elements.Buttons.ButtonTypes.TimingNoteGenerator;

import UserInterface.Elements.Buttons.MyButton;
import UserInterface.Elements.Buttons.MySubButton;
import UserInterface.Elements.ElementTypes;

import static DataManager.Parameters.logger;
import static DataManager.Parameters.verbose;

public class ToTwoColorTimingNotes extends MySubButton {
    public ToTwoColorTimingNotes(MyButton parent) {
        super(ElementTypes.TIMING_NOTES_TO_2_COLOR_TIMING_NOTES, parent);
    }

    @Override
    public void onClick() {
        logger.warn("NOTE: It is very likely that this feature is broken! Use at your own risk!");
        ui.map.toTimingNotes();
        logger.warn("Successfully converted Map to timing notes");
        logger.debug("Stacked timing notes: {}", ui.map.exportAsMap());
    }
}
