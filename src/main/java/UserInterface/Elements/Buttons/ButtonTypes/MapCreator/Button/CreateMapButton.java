package UserInterface.Elements.Buttons.ButtonTypes.MapCreator.Button;

import BeatSaberObjects.Objects.BeatSaberMap;
import MapGeneration.CreatePatterns;
import UserInterface.Elements.Buttons.ButtonTypes.MapCreator.Exceptions.MapDidntComputeException;
import UserInterface.Elements.Buttons.ButtonTypes.MapCreator.MapCreatorSubButton;
import UserInterface.Elements.Buttons.MyButton;
import UserInterface.Elements.ElementTypes;

import static DataManager.Parameters.verbose;

public class CreateMapButton extends MapCreatorSubButton {
    public CreateMapButton(MyButton parent) {
        super(ElementTypes.MAP_CREATOR_CREATE_MAP_BUTTON, parent);
    }

    @Override
    public void onClick() {
        ui.manageMap();
        ui.map.toBlueLeftBottomRowDotTimings();

        try {
            // Redirect the standard error stream to the custom PrintStream so that errors can be printed to the UI
            System.setErr(ui.ERROR_PRINT_STREAM);

            //<editor-fold desc="Debug Information">
            System.out.println("og: " + ui.map.exportAsMap());
            if (verbose) ui.statusCheck.append("\nVERBOSE: og: " + ui.map.exportAsMap());
            //</editor-fold>

            BeatSaberMap map = CreatePatterns.createMap(ui.map, ui.pattern, false, false);

            if (ui.map.exportAsMap().split("\"_cutDirection\":8").length >= 20) ui.statusCheck.append("There are a lot of errors. Do you really want to continue? It is recommended to try again\n");

            if (map.equals(ui.map)) throw new MapDidntComputeException("Something went wrong Map didn't compute...");
            else loadNewlyCreatedMap(map);

            //change back the error outputs
            ui.changeBackOutput();


        } catch (IllegalArgumentException | MapDidntComputeException ex) {
            printException(ex);
        }
    }
}
