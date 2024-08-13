package UserInterface.Elements.TextFields.GlobalTextFields;

import DataManager.Parameters;
import UserInterface.Elements.ElementTypes;
import UserInterface.Elements.TextFields.MyGlobalTextField;
import UserInterface.UserInterface;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;

import static DataManager.Parameters.BPM;
import static DataManager.Parameters.logger;

public class GlobalBPMField extends MyGlobalTextField {

    public GlobalBPMField(UserInterface ui) {
        super(ElementTypes.GLOBAL_BPM_FIELD, ui);

        JLabel label = new JLabel("BPM:");
        label.setBounds(60, 50, 40, 20);
        if (Parameters.DARK_MODE) label.setForeground(Color.white);
        ui.add(label);

        this.addKeyListener(new BPMKeyListener("BPM"));
        logger.debug("GlobalBPMField initialized: {}", BPM);
    }

    public void setBPM(double bpm){
        this.setText(bpm + "");
    }

    private class BPMKeyListener extends NumericKeyListener {
        public BPMKeyListener(String name) {
            super(name, 18, "abc");
        }

        @Override
        public void keyReleased(KeyEvent e) {
            super.setLabelText(GlobalBPMField.this.getText());

            double value = getDoubleValue();
            if (value >= 0) {
                BPM = value;
                setBPM(BPM);
            }
        }
    }
}
