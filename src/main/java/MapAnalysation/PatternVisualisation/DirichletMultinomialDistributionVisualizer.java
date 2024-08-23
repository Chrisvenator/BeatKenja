package MapAnalysation.PatternVisualisation;

import MapGeneration.GenerationElements.Pattern;

import javax.swing.*;
import java.awt.*;

/**
 * A visualizer for displaying the Dirichlet-Multinomial distribution of a pattern in the form of a heatmap.
 * This class extends {@link JFrame} and provides a graphical user interface to visualize the original
 * and modified counts of a pattern after applying the Dirichlet-Multinomial distribution.
 * <p>
 * The visualizer creates two heatmaps: one for the original pattern counts and one for the modified counts
 * after the distribution has been applied. The heatmaps are displayed in a grid layout within the frame.
 */
public class DirichletMultinomialDistributionVisualizer extends JFrame {

    private static final int MAX_ARRAY_SIZE = 40; // Adjust size according to the practical limits


    /**
     * Constructs a new DirichletMultinomialDistributionVisualizer with the given pattern and number of draws (N).
     * The visualizer will display the original and modified pattern counts as heatmaps.
     *
     * @param pattern The pattern to visualize.
     * @param N       The number of draws to apply in the Dirichlet-Multinomial distribution.
     */
    public DirichletMultinomialDistributionVisualizer(Pattern pattern, int N) {
        Pattern p = pattern.deepCopy();

        p.applyDirichletMultinomial(N);

        JPanel panel = new JPanel(new GridLayout(2, 1));
        panel.add(createHeatMapPanel(pattern.count, "Original Counts"));
        panel.add(createHeatMapPanel(p.count, "Modified Counts"));

        add(panel);

        setTitle("Pattern Heatmap Visualizer");
        setSize(400, 800); // Scaled up for visibility
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
    }

    /**
     * Creates a panel displaying a heatmap based on the provided count matrix.
     * The heatmap is represented by a grid of colored labels, where the color intensity represents the count value.
     *
     * @param counts The 2D array representing the counts to visualize.
     * @param title  The title of the heatmap panel.
     * @return A {@link JPanel} containing the heatmap.
     */
    private JPanel createHeatMapPanel(int[][] counts, String title) {
        JPanel gridPanel = new JPanel(new GridLayout(MAX_ARRAY_SIZE, MAX_ARRAY_SIZE));
        gridPanel.setBorder(BorderFactory.createTitledBorder(title));

        int maxCount = findMaxCount(counts);

        for (int i = 0; i < MAX_ARRAY_SIZE; i++) {
            for (int j = 0; j < MAX_ARRAY_SIZE; j++) {
                int value = i < counts.length && j < counts[i].length ? counts[i][j] : 0;
                JLabel label = new JLabel();
                label.setOpaque(true);
                label.setBackground(getColorForValue(value, maxCount));
                gridPanel.add(label);
            }
        }

        return gridPanel;
    }

    /**
     * Finds the maximum count value in a 2D array.
     * This value is used to scale the color intensity in the heatmap.
     *
     * @param counts The 2D array of counts.
     * @return The maximum count value found in the array.
     */
    private int findMaxCount(int[][] counts) {
        int max = 0;
        for (int[] row : counts) {
            for (int val : row) {
                if (val > max) {
                    max = val;
                }
            }
        }
        return max;
    }

    /**
     * Determines the color intensity for a given count value based on the maximum count.
     * The color is represented as a shade of blue, where higher counts result in a deeper blue.
     *
     * @param value    The count value to convert to a color.
     * @param maxCount The maximum count value used for scaling the color intensity.
     * @return A {@link Color} representing the intensity of the count.
     */
    private Color getColorForValue(int value, int maxCount) {
        float ratio = (maxCount > 0) ? (float) value / maxCount : 0;
        int blueIntensity = (int) (ratio * 255);
        return new Color(0, 0, blueIntensity); // Black to blue gradient
    }

    /**
     * Creates a deep copy of a 2D array of integers.
     * This method is used to duplicate the original count arrays without affecting the original data.
     *
     * @param source The source 2D array to copy.
     * @return A deep copy of the source array.
     */
    private int[][] copyArray(int[][] source) {
        int[][] copy = new int[source.length][];
        for (int i = 0; i < source.length; i++) {
            copy[i] = new int[source[i].length];
            System.arraycopy(source[i], 0, copy[i], 0, source[i].length);
        }
        return copy;
    }
}
