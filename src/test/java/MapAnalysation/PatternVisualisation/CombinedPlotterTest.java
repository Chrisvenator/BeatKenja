package MapAnalysation.PatternVisualisation;

import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CombinedPlotterTest {

    private XYSeries series1;
    private XYSeries series2;

    @BeforeEach
    void setUp() {
        series1 = new XYSeries("Series 1");
        series1.add(0.0, 1.0);
        series1.add(1.0, 2.0);

        series2 = new XYSeries("Series 2");
        series2.add(0.5, 1.5);
        series2.add(1.5, 2.5);
    }

    @Test
    void testConstructor() {
        // Arrange
        String title = "Combined Plotter Test";

        // Act
        CombinedPlotter plotter = new CombinedPlotter(title, series1, series2);

        // Assert
        assertNotNull(plotter.getSeriesList(), "The series list should be initialized");
        assertEquals(2, plotter.getSeriesList().size(), "The series list should contain exactly two series");
        assertEquals(series1, plotter.getSeriesList().get(0), "The first series should match the provided series1");
        assertEquals(series2, plotter.getSeriesList().get(1), "The second series should match the provided series2");
    }

    @Test
    void testGetCombinedDataset() {
        // Arrange
        CombinedPlotter plotter = new CombinedPlotter("Test Plot", series1, series2);

        // Act
        XYSeriesCollection dataset = plotter.getCombinedDataset();

        // Assert
        assertNotNull(dataset, "The dataset should not be null");
        assertEquals(2, dataset.getSeriesCount(), "The dataset should contain two series");
        assertEquals(series1, dataset.getSeries(0), "The first series in the dataset should match series1");
        assertEquals(series2, dataset.getSeries(1), "The second series in the dataset should match series2");
    }

    @Test
    void testVisualize() {
        // Arrange
        CombinedPlotter plotter = new CombinedPlotter("Test Plot", series1, series2);

        // Act
        plotter.visualize();

        // Assert
        assertTrue(plotter.getContentPane() instanceof org.jfree.chart.ChartPanel, "The content pane should be an instance of ChartPanel");
        org.jfree.chart.ChartPanel chartPanel = (org.jfree.chart.ChartPanel) plotter.getContentPane();
        assertNotNull(chartPanel.getChart(), "The chart should be created and set in the ChartPanel");

        XYSeriesCollection dataset = (XYSeriesCollection) chartPanel.getChart().getXYPlot().getDataset();
        assertEquals(3, dataset.getSeriesCount(), "The dataset should contain two series");
        assertEquals(series1, dataset.getSeries(0), "The first series in the dataset should match series1");
        assertEquals(series2, dataset.getSeries(1), "The second series in the dataset should match series2");
    }
}
