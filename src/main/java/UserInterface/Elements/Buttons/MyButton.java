package UserInterface.Elements.Buttons;

import UserInterface.Elements.MyElement;
import UserInterface.UserInterface;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import static DataManager.Parameters.logger;

public class MyButton extends JButton implements MyElement {
    public final UserInterface ui;
    public final List<Component> childElements = new ArrayList<>();

    public MyButton(ButtonType button, UserInterface ui) {
        this.ui = ui;
        this.setText(button.text());
        this.setBounds(button.x(), button.y(), button.w(), button.h());
        this.setVisible(button.setVisible());

        this.addActionListener(e -> {
            onClick();
            logger.debug("{} clicked", this.getClass().getSimpleName());
            System.out.println(this.getClass().getSimpleName() + " clicked");
        });
        ui.add(this);
    }

    public void addChild(Component button) {
        childElements.add(button);
        logger.debug("Added child: {} to: {}", button.getClass().getSimpleName(), this.getClass().getSimpleName());
    }

    public void onClick() {
        childElements.forEach(element -> element.setVisible(!element.isVisible()));
    }

    protected void printErrorMessage(Exception e, String errorMessage) {
        logger.error(e.getMessage());
        logger.error(errorMessage);
        e.printStackTrace();
    }
}

