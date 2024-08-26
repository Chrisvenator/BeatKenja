package UserInterface.Elements.Buttons.ButtonTypes.MapCreator.Exceptions;

import lombok.experimental.StandardException;

@StandardException
public class MapDidntComputeException extends Exception{
    public MapDidntComputeException(String message) {
        super(message);
    }
}
