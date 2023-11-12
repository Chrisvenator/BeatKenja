package UserInterface.Elements.Buttons.ButtonTypes.MapCreator;

import BeatSaberObjects.BeatSaberMap;
import DataManager.Parameters;
import MapGeneration.CreatePatterns;
import UserInterface.Elements.Buttons.MyButton;
import UserInterface.Elements.Buttons.MySubButton;
import UserInterface.Elements.ElementTypes;

public class CreateComplexMap extends MySubButton {
    public CreateComplexMap(MyButton parent) {
        super(ElementTypes.MAP_CREATOR_CREATE_COMPLEX_MAP_BUTTON, parent);
    }

    public void onClick() {
        ui.manageMap();
        ui.map.toBlueLeftBottomRowDotTimings();

        try {
            String ogJson = ui.map.originalJSON;
            ui.map = new BeatSaberMap(CreatePatterns.complexPatternFromTemplate(ui.map._notes, ui.pattern, false, false, null, null));
            ui.map.originalJSON = ogJson;
            ui.statusCheck.setText(ui.statusCheck.getText() + "\nMap creation finished");
            System.out.println("Created Map: " + ui.map.exportAsMap());
            if (Parameters.verbose) ui.statusCheck.setText(ui.statusCheck.getText() + "\n" + "VERBOSE: " + "Created Map: " + ui.map.exportAsMap());
            ui.checkMap();
        } catch (IllegalArgumentException ex) {
            ui.statusCheck.setText(ui.statusCheck.getText() + "\nThere was an error while creating. Please try again!");
        }
    }
}
