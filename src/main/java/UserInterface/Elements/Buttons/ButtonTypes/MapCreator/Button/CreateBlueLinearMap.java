package UserInterface.Elements.Buttons.ButtonTypes.MapCreator.Button;

import BeatSaberObjects.Objects.BeatSaberMap;
import UserInterface.Elements.Buttons.ButtonTypes.MapCreator.MapCreatorSubButton;
import UserInterface.Elements.Buttons.MyButton;
import UserInterface.Elements.ElementTypes;
import UserInterface.UserInterface;

import java.util.ArrayList;
import java.util.List;

import static MapGeneration.PatternGeneration.LinearSlowPattern.linearSlowPattern;

public class CreateBlueLinearMap extends MapCreatorSubButton {
    public CreateBlueLinearMap(MyButton parent) {
        super(ElementTypes.MAP_CREATOR_CREATE_ONE_HANDED_SIMPLE_LINEAR_MAP_BUTTON, parent);
    }

    @Override
    public void onClick() {
        List<BeatSaberMap> maps = new ArrayList<>();
        ui.manageMap();
        for (BeatSaberMap uiMap : ui.map) {
            UserInterface.currentDiff = uiMap.difficultyFileName;
            uiMap.toBlueLeftBottomRowDotTimings();

            Thread calculateNewMap = new Thread(() -> {
                BeatSaberMap map = new BeatSaberMap(linearSlowPattern(List.of(uiMap._notes), true, null, null));
                maps.add(map);
            });

            watchOverThread(calculateNewMap);
        }
        loadNewlyCreatedMaps(maps);
    }
}