package UserInterface.Elements.Buttons.ButtonTypes.MapCreator.Button;

import BeatSaberObjects.Objects.BeatSaberMap;
import MapGeneration.GenerationElements.Pattern;
import UserInterface.Elements.Buttons.ButtonTypes.MapCreator.Exceptions.MapDidntComputeException;
import UserInterface.Elements.Buttons.ButtonTypes.MapCreator.MapCreatorSubButton;
import UserInterface.Elements.Buttons.MyButton;
import UserInterface.Elements.ElementTypes;

import static DataManager.Parameters.verbose;
import static MapGeneration.CreateMap.createMap;
import static DataManager.Parameters.logger;

public class CreateMapButton extends MapCreatorSubButton {
    public CreateMapButton(MyButton parent) {
        super(ElementTypes.MAP_CREATOR_CREATE_MAP_BUTTON, parent);
        logger.debug("CreateMapButton initialized.");
    }

    @Override
    public void onClick() {
        ui.manageMap();
        ui.map.toBlueLeftBottomRowDotTimings();
        logger.info("Map management and initial processing done.");

        try {
            logger.trace("Original map: {}", ui.map.exportAsMap());

            BeatSaberMap map = createMap(ui.map, Pattern.adjustVariance(ui.pattern), false, false);

            if (ui.map.exportAsMap().split("\"_cutDirection\":8").length >= 20) logger.warn("There are a lot of errors in the map. It is recommended to try again.");

            if (map.equals(ui.map)) {
                logger.fatal("The map is the same as the original map!");
                throw new MapDidntComputeException("Something went wrong, Map didn't compute...");
            } else {
                loadNewlyCreatedMap(map);
                logger.info("New map created and loaded successfully.");
            }

        } catch (IllegalArgumentException | MapDidntComputeException ex) {
            printException(ex);
        }
    }
}
