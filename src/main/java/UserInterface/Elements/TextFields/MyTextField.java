package UserInterface.Elements.TextFields;

import UserInterface.Elements.Buttons.MyButton;

/**
 * The `MyTextField` class represents a specific type of text field that extends the functionality of `MyGlobalTextField`.
 * This class is typically associated with a `MyButton` instance, allowing the text field to interact with the button's parent user interface.
 */
public class MyTextField extends MyGlobalTextField {
    /**
     * Constructs a `MyTextField` with the specified text field type and parent button.
     * The text field is initialized based on the settings defined in the `TextFieldType` and is associated with the user interface of the parent button.
     *
     * @param textFieldType The type of the text field, which determines its initial configuration.
     * @param parent        The `MyButton` instance that acts as the parent of this text field.
     */
    public MyTextField(TextFieldType textFieldType, MyButton parent) {
        super(textFieldType, parent.ui);
    }
}
