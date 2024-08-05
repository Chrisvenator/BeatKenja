package UserInterface.Elements.Buttons;

import BeatSaberObjects.Objects.BeatSaberMap;
import BeatSaberObjects.Objects.Bookmark;
import DataManager.Parameters;

import java.util.List;

import static DataManager.Parameters.SAVE_PARITY_ERRORS_AS_BOOKMARKS;
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
        ui.checkMap();

        if (SAVE_PARITY_ERRORS_AS_BOOKMARKS){
            List<Bookmark> bookmarks = ui.parityErrorsAsBookmarks();
            ui.map.bookmarks.addAll(bookmarks);
        }

        logger.info("Map creation finished");
        System.out.println("Created Map: " + ui.map.exportAsMap());
        logger.debug("Created Map: {}", ui.map.exportAsMap());

        logger.info("Newly created map loaded.");
    }
}
