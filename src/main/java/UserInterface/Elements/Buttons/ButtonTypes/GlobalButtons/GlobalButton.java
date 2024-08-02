package UserInterface.Elements.Buttons.ButtonTypes.GlobalButtons;

import UserInterface.Elements.Buttons.ButtonType;
import UserInterface.Elements.Buttons.MyButton;
import UserInterface.Elements.Buttons.ButtonTypes.GlobalButtons.Exceptions.ActionNotSupportedException;
import UserInterface.Elements.Buttons.ButtonTypes.GlobalButtons.Exceptions.WrongFileException;
import UserInterface.UserInterface;

import javax.swing.*;
import java.io.FileNotFoundException;
import java.net.URISyntaxException;

import static DataManager.Parameters.verbose;

public abstract class GlobalButton extends MyButton {

    public GlobalButton(ButtonType button, UserInterface ui) {
        super(button, ui);
    }

    protected boolean approveFileLoading(int option) {
        if (verbose && option == JFileChooser.APPROVE_OPTION) ui.statusCheck.append("\n[INFO]: File loading approved!");
        if (option == JFileChooser.CANCEL_OPTION) ui.statusCheck.append("\n[INFO]: File loading aborted.");
        if (option == JFileChooser.ERROR_OPTION) ui.statusCheck.append("\n[ERROR]: There was an error while loading the file. Please try again!");

        return option == JFileChooser.APPROVE_OPTION;
    }

    protected void printException(Exception e) {
        String errorMessage;
        if (e instanceof FileNotFoundException) errorMessage = "File Not found!";
        else if (e instanceof NullPointerException) errorMessage = "NullPointerException!";
        else if (e instanceof NumberFormatException) errorMessage = "NumberFormatException!";
        else if (e instanceof URISyntaxException) errorMessage = "Map preview viewing encountered an error! This feature is currently not available :/";
        else if (e instanceof ActionNotSupportedException) errorMessage = "Map preview viewing is not supported on this platform.";
        else if (e instanceof WrongFileException)
            errorMessage = "You tried to load \"" + ((WrongFileException) e).filename + "\". This file (-type) is not supported. Please try again!";
        else errorMessage = e.getMessage();

        printErrorMessage(e, errorMessage);
    }
}
