package MapAnalysation.PatternVisualisation.NpsPlotters;

import lombok.Getter;
import org.jfree.data.xy.XYSeries;
import org.jfree.ui.ApplicationFrame;

/**
 * calculates the average nps throughout the map. It starts at 0 and ends with the last note
 */
@Getter
public class NpsCutOffPlotter extends ApplicationFrame {
    private final XYSeries series;

    public NpsCutOffPlotter(String title, double threshold, float from, float to) {
        super(title);

        this.series = new XYSeries(super.getTitle());

        series.add(from, threshold);
        series.add(to, threshold);
    }
}
