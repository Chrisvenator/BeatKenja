package UserInterface.Elements.Buttons.ButtonTypes.MapUtilities;

import BeatSaberObjects.BeatSaberMap;
import UserInterface.Elements.Buttons.MyButton;
import UserInterface.Elements.Buttons.MySubButton;
import UserInterface.Elements.ElementTypes;

import static DataManager.Parameters.verbose;

public class UtilsConvertAllFlashingLight extends MySubButton {
    public UtilsConvertAllFlashingLight(MyButton parent) {
        super(ElementTypes.MAP_UTILITIES_CONVERT_ALL_FLASHING_LIGHTS_BUTTON, parent);
    }

    @Override
    public void onClick() {
        ui.map.convertAllFlashLightsToOnLights();
        ui.statusCheck.setText(ui.statusCheck.getText() + "\n[INFO]: Removed flashing lights");
        System.out.println("flashing lights removed: " + new BeatSaberMap(ui.map._notes).exportAsMap());
        if (verbose) ui.statusCheck.setText(ui.statusCheck.getText() + "\n" + "VERBOSE: " + "flashing lights removed: " + new BeatSaberMap(ui.map._notes).exportAsMap());
    }
}
