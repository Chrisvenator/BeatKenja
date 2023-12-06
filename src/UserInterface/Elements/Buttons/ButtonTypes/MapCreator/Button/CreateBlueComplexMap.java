package UserInterface.Elements.Buttons.ButtonTypes.MapCreator.Button;

import BeatSaberObjects.Objects.BeatSaberMap;
import MapGeneration.CreatePatterns;
import UserInterface.Elements.Buttons.ButtonTypes.MapCreator.MapCreatorSubButton;
import UserInterface.Elements.Buttons.MyButton;
import UserInterface.Elements.ElementTypes;

public class CreateBlueComplexMap extends MapCreatorSubButton {
    public CreateBlueComplexMap(MyButton parent) {
        super(ElementTypes.MAP_CREATOR_CREATE_ONE_HANDED_COMPLEX_MAP_BUTTON, parent);
    }

    @Override
    public void onClick() {
        ui.manageMap();
        ui.map.toBlueLeftBottomRowDotTimings();

        try {
            BeatSaberMap map = new BeatSaberMap(CreatePatterns.complexPatternFromTemplate(ui.map._notes, ui.pattern, true, false, null, null));
            loadNewlyCreatedMap(map);

        } catch (IllegalArgumentException ex) {
            printException(ex);
        }

    }
}
