package UserInterface.Elements.Buttons.ButtonTypes.MapUtilities;

import BeatSaberObjects.Objects.BeatSaberMap;
import UserInterface.Elements.Buttons.MyButton;
import UserInterface.Elements.Buttons.MySubButton;
import UserInterface.Elements.ElementTypes;
import UserInterface.Elements.TextFields.MyTextField;

import static DataManager.Parameters.verbose;

public class UtilsFixPlacements extends MySubButton {
    MyTextField fixPlacementTextField;

    public UtilsFixPlacements(MyButton parent, MyTextField fixPlacementTextField) {
        super(ElementTypes.MAP_UTILITIES_FIX_PLACEMENTS_BUTTON, parent);
        this.fixPlacementTextField = fixPlacementTextField;
    }

    @Override
    public void onClick() {
        ui.map.fixPlacements((double) 1 / Integer.parseInt(fixPlacementTextField.getText().replaceAll("[^\\d.]", "")));
        ui.statusCheck.setText(ui.statusCheck.getText() + "\n[INFO]: Fixed Note Placement with a precision of 1/" + fixPlacementTextField.getText() + " of a beat.");
        System.out.println("Placements fixed: " + new BeatSaberMap(ui.map._notes).exportAsMap());
        if (verbose)
            ui.statusCheck.setText(ui.statusCheck.getText() + "\n" + "VERBOSE: " + "Placements fixed: " + new BeatSaberMap(ui.map._notes).exportAsMap());
    }
}
