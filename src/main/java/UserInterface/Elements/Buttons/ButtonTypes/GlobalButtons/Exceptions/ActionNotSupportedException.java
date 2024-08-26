package UserInterface.Elements.Buttons.ButtonTypes.GlobalButtons.Exceptions;

import lombok.experimental.StandardException;

@StandardException
public class ActionNotSupportedException extends Exception {
    public ActionNotSupportedException(String message) {
        super(message);
    }
}
