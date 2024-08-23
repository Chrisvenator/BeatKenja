package UserInterface.Elements.Buttons.ButtonTypes.MapCreator.Button;

import BeatSaberObjects.Objects.BeatSaberMap;
import MapGeneration.GenerationElements.Pattern;
import UserInterface.Elements.Buttons.ButtonTypes.MapCreator.MapCreatorSubButton;
import UserInterface.Elements.Buttons.MyButton;
import UserInterface.Elements.ElementTypes;
import UserInterface.UserInterface;

import java.util.ArrayList;
import java.util.List;

import static MapGeneration.ComplexPattern.complexPattern;

public class CreateBlueComplexMap extends MapCreatorSubButton {
    public CreateBlueComplexMap(MyButton parent) {
        super(ElementTypes.MAP_CREATOR_CREATE_ONE_HANDED_COMPLEX_MAP_BUTTON, parent);
    }

    @Override
    public void onClick() {
        List<BeatSaberMap> maps = new ArrayList<>();
        ui.manageMap();
        for (BeatSaberMap uiMap : ui.map) {
            UserInterface.currentDiff = uiMap.difficultyFileName;
            uiMap.toBlueLeftBottomRowDotTimings();

            try {
                maps.add(new BeatSaberMap(complexPattern(List.of(uiMap._notes), Pattern.adjustVariance(ui.pattern), UserInterface.easyPattern, true, true, false, false, null, null)));

            }
            catch (IllegalArgumentException ex) {
                printException(ex);
            }
        }
        loadNewlyCreatedMaps(maps);
    }
}
