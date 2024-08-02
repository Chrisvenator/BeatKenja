package UserInterface.Elements.Buttons.ButtonTypes.GlobalButtons.Buttons;

import BeatSaberObjects.Objects.BeatSaberMap;
import UserInterface.Elements.Buttons.ButtonTypes.GlobalButtons.Exceptions.WrongFileException;
import UserInterface.Elements.Buttons.ButtonTypes.GlobalButtons.GlobalButton;
import UserInterface.Elements.ElementTypes;
import UserInterface.UserInterface;

import java.awt.*;
import java.io.File;

import static DataManager.Parameters.*;

public class GlobalOpenMapButton extends GlobalButton {
    @lombok.SneakyThrows
    public GlobalOpenMapButton(UserInterface ui) {
        super(ElementTypes.GLOBAL_OPEN_MAP, ui);
        setBackground(Color.cyan);

        //Autoload default map for testing purposes
        if (AUTOLOAD_DEFAULT_MAP_for_testing){
            File f = new File(DEFAULT_PATH_FOR_AUTOLOAD_MAP);
            if (!f.exists() || f.isDirectory() || !f.isFile() || !f.canRead()) throw new WrongFileException(DEFAULT_PATH_FOR_AUTOLOAD_MAP, "Wrong file type!");

            ui.map = BeatSaberMap.newMapFromJSON(f.getAbsolutePath());
            successfullyLoaded(f.getAbsolutePath());
        }
    }

    @Override
    public void onClick() {
        int option = FILE_CHOOSER.showOpenDialog(this);

        if (!approveFileloading(option)) return;
        filePath = FILE_CHOOSER.getCurrentDirectory().toString();
        try {
            File path = FILE_CHOOSER.getSelectedFile();
            if (path.isDirectory() || path.getAbsolutePath().contains("Info.dat")) throw new WrongFileException(path.getName(), "Wrong file type!");

            ui.map = BeatSaberMap.newMapFromJSON(path.getAbsolutePath());

            successfullyLoaded(FILE_CHOOSER.getSelectedFile().getAbsolutePath());
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

    private void successfullyLoaded(String absolutePath) {
        setText("load an other diff");
        setBounds(270, 20, 200, 30);
        setBackground(Color.GREEN);
        ui.statusCheck.setText("Successfully loaded difficulty: \"" + absolutePath + "\"");
        ui.mapSuccessfullyLoaded = true;
    }
}
