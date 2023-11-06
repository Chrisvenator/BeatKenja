package UserInterface.Elements;

import UserInterface.UIElements;
import UserInterface.UserInterface;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

public class TimingNoteGenerator extends UIElements {
    public List<JComponent> timingNoteGeneratorElements = new ArrayList<>();

    public TimingNoteGenerator(boolean darkMode, UserInterface userInterface) {
        super(darkMode, userInterface);
    }

    public JButton toTimingNotes() {
        JButton element = new JButton("Map to timing Notes");
        element.setBounds(450, 200, 190, 30);
        element.setVisible(false);
        userInterface.add(element);

        return element;
    }

    public JButton toBlueOnlyTimingNotes() {
        JButton element = new JButton("To 1 color timing notes");
        element.setBounds(450, 180, 190, 15);
        element.setVisible(false);
        userInterface.add(element);
        timingNoteGeneratorElements.add(element);

        return element;
    }

    public JButton toStackedTimingNotes() {
        JButton element = new JButton("To 2 color timing notes");
        element.setBounds(450, 160, 190, 15);
        element.setVisible(false);
        userInterface.add(element);
        timingNoteGeneratorElements.add(element);

        return element;
    }
}
