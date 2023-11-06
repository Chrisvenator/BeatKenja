package CustomWaveGenerator;

import javax.swing.*;
import java.awt.*;
import java.util.List;

class WavePanel extends JPanel {
    private final int points;
    private final List<Coordinate> coordinates;

    public WavePanel(List<Coordinate> coordinates) {
        this.points = coordinates.size();
        this.coordinates = coordinates;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        // Set up the origin and scaling factors
        int width = getWidth();
        int height = getHeight();
        double xScale = width / (double) points;
        double yScale = height / 3.0;

        // Draw the wave
        for (int i = 0; i < coordinates.size() - 1; i++) {
            int x1 = (int) (coordinates.get(i).x() * xScale);
            int y1 = (int) ((3 - coordinates.get(i).y()) * yScale);
            int x2 = (int) (coordinates.get(i + 1).x() * xScale);
            int y2 = (int) ((3 - coordinates.get(i + 1).y()) * yScale);
            g2d.drawLine(x1, y1, x2, y2);
        }
    }
}
