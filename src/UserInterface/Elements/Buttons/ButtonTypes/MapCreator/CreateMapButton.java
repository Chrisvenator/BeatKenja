package UserInterface.Elements.Buttons.ButtonTypes.MapCreator;

import BeatSaberObjects.BeatSaberMap;
import MapGeneration.CreatePatterns;
import UserInterface.Elements.Buttons.MyButton;
import UserInterface.Elements.Buttons.MySubButton;
import UserInterface.Elements.ElementTypes;

import static DataManager.Parameters.verbose;

public class CreateMapButton extends MySubButton {
    public CreateMapButton(MyButton parent) {
        super(ElementTypes.MAP_CREATOR_CREATE_MAP_BUTTON, parent);
    }

    @Override
    public void onClick() {
        System.out.println("CreateMapButton clicked");


        ui.manageMap();
        ui.map.toBlueLeftBottomRowDotTimings();

        try {
            // Redirect the standard error stream to the custom PrintStream so that errors can be printed to the UI
            System.setErr(ui.ERROR_PRINT_STREAM);


            String exported = ui.map.exportAsMap();
            System.out.println("og: " + exported);
            if (verbose) ui.statusCheck.setText(ui.statusCheck.getText() + "\n" + "VERBOSE: " + "og: " + exported);
            ui.map = CreatePatterns.createMap(ui.map, ui.pattern, false, false);

            if (exported.equals(ui.map.exportAsMap()) || ui.map.exportAsMap().split("\"_cutDirection\":8").length >= 20) {
                ui.statusCheck.setText(ui.statusCheck.getText() + "\n" + "[ERROR]! Something went wrong while creating the map... Try another diff. If this error still continues then contact the creator of this tool");
            }

            //change back the error outputs
            ui.changeBackOutput();

            ui.statusCheck.setText(ui.statusCheck.getText() + "\nMap creation finished");
            System.out.println("Created map: " + new BeatSaberMap(ui.map._notes).exportAsMap());
            if (verbose)
                ui.statusCheck.setText(ui.statusCheck.getText() + "\n" + "VERBOSE: " + "Created map: " + new BeatSaberMap(ui.map._notes).exportAsMap());
        } catch (IllegalArgumentException ex) {
            ui.statusCheck.setText(ui.statusCheck.getText() + "\nThere was an error while creating. Please try again!");
            System.err.println(ex.getMessage());
            ui.changeBackOutput();
        }
    }
}
