package MapAnalysation.PatternVisualisation.NpsPlotters;

import lombok.Getter;
import org.jfree.data.xy.XYSeries;
import org.jfree.ui.ApplicationFrame;

/**
 * calculates the average nps throughout the map. It starts at 0 and ends with the last note
 * <p>
 * A class that plots a horizontal line representing a cutoff threshold for the Notes Per Second (NPS) over a specified time range.
 * This visualization helps to identify areas of the map where the NPS exceeds or falls below a certain threshold.
 * <p>
 * The plot is created using the JFreeChart library and displays a simple horizontal line across the specified time range,
 * indicating the NPS threshold.
 */

@Getter
public class NpsCutOffPlotter extends NpsPlotter {

    /**
     * Constructs a new NpsCutOffPlotter with the specified title, threshold, and time range.
     *
     * @param title     The title of the plot, which will also be used as the title of the window.
     * @param threshold The NPS threshold value that will be plotted as a horizontal line.
     * @param from      The start time of the range over which the threshold line will be plotted.
     * @param to        The end time of the range over which the threshold line will be plotted.
     */
    public NpsCutOffPlotter(String title, double threshold, float from, float to) {
        super(title, "NPS Cut Off");
        if (from > to) {
            float temp = from;
            from = to;
            to = temp;
        }

        series.add(from, threshold);
        series.add(to, threshold);
    }
}
