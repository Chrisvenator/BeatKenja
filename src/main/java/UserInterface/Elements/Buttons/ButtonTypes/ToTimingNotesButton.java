package UserInterface.Elements.Buttons.ButtonTypes;

import UserInterface.Elements.Buttons.ButtonTypes.TimingNoteGenerator.ToBlueOnlyTimingNotes;
import UserInterface.Elements.Buttons.ButtonTypes.TimingNoteGenerator.ToTwoColorTimingNotes;
import UserInterface.Elements.Buttons.MyButton;
import UserInterface.Elements.ElementTypes;
import UserInterface.UserInterface;

public class ToTimingNotesButton extends MyButton {
    public ToTimingNotesButton(UserInterface ui) {
        super(ElementTypes.TIMING_NOTES_TO_TIMING_NOTES, ui);
        initChildren();
    }

    private void initChildren() {
        this.addChild(new ToBlueOnlyTimingNotes(this));
        this.addChild(new ToTwoColorTimingNotes(this));
    }
}
