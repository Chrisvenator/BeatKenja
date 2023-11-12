package UserInterface.Elements.Buttons.ButtonTypes.MapCreator;

import BeatSaberObjects.BeatSaberMap;
import MapGeneration.CreatePatterns;
import UserInterface.Elements.Buttons.MyButton;
import UserInterface.Elements.Buttons.MySubButton;
import UserInterface.Elements.ElementTypes;

import static DataManager.Parameters.verbose;

public class CreateRandomV2Map extends MySubButton {
    public CreateRandomV2Map(MyButton parent) {
        super(ElementTypes.MAP_CREATOR_CREATE_RANDOM_MAP_V2_BUTTON, parent);
    }

    @Override
    public void onClick() {
        System.out.println("CreateRandomV2Map clicked");

        ui.manageMap();
        ui.map.toBlueLeftBottomRowDotTimings();

        try {
            String ogJson = ui.map.originalJSON;
            ui.map = new BeatSaberMap(CreatePatterns.randomV2FromTemplate(ui.map._notes, ui.pattern, false, null, null));
            ui.map.originalJSON = ogJson;
            ui.statusCheck.setText(ui.statusCheck.getText() + "\nMap creation finished");
            System.out.println("Created Map: " + ui.map.exportAsMap());
            if (verbose) ui.statusCheck.setText(ui.statusCheck.getText() + "\n" + "VERBOSE: " + "Created Map: " + ui.map.exportAsMap());
            ui.checkMap();
        } catch (IllegalArgumentException ex) {
            ui.statusCheck.setText(ui.statusCheck.getText() + "\nThere was an error while creating. Please try again!");
        }
    }
}
