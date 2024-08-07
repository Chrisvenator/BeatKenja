package UserInterface.Elements.Buttons.ButtonTypes.MapCreator.Button;

import BeatSaberObjects.Objects.BeatSaberMap;
import MapGeneration.GenerationElements.Pattern;
import UserInterface.Elements.Buttons.ButtonTypes.MapCreator.MapCreatorSubButton;
import UserInterface.Elements.Buttons.MyButton;
import UserInterface.Elements.ElementTypes;
import UserInterface.UserInterface;

import java.util.ArrayList;
import java.util.List;

import static DataManager.Parameters.logger;
import static MapGeneration.PatternGeneration.RandomV2FromTemplate.randomV2FromTemplate;

public class CreateRandomV2Map extends MapCreatorSubButton {
    public CreateRandomV2Map(MyButton parent) {
        super(ElementTypes.MAP_CREATOR_CREATE_RANDOM_MAP_V2_BUTTON, parent);
        logger.debug("RandomV2 button initialized.");
    }

    @Override
    public void onClick() {
        List<BeatSaberMap> maps = new ArrayList<>();
        ui.manageMap();
        for (BeatSaberMap uiMap : ui.map) {
            UserInterface.currentDiff = uiMap.difficultyFileName;
            uiMap.toBlueLeftBottomRowDotTimings();

            try {
                maps.add(new BeatSaberMap(randomV2FromTemplate(uiMap._notes, Pattern.adjustVariance(ui.pattern), false, null, null)));
            }
            catch (IllegalArgumentException ex) {
                printException(ex);
            }
        }
        loadNewlyCreatedMaps(maps);
    }
}
