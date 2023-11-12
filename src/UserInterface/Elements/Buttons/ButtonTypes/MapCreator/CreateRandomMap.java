package UserInterface.Elements.Buttons.ButtonTypes.MapCreator;

import BeatSaberObjects.BeatSaberMap;
import MapGeneration.CreatePatterns;
import UserInterface.Elements.Buttons.MyButton;
import UserInterface.Elements.Buttons.MySubButton;
import UserInterface.Elements.ElementTypes;

import static DataManager.Parameters.verbose;

public class CreateRandomMap extends MySubButton {
    public CreateRandomMap(MyButton parent) {
        super(ElementTypes.MAP_CREATOR_CREATE_RANDOM_MAP_BUTTON, parent);
    }

    @Override
    public void onClick() {
        System.out.println("CreateRandomMapButton clicked");

        ui.manageMap();
        ui.map.toBlueLeftBottomRowDotTimings();
        String ogJson = ui.map.originalJSON;

        try {
            ui.map = new BeatSaberMap(CreatePatterns.createRandomPattern(ui.map._notes, false));
            ui.map.originalJSON = ogJson;

            System.out.println("Created Map: " + new BeatSaberMap(ui.map._notes).exportAsMap());
            ui.statusCheck.setText(ui.statusCheck.getText() + "\nMap creation finished");
            if (verbose) ui.statusCheck.setText(ui.statusCheck.getText() + "\n" + "VERBOSE: " + "Created Map: " + new BeatSaberMap(ui.map._notes).exportAsMap());
            ui.statusCheck.setText(ui.statusCheck.getText() + "\nThere will be a lot of errors. But that's what you wanted lol");
        } catch (IllegalArgumentException ex) {
            ui.statusCheck.setText(ui.statusCheck.getText() + "\nThere was an error while creating. Please try again!");
        }
    }
}
