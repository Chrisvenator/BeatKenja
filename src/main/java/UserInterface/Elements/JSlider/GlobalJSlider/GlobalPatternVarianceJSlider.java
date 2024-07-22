package UserInterface.Elements.JSlider.GlobalJSlider;

import MapGeneration.GenerationElements.Pattern;
import UserInterface.Elements.ElementTypes;
import UserInterface.Elements.JSlider.MyGlobalJSlider;
import UserInterface.UserInterface;

import javax.swing.*;
import java.awt.*;
import java.util.Hashtable;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class GlobalPatternVarianceJSlider extends MyGlobalJSlider {
    public GlobalPatternVarianceJSlider(UserInterface ui) {
        super(ElementTypes.GLOBAL_PATTERN_VARIANCE_SLIDER, ui);

        setMajorTickSpacing(10);
        setMinorTickSpacing(10);

        setPaintTicks(true);
        setPaintLabels(true);

        // Create label table for specified values with custom color for 0 and 10
        Hashtable<Integer, JLabel> labelTable = new Hashtable<>();
        for (int i = -50; i <= 50; i += 10) {
            JLabel label = new JLabel(String.valueOf(i));
            if (i == 0) {
                label.setForeground(Color.BLUE);  // Set color to blue
            }
            labelTable.put(i, label);
        }

        setLabelTable(labelTable);
    }


    @Override
    public void onClick() {
        int value = ((JSlider) eventObject.getSource()).getValue();
        UserInterface.patternVariance = value * 10;
//        ui.pattern = Pattern.adjustVariance(patternWithoutVariance, value);

        System.out.println("Slider value (float): " + value);
        ui.statusCheck.append("Pattern Variance set to " + value + "\n");
    }
}
