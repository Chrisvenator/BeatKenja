package UserInterface.Elements.Buttons.ButtonTypes.MapCreator.Button;

import BeatSaberObjects.Objects.BeatSaberMap;
import UserInterface.Elements.Buttons.ButtonTypes.MapCreator.MapCreatorSubButton;
import UserInterface.Elements.Buttons.MyButton;
import UserInterface.Elements.ElementTypes;

import java.util.List;

import static MapGeneration.PatternGeneration.LinearSlowPattern.linearSlowPattern;

public class CreateLinearMap extends MapCreatorSubButton {
    public CreateLinearMap(MyButton parent) {
        super(ElementTypes.MAP_CREATOR_CREATE_LINEAR_MAP_BUTTON, parent);
    }

    @Override
    public void onClick() {
        ui.manageMap();
        ui.map.toBlueLeftBottomRowDotTimings();

        Thread calculateNewMap = new Thread(() -> {
            BeatSaberMap map = new BeatSaberMap(linearSlowPattern(List.of(ui.map._notes), false, null, null));
            loadNewlyCreatedMap(map);

        });

        watchOverThread(calculateNewMap);
    }
}
