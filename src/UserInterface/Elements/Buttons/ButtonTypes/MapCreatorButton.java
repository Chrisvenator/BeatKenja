package UserInterface.Elements.Buttons.ButtonTypes;

import UserInterface.Elements.Buttons.ButtonTypes.MapCreator.Button.*;
import UserInterface.Elements.Buttons.MyButton;
import UserInterface.Elements.ElementTypes;
import UserInterface.UserInterface;

public class MapCreatorButton extends MyButton {
    public MapCreatorButton(UserInterface ui) {
        super(ElementTypes.MAP_CREATOR_BUTTON, ui);

        initChildren();
    }

    public void initChildren() {
        this.addChild(new CreateMapButton(this));
        this.addChild(new CreateComplexMap(this));
        this.addChild(new CreateRandomV2Map(this));
        this.addChild(new CreateLinearMap(this));
        this.addChild(new CreateBlueLinearMap(this));
        this.addChild(new CreateBlueComplexMap(this));
        this.addChild(new CreateRandomMap(this));
    }
}
