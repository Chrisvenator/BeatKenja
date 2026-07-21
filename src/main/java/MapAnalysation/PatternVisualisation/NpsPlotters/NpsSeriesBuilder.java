package MapAnalysation.PatternVisualisation.NpsPlotters;

import BeatSaberObjects.Objects.Note;

import java.util.ArrayList;
import java.util.List;

/**
 * UI-free data layer for the NPS overview screen: turns a map's notes (in beats)
 * into an NPS-over-time series, headline KPIs and note-density bins.
 *
 * All methods copy the input notes and convert beats to seconds locally with the
 * given BPM. They never touch the global {@code Parameters.BPM} and never mutate
 * the passed array — important because {@code NpsBpmConverter} converts in place
 * and {@code DynamicNpsPlotter.computeNps} sorts its input list in place.
 */
public final class NpsSeriesBuilder {

    /** Headline numbers for one difficulty, all in seconds. */
    public record NpsKpis(float averageNps, float peakNps, float peakTimeSeconds, int noteCount, float mapLengthSeconds) {}

    private NpsSeriesBuilder() {
    }

    /**
     * Computes the NPS series for notes given in beats.
     * Copies the notes, converts the copies to seconds with the given BPM and
     * delegates to {@link DynamicNpsPlotter#computeNps} (same math as the old Review chart).
     */
    public static List<NpsInfo> computeNpsSeconds(Note[] notesInBeats, double bpm, float intervalSize, int rangeIntervals) {
        List<Note> seconds = copyInSeconds(notesInBeats, bpm);
        return DynamicNpsPlotter.computeNps(seconds, intervalSize, rangeIntervals, true);
    }

    /**
     * Derives the KPI row from the notes and a precomputed NPS series.
     * Average = note count / last note time (no audio duration is parsed anywhere,
     * so "map length" means "time of the last note"). Empty input yields all zeros;
     * the peak time is clamped to 0 because the first sliding windows start before 0.
     */
    public static NpsKpis computeKpis(Note[] notesInBeats, double bpm, List<NpsInfo> samples) {
        List<Note> seconds = copyInSeconds(notesInBeats, bpm);
        if (seconds.isEmpty()) return new NpsKpis(0, 0, 0, 0, 0);

        float lastTime = 0;
        for (Note note : seconds) lastTime = Math.max(lastTime, note._time);

        float peakNps = 0;
        float peakTime = 0;
        for (NpsInfo info : samples) {
            if (info.nps() > peakNps) {
                peakNps = info.nps();
                peakTime = Math.max(0, (info.fromTime() + info.toTime()) / 2);
            }
        }

        float averageNps = lastTime <= 0 ? 0 : seconds.size() / lastTime;
        return new NpsKpis(averageNps, peakNps, peakTime, seconds.size(), lastTime);
    }

    /**
     * Bins the notes into fixed-width time buckets (raw counts, for the density strip).
     * The last bin covers the remainder up to the last note; the caller normalizes
     * against the maximum when painting.
     */
    public static int[] computeDensityBins(Note[] notesInBeats, double bpm, float binSeconds) {
        List<Note> seconds = copyInSeconds(notesInBeats, bpm);
        if (seconds.isEmpty() || binSeconds <= 0) return new int[0];

        float lastTime = 0;
        for (Note note : seconds) lastTime = Math.max(lastTime, note._time);

        int[] bins = new int[(int) (lastTime / binSeconds) + 1];
        for (Note note : seconds) {
            int bin = Math.min(bins.length - 1, (int) (note._time / binSeconds));
            if (bin >= 0) bins[bin]++;
        }
        return bins;
    }

    /** Copies the notes and converts the copies' times from beats to seconds with the given BPM. */
    private static List<Note> copyInSeconds(Note[] notesInBeats, double bpm) {
        List<Note> copies = new ArrayList<>();
        if (notesInBeats == null || bpm <= 0) return copies;
        for (Note note : notesInBeats) {
            Note copy = new Note(note);
            copy._time = (float) (copy._time / bpm * 60);
            copies.add(copy);
        }
        return copies;
    }
}
