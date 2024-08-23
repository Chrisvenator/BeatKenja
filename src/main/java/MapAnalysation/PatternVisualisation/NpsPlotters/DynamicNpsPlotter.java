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
import java.util.Comparator;
import java.util.List;

/**
 * Probably the most realistic representation of the nps.
 * It takes the previous and following X notes into account.
 * This function should be used to calculate nps!
 * <p>
 * A class that visualizes the dynamic Notes Per Second (NPS) of a given set of notes.
 * The NPS is calculated by considering a specified range of notes around each time interval,
 * providing a more realistic representation of note density over time.
 * <p>
 * This class is designed to plot the NPS data using a graphical chart,
 * with the ability to adjust the size of the time intervals and the range of notes considered.
 * <p>
 * The visualization is built using the JFreeChart library, and it displays the NPS
 * on a line chart, where the x-axis represents time and the y-axis represents the NPS.
 * <p>
 * This class is intended for analyzing the note density in rhythm games such as Beat Saber.
 */
@Getter
public class DynamicNpsPlotter extends NpsPlotter {

    private final List<Note> notes;
    private final float intervalSize;
    private final int rangeIntervals;

    /**
     * Constructs a new DynamicNpsPlotter with the specified title and parameters for NPS calculation.
     *
     * @param title          The title of the plot, which will also be used as the title of the window.
     * @param notes          The list of notes to analyze. Each note must have a time attribute that indicates when it occurs.
     * @param intervalSize   The size of the time interval (in seconds) over which to calculate the NPS. This value determines how the time is divided into intervals.
     * @param rangeIntervals The number of intervals before and after the current time to consider when calculating the NPS.
     */
    public DynamicNpsPlotter(String title, List<Note> notes, float intervalSize, int rangeIntervals) {
        super(title, "Notes Per Second (NPS)");
        this.notes = notes;
        this.intervalSize = intervalSize;
        this.rangeIntervals = rangeIntervals;

        List<NpsInfo> npsInfoList = computeNps(notes, intervalSize, rangeIntervals, true);

        for (NpsInfo npsInfo : npsInfoList) {
            series.add((npsInfo.fromTime() + npsInfo.toTime()) / 2, npsInfo.nps());
        }
    }

    /**
     * Computes the Notes Per Second (NPS) for a given list of notes over specified time intervals.
     *The method calculates the NPS by analyzing how many notes fall within a sliding time window.
     * <br>
     * The timings of the Notes should be in Seconds to get the NPS.
     * !! IMPORTANT: !! The function returns NPB (Notes per Beat), when the timings are have not been converted to seconds instead of beats.
     *
     * @param notes          The list of notes to analyze. Each note must have a time attribute that indicates when it occurs.
     * @param intervalSize   The size of the time interval (in seconds) over which to calculate the NPS. This value determines how the time is divided into intervals.
     * @param rangeIntervals The number of intervals before and after the current time to consider when calculating the NPS.
     * @return A list of {@code NpsInfo} objects, each containing the NPS value for a specific time range, along with the start and end times of that range.
     */
    public static List<NpsInfo> computeNps(List<Note> notes, float intervalSize, int rangeIntervals, boolean ignoreStacksAndSliders) {
        List<NpsInfo> npsInfoList = new ArrayList<>();
        if (notes.isEmpty())
            return npsInfoList;

        // Sort notes by time to ensure proper interval calculation
        notes.sort(Comparator.comparingDouble(note -> note._time));

        float maxTime = notes.get(notes.size() - 1)._time;
        for (float currentTime = 0; currentTime <= maxTime; currentTime += intervalSize) {
            float fromTime = currentTime - rangeIntervals * intervalSize;
            float toTime = currentTime + rangeIntervals * intervalSize - 0.001f;
            float nps = getNps(notes, fromTime, toTime);

            npsInfoList.add(new NpsInfo(nps, fromTime, toTime));
        }

        return npsInfoList;
    }

    private static float getNps(List<Note> notes, float fromTime, float toTime) {
        int noteCount = 0;

        Note prev = null;
        for (Note note : notes) {
            if ((note._time >= fromTime && note._time < toTime)) {
                if (prev != null) {
                    float timeDiff = Math.abs(note._time - fromTime);
                    if (timeDiff >= (1.1/16)) { //SLiders are normally placed at 1/16 beats
                        noteCount++;
                    }
                }
            }
            prev = note;
        }

        return noteCount / (toTime - fromTime);
    }
}