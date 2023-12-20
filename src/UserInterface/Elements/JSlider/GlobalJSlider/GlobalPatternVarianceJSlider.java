package UserInterface.Elements.JSlider.GlobalJSlider;

import UserInterface.Elements.ElementTypes;
import UserInterface.Elements.JSlider.MyGlobalJSlider;
import UserInterface.Elements.JSlider.SliderTypes;
import UserInterface.UserInterface;

import javax.swing.*;
import java.util.EventObject;
import java.util.Hashtable;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class GlobalPatternVarianceJSlider extends MyGlobalJSlider {
    public GlobalPatternVarianceJSlider(UserInterface ui) {
        super(ElementTypes.GLOBAL_PATTERN_VARIANCE_SLIDER, ui);

        setMajorTickSpacing(10);
        setMinorTickSpacing(1);
        setPaintTicks(true);
        setPaintLabels(true);

        setLabelTable(new Hashtable<>(IntStream.iterate(0, i -> i <= 100, i -> i + 10)
                .boxed()
                .collect(Collectors.toMap(
                        i -> i,
                        i -> new JLabel(String.valueOf(i / 10f)))
                )
        ));
    }


    @Override
    public void onClick() {
        float value = ((JSlider) eventObject.getSource()).getValue() / 10f;
        ui.patternVariance = value;
        System.out.println("Slider value (float): " + value);
        ui.statusCheck.append("Pattern Variance set to " + value + "\n");
    }
}
