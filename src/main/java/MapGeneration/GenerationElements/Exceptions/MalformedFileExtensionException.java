package MapGeneration.GenerationElements.Exceptions;

import lombok.experimental.StandardException;

@StandardException
public class MalformedFileExtensionException extends MapGenerationException {
    public MalformedFileExtensionException(String message) {
        super(message);
    }
}
