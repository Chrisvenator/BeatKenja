package MapGeneration.GenerationElements.Exceptions;

import lombok.experimental.StandardException;

@StandardException
public class MalformedSequenceException extends MapGenerationException {
    public MalformedSequenceException(String message) {
        super(message);
    }
}
