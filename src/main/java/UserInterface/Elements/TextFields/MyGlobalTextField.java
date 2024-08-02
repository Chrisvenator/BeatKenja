package UserInterface.Elements.TextFields;

import UserInterface.UserInterface;

import java.awt.*;

public class MyGlobalTextField extends TextField {
    protected final UserInterface ui;

    public MyGlobalTextField(TextFieldType textFieldType, UserInterface ui) {
        super(textFieldType.value(), 0);
        this.ui = ui;

        setBounds(textFieldType.x(), textFieldType.y(), textFieldType.w(), textFieldType.h());
        setVisible(textFieldType.setVisible());
        ui.add(this);
    }
}
