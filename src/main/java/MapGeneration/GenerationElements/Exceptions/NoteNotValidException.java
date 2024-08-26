package MapGeneration.GenerationElements.Exceptions;

import lombok.experimental.StandardException;

@StandardException
public class NoteNotValidException extends MapGenerationException {
    public NoteNotValidException(String message) {
        super(message);
    }
}
