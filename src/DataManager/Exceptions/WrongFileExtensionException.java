package DataManager.Exceptions;

import java.io.File;

public class WrongFileExtensionException extends Exception {
    public WrongFileExtensionException(File file, String extension) {
        super("File \"" + file.getName() + "\" is not a \"" + extension + "\" file.");
    }
}
