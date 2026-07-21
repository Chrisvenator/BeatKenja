package MapAnalysation.PatternVisualisation.NpsPlotters;

import BeatSaberObjects.Objects.Note;
import DataManager.Parameters;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class NpsSeriesBuilderTest {

    /** 120 BPM, one note per beat for 60 beats = one note every 0.5 s for 30 s. */
    private static Note[] oneNotePerBeat120Bpm() {
        Note[] notes = new Note[60];
        for (int i = 0; i < notes.length; i++) notes[i] = new Note((float) i);
        return notes;
    }

    @Test
    void computeNpsSecondsDoesNotMutateInputOrGlobalBpm() {
        Note[] notes = oneNotePerBeat120Bpm();
        float[] timesBefore = new float[notes.length];
        for (int i = 0; i < notes.length; i++) timesBefore[i] = notes[i]._time;
        double globalBpmBefore = Parameters.BPM;

        NpsSeriesBuilder.computeNpsSeconds(notes, 120, 0.5f, 2);

        for (int i = 0; i < notes.length; i++) assertEquals(timesBefore[i], notes[i]._time, "note times must stay in beats");
        assertEquals(globalBpmBefore, Parameters.BPM, "global BPM must not be touched");
    }

    @Test
    void computeNpsSecondsMatchesManualConversion() {
        Note[] notes = oneNotePerBeat120Bpm();

        List<NpsInfo> viaBuilder = NpsSeriesBuilder.computeNpsSeconds(notes, 120, 1f, 1);

        java.util.List<Note> manual = new java.util.ArrayList<>();
        for (Note n : notes) {
            Note copy = new Note(n);
            copy._time = copy._time / 120f * 60f;
            manual.add(copy);
        }
        List<NpsInfo> viaPlotter = DynamicNpsPlotter.computeNps(manual, 1f, 1, true);

        assertEquals(viaPlotter.size(), viaBuilder.size());
        for (int i = 0; i < viaBuilder.size(); i++) {
            assertEquals(viaPlotter.get(i).nps(), viaBuilder.get(i).nps(), 1e-6);
            assertEquals(viaPlotter.get(i).fromTime(), viaBuilder.get(i).fromTime(), 1e-6);
        }
    }

    @Test
    void computeNpsSecondsPlateauIsRoughlyTwoNps() {
        // 2 notes per second → mid-map samples should sit near 2 NPS
        List<NpsInfo> samples = NpsSeriesBuilder.computeNpsSeconds(oneNotePerBeat120Bpm(), 120, 1f, 2);
        NpsInfo mid = samples.get(samples.size() / 2);
        assertEquals(2f, mid.nps(), 0.3f);
    }

    @Test
    void computeKpisOnKnownFixture() {
        Note[] notes = oneNotePerBeat120Bpm();
        List<NpsInfo> samples = NpsSeriesBuilder.computeNpsSeconds(notes, 120, 1f, 2);

        NpsSeriesBuilder.NpsKpis kpis = NpsSeriesBuilder.computeKpis(notes, 120, samples);

        assertEquals(60, kpis.noteCount());
        assertEquals(29.5f, kpis.mapLengthSeconds(), 1e-6, "last note at beat 59 = 29.5 s");
        assertEquals(60 / 29.5f, kpis.averageNps(), 1e-6);
        assertTrue(kpis.peakNps() > 0);
        assertTrue(kpis.peakTimeSeconds() >= 0, "peak time must be clamped to >= 0");
    }

    @Test
    void computeKpisHandlesEmptyAndSingleNote() {
        NpsSeriesBuilder.NpsKpis empty = NpsSeriesBuilder.computeKpis(new Note[0], 120, List.of());
        assertEquals(new NpsSeriesBuilder.NpsKpis(0, 0, 0, 0, 0), empty);

        // Single note at t=0: last time 0 → average must not divide by zero
        NpsSeriesBuilder.NpsKpis single = NpsSeriesBuilder.computeKpis(new Note[]{new Note(0f)}, 120, List.of());
        assertEquals(0, single.averageNps());
        assertEquals(1, single.noteCount());
        assertEquals(0, single.mapLengthSeconds());
    }

    @Test
    void computeDensityBinsCountsPerSecond() {
        // 120 BPM: beats 0,1 land in [0,1), beat 2 in [1,2), beat 5 in [2.5] → 3rd bin (index 2)
        Note[] notes = {new Note(0f), new Note(1f), new Note(2f), new Note(5f)};

        int[] bins = NpsSeriesBuilder.computeDensityBins(notes, 120, 1f);

        assertEquals(3, bins.length, "last note at 2.5 s → bins [0,1),[1,2),[2,..]");
        assertEquals(2, bins[0]);
        assertEquals(1, bins[1]);
        assertEquals(1, bins[2]);
    }

    @Test
    void computeDensityBinsHandlesDegenerateInput() {
        assertEquals(0, NpsSeriesBuilder.computeDensityBins(new Note[0], 120, 1f).length);
        assertEquals(0, NpsSeriesBuilder.computeDensityBins(new Note[]{new Note(1f)}, 120, 0f).length);
        assertEquals(0, NpsSeriesBuilder.computeDensityBins(null, 120, 1f).length);
    }
}
