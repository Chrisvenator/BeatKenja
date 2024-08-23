package MapAnalysation.PatternVisualisation.NpsPlotters;

import BeatSaberObjects.Objects.Note;
import DataManager.Parameters;
import lombok.Getter;
import org.jfree.data.xy.XYSeries;
import org.jfree.ui.ApplicationFrame;

import java.util.ArrayList;
import java.util.List;

/**
 * Calculates the average nps throughout the map. It starts at 0 and ends with the last note
 * <p>
 * Plots the average Notes Per Second (NPS) across an entire map, starting from the first note and ending with the last note.
 * The average NPS is calculated as the total number of notes divided by the time span from the first to the last note.
 * <p>
 * This class is designed to provide a quick visualization of the overall note density in a rhythm game map,
 * allowing for an easy comparison between different maps or sections of a map.
 * <p>
 * The visualization is built using the JFreeChart library, and it displays a simple line plot
 * where the NPS is constant across the time span of the map.
 */
@Getter
public class AverageNpsPlotter extends NpsPlotter{
    /**
     * Constructs a new AverageNpsPlotter with the specified title and notes list.
     *
     * @param title The title of the plot, which will also be used as the title of the window.
     * @param notes The list of notes to analyze. Each note must have a time attribute that indicates when it occurs.
     */
    public AverageNpsPlotter(String title, List<Note> notes) {
        super(title, "Average Notes Per Second (NPS)");

        NpsInfo info = getAverageNps(notes).get(0);
        series.add(info.fromTime(), info.nps());
        series.add(info.toTime(), info.nps());
    }

    /**
     * Calculates the average Notes Per Second (NPS) for the given list of notes.
     * The method determines the average NPS by dividing the total number of notes
     * by the total duration from the first to the last note.
     *
     * @param notes The list of notes to analyze. Each note must have a time attribute that indicates when it occurs.
     * @return A list containing a single {@code NpsInfo} object, which holds the average NPS value,
     *          along with the start and end times of the note sequence.
     */
    public static List<NpsInfo> getAverageNps(List<Note> notes) {
        List<NpsInfo> npsInfos = new ArrayList<>();

        float f = notes.size() / notes.get(notes.size() - 1)._time;
        Parameters.logger.debug("Average Nps: {}", f);
        npsInfos.add(new NpsInfo(notes.size() / notes.get(notes.size() - 1)._time, 0, notes.get(notes.size() - 1)._time));

        return npsInfos;
    }
}
