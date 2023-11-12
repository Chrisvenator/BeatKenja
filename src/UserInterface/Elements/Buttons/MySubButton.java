package UserInterface.Elements.Buttons;

public abstract class MySubButton extends MyButton {
    public MySubButton(ButtonType button, MyButton parent) {
        super(button, parent.ui);
        parent.addChild(this);
    }
}
