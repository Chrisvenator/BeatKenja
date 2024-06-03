package UserInterface.Elements.Buttons.ButtonTypes.AdvancedMapCreation;

import DataManager.Parameters;
import UserInterface.Elements.Buttons.ButtonType;
import UserInterface.Elements.Buttons.MyButton;
import UserInterface.Elements.Buttons.MySubButton;
import UserInterface.Elements.ElementTypes;

import javax.swing.*;
import java.awt.event.ItemEvent;
import java.util.List;
import java.util.Objects;

public abstract class AbstractDropDownMenu extends MySubButton {
    protected static JFrame dropdownFrame;
    protected final int index;
    protected final List<String> items;
    protected final List<String> targetList;

    public AbstractDropDownMenu(ButtonType elementType, MyButton parent, int index, List<String> items, List<String> targetList) {
        super(elementType, parent);
        this.index = index;
        this.items = items;
        this.targetList = targetList;
    }

    @Override
    public void onClick() {
        openDropdownWindow();
    }

    protected void openDropdownWindow() {
        if (dropdownFrame == null || !dropdownFrame.isDisplayable()) {
            dropdownFrame = new JFrame("Select Item");
            dropdownFrame.setSize(300, 100);
            dropdownFrame.setLayout(null);
            dropdownFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

            JComboBox<String> comboBox = new JComboBox<>(items.toArray(new String[0]));
            comboBox.setBounds(50, 20, 200, 25);

            comboBox.addItemListener(e -> {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    updateSelection(Objects.requireNonNull(comboBox.getSelectedItem()).toString());
                }
            });

            dropdownFrame.add(comboBox);
            dropdownFrame.setVisible(true);
        }
    }

    protected void updateSelection(String selectedItem) {
        setText(selectedItem);
        targetList.set(index, selectedItem);
        System.out.println("Item updated to: " + selectedItem);
        dropdownFrame.dispose();
    }
}
