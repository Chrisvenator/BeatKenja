package DataManager.Exceptions;

import java.io.File;

/**
 * An exception that is thrown when a file does not have the expected file extension.
 * This exception is typically used to signal that an operation cannot proceed because the file is of the wrong type.
 */
public class WrongFileExtensionException extends Exception {
    /**
     * Constructs a new `WrongFileExtensionException` with a detailed error message.
     * The message specifies the name of the file and the expected file extension.
     *
     * @param file      The file that caused the exception.
     * @param extension The expected file extension.
     */
    public WrongFileExtensionException(File file, String extension) {
        super("File \"" + file.getName() + "\" is not a \"" + extension + "\" file.");
    }
}
