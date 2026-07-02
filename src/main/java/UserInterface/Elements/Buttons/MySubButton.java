package UserInterface.Elements.Buttons;

import BeatSaberObjects.Objects.BeatSaberMap;

import java.util.List;

import static DataManager.Parameters.logger;

/**
 * The `MySubButton` class is an abstract extension of `MyButton`, designed to handle specific button actions within a parent button context.
 * Loading newly created maps is delegated to the AppController, which owns the map session.
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
     * Hands the newly generated maps over to the AppController, which replaces the session
     * maps, runs the parity checks, and adds parity bookmarks if configured.
     *
     * @param newmap A list of `BeatSaberMap` objects representing the newly created maps.
     */
    protected void loadNewlyCreatedMaps(List<BeatSaberMap> newmap) {
        ui.controller.acceptGeneratedMaps(newmap);
    }
}