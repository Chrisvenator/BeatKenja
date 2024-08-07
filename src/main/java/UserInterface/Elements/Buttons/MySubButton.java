package UserInterface.Elements.Buttons;

import BeatSaberObjects.Objects.BeatSaberMap;
import BeatSaberObjects.Objects.Bookmark;
import UserInterface.UserInterface;

import java.util.List;

import static DataManager.Parameters.SAVE_PARITY_ERRORS_AS_BOOKMARKS;
import static DataManager.Parameters.logger;

public abstract class MySubButton extends MyButton {
    public MySubButton(ButtonType button, MyButton parent) {
        super(button, parent.ui);
        logger.debug("MySubButton initialized with button type: {}", button);
    }

    protected void loadNewlyCreatedMaps(List<BeatSaberMap> newmap) {
        for (int i = 0; i < newmap.size(); i++) {
            BeatSaberMap uimap = ui.map.get(i);
            BeatSaberMap map = newmap.get(i);

            logger.info("Checking map: {}", uimap.difficultyFileName);

            String ogJson = uimap.originalJSON;
            String diffName = uimap.difficultyFileName;
            uimap = map;
            uimap.originalJSON = ogJson;
            uimap.bookmarks = uimap.calculateBookmarks();
            uimap.difficultyFileName = diffName;
            UserInterface.checkMap(uimap);

            if (SAVE_PARITY_ERRORS_AS_BOOKMARKS) {
                List<Bookmark> bookmarks = ui.parityErrorsAsBookmarks(uimap.difficultyFileName);
                uimap.bookmarks.addAll(bookmarks);
            }

            logger.debug("Map creation finished");
            System.out.println("Created Map: " + uimap.exportAsMap());
            logger.debug("Created Map: {}", uimap.exportAsMap());

            logger.info("Newly created map loaded!\n\n");
        }

        UserInterface.currentDiff = "NULL";
    }
}
