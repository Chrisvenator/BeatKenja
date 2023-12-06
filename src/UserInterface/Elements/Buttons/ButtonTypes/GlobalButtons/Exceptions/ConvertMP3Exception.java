package UserInterface.Elements.Buttons.ButtonTypes.GlobalButtons.Exceptions;

import java.io.IOException;

public class ConvertMP3Exception extends IOException {
    public ConvertMP3Exception(String message) {
        super(message);
    }

    public ConvertMP3Exception(String message, Throwable cause) {
        super(message, cause);
    }
}
