package MapAnalysation.PatternVisualisation.NpsPlotters;

import BeatSaberObjects.Objects.Note;
import lombok.Getter;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RefineryUtilities;

import java.util.List;

/**
 * Calculates the distance between each note and plots it
 */
@Getter
public class NoteDistancePlotter extends ApplicationFrame {
    private final XYSeries series;

    private final List<Note> notes;

    public NoteDistancePlotter(String title, List<Note> notes) {
        super(title);
        this.notes = notes;

        series = new XYSeries(super.getTitle());

        computeNoteDistanceSeries();
    }

    private void computeNoteDistanceSeries() {
        // Calculate the distance between each note and plot it
        for (int i = 1; i < notes.size(); i++) {
            float timeCurrent = notes.get(i)._time;
            float timePrevious = notes.get(i - 1)._time;
            float distance = timeCurrent - timePrevious;

            // Invert the distance for plotting
            if (distance > 0) {
                float invertedDistance = 1 / distance;

                // Add the data point to the series
                series.add(timeCurrent, invertedDistance);
            }
        }
    }


    public void visualize() {
        XYSeriesCollection dataset = new XYSeriesCollection();
        dataset.addSeries(series);

        JFreeChart xylineChart = ChartFactory.createXYLineChart(
                "Note Distances",
                "Time (Seconds)",
                "Inverted Distance",
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
