package UserInterface.Elements.Buttons.ButtonTypes.MapCreator.Button;

import BeatSaberObjects.Objects.BeatSaberMap;
import UserInterface.Elements.Buttons.ButtonTypes.MapCreator.MapCreatorSubButton;
import UserInterface.Elements.Buttons.MyButton;
import UserInterface.Elements.ElementTypes;
import UserInterface.UserInterface;

import java.util.ArrayList;
import java.util.List;

import static MapGeneration.PatternGeneration.RandomPattern.createRandomPattern;

public class CreateRandomMap extends MapCreatorSubButton {
    public CreateRandomMap(MyButton parent) {
        super(ElementTypes.MAP_CREATOR_CREATE_RANDOM_MAP_BUTTON, parent);
    }

    @Override
    public void onClick() {
        List<BeatSaberMap> maps = new ArrayList<>();
        ui.manageMap();
        for (BeatSaberMap uiMap : ui.map) {
            UserInterface.currentDiff = uiMap.difficultyFileName;
            uiMap.toBlueLeftBottomRowDotTimings();

            try {
                maps.add(new BeatSaberMap(createRandomPattern(uiMap._notes, false)));
            }
            catch (IllegalArgumentException ex) {
                printException(ex);
            }
        }
        loadNewlyCreatedMaps(maps);
    }
}
