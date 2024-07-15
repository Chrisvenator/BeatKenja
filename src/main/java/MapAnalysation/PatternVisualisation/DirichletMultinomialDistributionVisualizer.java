package MapAnalysation.PatternVisualisation;

import MapGeneration.GenerationElements.Exceptions.NoteNotValidException;
import MapGeneration.GenerationElements.Pattern;

import javax.swing.*;
import java.awt.*;
import java.util.Random;

public class DirichletMultinomialDistributionVisualizer extends JFrame {

    private static final int MAX_ARRAY_SIZE = 10; // Adjust size according to the practical limits

    public static void main(String[] args) throws NoteNotValidException {
        Pattern pattern = new Pattern("C:\\Users\\SCCO\\IdeaProjects\\BeatKenja\\src\\main\\resources\\MapTemplates\\Template--ISeeFire.txt");
        double[] alpha = {2.0, 2.0, 2.0}; // Dirichlet parameters
        int N = 10; // Number of draws

        EventQueue.invokeLater(() -> {
            DirichletMultinomialDistributionVisualizer ex = new DirichletMultinomialDistributionVisualizer(pattern, alpha, N);
            ex.setVisible(true);
        });

        pattern.visualizeAsHeatmapNormalizedLogarithmically();
    }

    public DirichletMultinomialDistributionVisualizer(Pattern pattern, double[] alpha, int N) {
        int[][] originalCount = copyArray(pattern.count);

        pattern.applyDirichletMultinomial(alpha, N);

        JPanel panel = new JPanel(new GridLayout(2, 1));
        panel.add(createHeatMapPanel(originalCount, "Original Counts"));
        panel.add(createHeatMapPanel(pattern.count, "Modified Counts"));

        add(panel);

        setTitle("Pattern Heatmap Visualizer");
        setSize(400, 800); // Scaled up for visibility
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
    }

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

    private Color getColorForValue(int value, int maxCount) {
        float ratio = (maxCount > 0) ? (float) value / maxCount : 0;
        int blueIntensity = (int) (ratio * 255);
        return new Color(0, 0, blueIntensity); // Black to blue gradient
    }

    private int[][] copyArray(int[][] source) {
        int[][] copy = new int[source.length][];
        for (int i = 0; i < source.length; i++) {
            copy[i] = new int[source[i].length];
            System.arraycopy(source[i], 0, copy[i], 0, source[i].length);
        }
        return copy;
    }
}
