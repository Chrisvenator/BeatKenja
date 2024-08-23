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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * First try at calculating nps. <br>
 * This function is not working as intended...
 * <p>
 * A class that attempts to calculate and visualize the Notes Per Second (NPS) for a given set of notes.
 * This class aggregates the note counts over specified time intervals, but it is noted that this implementation
 * may not work as intended and could produce inaccurate results.
 * <p>
 * The visualization is created using the JFreeChart library, displaying how the NPS varies over time.
 * This plot may help in analyzing the overall distribution of notes in a rhythm game map, though the results
 * should be interpreted with caution due to potential issues in the NPS calculation.
 */
@Getter
public class StaticNpsPlotter extends NpsPlotter {
    private final int rangeIntervals;

    /**
     * Constructs a new StaticNpsPlotter with the specified title, list of notes, and range intervals.
     * It calculates the NPS based on the provided notes and prepares the data for visualization.
     *
     * @param title          The title of the chart, which will also be used as the title of the window.
     * @param notes          The list of notes to be analyzed, where each note must have a time attribute indicating when it occurs.
     * @param rangeIntervals The number of intervals before and after each second to include in the NPS calculation.
     */
    public StaticNpsPlotter(String title, List<Note> notes, int rangeIntervals) {
        super(title, "Notes Per Second (NPS)");
        this.rangeIntervals = rangeIntervals;

        for (NpsInfo npsInfo : computeNps(notes)) {
            series.add((npsInfo.fromTime() + npsInfo.toTime()) / 2, npsInfo.nps());
        }
    }

    /**
     * Converts a map of NPS data into a list of NpsInfo objects.
     *
     * @param npsMap A map where the key is the time in seconds and the value is the NPS value.
     * @return A list of NpsInfo objects representing the NPS data.
     */
    private static List<NpsInfo> toNpsInfo(Map<Integer, Integer> npsMap) {
        List<NpsInfo> npsInfos = new ArrayList<>();
        for (Map.Entry<Integer, Integer> entry : npsMap.entrySet()) {
            int fromTime = npsMap.isEmpty() ? 0 : npsMap.get(entry.getKey());
            int toTime = entry.getKey();
            int nps = entry.getValue();

            npsInfos.add(new NpsInfo(fromTime, toTime, nps));
        }

        return npsInfos;
    }

    /**
     * Computes the Notes Per Second (NPS) from a list of notes.
     *
     * @param notes A list of Note objects where each note has a time in seconds.
     * @return A map where the key is the Second and the value is the aggregated number of notes within the defined interval.
     */
    public List<NpsInfo> computeNps(List<Note> notes) {
        Map<Integer, Integer> npsMap = new TreeMap<>();

        // First, compute the basic NPS (notes per each second)
        for (Note note : notes) {
            int second = (int) Math.floor(note._time);
            npsMap.put(second, npsMap.getOrDefault(second, 0) + 1);
        }

        // Now, adjust the NPS to include notes from the previous and next intervals
        Map<Integer, Integer> adjustedNpsMap = new TreeMap<>();
        for (Map.Entry<Integer, Integer> entry : npsMap.entrySet()) {
            int second = entry.getKey();
            int aggregatedNps = 0;

            // Sum up the notes from second-rangeIntervals to second+rangeIntervals
            for (int offset = -rangeIntervals; offset <= rangeIntervals; offset++) {
                aggregatedNps += npsMap.getOrDefault(second + offset, 0);
            }

            adjustedNpsMap.put(second, aggregatedNps);
        }

        return toNpsInfo(adjustedNpsMap);
    }
}