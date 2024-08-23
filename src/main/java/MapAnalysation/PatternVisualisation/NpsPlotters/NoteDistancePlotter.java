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
 * <p>
 * A class that calculates and visualizes the distance between consecutive notes in a rhythm game map.
 * The distance is computed as the time difference between each note and its predecessor, with the result inverted for better visualization.
 * The plot created by this class shows how note density changes over time, providing insight into the map's pacing and difficulty.
 * <p>
 * The visualization is created using the JFreeChart library, and it displays an inverted distance on the y-axis, with time on the x-axis.
 * This allows users to easily spot areas of the map with high or low note density.
 */
@Getter
public class NoteDistancePlotter extends NpsPlotter {
    private final XYSeries series;
    private final List<Note> notes;

    /**
     * Constructs a new NoteDistancePlotter with the specified title and list of notes.
     * The constructor computes the distance between each note and prepares the data for visualization.
     *
     * @param title The title of the plot, which will also be used as the title of the window.
     * @param notes The list of notes for which the distances will be calculated and plotted.
     *              Each note must have a time attribute indicating when it occurs.
     */
    public NoteDistancePlotter(String title, List<Note> notes) {
        super(title, "Note Distances");
        this.notes = notes;
        series = new XYSeries(super.getTitle());

        computeNoteDistanceSeries();
    }

    /**
     * Computes the time distance between consecutive notes and stores the inverted distance in the series for plotting.
     * The inversion of the distance is done to make higher densities more prominent in the visualization.
     */
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
}
