package UserInterface.Elements.Buttons.ButtonTypes.GlobalButtons.Exceptions;

public class WrongFileException extends Exception {
    public final String filename;

    public WrongFileException(String filename, String message) {
        super(message);
        this.filename = filename;
    }
}
