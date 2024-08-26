package MapAnalysation.PatternVisualisation.NpsPlotters;

import BeatSaberObjects.Objects.Note;
import org.jfree.data.xy.XYSeries;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AverageNpsPlotterTest {

    private List<Note> notes;

    @BeforeEach
    void setUp() {
        notes = new ArrayList<>();
        notes.add(new Note(0.0f));  // Note at time 0.0
        notes.add(new Note(1.0f));  // Note at time 1.0
        notes.add(new Note(2.0f));  // Note at time 2.0
        notes.add(new Note(3.0f));  // Note at time 3.0
    }

    @Test
    void testGetAverageNps() {
        // Act
        List<NpsInfo> npsInfos = AverageNpsPlotter.getAverageNps(notes);

        // Assert
        assertNotNull(npsInfos, "NpsInfos should not be null");
        assertEquals(1, npsInfos.size(), "There should be exactly one NpsInfo object in the list");

        NpsInfo info = npsInfos.get(0);
        assertEquals(4.0f / 3.0f, info.nps(), 1e-6, "The average NPS should be correctly calculated");
        assertEquals(0.0f, info.fromTime(), "The start time should be 0.0");
        assertEquals(3.0f, info.toTime(), "The end time should be the time of the last note (3.0)");
    }

    @Test
    void testConstructor() {
        // Arrange
        String title = "Test Plot";

        // Act
        AverageNpsPlotter plotter = new AverageNpsPlotter(title, notes);

        // Assert
        assertNotNull(plotter, "The plotter should be created successfully");
        assertEquals(title, plotter.getTitle(), "The title of the plot should match the input title");

        XYSeries series = plotter.getSeries();
        assertNotNull(series, "The XYSeries should not be null");
        assertEquals(2, series.getItemCount(), "The series should contain exactly two points");

        assertEquals(0.0, series.getX(0).doubleValue(), 1e-6, "The first point should be at time 0.0");
        assertEquals(3.0, series.getX(1).doubleValue(), 1e-6, "The second point should be at the time of the last note (3.0)");

        double expectedNps = 4.0 / 3.0;  // 4 notes over 3 seconds
        assertEquals(expectedNps, series.getY(0).doubleValue(), 1e-6, "The NPS value should be correctly calculated");
        assertEquals(expectedNps, series.getY(1).doubleValue(), 1e-6, "The NPS value should be correctly calculated");
    }

    @Test
    void testGetAverageNpsWithSingleNote() {
        // Arrange
        notes.clear();
        notes.add(new Note(2.0f));  // Single note at time 2.0

        // Act
        List<NpsInfo> npsInfos = AverageNpsPlotter.getAverageNps(notes);

        // Assert
        assertNotNull(npsInfos, "NpsInfos should not be null");
        assertEquals(1, npsInfos.size(), "There should be exactly one NpsInfo object in the list");

        NpsInfo info = npsInfos.get(0);
        assertEquals(0.5f, info.nps(), 1e-6, "The average NPS should be correctly calculated with a single note");
        assertEquals(0.0f, info.fromTime(), "The start time should be 0.0");
        assertEquals(2.0f, info.toTime(), "The end time should be the time of the last note (2.0)");
    }

    @Test
    void testGetAverageNpsWithNoNotes() {
        // Arrange
        notes.clear();  // No notes

        // Act & Assert
        Exception exception = assertThrows(IndexOutOfBoundsException.class, () -> AverageNpsPlotter.getAverageNps(notes),
                "An IndexOutOfBoundsException should be thrown if there are no notes");
    }
}
