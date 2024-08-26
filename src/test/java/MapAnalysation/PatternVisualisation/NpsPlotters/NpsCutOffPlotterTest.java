package MapAnalysation.PatternVisualisation.NpsPlotters;

import org.jfree.data.xy.XYSeries;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class NpsCutOffPlotterTest {

    @Test
    void testConstructor() {
        // Arrange
        String title = "NPS Cut Off Plot";
        double threshold = 2.5;
        float from = 0.0f;
        float to = 10.0f;

        // Act
        NpsCutOffPlotter plotter = new NpsCutOffPlotter(title, threshold, from, to);
        XYSeries series = plotter.getSeries();

        // Assert
        assertNotNull(series, "The XYSeries should not be null");
        assertEquals(2, series.getItemCount(), "The series should contain exactly two points");

        assertEquals(from, series.getX(0).floatValue(), 1e-6, "The first point should be at the start time");
        assertEquals(to, series.getX(1).floatValue(), 1e-6, "The second point should be at the end time");

        assertEquals(threshold, series.getY(0).doubleValue(), 1e-6, "The threshold value should be correctly set for the first point");
        assertEquals(threshold, series.getY(1).doubleValue(), 1e-6, "The threshold value should be correctly set for the second point");
    }

    @Test
    void testConstructorWithNegativeThreshold() {
        // Arrange
        String title = "NPS Cut Off Plot with Negative Threshold";
        double threshold = -1.0;
        float from = 0.0f;
        float to = 5.0f;

        // Act
        NpsCutOffPlotter plotter = new NpsCutOffPlotter(title, threshold, from, to);
        XYSeries series = plotter.getSeries();

        // Assert
        assertNotNull(series, "The XYSeries should not be null");
        assertEquals(2, series.getItemCount(), "The series should contain exactly two points");

        assertEquals(from, series.getX(0).floatValue(), 1e-6, "The first point should be at the start time");
        assertEquals(to, series.getX(1).floatValue(), 1e-6, "The second point should be at the end time");

        assertEquals(threshold, series.getY(0).doubleValue(), 1e-6, "The negative threshold value should be correctly set for the first point");
        assertEquals(threshold, series.getY(1).doubleValue(), 1e-6, "The negative threshold value should be correctly set for the second point");
    }

    @Test
    void testConstructorWithZeroThreshold() {
        // Arrange
        String title = "NPS Cut Off Plot with Zero Threshold";
        double threshold = 0.0;
        float from = 0.0f;
        float to = 5.0f;

        // Act
        NpsCutOffPlotter plotter = new NpsCutOffPlotter(title, threshold, from, to);
        XYSeries series = plotter.getSeries();

        // Assert
        assertNotNull(series, "The XYSeries should not be null");
        assertEquals(2, series.getItemCount(), "The series should contain exactly two points");

        assertEquals(from, series.getX(0).floatValue(), 1e-6, "The first point should be at the start time");
        assertEquals(to, series.getX(1).floatValue(), 1e-6, "The second point should be at the end time");

        assertEquals(threshold, series.getY(0).doubleValue(), 1e-6, "The zero threshold value should be correctly set for the first point");
        assertEquals(threshold, series.getY(1).doubleValue(), 1e-6, "The zero threshold value should be correctly set for the second point");
    }

    @Test
    void testConstructorWithFromGreaterThanTo() {
        // Arrange
        String title = "NPS Cut Off Plot with Inverted Time Range";
        double threshold = 2.5;
        float from = 0.0f;
        float to = 10.0f;

        // Act
        NpsCutOffPlotter plotter = new NpsCutOffPlotter(title, threshold, to, from);
        XYSeries series = plotter.getSeries();

        // Assert
        assertNotNull(series, "The XYSeries should not be null");
        assertEquals(2, series.getItemCount(), "The series should contain exactly two points");

        assertEquals(from, series.getX(0).floatValue(), 1e-6, "The first point should be at the 'from' time");
        assertEquals(to, series.getX(1).floatValue(), 1e-6, "The second point should be at the 'to' time");

        assertEquals(threshold, series.getY(0).doubleValue(), 1e-6, "The threshold value should be correctly set for the first point");
        assertEquals(threshold, series.getY(1).doubleValue(), 1e-6, "The threshold value should be correctly set for the second point");
    }
}
