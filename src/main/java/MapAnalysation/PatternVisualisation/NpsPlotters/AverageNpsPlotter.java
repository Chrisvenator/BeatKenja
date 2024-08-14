package MapAnalysation.PatternVisualisation.NpsPlotters;

import BeatSaberObjects.Objects.Note;
import lombok.Getter;
import org.jfree.data.xy.XYSeries;
import org.jfree.ui.ApplicationFrame;

import java.util.ArrayList;
import java.util.List;

/**
 * calculates the average nps throughout the map. It starts at 0 and ends with the last note
 */
@Getter
public class AverageNpsPlotter extends ApplicationFrame {
    private final XYSeries series;

    public AverageNpsPlotter(String title, List<Note> notes) {
        super(title);

        this.series = new XYSeries(super.getTitle());

        NpsInfo info = getAverageNps(notes).get(0);
        series.add(info.fromTime(), info.nps());
        series.add(info.toTime(), info.nps());
    }

    public static List<NpsInfo> getAverageNps(List<Note> notes) {
        List<NpsInfo> npsInfos = new ArrayList<>();

        float f = notes.size()/notes.get(notes.size() - 1)._time;
        System.out.println(f);
        npsInfos.add(new NpsInfo(notes.size()/notes.get(notes.size() - 1)._time, 0, notes.get(notes.size() - 1)._time));

        return npsInfos;
    }
}
