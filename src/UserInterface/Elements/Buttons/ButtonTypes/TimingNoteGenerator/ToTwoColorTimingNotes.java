package UserInterface.Elements.Buttons.ButtonTypes.TimingNoteGenerator;

import UserInterface.Elements.Buttons.MyButton;
import UserInterface.Elements.Buttons.MySubButton;
import UserInterface.Elements.ElementTypes;

import static DataManager.Parameters.verbose;

public class ToTwoColorTimingNotes extends MySubButton {
    public ToTwoColorTimingNotes(MyButton parent) {
        super(ElementTypes.TIMING_NOTES_TO_2_COLOR_TIMING_NOTES, parent);
    }

    @Override
    public void onClick() {
        ui.map.toTimingNotes();
        System.out.println();
        ui.statusCheck.setText(ui.statusCheck.getText() + "\nNOTE: It is very likely that this feature is broken! Use at your own risk!");
        ui.statusCheck.setText(ui.statusCheck.getText() + "\n[INFO]: Successfully converted Map to timing notes");
        if (verbose) ui.statusCheck.setText(ui.statusCheck.getText() + "\n" + "VERBOSE: " + "Stacked timing notes: " + ui.map.exportAsMap());
    }
}
