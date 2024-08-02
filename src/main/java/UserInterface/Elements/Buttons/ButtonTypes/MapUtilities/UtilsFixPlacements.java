package UserInterface.Elements.Buttons.ButtonTypes.MapUtilities;

import BeatSaberObjects.Objects.BeatSaberMap;
import UserInterface.Elements.Buttons.MyButton;
import UserInterface.Elements.Buttons.MySubButton;
import UserInterface.Elements.ElementTypes;
import UserInterface.Elements.TextFields.MyTextField;

import static DataManager.Parameters.logger;
import static DataManager.Parameters.verbose;

public class UtilsFixPlacements extends MySubButton {
    private final MyTextField fixPlacementTextField;

    public UtilsFixPlacements(MyButton parent, MyTextField fixPlacementTextField) {
        super(ElementTypes.MAP_UTILITIES_FIX_PLACEMENTS_BUTTON, parent);
        this.fixPlacementTextField = fixPlacementTextField;
    }

    @Override
    public void onClick() {
        ui.map.fixPlacements((double) 1 / Double.parseDouble(fixPlacementTextField.getText().replaceAll("[^\\d.]", "")));
        logger.info("Fixed Note Placement with a precision of 1/{} of a beat.", fixPlacementTextField.getText());
        logger.debug("Placements fixed: {}", new BeatSaberMap(ui.map._notes).exportAsMap());
    }
}
