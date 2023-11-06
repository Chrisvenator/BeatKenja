package UserInterface.Elements;

import UserInterface.UIElements;
import UserInterface.UserInterface;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

public class MapUtils extends UIElements {
    public List<JComponent> mapUtilsElements = new ArrayList<>();

    public MapUtils(boolean darkMode, UserInterface userInterface) {
        super(darkMode, userInterface);
    }

    public JButton mapUtils() {
        JButton mapUtils = new JButton("Map Utilities");
        mapUtils.setBounds(250, 200, 190, 30);
        mapUtils.setVisible(false);
        userInterface.add(mapUtils);

        return mapUtils;
    }

    public JButton mapUtilsFixPlacements() {
        JButton element = new JButton("Fix Placements");
        element.setBounds(250, 180, 145, 15);
        element.setVisible(false);
        userInterface.add(element);

        mapUtilsElements.add(element);

        return element;
    }

    public JTextField fixPlacementTextField(double placementPrecision) {
        JTextField element = new JFormattedTextField(1 / placementPrecision);
        element.setBounds(400, 180, 40, 15);
        element.setVisible(false);
        userInterface.add(element);
        mapUtilsElements.add(element);

        return element;
    }

    public JButton mapUtilsMakeOneHanded() {
        JButton element = new JButton("Delete Note Type");
        element.setBounds(250, 160, 145, 15);
        element.setVisible(false);
        userInterface.add(element);
        mapUtilsElements.add(element);

        return element;
    }

    public JTextField makeOneHandDeleteType() {
        JTextField element = new JFormattedTextField(0);
        element.setBounds(400, 160, 40, 15);
        element.setVisible(false);
        userInterface.add(element);
        mapUtilsElements.add(element);

        return element;
    }

    public JButton mapUtilsConvertAllFlashingLight() {
        JButton element = new JButton("Convert All FlashingLight");
        element.setBounds(250, 140, 190, 15);
        element.setVisible(false);
        userInterface.add(element);
        mapUtilsElements.add(element);

        return element;
    }

    public JButton mapUtilsMakeIntoNoArrowMap() {
        JButton element = new JButton("Make into no arrow map");
        element.setBounds(250, 120, 190, 15);
        element.setVisible(false);
        userInterface.add(element);
        mapUtilsElements.add(element);

        return element;
    }
}
