package UserInterface.Elements.Buttons.ButtonTypes.MapCreator.Button;

import BeatSaberObjects.BeatSaberMap;
import MapGeneration.CreatePatterns;
import UserInterface.Elements.Buttons.ButtonTypes.MapCreator.MapCreatorSubButton;
import UserInterface.Elements.Buttons.MyButton;
import UserInterface.Elements.ElementTypes;


public class CreateRandomMap extends MapCreatorSubButton {
    public CreateRandomMap(MyButton parent) {
        super(ElementTypes.MAP_CREATOR_CREATE_RANDOM_MAP_BUTTON, parent);
    }

    @Override
    public void onClick() {
        ui.manageMap();
        ui.map.toBlueLeftBottomRowDotTimings();

        try {
            BeatSaberMap map = new BeatSaberMap(CreatePatterns.createRandomPattern(ui.map._notes, false));
            loadNewlyCreatedMap(map);

        } catch (IllegalArgumentException ex) {
            printException(ex);
        }
    }
}
