package UserInterface.Elements.Buttons.ButtonTypes;

import UserInterface.Elements.Buttons.ButtonTypes.WaveGeneration.WaveGenerationGenerateWave;
import UserInterface.Elements.Buttons.MyButton;
import UserInterface.Elements.ElementTypes;
import UserInterface.UserInterface;

public class WaveGenerationButton extends MyButton {
    public WaveGenerationButton(UserInterface ui) {
        super(ElementTypes.WAVE_GENERATOR_BUTTON, ui);
        initChildren();
    }

    private void initChildren() {
        this.addChild(new WaveGenerationGenerateWave(this));
    }
}
