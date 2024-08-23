package MapAnalysation.PatternVisualisation;

import MapGeneration.GenerationElements.Pattern;

import javax.swing.*;
import java.awt.*;


/**
 * A Java Swing application that visualizes the count array of a {@link Pattern} as a heatmap.
 * The heatmap can be displayed in different modes: normalized, truncated, logarithmically normalized, or raw values.
 * <p>
 * The class provides multiple static methods for launching the visualization in various modes.
 * The visualization is rendered in a Swing window, with each value in the count array represented by a colored rectangle.
 */
public class PatternVisualisationHeatMap extends JFrame {
    /** The maximum size of the array to be visualized, defining the grid dimensions for the heatmap. This value determines the number of cells (rows and columns) in the heatmap. */
    private static final int MAX_ARRAY_SIZE = 109;
    /** The size of each rectangle (cell) in the heatmap, in pixels. This value determines the resolution of the visualization, with larger values creating bigger cells. */
    private static final int RECT_SIZE = 10;
    /** A 2D array representing the count data to be visualized. Each element in the array corresponds to a specific count value that will be mapped to a color on the heatmap.*/
    private final int[][] count;
    /** A flag indicating whether the count array values should be normalized. If true, the values will be scaled to a standard range, typically between 0 and 255, for visualization.*/
    private final boolean normalize;
    /** A flag indicating whether the count array values should be truncated. If true, the values will be clamped to a range of 0 to 255, ensuring that all visualized values fit within this range.*/
    private final boolean truncate;
    /** A flag indicating whether the count array values should be normalized logarithmically. If true, the values will be scaled using a logarithmic function, which can highlight differences in data with a wide range of values. */
    private final boolean logarithmic;

    /**
     * Launches the visualization in a Swing window.
     * Depending on the provided parameters, the visualization can be normalized, truncated, or logarithmically scaled.
     * <p>
     * Normalize has a higher priority than truncate.
     *
     * @param normalize If true, normalize the count array values.
     * @param truncate  If true, truncate the count array values to the range 0-255.
     * @param log       If true, apply logarithmic scaling to the count array values.
     * @param p         The pattern to visualize.
     * @param name      The title of the window.
     */
    private static void visualize(boolean normalize, boolean truncate, boolean log, Pattern p, String name) {
        new PatternVisualisationHeatMap(normalize, truncate, log, p, name);
    }

    /**
     * Visualizes the given pattern as a normalized heatmap.
     *
     * @param p    The pattern to visualize.
     * @param name The title of the window.
     */
    public static void visualizeAsHeatmapNormalized(Pattern p, String name) {
        PatternVisualisationHeatMap.visualize(true, false, false, p, name);
    }

    /**
     * Visualizes the given pattern as a logarithmically normalized heatmap.
     *
     * @param p    The pattern to visualize.
     * @param name The title of the window.
     */
    public static void visualizeAsHeatmapLogarithmicNormalized(Pattern p, String name) {
        PatternVisualisationHeatMap.visualize(true, false, true, p, name);
    }

    /**
     * Visualizes the given pattern as a raw heatmap without any normalization or truncation.
     *
     * @param p    The pattern to visualize.
     * @param name The title of the window.
     */
    public static void visualizeAsHeatmap(Pattern p, String name) {
        PatternVisualisationHeatMap.visualize(false, false, false, p, name);
    }

    /**
     * Visualizes the given pattern as a truncated heatmap.
     *
     * @param p    The pattern to visualize.
     * @param name The title of the window.
     */
    public static void visualizeAsHeatmapTruncated(Pattern p, String name) {
        PatternVisualisationHeatMap.visualize(false, true, false, p, name);
    }


    /**
     * Constructs the PatternVisualisationHeatMap window.
     * The heatmap can be customized to display normalized, truncated, or logarithmically scaled values.
     *
     * @param normalize If true, normalize the count array values.
     * @param truncate  If true, truncate the count array values to the range 0-255.
     * @param log       If true, apply logarithmic scaling to the count array values.
     * @param p         The pattern to visualize.
     * @param name      The title of the window.
     */
    private PatternVisualisationHeatMap(boolean normalize, boolean truncate, boolean log, Pattern p, String name) {
        this.normalize = normalize;
        this.truncate = truncate;
        this.logarithmic = log;
        this.count = copyArray(p.count);

        if (log) this.setTitle("Logarithmically Normalized Pattern Heatmap Visualization");
        if (normalize) this.setTitle("Normalized Pattern Heatmap Visualization");
        else if (truncate) this.setTitle("Truncated Pattern Heatmap Visualization");
        else this.setTitle("Pattern Heatmap Visualization");
        if (name != null) this.setTitle(name);

        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        this.setSize(MAX_ARRAY_SIZE * RECT_SIZE + 10, MAX_ARRAY_SIZE * RECT_SIZE + 10);


        if (normalize) normalizeCountArray();

        this.add(new HeatmapPanel());
        this.setVisible(true);
    }

    /**
     * Creates a deep copy of the given 2D array.
     * This method is used to duplicate the original count arrays without modifying the original data.
     *
     * @param original The original 2D array to copy.
     * @return A deep copy of the original 2D array.
     */
    private int[][] copyArray(int[][] original) {
        int[][] copy = new int[original.length][];
        for (int i = 0; i < original.length; i++) {
            copy[i] = original[i].clone();
        }
        return copy;
    }

    /**
     * Normalizes the count array if normalization is enabled.
     * The normalization can be either linear or logarithmic, depending on the provided parameters.
     */
    private void normalizeCountArray() {
        Pattern.normalizeCountArray(count, logarithmic);
    }


    /**
     * A JPanel subclass that draws the heatmap based on the count array values.
     * The heatmap is displayed as a grid of colored rectangles, with the color intensity representing the value in the count array.
     */
    private class HeatmapPanel extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            for (int i = 0; i < MAX_ARRAY_SIZE; i++) {
                for (int j = 0; j < MAX_ARRAY_SIZE; j++) {
                    g.setColor(getColorForValue(count[i][j]));
                    g.fillRect(j * RECT_SIZE, i * RECT_SIZE, RECT_SIZE, RECT_SIZE);
                }
            }
        }

        /**
         * Returns a color based on the given value.
         * If normalize is true, the value is interpreted directly.
         * If truncate is true, the value is clamped to the range 0-255.
         * Otherwise, the value determines the color components.
         * The redder the value, the higher, the bigger the value.
         * If the color is white, it's a very high value.
         *
         * @param value The value to interpret as a color.
         * @return The color corresponding to the value.
         */
        private Color getColorForValue(int value) {
            // Normalized values have a range from 0 to 255. We don't need to truncate the value anymore
            if (normalize) new Color(0, 0, value);

            // Ensure the value is within the expected range (0-255)
            if (truncate) {
                int blueIntensity = Math.max(0, Math.min(255, value));
                return new Color(0, 0, blueIntensity);
            }

            if (value <= 0) return new Color(0, 0, 0); // Black for zero or negative values
            else if (value >= 255 * 255 * 255) return new Color(1.0f, 1.0f, 1.0f); // White for extremely large values
            else {
                // Compute color components
                float blueIntensity = (value % 255) / 255.0f;
                float redIntensity = (((float) value / 255) % 255) / 255.0f;
                float greenIntensity = (((float) value / (255 * 255)) % 255) / 255.0f;
                return new Color(redIntensity, greenIntensity, blueIntensity);
            }
        }
    }
}