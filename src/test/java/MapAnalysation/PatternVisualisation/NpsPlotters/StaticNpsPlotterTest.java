package MapAnalysation.PatternVisualisation.NpsPlotters;

import BeatSaberObjects.Objects.Note;
import org.jfree.data.xy.XYSeries;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static org.junit.jupiter.api.Assertions.*;

class StaticNpsPlotterTest {

    private List<Note> notes;

    @BeforeEach
    void setUp() {
        notes = new ArrayList<>();
        notes.add(new Note(0.0f));  // Note at time 0.0
        notes.add(new Note(1.2f));  // Note at time 1.2
        notes.add(new Note(1.8f));  // Note at time 1.8
        notes.add(new Note(2.5f));  // Note at time 2.5
        notes.add(new Note(4.0f));  // Note at time 4.0
    }

    @Test
    void testComputeNps() {
        // Arrange
        StaticNpsPlotter plotter = new StaticNpsPlotter("Static NPS Plotter", notes, 1);

        // Act
        List<NpsInfo> npsInfos = plotter.computeNps(notes);

        // Assert
        assertNotNull(npsInfos, "NpsInfos list should not be null");
        assertFalse(npsInfos.isEmpty(), "NpsInfos list should not be empty");

        assertEquals(notes.size()-1, npsInfos.size(), "The number of NpsInfo entries should match the number of seconds with notes");

        NpsInfo npsInfo1 = npsInfos.get(0);
        assertEquals(0.0f, npsInfo1.fromTime(), 1e-6, "The fromTime for the first entry should be 0.0f");
        assertEquals(3, npsInfo1.nps(), "The NPS for the first second should be 3");

        NpsInfo npsInfo2 = npsInfos.get(1);
        assertEquals(1.0f, npsInfo2.fromTime(), 1e-6, "The fromTime for the second entry should be 1.0f");
        assertEquals(4, npsInfo2.nps(), "The NPS for the second second should be 3");

        NpsInfo npsInfo3 = npsInfos.get(2);
        assertEquals(2.0f, npsInfo3.fromTime(), 1e-6, "The fromTime for the third entry should be 2.0f");
        assertEquals(3, npsInfo3.nps(), "The NPS for the third second should be 3");

        NpsInfo npsInfo4 = npsInfos.get(3);
        assertEquals(4.0f, npsInfo4.fromTime(), 1e-6, "The fromTime for the fourth entry should be 4.0f");
        assertEquals(1, npsInfo4.nps(), "The NPS for the fourth second should be 1");

        assertThrows(IndexOutOfBoundsException.class, () -> npsInfos.get(4));
    }

    @Test
    void testConstructor() {
        // Arrange
        String title = "Static NPS Plotter";
        int rangeIntervals = 1;

        // Act
        StaticNpsPlotter plotter = new StaticNpsPlotter(title, notes, rangeIntervals);
        XYSeries series = plotter.getSeries();

        // Assert
        assertNotNull(series, "The XYSeries should not be null");
        assertEquals(notes.size()-1, series.getItemCount(), "The series should contain the correct number of points");

        assertEquals(1.5f, series.getX(0).floatValue(), 1e-6, "The X value of the first series point should be the midpoint of the first interval");
        assertEquals(2.5f, series.getX(1).floatValue(), 1e-6, "The X value of the second series point should be the midpoint of the second interval");

        assertEquals(4.0, series.getY(1).doubleValue(), 1e-6, "The Y value should be correctly calculated as the NPS for the interval");
    }

    @Test
    void testToNpsInfo() {
        // Arrange
        Map<Integer, Integer> npsMap = new TreeMap<>();
        npsMap.put(0, 1);
        npsMap.put(1, 3);
        npsMap.put(2, 2);

        // Act
        List<NpsInfo> npsInfos = StaticNpsPlotter.toNpsInfo(npsMap);

        // Assert
        assertNotNull(npsInfos, "NpsInfos list should not be null");
        assertEquals(3, npsInfos.size(), "NpsInfos list should have the same size as the npsMap");

        NpsInfo npsInfo1 = npsInfos.get(0);
        assertEquals(1, npsInfo1.nps(), "The NPS value for the first entry should be 1");

        NpsInfo npsInfo2 = npsInfos.get(1);
        assertEquals(3, npsInfo2.nps(), "The NPS value for the second entry should be 3");

        NpsInfo npsInfo3 = npsInfos.get(2);
        assertEquals(2, npsInfo3.nps(), "The NPS value for the third entry should be 2");
    }

    @Test
    void testComputeNpsWithNoNotes() {
        // Arrange
        List<Note> emptyNotes = new ArrayList<>();

        // Act
        StaticNpsPlotter plotter = new StaticNpsPlotter("Empty NPS Plotter", emptyNotes, 1);
        List<NpsInfo> npsInfos = plotter.computeNps(emptyNotes);

        // Assert
        assertNotNull(npsInfos, "NpsInfos list should not be null even if no notes are provided");
        assertTrue(npsInfos.isEmpty(), "NpsInfos list should be empty when no notes are provided");
    }
}
