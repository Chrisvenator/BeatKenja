package UserInterface.Elements.Buttons;

import UserInterface.Elements.MyElement;
import UserInterface.UserInterface;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import static DataManager.Parameters.logger;

/**
 * The `MyButton` class represents a customizable button within the user interface, extending the `JButton` component.
 * It implements the `MyElement` interface, allowing it to handle click events and manage associated child elements.
 * This button can be configured with specific text, position, size, and visibility based on the provided `ButtonType`.
 */
public class MyButton extends JButton implements MyElement {
    public final UserInterface ui;
    /** A list of child components associated with this button. These components' visibility can be toggled when the button is clicked.*/
    public final List<Component> childElements = new ArrayList<>();

    /**
     * Constructs a `MyButton` with the specified button type and user interface context.
     * The button is initialized with properties defined in the `ButtonType`, such as text, position, size, and visibility.
     * An action listener is added to handle click events, which toggles the visibility of child elements.
     *
     * @param button The `ButtonType` that defines the button's properties.
     * @param ui     The `UserInterface` instance that this button belongs to.
     */
    public MyButton(ButtonType button, UserInterface ui) {
        this.ui = ui;
        this.setText(button.text());
        this.setBounds(button.x(), button.y(), button.w(), button.h());
        this.setVisible(button.setVisible());

        this.addActionListener(e -> {
            onClick();
            logger.debug("{} clicked", this.getClass().getSimpleName());
            System.out.println(this.getClass().getSimpleName() + " clicked");
        });
        ui.add(this);
    }

    /**
     * Adds a child component to this button.
     * The child component's visibility can be toggled when the button is clicked.
     *
     * @param button The child component to be added.
     */
    public void addChild(Component button) {
        childElements.add(button);
        logger.debug("Added child: {} to: {}", button.getClass().getSimpleName(), this.getClass().getSimpleName());
    }

    /**
     * Handles the click event for this button.
     * Toggles the visibility of all child components when the button is clicked.
     */
    public void onClick() {
        childElements.forEach(element -> element.setVisible(!element.isVisible()));
    }

    /**
     * Logs and prints an error message when an exception occurs.
     * This method is used to handle exceptions that may arise during button operations.
     *
     * @param e            The exception that was caught.
     * @param errorMessage The error message to be logged and printed.
     */
    protected void printErrorMessage(Exception e, String errorMessage) {
        logger.error(errorMessage);
        System.err.println(e.getMessage());
        e.printStackTrace();
    }
}

