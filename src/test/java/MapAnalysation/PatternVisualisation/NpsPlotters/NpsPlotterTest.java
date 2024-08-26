package MapAnalysation.PatternVisualisation.NpsPlotters;

import org.jfree.chart.ChartPanel;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.junit.jupiter.api.Test;

import javax.swing.*;

import static org.junit.jupiter.api.Assertions.*;

class NpsPlotterTest {

    // Concrete subclass for testing the abstract NpsPlotter class
    private static class TestNpsPlotter extends NpsPlotter {
        public TestNpsPlotter(String title, String freeChartTitle) {
            super(title, freeChartTitle);
        }
    }

    @Test
    void testConstructor() {
        // Arrange
        String title = "Test NPS Plotter";
        String chartTitle = "Test Chart";

        // Act
        NpsPlotter plotter = new TestNpsPlotter(title, chartTitle);

        // Assert
        assertNotNull(plotter.getSeries(), "The series should be initialized");
        assertEquals(chartTitle, plotter.getFreeChartTitle(), "The chart title should match the input");
        assertEquals(title, plotter.getTitle(), "The frame title should match the input");
        assertEquals(WindowConstants.DISPOSE_ON_CLOSE, plotter.getDefaultCloseOperation(), "The default close operation should be DISPOSE_ON_CLOSE");
    }

    @Test
    void testVisualizeWithSingleSeries() {
        // Arrange
        NpsPlotter plotter = new TestNpsPlotter("Test NPS Plotter", "Test Chart");
        XYSeries series = plotter.getSeries();
        series.add(0.0, 1.0);
        series.add(1.0, 2.0);

        // Act
        plotter.visualize();

        // Assert
        assertTrue(plotter.getContentPane() instanceof ChartPanel, "The content pane should be an instance of ChartPanel");
        ChartPanel chartPanel = (ChartPanel) plotter.getContentPane();
        assertNotNull(chartPanel.getChart(), "The chart should be created and set in the ChartPanel");

        XYSeriesCollection dataset = (XYSeriesCollection) chartPanel.getChart().getXYPlot().getDataset();
        assertEquals(1, dataset.getSeriesCount(), "The dataset should contain one series");
        assertEquals(series, dataset.getSeries(0), "The series should match the one added to the plotter");
    }

    @Test
    void testVisualizeWithMultipleSeries() {
        // Arrange
        NpsPlotter plotter = new TestNpsPlotter("Test NPS Plotter", "Test Chart");
        XYSeries series1 = new XYSeries("Series 1");
        series1.add(0.0, 1.0);
        series1.add(1.0, 2.0);

        XYSeries series2 = new XYSeries("Series 2");
        series2.add(0.5, 1.5);
        series2.add(1.5, 2.5);

        // Act
        plotter.visualize(series1, series2);

        // Assert
        assertTrue(plotter.getContentPane() instanceof ChartPanel, "The content pane should be an instance of ChartPanel");
        ChartPanel chartPanel = (ChartPanel) plotter.getContentPane();
        assertNotNull(chartPanel.getChart(), "The chart should be created and set in the ChartPanel");

        XYSeriesCollection dataset = (XYSeriesCollection) chartPanel.getChart().getXYPlot().getDataset();
        assertEquals(3, dataset.getSeriesCount(), "The dataset should contain three series (including the plotter's series)");

        assertEquals(series1, dataset.getSeries(0), "The first series should match series1");
        assertEquals(series2, dataset.getSeries(1), "The second series should match series2");
        assertEquals(plotter.getSeries(), dataset.getSeries(2), "The third series should match the plotter's series");
    }

    @Test
    void testVisualizeWithNoAdditionalSeries() {
        // Arrange
        NpsPlotter plotter = new TestNpsPlotter("Test NPS Plotter", "Test Chart");

        // Act
        plotter.visualize();

        // Assert
        assertTrue(plotter.getContentPane() instanceof ChartPanel, "The content pane should be an instance of ChartPanel");
        ChartPanel chartPanel = (ChartPanel) plotter.getContentPane();
        assertNotNull(chartPanel.getChart(), "The chart should be created and set in the ChartPanel");

        XYSeriesCollection dataset = (XYSeriesCollection) chartPanel.getChart().getXYPlot().getDataset();
        assertEquals(1, dataset.getSeriesCount(), "The dataset should contain only the plotter's series");
        assertEquals(plotter.getSeries(), dataset.getSeries(0), "The series in the dataset should match the plotter's series");
    }
}
