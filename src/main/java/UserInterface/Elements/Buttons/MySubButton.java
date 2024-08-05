package UserInterface.Elements.Buttons;

import BeatSaberObjects.Objects.BeatSaberMap;

import static DataManager.Parameters.logger;
import static DataManager.Parameters.verbose;

public abstract class MySubButton extends MyButton {
    public MySubButton(ButtonType button, MyButton parent) {
        super(button, parent.ui);
        logger.debug("MySubButton initialized with button type: {}", button);
    }

    protected void loadNewlyCreatedMap(BeatSaberMap map) {
        String ogJson = ui.map.originalJSON;
        ui.map = map;
        ui.map.originalJSON = ogJson;
        ui.map.bookmarks = ui.map.calculateBookmarks();

        logger.info("Map creation finished");
        System.out.println("Created Map: " + ui.map.exportAsMap());
        logger.debug("Created Map: {}", ui.map.exportAsMap());

        ui.checkMap();
        logger.info("Newly created map loaded checked successfully.");
    }
}
