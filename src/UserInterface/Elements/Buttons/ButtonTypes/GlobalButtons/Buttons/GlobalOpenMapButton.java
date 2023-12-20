package UserInterface.Elements.Buttons.ButtonTypes.GlobalButtons.Buttons;

import UserInterface.Elements.Buttons.ButtonTypes.GlobalButtons.GlobalButton;
import UserInterface.Elements.ElementTypes;
import UserInterface.UserInterface;

import java.awt.*;
import java.io.File;

import static DataManager.Parameters.*;

public class GlobalOpenMapButton extends GlobalButton {
    public GlobalOpenMapButton(UserInterface ui) {
        super(ElementTypes.GLOBAL_OPEN_MAP, ui);
        setBackground(Color.cyan);
    }

    @Override
    public void onClick() {
        int option = FILE_CHOOSER.showOpenDialog(this);

        if (!approveFileloading(option)) return;
        filePath = FILE_CHOOSER.getCurrentDirectory().toString();
        try {
            ui.map = this.convertToMap(FILE_CHOOSER.getSelectedFile());

            ui.statusCheck.setText("Successfully loaded difficulty: \"" + FILE_CHOOSER.getSelectedFile().getAbsolutePath() + "\"");
            ui.mapSuccessfullyLoaded = true;
            successfullyLoaded();
        } catch (Exception e) {
            errorWhileLoading(e);
            printException(e);
        }
    }

    private void errorWhileLoading(Exception e) {
        System.err.println("[ERROR]: Map probably has the wrong format: \n" + e);
        ui.labelMapDiff.setText("There was an error while importing the map!");
        ui.labelMapDiff.setBounds(100, 20, 300, 30);
        setBounds(320, 20, 300, 30);
        setBackground(Color.RED);
        ui.mapSuccessfullyLoaded = false;
    }

    private void successfullyLoaded() {
        setText("load an other diff");
        setBounds(270, 20, 200, 30);
        setBackground(Color.GREEN);
    }
}
