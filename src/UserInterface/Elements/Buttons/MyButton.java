package UserInterface.Elements.Buttons;

import UserInterface.Elements.MyElement;
import UserInterface.UserInterface;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class MyButton extends JButton implements MyElement {
    public UserInterface ui;
    public final List<Component> childElements = new ArrayList<>();

    public MyButton(ButtonType button, UserInterface ui) {
        this.ui = ui;
        this.setText(button.text());
        this.setBounds(button.x(), button.y(), button.w(), button.h());
        this.setVisible(button.setVisible());

        this.addActionListener(e -> onClick());
        ui.add(this);
    }

    public void addChild(Component button) {
        childElements.add(button);
    }

    public void onClick() {
        childElements.forEach(button -> button.setVisible(!button.isVisible()));
    }

}

