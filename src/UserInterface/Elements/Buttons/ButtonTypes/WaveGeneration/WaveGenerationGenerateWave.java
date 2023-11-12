package UserInterface.Elements.Buttons.ButtonTypes.WaveGeneration;

import BeatSaberObjects.BeatSaberMap;
import CustomWaveGenerator.CustomWaveGenerator;
import MapGeneration.CreatePatterns;
import UserInterface.Elements.Buttons.MyButton;
import UserInterface.Elements.Buttons.MySubButton;
import UserInterface.Elements.ElementTypes;
import CustomWaveGenerator.*;

import javax.swing.*;
import java.util.Arrays;
import java.util.List;

import static DataManager.Parameters.SEED;
import static DataManager.Parameters.verbose;

public class WaveGenerationGenerateWave extends MySubButton {
    public WaveGenerationGenerateWave(MyButton parent) {
        super(ElementTypes.WAVE_GENERATOR_Generate_WAVE_BUTTON, parent);
    }

    @Override
    public void onClick() {
        System.out.println("WAVE_GENERATOR_Generate_WAVE_BUTTON clicked");


        CustomWaveGenerator waveGenerator = new CustomWaveGenerator(SEED);
        List<Coordinate> coordinates = waveGenerator.getCoordinates(Arrays.stream(ui.map._notes).toList());


        String ogJson = ui.map.originalJSON;
        ui.map.toBlueLeftBottomRowDotTimings();
        ui.map = new BeatSaberMap(CreatePatterns.createMapFromWaves(coordinates));
        ui.map.originalJSON = ogJson;
        ui.statusCheck.setText(ui.statusCheck.getText() + "\nMap creation finished");
        System.out.println("Created Map: " + ui.map.exportAsMap());
        if (verbose) ui.statusCheck.setText(ui.statusCheck.getText() + "\n" + "VERBOSE: " + "Created Map: " + ui.map.exportAsMap());
        ui.checkMap();

        SwingUtilities.invokeLater(() -> {
            WaveVisualizationFrame frame = new WaveVisualizationFrame(coordinates);
            frame.setVisible(true);
        });
    }
}
