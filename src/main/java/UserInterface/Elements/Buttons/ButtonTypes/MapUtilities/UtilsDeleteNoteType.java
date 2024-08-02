package UserInterface.Elements.Buttons.ButtonTypes.MapUtilities;

import BeatSaberObjects.Objects.BeatSaberMap;
import UserInterface.Elements.Buttons.MyButton;
import UserInterface.Elements.Buttons.MySubButton;
import UserInterface.Elements.ElementTypes;
import UserInterface.Elements.TextFields.MyTextField;

import static DataManager.Parameters.logger;
import static DataManager.Parameters.verbose;

public class UtilsDeleteNoteType extends MySubButton {
    private final MyTextField makeOneHandDeleteType;

    public UtilsDeleteNoteType(MyButton parent, MyTextField makeOneHandDeleteType) {
        super(ElementTypes.MAP_UTILITIES_DELETE_NOTE_TYPES_BUTTON, parent);
        this.makeOneHandDeleteType = makeOneHandDeleteType;
    }

    @Override
    public void onClick() {
        ui.map.makeOneHanded((int) Math.round(Double.parseDouble(makeOneHandDeleteType.getText())));
        logger.info("Removed All Notes with type: {}", makeOneHandDeleteType.getText());
        logger.debug("One handed diff: : {}", new BeatSaberMap(ui.map._notes).exportAsMap());
    }
}
