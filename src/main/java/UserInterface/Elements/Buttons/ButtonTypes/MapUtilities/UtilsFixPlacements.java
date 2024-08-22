package UserInterface.Elements.Buttons.ButtonTypes.MapUtilities;

import BeatSaberObjects.Objects.BeatSaberMap;
import UserInterface.Elements.Buttons.MyButton;
import UserInterface.Elements.Buttons.MySubButton;
import UserInterface.Elements.ElementTypes;
import UserInterface.Elements.TextFields.MyTextField;

import static DataManager.Parameters.logger;

/**
 * The `UtilsFixPlacements` class is a specialized sub-button that extends `MySubButton`.
 * This button allows the user to adjust the placement of notes in every `BeatSaberMap` loaded in the user interface.
 * The precision for fixing placements is specified via a `MyTextField` input.
 * When the button is clicked, the note placements are adjusted according to the specified precision, and the results are logged.
 */
public class UtilsFixPlacements extends MySubButton {
    private final MyTextField fixPlacementTextField;

    public UtilsFixPlacements(MyButton parent, MyTextField fixPlacementTextField) {
        super(ElementTypes.MAP_UTILITIES_FIX_PLACEMENTS_BUTTON, parent);
        this.fixPlacementTextField = fixPlacementTextField;
    }

    @Override
    public void onClick() {
        for (BeatSaberMap uiMap : ui.map) {

            uiMap.fixPlacements((double) 1 / Double.parseDouble(fixPlacementTextField.getText().replaceAll("[^\\d.]", "")));
            logger.info("Fixed Note Placement with a precision of 1/{} of a beat.", fixPlacementTextField.getText());
            logger.debug("Placements fixed: {}", new BeatSaberMap(uiMap._notes).exportAsMap());
            System.out.println("Placements fixed: " + new BeatSaberMap(uiMap._notes).exportAsMap());
        }
    }
}
