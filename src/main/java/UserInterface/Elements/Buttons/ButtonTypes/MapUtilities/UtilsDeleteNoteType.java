package UserInterface.Elements.Buttons.ButtonTypes.MapUtilities;

import BeatSaberObjects.Objects.BeatSaberMap;
import UserInterface.Elements.Buttons.MyButton;
import UserInterface.Elements.Buttons.MySubButton;
import UserInterface.Elements.ElementTypes;
import UserInterface.Elements.TextFields.MyTextField;

import static DataManager.Parameters.logger;

/**
 * The `UtilsDeleteNoteType` class is a specialized sub-button that extends `MySubButton`.
 * This button allows the user to delete all notes of a specific type in every `BeatSaberMap` loaded in the user interface.
 * The note type to be deleted is specified via a `MyTextField` input.
 * When the button is clicked, the specified note type is removed, and the results are logged.
 */
public class UtilsDeleteNoteType extends MySubButton {
    private final MyTextField makeOneHandDeleteType;

    public UtilsDeleteNoteType(MyButton parent, MyTextField makeOneHandDeleteType) {
        super(ElementTypes.MAP_UTILITIES_DELETE_NOTE_TYPES_BUTTON, parent);
        this.makeOneHandDeleteType = makeOneHandDeleteType;
    }

    @Override
    public void onClick() {
        for (BeatSaberMap uiMap : ui.map) {

            uiMap.makeOneHanded((int) Math.round(Double.parseDouble(makeOneHandDeleteType.getText())));
            logger.info("Removed All Notes with type: {}", makeOneHandDeleteType.getText());
            logger.debug("One handed diff: : {}", new BeatSaberMap(uiMap._notes).exportAsMap());
            System.out.println("One handed diff: : " + new BeatSaberMap(uiMap._notes).exportAsMap());
        }
    }
}
