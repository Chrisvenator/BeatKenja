package UserInterface.Elements.Buttons.ButtonTypes.MapCreator.Button;

import BeatSaberObjects.Objects.BeatSaberMap;
import MapGeneration.GenerationElements.Pattern;
import UserInterface.Elements.Buttons.ButtonTypes.MapCreator.MapCreatorSubButton;
import UserInterface.Elements.Buttons.MyButton;
import UserInterface.Elements.ElementTypes;

import static DataManager.Parameters.logger;
import static MapGeneration.PatternGeneration.RandomV2FromTemplate.randomV2FromTemplate;


public class CreateRandomV2Map extends MapCreatorSubButton {
    public CreateRandomV2Map(MyButton parent) {
        super(ElementTypes.MAP_CREATOR_CREATE_RANDOM_MAP_V2_BUTTON, parent);
        logger.debug("RandomV2 button initialized.");
    }

    @Override
    public void onClick() {
        ui.manageMap();
        ui.map.toBlueLeftBottomRowDotTimings();

        try {
            BeatSaberMap map = new BeatSaberMap(randomV2FromTemplate(ui.map._notes, Pattern.adjustVariance(ui.pattern), false, null, null));
            loadNewlyCreatedMap(map);

        } catch (IllegalArgumentException ex) {
            printException(ex);
        }
    }
}
