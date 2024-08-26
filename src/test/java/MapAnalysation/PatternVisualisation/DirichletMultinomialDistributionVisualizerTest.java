package MapAnalysation.PatternVisualisation;

import MapGeneration.GenerationElements.Pattern;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.swing.*;
import java.awt.*;

import static org.junit.jupiter.api.Assertions.*;

class DirichletMultinomialDistributionVisualizerTest {

    private int[][] counts;
    private DirichletMultinomialDistributionVisualizer visualizer;

    @BeforeEach
    void setUp() {
        counts = new int[][]{
                {1, 2, 3},
                {4, 5, 6},
                {7, 8, 9}
        };

        // Mock Pattern and necessary methods for testing
        Pattern pattern = new Pattern();
        pattern.count = counts;

        visualizer = new DirichletMultinomialDistributionVisualizer(pattern, 10);
    }

    @Test
    void testFindMaxCount() {
        // Act
        int maxCount = visualizer.findMaxCount(counts);

        // Assert
        assertEquals(9, maxCount, "The maximum count should be 9");
    }

    @Test
    void testGetColorForValue() {
        // Arrange
        int maxCount = 9;

        // Act & Assert
        Color color1 = visualizer.getColorForValue(1, maxCount);
        assertEquals(new Color(0, 0, 28), color1, "Color for value 1 should be a light blue");

        Color color9 = visualizer.getColorForValue(9, maxCount);
        assertEquals(new Color(0, 0, 255), color9, "Color for value 9 should be a deep blue");

        Color color0 = visualizer.getColorForValue(0, maxCount);
        assertEquals(new Color(0, 0, 0), color0, "Color for value 0 should be black");
    }

    @Test
    void testCreateHeatMapPanel() {
        // Act
        JPanel panel = visualizer.createHeatMapPanel(counts, "Test Heatmap");

        // Assert
        assertNotNull(panel, "The panel should not be null");
        assertEquals(DirichletMultinomialDistributionVisualizer.getMaxArraySize() * DirichletMultinomialDistributionVisualizer.getMaxArraySize(), panel.getComponentCount(),
                "The panel should contain the correct number of components");
    }

    @Test
    void testCreateHeatMapPanelWithSmallerArray() {
        // Arrange
        int[][] smallCounts = {
                {1, 2},
                {3, 4}
        };

        // Act
        JPanel panel = visualizer.createHeatMapPanel(smallCounts, "Small Heatmap");

        // Assert
        assertNotNull(panel, "The panel should not be null");
        assertEquals(DirichletMultinomialDistributionVisualizer.getMaxArraySize() * DirichletMultinomialDistributionVisualizer.getMaxArraySize(), panel.getComponentCount(),
                "The panel should contain the correct number of components");
    }

    @Test
    void testVisualizerFrameProperties() {
        // Assert the basic properties of the frame
        assertEquals("Pattern Heatmap Visualizer", visualizer.getTitle(), "The title should be 'Pattern Heatmap Visualizer'");
        assertEquals(new Dimension(400, 800), visualizer.getSize(), "The frame size should be 400x800");
        assertEquals(JFrame.DISPOSE_ON_CLOSE, visualizer.getDefaultCloseOperation(), "The close operation should be DISPOSE_ON_CLOSE");
    }
}
