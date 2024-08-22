package MapAnalysation.PatternVisualisation.NpsPlotters;

import lombok.Getter;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RefineryUtilities;

@Getter
public abstract class NpsPlotter extends ApplicationFrame {
    protected final XYSeries series;
    protected final String freeChartTitle;

    public NpsPlotter(String title, String freeChartTitle) {
        super(title);
        this.series = new XYSeries(super.getTitle());
        this.freeChartTitle = freeChartTitle;

    }

    /**
     * Displays the NPS visualization in a new window.
     * The plot shows how the NPS varies over time, using the data computed from the notes.
     * <p>
     * The chart is displayed in a window with a size of 800x600 pixels and is centered on the screen.
     */
    public void visualize(){
        XYSeriesCollection dataset = new XYSeriesCollection();
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
