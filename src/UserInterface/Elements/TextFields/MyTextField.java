package UserInterface.Elements.TextFields;

import UserInterface.Elements.Buttons.MyButton;

import java.awt.*;

public class MyTextField extends TextField {
    public MyTextField(TextFieldType textFieldType, MyButton parent) {
        super(textFieldType.value(), 0);
        setBounds(textFieldType.x(), textFieldType.y(), textFieldType.w(), textFieldType.h());
        setVisible(textFieldType.setVisible());
        parent.ui.add(this);

        addToParent(parent);
    }

    public void addToParent(MyButton parent) {
        parent.addChild(this);
    }
}
