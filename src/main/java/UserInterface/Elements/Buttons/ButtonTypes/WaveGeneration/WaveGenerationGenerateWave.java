package UserInterface.Elements.Buttons.ButtonTypes.WaveGeneration;

import BeatSaberObjects.Objects.BeatSaberMap;
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

public class WaveGenerationGenerateWave extends MySubButton {
    public WaveGenerationGenerateWave(MyButton parent) {
        super(ElementTypes.WAVE_GENERATOR_Generate_WAVE_BUTTON, parent);
    }

    @Override
    public void onClick() {


        CustomWaveGenerator waveGenerator = new CustomWaveGenerator(SEED);
        List<Coordinate> coordinates = waveGenerator.getCoordinates(Arrays.stream(ui.map._notes).toList());


        ui.map.toBlueLeftBottomRowDotTimings();
        BeatSaberMap map = new BeatSaberMap(CreatePatterns.createMapFromWaves(coordinates));
        loadNewlyCreatedMap(map);

        SwingUtilities.invokeLater(() -> {
            WaveVisualizationFrame frame = new WaveVisualizationFrame(coordinates);
            frame.setVisible(true);
        });
    }
}
