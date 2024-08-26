package MapGeneration.GenerationElements.Exceptions;

import lombok.experimental.StandardException;

@StandardException
public abstract class MapGenerationException extends Exception {
    public MapGenerationException(String message) {
        super(message);
    }
}
