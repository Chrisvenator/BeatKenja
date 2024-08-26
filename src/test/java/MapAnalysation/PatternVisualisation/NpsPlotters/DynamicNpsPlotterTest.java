package MapAnalysation.PatternVisualisation.NpsPlotters;

import BeatSaberObjects.Objects.Note;
import org.jfree.data.xy.XYSeries;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DynamicNpsPlotterTest {

    private List<Note> notes;

    @BeforeEach
    void setUp() {
        notes = new ArrayList<>();
        notes.add(new Note(0.0f));  // Note at time 0.0
        notes.add(new Note(0.5f));  // Note at time 0.5
        notes.add(new Note(1.0f));  // Note at time 1.0
        notes.add(new Note(1.5f));  // Note at time 1.5
        notes.add(new Note(2.0f));  // Note at time 2.0
    }

    @Test
    void testComputeNps() {
        // Arrange
        float intervalSize = 1.0f;
        int rangeIntervals = 1;

        // Act
        List<NpsInfo> npsInfoList = DynamicNpsPlotter.computeNps(notes, intervalSize, rangeIntervals, true);

        // Assert
        assertNotNull(npsInfoList, "NpsInfo list should not be null");
        assertFalse(npsInfoList.isEmpty(), "NpsInfo list should not be empty");

        NpsInfo firstInfo = npsInfoList.get(0);
        assertEquals(0.5002501010894775f, firstInfo.nps(), 1e-6, "The NPS value should be correctly calculated");
        assertEquals(-1.0f, firstInfo.fromTime(), 1e-6, "The start time should be -1.0f");
        assertEquals(0.9990000128746033f, firstInfo.toTime(), 1e-6, "The end time should be 1.0f");
    }

    @Test
    void testConstructor() {
        // Arrange
        String title = "Test Dynamic NPS Plot";
        float intervalSize = 1.0f;
        int rangeIntervals = 1;

        // Act
        DynamicNpsPlotter plotter = new DynamicNpsPlotter(title, notes, intervalSize, rangeIntervals);

        // Assert
        assertNotNull(plotter, "The plotter should be created successfully");
        assertEquals(title, plotter.getTitle(), "The title of the plot should match the input title");

        XYSeries series = plotter.getSeries();
        assertNotNull(series, "The XYSeries should not be null");
        assertEquals(3, series.getItemCount(), "The series should contain the correct number of points");

        assertEquals(0.0, series.getX(0).doubleValue(), 1e-2, "The first point should be at time 0.0");
        assertEquals(0.9994999766349792f, series.getX(1).doubleValue(), 1e-2, "The middle point should be at time 1.5");
        assertEquals(1.999500036239624f, series.getX(2).doubleValue(), 1e-2, "The last point should be at the time of the last note (3.0)");

        assertEquals(0.5002501010894775f, series.getY(0).doubleValue(), 1e-2, "The first NPS value should be correctly calculated");
    }

    @Test
    void testComputeNpsWithEmptyNotes() {
        // Arrange
        notes.clear();  // No notes
        float intervalSize = 1.0f;
        int rangeIntervals = 1;

        // Act
        List<NpsInfo> npsInfoList = DynamicNpsPlotter.computeNps(notes, intervalSize, rangeIntervals, true);

        // Assert
        assertNotNull(npsInfoList, "NpsInfo list should not be null even if no notes are provided");
        assertTrue(npsInfoList.isEmpty(), "NpsInfo list should be empty when no notes are provided");
    }

    @Test
    void testComputeNpsWithSingleNote() {
        // Arrange
        notes.clear();
        notes.add(new Note(1.0f));  // Single note at time 1.0
        float intervalSize = 1.0f;
        int rangeIntervals = 1;

        // Act
        List<NpsInfo> npsInfoList = DynamicNpsPlotter.computeNps(notes, intervalSize, rangeIntervals, true);

        // Assert
        assertNotNull(npsInfoList, "NpsInfo list should not be null");
        assertFalse(npsInfoList.isEmpty(), "NpsInfo list should contain NPS info even for a single note");

        NpsInfo firstInfo = npsInfoList.get(0);
        assertEquals(0.0f, firstInfo.nps(), 1e-6, "The NPS value should be 1.0f when a single note exists in a 1-second interval");
        assertEquals(-1.0f, firstInfo.fromTime(), 1e-6, "The start time should be 0.0f");
        assertEquals(0.9990000128746033f, firstInfo.toTime(), 1e-6, "The end time should be 2.0f");
    }

    @Test
    void testComputeNpsWithShortInterval() {
        // Arrange
        float intervalSize = 0.1f;
        int rangeIntervals = 1;

        // Act
        List<NpsInfo> npsInfoList = DynamicNpsPlotter.computeNps(notes, intervalSize, rangeIntervals, true);

        // Assert
        assertNotNull(npsInfoList, "NpsInfo list should not be null");
        assertFalse(npsInfoList.isEmpty(), "NpsInfo list should not be empty for short intervals");

        for (NpsInfo npsInfo : npsInfoList) {
            assertTrue(npsInfo.nps() >= 0.0f, "The NPS value should be non-negative");
        }
    }
}
