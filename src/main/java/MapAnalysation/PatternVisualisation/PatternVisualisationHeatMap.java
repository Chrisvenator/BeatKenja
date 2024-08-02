package MapAnalysation.PatternVisualisation;

import DataManager.Records.PatMetadata;
import MapGeneration.GenerationElements.Pattern;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;


/**
 * PatternVisualisationHeatMap is a Java Swing application that visualizes the count array of a Pattern as a heatmap.
 */
public class PatternVisualisationHeatMap extends JFrame {
    private static final int MAX_ARRAY_SIZE = 109;
    private static final int RECT_SIZE = 10;
    private final int[][] count;
    private final boolean normalize;
    private final boolean truncate;
    private final boolean logaritmic;

    public static void main(String[] args) {
        //AllMapsGroupedV1; 98; 4;[StandardExpert];Dance;Dance
        ArrayList<String> difficulties = new ArrayList<>();
        difficulties.add("StandardExpert");

        ArrayList<String> tags = new ArrayList<>();
        tags.add("Dance");

        ArrayList<String> genres = new ArrayList<>();
        genres.add("Dance");


        PatMetadata patMetadata = new PatMetadata("AllMapsGroupedV1", 98, 4, difficulties, tags, genres);
        Pattern p = new Pattern(patMetadata);

        p.visualizeAsHeatmapTruncated();
        p.visualizeAsHeatmap();
        p.visualizeAsHeatmapNormalized("");
        p.visualizeAsHeatmapNormalizedLogarithmically();
    }

    /**
     * Launches the visualization in a Swing window.
     * Normalize has a higher priority than truncate
     *
     * @param normalize If true, normalize the count array values.
     * @param truncate  If true, truncate the count array values to the range 0-255.
     * @param p         The pattern to visualize.
     */
    private static void visualize(boolean normalize, boolean truncate, boolean log, Pattern p, String name) {
        new PatternVisualisationHeatMap(normalize, truncate, log, p, name);
    }

    public static void visualizeAsHeatmapNormalized(Pattern p, String name) {
        PatternVisualisationHeatMap.visualize(true, false, false, p, name);
    }

    public static void visualizeAsHeatmapLogarithmicNormalized(Pattern p, String name) {
        PatternVisualisationHeatMap.visualize(true, false, true, p, name);
    }

    public static void visualizeAsHeatmap(Pattern p, String name) {
        PatternVisualisationHeatMap.visualize(false, false, false, p, name);
    }

    public static void visualizeAsHeatmapTruncated(Pattern p, String name) {
        PatternVisualisationHeatMap.visualize(false, true, false, p, name);
    }


    /**
     * Constructs the PatternVisualisationHeatMap window.
     *
     * @param normalize If true, normalize the count array values.
     * @param truncate  If true, truncate the count array values to the range 0-255.
     * @param log       If true, normalize the count array values logarithmically.
     * @param p         The pattern to visualize.
     */
    private PatternVisualisationHeatMap(boolean normalize, boolean truncate, boolean log, Pattern p, String name) {
        this.normalize = normalize;
        this.truncate = truncate;
        this.logaritmic = log;
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

    private void normalizeCountArray() {
        Pattern.normalizeCountArray(count, logaritmic);
    }


    /**
     * A JPanel subclass that draws the heatmap based on the count array values.
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