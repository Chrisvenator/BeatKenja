package UserInterface.Elements;

import UserInterface.UserInterface;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

public class MapCreator extends WaveFunctions {
    public List<JComponent> mapCreatorElements = new ArrayList<>();

    public MapCreator(boolean darkMode, UserInterface userInterface) {
        super(darkMode, userInterface);
    }

    public JButton mapCreator() {
        JButton element = new JButton("Map creator");
        element.setBounds(650, 200, 190, 30);
        element.setVisible(false);
        userInterface.add(element);

        return element;
    }

    public JButton mapCreatorCreateMap() {
        JButton element = new JButton("Create Map");
        element.setBounds(650, 180, 190, 15);
        element.setVisible(false);
        userInterface.add(element);
        mapCreatorElements.add(element);

        return element;
    }

    public JButton mapCreatorCreateComplexMap() {
        JButton element = new JButton("Complex");
        element.setBounds(650, 160, 190, 15);
        element.setVisible(false);
        userInterface.add(element);
        mapCreatorElements.add(element);

        return element;
    }


    public JButton mapCreatorCreateLinearMap() {
        JButton element = new JButton("Create Linear Map");
        element.setBounds(650, 140, 190, 15);
        element.setVisible(false);
        userInterface.add(element);
        mapCreatorElements.add(element);

        return element;
    }

    public JButton mapCreatorCreateBlueLinearMap() {
        JButton element = new JButton("one handed simpl linear");
        element.setBounds(650, 120, 90, 15);
        element.setVisible(false);
        userInterface.add(element);
        mapCreatorElements.add(element);

        return element;
    }

    public JButton mapCreatorCreateBlueComplexMap() {
        JButton element = new JButton("complex");
        element.setBounds(750, 120, 90, 15);
        element.setVisible(false);
        userInterface.add(element);
        mapCreatorElements.add(element);

        return element;
    }

    public JButton mapCreatorCreateRandomMap() {
        JButton element = new JButton("random");
        element.setBounds(650, 100, 90, 15);
        element.setVisible(false);
        userInterface.add(element);
        mapCreatorElements.add(element);

        return element;
    }

    public JButton mapCreatorCreateRandomV2Map() {
        JButton element = new JButton("rand. V2");
        element.setBounds(750, 100, 90, 15);
        element.setVisible(false);
        userInterface.add(element);
        mapCreatorElements.add(element);

        return element;
    }
}
