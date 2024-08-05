package UserInterface.Elements.Buttons.ButtonTypes.MapCreator.Button;

import BeatSaberObjects.Objects.BeatSaberMap;
import DataManager.Parameters;
import MapGeneration.GenerationElements.Pattern;
import UserInterface.Elements.Buttons.ButtonTypes.MapCreator.MapCreatorSubButton;
import UserInterface.Elements.Buttons.MyButton;
import UserInterface.Elements.ElementTypes;

import java.util.List;

import static MapGeneration.ComplexPatternFromTemplate.complexPatternFromTemplate;

public class CreateBlueComplexMap extends MapCreatorSubButton {
    public CreateBlueComplexMap(MyButton parent) {
        super(ElementTypes.MAP_CREATOR_CREATE_ONE_HANDED_COMPLEX_MAP_BUTTON, parent);
    }

    @Override
    public void onClick() {
        ui.manageMap();
        ui.map.toBlueLeftBottomRowDotTimings();

        try {
            BeatSaberMap map = new BeatSaberMap(complexPatternFromTemplate(List.of(ui.map._notes), Pattern.adjustVariance(ui.pattern), true, false, false,null, null));
            loadNewlyCreatedMap(map);

        } catch (IllegalArgumentException ex) {
            printException(ex);
        }
    }
}
