package UserInterface.Elements.Buttons;

import BeatSaberObjects.Objects.BeatSaberMap;

import static DataManager.Parameters.verbose;

public abstract class MySubButton extends MyButton {
    public MySubButton(ButtonType button, MyButton parent) {
        super(button, parent.ui);
//        parent.addChild(this);
    }

    protected void loadNewlyCreatedMap(BeatSaberMap map) {
        String ogJson = ui.map.originalJSON;
        ui.map = map;
        ui.map.originalJSON = ogJson;
        ui.statusCheck.append("\nMap creation finished");
        System.out.println("Created Map: " + ui.map.exportAsMap());
        if (verbose) ui.statusCheck.append("\nVERBOSE: Created Map: " + ui.map.exportAsMap());
        ui.checkMap();
    }
}
