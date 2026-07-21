package UserInterfaceFX;

import MapGeneration.GenerationElements.Pattern;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/**
 * Draws a pattern's transition-probability matrix onto a canvas, normalized per row
 * (like the old Swing "Normalized Heatmap"): row = current note, column = possible
 * next note, stronger blue = more likely. Shared by Review and Patterns views.
 */
public final class PatternHeatmap {

    private PatternHeatmap() {
    }

    public static void draw(Canvas canvas, Pattern pattern) {
        GraphicsContext g = canvas.getGraphicsContext2D();
        g.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());

        if (pattern == null) {
            g.fillText("No pattern loaded — load one under 3 · Generate or in the Patterns tool", 20, 30);
            return;
        }

        int size = 0;
        while (size < pattern.patterns.length && pattern.patterns[size][0] != null) size++;
        if (size == 0) return;

        double cell = Math.min(canvas.getWidth(), canvas.getHeight()) / size;
        for (int row = 0; row < size; row++) {
            float rowMax = 0;
            for (int col = 0; col < size; col++) rowMax = Math.max(rowMax, pattern.probabilities[row][col]);
            for (int col = 0; col < size; col++) {
                double intensity = rowMax == 0 ? 0 : pattern.probabilities[row][col] / rowMax;
                g.setFill(Color.color(1 - intensity, 1 - intensity, 1, 1));
                g.fillRect(col * cell, row * cell, cell, cell);
            }
        }
    }
}
