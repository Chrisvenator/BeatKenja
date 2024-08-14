package MapAnalysation.PatternVisualisation;

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
 * Combines multiple XYSeries and plots them over each other
 */
@Getter
public class CombinedPlotter extends ApplicationFrame {
    private final List<XYSeries> seriesList;

    public CombinedPlotter(String title, XYSeries ... series) {
        super(title);
        seriesList = new ArrayList<>(List.of(series));
    }

    private XYSeriesCollection getCombinedDataset() {
        XYSeriesCollection dataset = new XYSeriesCollection();
        seriesList.forEach(dataset::addSeries);
        return dataset;
    }


    public void visualize() {
        JFreeChart xylineChart = ChartFactory.createXYLineChart(
                super.getTitle(),
                "Time (Seconds)",
                "Values",
                getCombinedDataset(),
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


















