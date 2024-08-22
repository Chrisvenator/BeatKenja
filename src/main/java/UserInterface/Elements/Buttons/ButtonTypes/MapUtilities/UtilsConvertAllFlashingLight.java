package UserInterface.Elements.Buttons.ButtonTypes.MapUtilities;

import BeatSaberObjects.Objects.BeatSaberMap;
import UserInterface.Elements.Buttons.MyButton;
import UserInterface.Elements.Buttons.MySubButton;
import UserInterface.Elements.ElementTypes;

import static DataManager.Parameters.logger;

/**
 * The `UtilsConvertAllFlashingLight` class is a specialized sub-button that extends `MySubButton`.
 * This button is used to convert all flashing lights to constant "on" lights in every `BeatSaberMap` loaded in the user interface.
 * When the button is clicked, it performs the conversion and logs the results.
 */
public class UtilsConvertAllFlashingLight extends MySubButton {
    public UtilsConvertAllFlashingLight(MyButton parent) {
        super(ElementTypes.MAP_UTILITIES_CONVERT_ALL_FLASHING_LIGHTS_BUTTON, parent);
    }

    @Override
    public void onClick() {
        for (BeatSaberMap uiMap : ui.map) {

            uiMap.convertAllFlashLightsToOnLights();
            logger.info("Removed flashing lights");
            logger.debug("flashing lights removed: {}", new BeatSaberMap(uiMap._notes).exportAsMap());
            System.out.println("flashing lights removed: " + new BeatSaberMap(uiMap._notes).exportAsMap());
        }
    }
}
