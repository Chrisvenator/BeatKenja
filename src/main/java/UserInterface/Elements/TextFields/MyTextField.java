package UserInterface.Elements.TextFields;

import UserInterface.Elements.Buttons.MyButton;


public class MyTextField extends MyGlobalTextField {
    public MyTextField(TextFieldType textFieldType, MyButton parent) {
        super(textFieldType, parent.ui);
    }
}
