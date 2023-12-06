package UserInterface.Elements.Buttons;

import BeatSaberObjects.BeatSaberMap;
import UserInterface.Elements.Buttons.ButtonTypes.GlobalButtons.Exceptions.WrongFileException;
import UserInterface.Elements.MyElement;
import UserInterface.UserInterface;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import static DataManager.Parameters.FILE_CHOOSER;

public class MyButton extends JButton implements MyElement {
    public UserInterface ui;
    public final List<Component> childElements = new ArrayList<>();

    public MyButton(ButtonType button, UserInterface ui) {
        this.ui = ui;
        this.setText(button.text());
        this.setBounds(button.x(), button.y(), button.w(), button.h());
        this.setVisible(button.setVisible());

        this.addActionListener(e -> {
            onClick();
            System.out.println(this.getClass().getSimpleName() + " clicked");
        });
        ui.add(this);
    }

    public void addChild(Component button) {
        childElements.add(button);
    }

    public void onClick() {
        childElements.forEach(button -> button.setVisible(!button.isVisible()));
    }

    protected BeatSaberMap convertToMap(File path) throws FileNotFoundException, WrongFileException {
        if (path.isDirectory() || path.getAbsolutePath().contains("Info.dat")) throw new WrongFileException(path.getName(), "Wrong file type!");

        Scanner scanner = new Scanner(FILE_CHOOSER.getSelectedFile());
        String mapAsString = scanner.nextLine();

        return BeatSaberMap.newMapFromJSON(mapAsString);
    }

    protected void printErrorMessage(Exception e, String errorMessage) {
        System.err.println(e.getMessage());
        System.err.println("[ERROR]: " + errorMessage);
        ui.statusCheck.append("\n[ERROR]: " + errorMessage);
        e.printStackTrace();
    }
}

