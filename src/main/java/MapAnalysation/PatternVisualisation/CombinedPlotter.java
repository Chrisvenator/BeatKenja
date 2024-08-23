package MapAnalysation.PatternVisualisation;

import MapAnalysation.PatternVisualisation.NpsPlotters.NpsPlotter;
import lombok.Getter;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RefineryUtilities;

import java.util.ArrayList;
import java.util.List;

/**
 * A class that combines multiple XYSeries datasets and plots them together on a single chart.
 * This is useful for visualizing and comparing multiple data series in the same graphical context.
 * <p>
 * The combined plot allows for easy comparison between different datasets, making it suitable for analysis
 * where multiple variables or metrics need to be observed together.
 */
@Getter
public class CombinedPlotter extends NpsPlotter {
    private final List<XYSeries> seriesList;

    /**
     * A class that combines multiple XYSeries datasets and plots them together on a single chart.
     * This is useful for visualizing and comparing multiple data series in the same graphical context.
     * <p>
     * The combined plot allows for easy comparison between different datasets, making it suitable for analysis
     * where multiple variables or metrics need to be observed together.
     */
    public CombinedPlotter(String title, XYSeries ... series) {
        super(title, title);
        seriesList = new ArrayList<>(List.of(series));
    }

    /**
     * Combines the individual XYSeries into a single dataset for plotting.
     *
     * @return A combined {@code XYSeriesCollection} containing all the series provided to this plotter.
     */
    private XYSeriesCollection getCombinedDataset() {
        XYSeriesCollection dataset = new XYSeriesCollection();
        seriesList.forEach(dataset::addSeries);
        return dataset;
    }

    @Override
    public void visualize(XYSeries ... seriesList) {
        super.visualize(this.seriesList.toArray(new XYSeries[0]));
    }
}


















