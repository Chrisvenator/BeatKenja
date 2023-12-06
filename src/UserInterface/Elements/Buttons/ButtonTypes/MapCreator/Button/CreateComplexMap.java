package UserInterface.Elements.Buttons.ButtonTypes.MapCreator.Button;

import BeatSaberObjects.BeatSaberMap;
import MapGeneration.CreatePatterns;
import UserInterface.Elements.Buttons.ButtonTypes.MapCreator.MapCreatorSubButton;
import UserInterface.Elements.Buttons.MyButton;
import UserInterface.Elements.ElementTypes;

public class CreateComplexMap extends MapCreatorSubButton {
    public CreateComplexMap(MyButton parent) {
        super(ElementTypes.MAP_CREATOR_CREATE_COMPLEX_MAP_BUTTON, parent);
    }

    public void onClick() {
        ui.manageMap();
        ui.map.toBlueLeftBottomRowDotTimings();

        try {
            BeatSaberMap map = new BeatSaberMap(CreatePatterns.complexPatternFromTemplate(ui.map._notes, ui.pattern, false, false, null, null));
            loadNewlyCreatedMap(map);

        } catch (IllegalArgumentException ex) {
            printException(ex);
        }
    }
}
