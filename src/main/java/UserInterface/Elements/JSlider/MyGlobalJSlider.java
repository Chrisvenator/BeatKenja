package UserInterface.Elements.JSlider;

import UserInterface.Elements.MyElement;
import UserInterface.UserInterface;

import javax.swing.*;
import java.util.EventObject;

public abstract class MyGlobalJSlider extends JSlider implements MyElement {
    protected final UserInterface ui;
    protected EventObject eventObject;

    public MyGlobalJSlider(SliderTypes sliderTypes, UserInterface ui) {
        super(sliderTypes.min(), sliderTypes.max(), sliderTypes.value());

        this.ui = ui;

        setBounds(sliderTypes.x(), sliderTypes.y(), sliderTypes.w(), sliderTypes.h());
        setVisible(sliderTypes.setVisible());
        ui.add(this);

        this.addChangeListener(e -> {
            JSlider source = (JSlider) e.getSource();
            if (!source.getValueIsAdjusting()) {
                System.out.println(this.getClass().getSimpleName() + " clicked");
                eventObject = e;
                onClick();
            }
        });
    }
}
