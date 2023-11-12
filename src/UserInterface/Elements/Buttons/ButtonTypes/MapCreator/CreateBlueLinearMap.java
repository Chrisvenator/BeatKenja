package UserInterface.Elements.Buttons.ButtonTypes.MapCreator;

import BeatSaberObjects.BeatSaberMap;
import DataManager.Parameters;
import MapGeneration.CreatePatterns;
import UserInterface.Elements.Buttons.MyButton;
import UserInterface.Elements.Buttons.MySubButton;
import UserInterface.Elements.ElementTypes;

public class CreateBlueLinearMap extends MySubButton {
    public CreateBlueLinearMap(MyButton parent) {
        super(ElementTypes.MAP_CREATOR_CREATE_ONE_HANDED_SIMPLE_LINEAR_MAP_BUTTON, parent);
    }

    @Override
    public void onClick() {
        System.out.println("CreateBlueLinearMapButton clicked");

        //DO NOT QUESTION THIS SECTION
        //IT WAS NECESSARY TO ENSURE THAT THERE IS NO INFINITE LOOP
        ui.manageMap();
        ui.map.toBlueLeftBottomRowDotTimings();

        Thread calculateNewMap = new Thread(() -> {
            String ogJson = ui.map.originalJSON;
            ui.map = new BeatSaberMap(CreatePatterns.linearSlowPattern(ui.map._notes, true, null, null));
            ui.map.originalJSON = ogJson;
            ui.statusCheck.setText(ui.statusCheck.getText() + "\nMap creation finished");
            System.out.println("Created Map: " + ui.map.exportAsMap());
            if (Parameters.verbose)
                ui.statusCheck.setText(ui.statusCheck.getText() + "\n" + "VERBOSE: " + "Created Map: " + ui.map.exportAsMap());
            ui.checkMap();
        });
        Thread watchForInfiniteLoop = new Thread(() -> {
            try {
                Thread.sleep(5000);
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
            calculateNewMap.interrupt();
            throw new IllegalArgumentException("Took too long lol");
        });

        try {
            watchForInfiniteLoop.start();
            calculateNewMap.start();
            watchForInfiniteLoop.interrupt();
        } catch (IllegalArgumentException ex) {
            ui.statusCheck.setText(ui.statusCheck.getText() + "\nThere was an error while creating. Please try again!");
        }
    }
}
