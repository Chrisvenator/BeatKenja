package UserInterface.Elements.Buttons.ButtonTypes.MapUtilities;

import UserInterface.Elements.Buttons.MyButton;
import UserInterface.Elements.Buttons.MySubButton;
import UserInterface.Elements.ElementTypes;

import static DataManager.Parameters.verbose;

public class UtilsMakeIntoNoArrowMap extends MySubButton {
    public UtilsMakeIntoNoArrowMap(MyButton parent) {
        super(ElementTypes.MAP_UTILITIES_MAKE_NO_ARROW_MAP_BUTTON, parent);
    }

    @Override
    public void onClick() {
        System.out.println("MAP_UTILITIES_MAKE_NO_ARROW_MAP_BUTTON clicked");

        ui.map.makeNoArrows();
        ui.statusCheck.setText(ui.statusCheck.getText() + "\n[INFO]: Map is now a no arrows map");
        System.out.println("No Arrow Map: " + ui.map.exportAsMap());
        if (verbose) ui.statusCheck.setText(ui.statusCheck.getText() + "\n" + "VERBOSE: " + "No Arrow Map: " + ui.map.exportAsMap());
    }
}
