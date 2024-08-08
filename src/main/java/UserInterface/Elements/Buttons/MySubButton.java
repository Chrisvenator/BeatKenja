package UserInterface.Elements.Buttons;

import BeatSaberObjects.Objects.BeatSaberMap;
import BeatSaberObjects.Objects.Bookmark;
import UserInterface.UserInterface;

import java.util.Arrays;
import java.util.HashSet;
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
            BeatSaberMap map = newmap.get(i);   //New Map

            if (ui.map.get(i).equals(map) || new HashSet<>(Arrays.stream(ui.map.get(i)._notes).toList()).containsAll(Arrays.stream(map._notes).toList())) {
                logger.error("Map couldn't be loaded!");
                System.err.println("Map couldn't be loaded!");
            }

            logger.info("Checking map: {}", ui.map.get(i).difficultyFileName);

            //Copy Original Json from Original Map
            String ogJson = ui.map.get(i).originalJSON;
            String diffName = ui.map.get(i).difficultyFileName;
            ui.map.set(i, map);
            ui.map.get(i).originalJSON = ogJson;
            ui.map.get(i).bookmarks = ui.map.get(i).calculateBookmarks();
            ui.map.get(i).difficultyFileName = diffName;
            UserInterface.checkMap(ui.map.get(i));

            if (SAVE_PARITY_ERRORS_AS_BOOKMARKS) {
                List<Bookmark> bookmarks = ui.parityErrorsAsBookmarks(ui.map.get(i).difficultyFileName);
                ui.map.get(i).bookmarks.addAll(bookmarks);
            }

            logger.debug("Map creation finished");
            System.out.println("Created Map: " + ui.map.get(i).exportAsMap());
            logger.debug("Created Map: {}", ui.map.get(i).exportAsMap());

            logger.info("Newly created map loaded!\n\n");
        }

        UserInterface.currentDiff = "NULL";
    }
}
