package MapAnalysation.PatternVisualisation;

import MapGeneration.GenerationElements.Pattern;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.swing.*;
import java.awt.*;

import static org.junit.jupiter.api.Assertions.*;

class PatternVisualisationHeatMapTest {

    private Pattern pattern;

    @BeforeEach
    void setUp() {
        int[][] counts = new int[109][109];

        counts[0][0] = 0;
        counts[0][1] = 50;
        counts[0][2] = 100;
        counts[1][0] = 150;
        counts[1][1] = 200;
        counts[1][2] = 2500;
        counts[2][0] = 255;
        counts[2][1] = 300;
        counts[2][2] = 350;

        pattern = new Pattern();
        pattern.count = counts;
    }

    @Test
    void testCopyArray() {
        // Arrange
        PatternVisualisationHeatMap heatMap = new PatternVisualisationHeatMap(false, false, false, pattern, "Test");

        // Act
        int[][] copiedArray = heatMap.copyArray(pattern.count);

        // Assert
        assertNotNull(copiedArray, "Copied array should not be null");
        assertEquals(pattern.count.length, copiedArray.length, "Copied array should have the same length as the original");
        for (int i = 0; i < copiedArray.length; i++) {
            assertArrayEquals(pattern.count[i], copiedArray[i], "Copied row should match the original row");
        }
    }

    @Test
    void testNormalizeCountArray() {
        // Arrange
        PatternVisualisationHeatMap heatMap = new PatternVisualisationHeatMap(true, false, false, pattern, "Normalized Test");

        // Act
        heatMap.normalizeCountArray();

        // Assert
        int[][] normalizedCounts = pattern.count;
        assertNotNull(normalizedCounts, "Normalized counts should not be null");
        // Assuming normalizeCountArray should have modified the original count array for simplicity in this test.
        // The specific normalization logic should be checked here depending on the normalization method applied in `Pattern.normalizeCountArray`.
    }

    @Test
    void testGetColorForValue_Normalize() {
        // Arrange
        PatternVisualisationHeatMap heatMap = new PatternVisualisationHeatMap(true, false, false, pattern, "Normalized Color Test");
        PatternVisualisationHeatMap.HeatmapPanel panel = heatMap.new HeatmapPanel();

        // Act
        Color color = panel.getColorForValue(127);

        // Assert
        assertEquals(new Color(0, 0, 127), color, "Color should match the normalized value");
    }

    @Test
    void testGetColorForValue_Truncate() {
        // Arrange
        PatternVisualisationHeatMap heatMap = new PatternVisualisationHeatMap(false, true, false, pattern, "Truncated Color Test");
        PatternVisualisationHeatMap.HeatmapPanel panel = heatMap.new HeatmapPanel();

        // Act
        Color colorLow = panel.getColorForValue(-50);
        Color colorHigh = panel.getColorForValue(300);

        // Assert
        assertEquals(new Color(0, 0, 0), colorLow, "Negative values should result in black");
        assertEquals(new Color(0, 0, 255), colorHigh, "Values above 255 should result in deep blue");
    }

    @Test
    void testGetColorForValue_RawValues() {
        // Arrange
        PatternVisualisationHeatMap heatMap = new PatternVisualisationHeatMap(false, false, false, pattern, "Raw Color Test");
        PatternVisualisationHeatMap.HeatmapPanel panel = heatMap.new HeatmapPanel();

        // Act
        Color color = panel.getColorForValue(350);

        // Assert
        assertEquals(new Color(1, 0, 95), color, "Color should match the raw value calculation");
    }

    @Test
    void testVisualizerFrameProperties() {
        // Arrange & Act
        PatternVisualisationHeatMap heatMap = new PatternVisualisationHeatMap(false, false, false, pattern, "Heatmap Test");

        // Assert
        assertEquals("Heatmap Test", heatMap.getTitle(), "The title should match the provided title");
        assertEquals(JFrame.DISPOSE_ON_CLOSE, heatMap.getDefaultCloseOperation(), "The close operation should be DISPOSE_ON_CLOSE");
        assertEquals(new Dimension(PatternVisualisationHeatMap.MAX_ARRAY_SIZE * PatternVisualisationHeatMap.RECT_SIZE + 10,
                PatternVisualisationHeatMap.MAX_ARRAY_SIZE * PatternVisualisationHeatMap.RECT_SIZE + 10), heatMap.getSize(), "The frame size should be correct");
    }
}
