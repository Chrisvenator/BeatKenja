package UserInterface.Elements.Buttons.ButtonTypes;

import UserInterface.Elements.Buttons.ButtonType;
import UserInterface.Elements.Buttons.ButtonTypes.GlobalButtons.Buttons.*;
import UserInterface.Elements.Buttons.ButtonTypes.MapChecks.MapChecks;
import UserInterface.Elements.Buttons.MyButton;
import UserInterface.Elements.TextFields.GlobalTextFields.GlobalBPMField;
import UserInterface.Elements.TextFields.GlobalTextFields.GlobalSeedFrame;
import UserInterface.UserInterface;

public class GlobalButton extends MyButton {
    public final GlobalBPMField globalBPMField;

    public GlobalButton(UserInterface ui) {
        //Create a quasi-button so that the other global buttons can inherit from it
        super(new ButtonType("", -1, -1, 0, 0, false), ui);
        globalBPMField = new GlobalBPMField(ui);
//        init();
    }

    public GlobalButton init() {
        this.addChild(new GlobalOpenMapButton(ui));
        this.addChild(globalBPMField);
        this.addChild(new GlobalConvertMP3ToMaps(ui));
        this.addChild(new GlobalOpenFolder(ui));
        this.addChild(new GlobalLoadPatterns(ui));
        this.addChild(new GlobalShowPatterns(ui));
        this.addChild(new MapChecks(ui));
        this.addChild(new GlobalSeedFrame(ui));

        return this;
    }
}
