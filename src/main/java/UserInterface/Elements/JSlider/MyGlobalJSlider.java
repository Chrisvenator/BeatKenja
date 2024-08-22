package UserInterface.Elements.JSlider;

import UserInterface.Elements.MyElement;
import UserInterface.UserInterface;

import javax.swing.*;
import java.util.EventObject;

/**
 * The `MyGlobalJSlider` class is an abstract extension of `JSlider` that integrates with the `MyElement` interface.
 * It is designed to be part of the user interface, specifically within the context of a `UserInterface` instance.
 * This slider component automatically handles changes in value and triggers an `onClick` event when the slider is adjusted.
 */
public abstract class MyGlobalJSlider extends JSlider implements MyElement {
    protected final UserInterface ui;
    /** An event object that captures the last event associated with the slider, typically a change in value.*/
    protected EventObject eventObject;

    /**
     * Constructs a `MyGlobalJSlider` with the specified slider type and user interface context.
     * The slider is initialized with the parameters defined in `SliderTypes`, such as minimum, maximum, and initial values.
     * It also adds itself to the user interface and sets up a listener to handle value changes.
     *
     * @param sliderTypes The type of the slider, determining its configuration.
     * @param ui          The `UserInterface` instance that this slider belongs to.
     */
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
