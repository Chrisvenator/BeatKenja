package UserInterface.Elements;

import UserInterface.UIElements;
import UserInterface.UserInterface;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

public class WaveFunctions extends UIElements {
    public List<JComponent> waveFunctionsElements = new ArrayList<>();

    public WaveFunctions(boolean darkMode, UserInterface userInterface) {
        super(darkMode, userInterface);
    }

    public JButton showWaveFunctions() {
        JButton element = new JButton("Wave Functions");
        element.setBounds(50, 200, 190, 30);
        element.setVisible(false);
        userInterface.add(element);

        return element;
    }

    public JButton WaveFunctionsGenerateWave() {
        JButton element = new JButton("Generate Wave");
        element.setBounds(50, 180, 145, 15);
        element.setVisible(false);
        userInterface.add(element);

        waveFunctionsElements.add(element);

        return element;
    }

}
