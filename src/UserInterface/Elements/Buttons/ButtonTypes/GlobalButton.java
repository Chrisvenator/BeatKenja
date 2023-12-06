package UserInterface.Elements.Buttons.ButtonTypes;

import UserInterface.Elements.Buttons.ButtonType;
import UserInterface.Elements.Buttons.ButtonTypes.GlobalButtons.Buttons.*;
import UserInterface.Elements.Buttons.ButtonTypes.MapChecks.MapChecks;
import UserInterface.Elements.Buttons.MyButton;
import UserInterface.Elements.TextFields.GlobalTextFields.GlobalSeedFrame;
import UserInterface.UserInterface;

public class GlobalButton extends MyButton {

    public GlobalButton(UserInterface ui) {
        super(new ButtonType("", -1, -1, 0, 0, false), ui);
        init();
    }

    private void init() {
        this.addChild(new GlobalOpenMapButton(ui));
        this.addChild(new GlobalConvertMP3ToMaps(ui));
        this.addChild(new GlobalOpenFolder(ui));
        this.addChild(new GlobalLoadPatterns(ui));
        this.addChild(new MapChecks(ui));
        this.addChild(new GlobalSeedFrame(ui));
    }
}
