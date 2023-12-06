package UserInterface.Elements.Buttons.ButtonTypes.MapCreator.Button;

import BeatSaberObjects.BeatSaberMap;
import MapGeneration.CreatePatterns;
import UserInterface.Elements.Buttons.ButtonTypes.MapCreator.Exceptions.TookTooLongException;
import UserInterface.Elements.Buttons.ButtonTypes.MapCreator.MapCreatorSubButton;
import UserInterface.Elements.Buttons.MyButton;
import UserInterface.Elements.ElementTypes;

public class CreateBlueLinearMap extends MapCreatorSubButton {
    public CreateBlueLinearMap(MyButton parent) {
        super(ElementTypes.MAP_CREATOR_CREATE_ONE_HANDED_SIMPLE_LINEAR_MAP_BUTTON, parent);
    }

    @Override
    public void onClick() {
        ui.manageMap();
        ui.map.toBlueLeftBottomRowDotTimings();

        Thread calculateNewMap = new Thread(() -> {
            BeatSaberMap map = new BeatSaberMap(CreatePatterns.linearSlowPattern(ui.map._notes, true, null, null));
            loadNewlyCreatedMap(map);
        });

        watchOverThread(calculateNewMap);
    }
}
