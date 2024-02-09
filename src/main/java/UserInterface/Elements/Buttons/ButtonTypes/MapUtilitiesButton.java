package UserInterface.Elements.Buttons.ButtonTypes;

import UserInterface.Elements.Buttons.ButtonTypes.MapUtilities.UtilsConvertAllFlashingLight;
import UserInterface.Elements.Buttons.ButtonTypes.MapUtilities.UtilsDeleteNoteType;
import UserInterface.Elements.Buttons.ButtonTypes.MapUtilities.UtilsFixPlacements;
import UserInterface.Elements.Buttons.ButtonTypes.MapUtilities.UtilsMakeIntoNoArrowMap;
import UserInterface.Elements.Buttons.MyButton;
import UserInterface.Elements.ElementTypes;
import UserInterface.Elements.TextFields.MyTextField;
import UserInterface.Elements.TextFields.TextFieldTypes.MapUtils.UtilsDeleteNoteTypeTextField;
import UserInterface.Elements.TextFields.TextFieldTypes.MapUtils.UtilsFixPlacementsTextField;
import UserInterface.UserInterface;

public class MapUtilitiesButton extends MyButton {
    public MapUtilitiesButton(UserInterface ui) {
        super(ElementTypes.MAP_UTILITIES_MAP_UTILS_BUTTON, ui);
        initChildren();
    }

    private void initChildren() {
        MyTextField utilsFixPlacementTextField = new UtilsFixPlacementsTextField(this);
        MyTextField utilsDeleteNoteTypeTextField = new UtilsDeleteNoteTypeTextField(this);

        this.addChild(utilsFixPlacementTextField);
        this.addChild(utilsDeleteNoteTypeTextField);

        this.addChild(new UtilsFixPlacements(this, utilsFixPlacementTextField));
        this.addChild(new UtilsDeleteNoteType(this, utilsFixPlacementTextField));
        this.addChild(new UtilsConvertAllFlashingLight(this));
        this.addChild(new UtilsDeleteNoteTypeTextField(this));
        this.addChild(new UtilsMakeIntoNoArrowMap(this));

    }
}
