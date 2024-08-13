package UserInterface.Elements.TextFields.GlobalTextFields;

import DataManager.Parameters;
import UserInterface.Elements.ElementTypes;
import UserInterface.Elements.TextFields.MyGlobalTextField;
import UserInterface.UserInterface;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;

import static DataManager.Parameters.SEED;
import static DataManager.Parameters.logger;

public class GlobalSeedFrame extends MyGlobalTextField {

    public GlobalSeedFrame(UserInterface ui) {
        super(ElementTypes.GLOBAL_SEED_FIELD, ui);

        JLabel seedLabel = new JLabel("Seed:");
        seedLabel.setBounds(960, 17, 40, 20);
        if (Parameters.DARK_MODE) seedLabel.setForeground(Color.white);
        ui.add(seedLabel);

        this.addKeyListener(new SeedKeyListener("Seed"));
        logger.debug("GlobalSeedFrame initialized: {}", SEED);
    }

    private class SeedKeyListener extends NumericKeyListener {
        public SeedKeyListener(String name) {
            super(name, 18, ElementTypes.GLOBAL_SEED_FIELD.value());
        }

        @Override
        public void keyReleased(KeyEvent e) {
            super.setLabelText(GlobalSeedFrame.this.getText());

            long value = getLongValue();
            if (value >= 0) SEED = value;
        }
    }
}
