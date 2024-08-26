package UserInterface.Elements.Buttons.ButtonTypes.GlobalButtons.Exceptions;

import lombok.experimental.StandardException;

@StandardException
public class MapHasWrongFormatException extends Exception {
    public MapHasWrongFormatException(String message) {
        super(message);
    }
}
