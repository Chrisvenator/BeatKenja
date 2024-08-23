package UserInterface.Elements;

/**
 * The `MyElement` interface defines a contract for UI elements that can respond to click events.
 * Any class implementing this interface must provide an implementation for the `onClick` method,
 * which will be invoked when the element is clicked.
 */
public interface MyElement {
    /**
     * Handles the click event for the implementing UI element.
     * This method is expected to be called when the element is interacted with by the user, typically through a mouse click.
     */
    void onClick();
}
