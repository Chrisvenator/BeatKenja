package UserInterface.Elements.Buttons.ButtonTypes.MapCreator;

import BeatSaberObjects.BeatSaberMap;
import MapGeneration.CreatePatterns;
import UserInterface.Elements.Buttons.MyButton;
import UserInterface.Elements.Buttons.MySubButton;
import UserInterface.Elements.ElementTypes;

import static DataManager.Parameters.verbose;

public class CreateBlueComplexMap extends MySubButton {
    public CreateBlueComplexMap(MyButton parent) {
        super(ElementTypes.MAP_CREATOR_CREATE_ONE_HANDED_COMPLEX_MAP_BUTTON, parent);
    }

    @Override
    public void onClick() {
        System.out.println("CreateBlueComplexMapButton clicked");

        ui.manageMap();
        ui.map.toBlueLeftBottomRowDotTimings();

        try {
            String ogJson = ui.map.originalJSON;
            ui.map = new BeatSaberMap(CreatePatterns.complexPatternFromTemplate(ui.map._notes, ui.pattern, true, false, null, null));
            ui.map.originalJSON = ogJson;

            ui.statusCheck.setText(ui.statusCheck.getText() + "\nMap creation finished");
            System.out.println("Created Map: " + new BeatSaberMap(ui.map._notes).exportAsMap());
            if (verbose) ui.statusCheck.setText(ui.statusCheck.getText() + "\n" + "VERBOSE: " + "Created Map: " + new BeatSaberMap(ui.map._notes).exportAsMap());
            ui.checkMap();
        } catch (IllegalArgumentException ex) {
            ui.statusCheck.setText(ui.statusCheck.getText() + "\nThere was an error while creating. Please try again!");
        }

    }
}
