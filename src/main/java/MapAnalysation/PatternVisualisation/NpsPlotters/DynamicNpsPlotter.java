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
 */
@Getter
public class DynamicNpsPlotter extends ApplicationFrame {
    private final XYSeries series;

    private final List<Note> notes;
    private final float intervalSize;
    private final int rangeIntervals;

    public DynamicNpsPlotter(String title, List<Note> notes, float intervalSize, int rangeIntervals) {
        super(title);
        this.notes = notes;
        this.intervalSize = intervalSize;
        this.rangeIntervals = rangeIntervals;

        series = new XYSeries(super.getTitle());

        List<NpsInfo> npsInfoList = computeNps(notes, intervalSize, rangeIntervals);

        for (NpsInfo npsInfo : npsInfoList) {
            series.add((npsInfo.fromTime() + npsInfo.toTime()) / 2, npsInfo.nps());
        }
    }

    public static List<NpsInfo> computeNps(List<Note> notes, float intervalSize, int rangeIntervals) {
        List<NpsInfo> npsInfoList = new ArrayList<>();
        if (notes.isEmpty())
            return npsInfoList;

        // Sort notes by time to ensure proper interval calculation
        notes.sort(Comparator.comparingDouble(note -> note._time));

        float maxTime = notes.get(notes.size() - 1)._time;
        for (float currentTime = 0; currentTime <= maxTime; currentTime += intervalSize) {
            float fromTime = currentTime - rangeIntervals * intervalSize;
            float toTime = currentTime + rangeIntervals * intervalSize;
            int noteCount = 0;

            for (Note note : notes) {
                if (note._time >= fromTime && note._time < toTime) {
                    noteCount++;
                }
            }

            float nps = noteCount / (toTime - fromTime);
            npsInfoList.add(new NpsInfo(nps, fromTime, toTime));
        }

        return npsInfoList;
    }

    public void visualize() {
        XYSeriesCollection dataset = new XYSeriesCollection();
        dataset.addSeries(series);

        JFreeChart xylineChart = ChartFactory.createXYLineChart(
                "Notes Per Second (NPS)",
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