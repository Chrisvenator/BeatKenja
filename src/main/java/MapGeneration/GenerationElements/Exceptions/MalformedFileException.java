package MapGeneration.GenerationElements.Exceptions;

import lombok.experimental.StandardException;

@StandardException
public class MalformedFileException extends MapGenerationException {
    public MalformedFileException(String message) {
        super(message);
    }
}
