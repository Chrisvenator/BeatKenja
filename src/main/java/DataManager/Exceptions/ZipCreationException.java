package DataManager.Exceptions;

import lombok.experimental.StandardException;

import java.io.IOException;

@StandardException
public class ZipCreationException extends IOException {
    public ZipCreationException(String message) {
        super(message);
    }
}
