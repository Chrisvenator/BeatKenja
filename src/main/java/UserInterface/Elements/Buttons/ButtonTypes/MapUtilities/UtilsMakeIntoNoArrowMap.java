package UserInterface.Elements.Buttons.ButtonTypes.MapUtilities;

import BeatSaberObjects.Objects.BeatSaberMap;
import UserInterface.Elements.Buttons.MyButton;
import UserInterface.Elements.Buttons.MySubButton;
import UserInterface.Elements.ElementTypes;

import static DataManager.Parameters.logger;

/**
 * The `UtilsMakeIntoNoArrowMap` class is a specialized sub-button that extends `MySubButton`.
 * This button allows the user to convert every `BeatSaberMap` loaded in the user interface into a "no arrows" map,
 * meaning all notes will have no directional arrows.
 * When the button is clicked, the maps are converted and the results are logged.
 */
public class UtilsMakeIntoNoArrowMap extends MySubButton {
    public UtilsMakeIntoNoArrowMap(MyButton parent) {
        super(ElementTypes.MAP_UTILITIES_MAKE_NO_ARROW_MAP_BUTTON, parent);
    }

    @Override
    public void onClick() {
        for (BeatSaberMap uiMap : ui.map) {

            uiMap.makeNoArrows();
            logger.info("Map is now a no arrows map");
            logger.debug("No Arrow Map: {}", uiMap.exportAsMap());
            System.out.println("No Arrow Map: " + uiMap.exportAsMap());
        }
    }
}
