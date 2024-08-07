package UserInterface.Elements.Buttons.ButtonTypes.MapCreator.Button;

import BeatSaberObjects.Objects.BeatSaberMap;
import MapGeneration.GenerationElements.Pattern;
import UserInterface.Elements.Buttons.ButtonTypes.MapCreator.Exceptions.MapDidntComputeException;
import UserInterface.Elements.Buttons.ButtonTypes.MapCreator.MapCreatorSubButton;
import UserInterface.Elements.Buttons.MyButton;
import UserInterface.Elements.ElementTypes;
import UserInterface.UserInterface;

import java.util.ArrayList;
import java.util.List;

import static DataManager.Parameters.logger;
import static MapGeneration.CreateMap.createMap;

public class CreateMapButton extends MapCreatorSubButton {
    public CreateMapButton(MyButton parent) {
        super(ElementTypes.MAP_CREATOR_CREATE_MAP_BUTTON, parent);
        logger.debug("CreateMapButton initialized.");
    }

    @Override
    public void onClick() {
        List<BeatSaberMap> maps = new ArrayList<>();
        ui.manageMap();
        for (BeatSaberMap uiMap : ui.map) {
            UserInterface.currentDiff = uiMap.difficultyFileName;
            uiMap.toBlueLeftBottomRowDotTimings();
            logger.info("Map management and initial processing done.");

            try {
                logger.trace("Original map: {}", uiMap.exportAsMap());

                BeatSaberMap map = createMap(uiMap, Pattern.adjustVariance(ui.pattern), false, false);

                if (uiMap.exportAsMap().split("\"_cutDirection\":8").length >= 20)
                    logger.warn("There are a lot of errors in the map. It is recommended to try again.");

                if (map.equals(uiMap)) {
                    logger.fatal("The map is the same as the original map!");
                    throw new MapDidntComputeException("Something went wrong, Map didn't compute...");
                }
                else {
                    maps.add(map);
                }
            }
            catch (IllegalArgumentException | MapDidntComputeException ex) {
                printException(ex);
            }
        }
        loadNewlyCreatedMaps(maps);
    }
}
