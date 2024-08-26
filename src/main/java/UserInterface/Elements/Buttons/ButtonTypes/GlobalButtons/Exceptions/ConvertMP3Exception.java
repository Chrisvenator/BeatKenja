package UserInterface.Elements.Buttons.ButtonTypes.GlobalButtons.Exceptions;

import lombok.experimental.StandardException;

import java.io.IOException;
@StandardException
public class ConvertMP3Exception extends IOException {
    public ConvertMP3Exception(String message) {
        super(message);
    }
}
