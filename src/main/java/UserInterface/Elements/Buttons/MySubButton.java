package UserInterface.Elements.Buttons;

import BeatSaberObjects.Objects.BeatSaberMap;
import BeatSaberObjects.Objects.Bookmark;
import UserInterface.UserInterface;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import static DataManager.Parameters.SAVE_PARITY_ERRORS_AS_BOOKMARKS;
import static DataManager.Parameters.logger;

/**
 * The `MySubButton` class is an abstract extension of `MyButton`, designed to handle specific button actions within a parent button context.
 * It adds functionality to load newly created maps into the user interface and manage their state, including error checking and bookmark handling.
 */
public abstract class MySubButton extends MyButton {

    /**
     * Constructs a `MySubButton` with the specified button type and parent button.
     * This button is initialized within the context of the parent button's user interface.
     *
     * @param button The `ButtonType` that defines the button's properties.
     * @param parent The parent `MyButton` instance that this sub-button is associated with.
     */
    public MySubButton(ButtonType button, MyButton parent) {
        super(button, parent.ui);
        logger.debug("MySubButton initialized with button type: {}", button);
    }

    /**
     * Loads newly created maps into the user interface, replacing existing maps if necessary.
     * This method checks for parity errors and adds bookmarks if the relevant setting is enabled.
     * The process includes copying original JSON data, setting bookmarks, and logging the results.
     *
     * @param newmap A list of `BeatSaberMap` objects representing the newly created maps.
     */
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
