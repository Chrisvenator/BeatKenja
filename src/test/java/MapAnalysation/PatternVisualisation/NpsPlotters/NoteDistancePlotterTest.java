package MapAnalysation.PatternVisualisation.NpsPlotters;

import BeatSaberObjects.Objects.Note;
import org.jfree.data.xy.XYSeries;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class NoteDistancePlotterTest {

    private List<Note> notes;

    @BeforeEach
    void setUp() {
        notes = new ArrayList<>();
        notes.add(new Note(0.0f));  // Note at time 0.0
        notes.add(new Note(1.0f));  // Note at time 1.0
        notes.add(new Note(1.5f));  // Note at time 1.5
        notes.add(new Note(2.0f));  // Note at time 2.0
        notes.add(new Note(3.0f));  // Note at time 3.0
    }

    @Test
    void testComputeNoteDistanceSeries() {
        // Arrange
        NoteDistancePlotter plotter = new NoteDistancePlotter("Note Distance Plot", notes);

        // Act
        XYSeries series = plotter.getSeries();

        // Assert
        assertNotNull(series, "The XYSeries should not be null");
        assertEquals(4, series.getItemCount(), "The series should contain the correct number of points");

        assertEquals(1.0f, series.getX(0).floatValue(), 1e-6, "The first point should be at time 1.0");
        assertEquals(1.0f, series.getY(0).floatValue(), 1e-6, "The first inverted distance should be 1.0");

        assertEquals(1.5f, series.getX(1).floatValue(), 1e-6, "The second point should be at time 1.5");
        assertEquals(2.0f, series.getY(1).floatValue(), 1e-6, "The second inverted distance should be 2.0");

        assertEquals(2.0f, series.getX(2).floatValue(), 1e-6, "The third point should be at time 2.0");
        assertEquals(2.0f, series.getY(2).floatValue(), 1e-6, "The third inverted distance should be 2.0");

        assertEquals(3.0f, series.getX(3).floatValue(), 1e-6, "The fourth point should be at time 3.0");
        assertEquals(1.0f, series.getY(3).floatValue(), 1e-6, "The fourth inverted distance should be 1.0");
    }

    @Test
    void testConstructorWithEmptyNotes() {
        // Arrange
        List<Note> emptyNotes = new ArrayList<>();

        // Act
        NoteDistancePlotter plotter = new NoteDistancePlotter("Empty Note Distance Plot", emptyNotes);
        XYSeries series = plotter.getSeries();

        // Assert
        assertNotNull(series, "The XYSeries should not be null even with an empty note list");
        assertEquals(0, series.getItemCount(), "The series should contain no points when there are no notes");
    }

    @Test
    void testConstructorWithSingleNote() {
        // Arrange
        List<Note> singleNote = new ArrayList<>();
        singleNote.add(new Note(1.0f));  // Only one note

        // Act
        NoteDistancePlotter plotter = new NoteDistancePlotter("Single Note Distance Plot", singleNote);
        XYSeries series = plotter.getSeries();

        // Assert
        assertNotNull(series, "The XYSeries should not be null with a single note");
        assertEquals(0, series.getItemCount(), "The series should contain no points when there is only one note");
    }

    @Test
    void testConstructorWithNotesHavingSameTime() {
        // Arrange
        List<Note> sameTimeNotes = new ArrayList<>();
        sameTimeNotes.add(new Note(1.0f));  // First note at time 1.0
        sameTimeNotes.add(new Note(1.0f));  // Second note also at time 1.0

        // Act
        NoteDistancePlotter plotter = new NoteDistancePlotter("Same Time Note Distance Plot", sameTimeNotes);
        XYSeries series = plotter.getSeries();

        // Assert
        assertNotNull(series, "The XYSeries should not be null with notes having the same time");
        assertEquals(0, series.getItemCount(), "The series should contain no points when notes have the same time");
    }
}
