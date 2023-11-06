package CustomWaveGenerator;

import javax.swing.*;
import java.util.List;


public class WaveVisualizationFrame extends JFrame {
    public WaveVisualizationFrame(List<Coordinate> coordinates) {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setTitle("Custom Wave Visualization");
        add(new WavePanel(coordinates));
        setSize(2000, 100);
        setLocationRelativeTo(null);
    }
}
