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
 * First try, calculating nps. <br>
 * This function is not working as intended...
 */
@Getter
public class StaticNpsPlotter extends ApplicationFrame {
    private final XYSeries series;

    private final int rangeIntervals;

    /**
     * Constructor to create and display the NPS chart.
     *
     * @param title          The title of the chart window.
     * @param notes          The list of notes to be analyzed.
     * @param rangeIntervals The number of intervals to include in NPS calculation.
     */
    public StaticNpsPlotter(String title, List<Note> notes, int rangeIntervals) {
        super(title);
        this.rangeIntervals = rangeIntervals;

        series = new XYSeries(super.getTitle());


        for (NpsInfo npsInfo : computeNps(notes)) {
            series.add((npsInfo.fromTime() + npsInfo.toTime()) / 2, npsInfo.nps());
        }

    }

    /**
     * Converts the Map into a NpsInfo-List
     *
     * @param npsMap map
     * @return List containing the from-, to- nps- values
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
    private List<NpsInfo> computeNps(List<Note> notes) {
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


    public void visualize() {
        XYSeriesCollection dataset = new XYSeriesCollection();
        dataset.addSeries(series);

        // Step 3: Create the chart
        JFreeChart xylineChart = ChartFactory.createXYLineChart(
                getTitle(),
                "Time (Seconds)",
                "NPS",
                dataset,
                PlotOrientation.VERTICAL,
                true, true, false);

        ChartPanel chartPanel = new ChartPanel(xylineChart);
        chartPanel.setPreferredSize(new java.awt.Dimension(800, 600));
        setContentPane(chartPanel);

        pack();
        RefineryUtilities.centerFrameOnScreen(this);
        setVisible(true);
    }

}