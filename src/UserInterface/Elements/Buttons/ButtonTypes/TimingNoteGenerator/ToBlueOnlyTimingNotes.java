package UserInterface.Elements.Buttons.ButtonTypes.TimingNoteGenerator;

import BeatSaberObjects.BeatSaberMap;
import UserInterface.Elements.Buttons.MyButton;
import UserInterface.Elements.Buttons.MySubButton;
import UserInterface.Elements.ElementTypes;

import static DataManager.Parameters.verbose;

public class ToBlueOnlyTimingNotes extends MySubButton {
    public ToBlueOnlyTimingNotes(MyButton parent) {
        super(ElementTypes.TIMING_NOTES_TO_BLUE_ONLY_TIMING_NOTES, parent);
    }

    @Override
    public void onClick() {
        ui.map = new BeatSaberMap(ui.map._notes);
        System.out.println("Normal timing notes: " + ui.map.exportAsMap());
        ui.statusCheck.setText(ui.statusCheck.getText() + "\n[INFO]: Successfully converted Map to only blue timing notes");
        if (verbose) ui.statusCheck.setText(ui.statusCheck.getText() + "\n" + "VERBOSE: " + "Normal timing notes: " + ui.map.exportAsMap());
    }
}
