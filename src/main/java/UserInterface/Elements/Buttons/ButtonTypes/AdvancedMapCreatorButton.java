package UserInterface.Elements.Buttons.ButtonTypes;

import UserInterface.Elements.Buttons.ButtonTypes.AdvancedMapCreation.CreateAdvancedMapButton;
import UserInterface.Elements.Buttons.ButtonTypes.AdvancedMapCreation.DropDowns.CreateAdvancedMapDropDownMenuDifficulty;
import UserInterface.Elements.Buttons.ButtonTypes.AdvancedMapCreation.DropDowns.CreateAdvancedMapDropDownMenuGenres;
import UserInterface.Elements.Buttons.ButtonTypes.AdvancedMapCreation.DropDowns.CreateAdvancedMapDropDownMenuTags;
import UserInterface.Elements.Buttons.ButtonTypes.AdvancedMapCreation.TextFields.CreateAdvancedMapBPM;
import UserInterface.Elements.Buttons.ButtonTypes.AdvancedMapCreation.TextFields.CreateAdvancedMapNPS;
import UserInterface.Elements.Buttons.MyButton;
import UserInterface.Elements.ElementTypes;
import UserInterface.UserInterface;


/**
 * Represents the button for advanced map creation in the user interface.
 * This button initializes various child elements used for creating an advanced map,
 * including text fields, dropdown menus, and the create button itself.
 */
public class AdvancedMapCreatorButton extends MyButton {
    public AdvancedMapCreatorButton(UserInterface ui) {
        super(ElementTypes.ADVANCED_MAP_CREATOR_BUTTON, ui);

        initChildren();
    }

    public void initChildren() {
        CreateAdvancedMapNPS npsField = new CreateAdvancedMapNPS(this);
        CreateAdvancedMapBPM bpmField = new CreateAdvancedMapBPM(this);

        this.addChild(npsField);
        this.addChild(bpmField);

        this.addChild(new CreateAdvancedMapButton(this, npsField, bpmField));
        this.addChild(new CreateAdvancedMapDropDownMenuGenres(this, 1));
        this.addChild(new CreateAdvancedMapDropDownMenuGenres(this, 2));
        this.addChild(new CreateAdvancedMapDropDownMenuTags(this, 1));
        this.addChild(new CreateAdvancedMapDropDownMenuTags(this, 2));
        this.addChild(new CreateAdvancedMapDropDownMenuDifficulty(this));
    }
}
