package MapAnalysation.PatternVisualisation.NpsPlotters;

import lombok.Getter;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.RefineryUtilities;

import javax.swing.*;
import java.util.Arrays;

/**
 * An abstract class that serves as a base for different types of Notes Per Second (NPS) plotters.
 * This class provides common functionality for visualizing NPS data, such as setting up the chart and displaying it in a window.
 * Subclasses are expected to compute the NPS data and populate the `series` object with the relevant data points.
 * <p>
 * The visualization is created using the JFreeChart library, and it displays how the NPS varies over time.
 * The chart is shown in a window with a default size of 800x600 pixels.
 */
@Getter
public class NpsPlotter extends JFrame {
    protected final XYSeries series;
    protected final String freeChartTitle;

    /**
     * Constructs an NpsPlotter with the specified title and chart title.
     *
     * @param title           The title of the application frame, which will also be used as the title of the window.
     * @param freeChartTitle  The title of the chart that will be displayed on the plot.
     */
    public NpsPlotter(String title, String freeChartTitle) {
        super(title);
        this.series = new XYSeries(super.getTitle());
        this.freeChartTitle = freeChartTitle;
        this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    }

    /**
     * Displays the NPS visualization in a new window.
     * The plot shows how the NPS varies over time, using the data computed from the notes.
     * <p>
     * The chart is displayed in a window with a size of 800x600 pixels and is centered on the screen.
     */
    public void visualize(XYSeries ... seriesList){
        XYSeriesCollection dataset = new XYSeriesCollection();

        if (seriesList != null && seriesList.length != 0) Arrays.stream(seriesList).toList().forEach(dataset::addSeries);
        dataset.addSeries(series);

        JFreeChart xylineChart = ChartFactory.createXYLineChart(
                freeChartTitle,
                "Time (Seconds)",
                "NPS",
                dataset,
                PlotOrientation.VERTICAL,
                true, true, false);

        ChartPanel chartPanel = new ChartPanel(xylineChart);
        chartPanel.setPreferredSize(new java.awt.Dimension(800, 600));
        setContentPane(chartPanel);

        this.pack();
        RefineryUtilities.centerFrameOnScreen(this);
        this.setVisible(true);
    }
}
