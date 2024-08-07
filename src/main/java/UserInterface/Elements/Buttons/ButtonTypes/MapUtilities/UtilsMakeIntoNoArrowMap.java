package UserInterface.Elements.Buttons.ButtonTypes.MapUtilities;

import BeatSaberObjects.Objects.BeatSaberMap;
import UserInterface.Elements.Buttons.MyButton;
import UserInterface.Elements.Buttons.MySubButton;
import UserInterface.Elements.ElementTypes;

import static DataManager.Parameters.logger;
import static DataManager.Parameters.verbose;

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
