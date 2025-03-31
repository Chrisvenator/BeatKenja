package BeatSaberObjects.Objects.NoteTests;

import BeatSaberObjects.Objects.Note;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for Note.equals() and Note.compareTo() methods.
 */
@DisplayName("Tests for Note equals and compareTo methods")
class NoteTest_CompareTo {
    
    /**
     * Helper method to create a Note instance with the provided field values.
     */
    private Note createNote(float time, int lineIndex, int lineLayer, int type, int cutDirection) {
        return new Note(time, lineIndex, lineLayer, type, cutDirection);
    }
    
    /**
     * A minimal stub for TimingNote.
     * We assume TimingNote is a subclass of Note that only cares about _time in equals.
     */
    static class TimingNote extends Note {
        public TimingNote(float time) {
            // You might need to call a proper superclass constructor; here we assume a no-arg constructor exists.
            super();
            this._time = time;
        }
    }
    
    // ======================= Tests for equals() =======================
    
    @Test
    @DisplayName("equals: Same instance returns true")
    void testEqualsSameInstance() {
        Note note = createNote(5.0f, 1, 1, 0, 2);
        assertEquals(note, note, "A note should equal itself.");
    }
    
    @Test
    @DisplayName("equals: Comparing with null returns false")
    void testEqualsNull() {
        Note note = createNote(5.0f, 1, 1, 0, 2);
        assertNotEquals(null, note, "A note should not equal null.");
    }
    
    @Test
    @DisplayName("equals: Comparing with an object of a different type returns false")
    void testEqualsDifferentType() {
        Note note = createNote(5.0f, 1, 1, 0, 2);
        String notANote = "Not a note";
        assertNotEquals(notANote, note, "A note should not equal an object of a different type.");
    }
    
    @Test
    @DisplayName("equals: Two notes with identical fields are equal")
    void testEqualsSameFields() {
        Note note1 = createNote(5.0f, 1, 1, 0, 2);
        Note note2 = createNote(5.0f, 1, 1, 0, 2);
        assertEquals(note1, note2, "Notes with the same fields should be equal.");
        assertEquals(note2, note1, "Equality should be symmetric.");
    }
    
    @Test
    @DisplayName("equals: Two notes with different _time values are not equal")
    void testEqualsDifferentTime() {
        Note note1 = createNote(5.0f, 1, 1, 0, 2);
        Note note2 = createNote(6.0f, 1, 1, 0, 2);
        assertNotEquals(note1, note2, "Notes with different time values should not be equal.");
    }
    
    @Test
    @DisplayName("equals: Two notes with same _time but different grid positions are not equal")
    void testEqualsDifferentGrid() {
        Note note1 = createNote(5.0f, 1, 1, 0, 2);
        Note note2 = createNote(5.0f, 2, 1, 0, 2);
        assertNotEquals(note1, note2, "Notes with the same time but different grid positions should not be equal.");
    }
    
    @Test
    @DisplayName("equals: Note equals TimingNote when _time values are equal")
    void testEqualsTimingNoteSameTime() {
        Note note = createNote(5.0f, 1, 1, 0, 2);
        TimingNote timingNote = new TimingNote(5.0f);
        assertNotEquals(note, timingNote, "A Note not should equal a TimingNote even if their _time values are equal.");
    }
    
    @Test
    @DisplayName("equals: Note does not equal TimingNote when _time values differ")
    void testEqualsTimingNoteDifferentTime() {
        Note note = createNote(5.0f, 1, 1, 0, 2);
        TimingNote timingNote = new TimingNote(6.0f);
        assertNotEquals(note, timingNote, "A Note should not equal a TimingNote if their _time values differ.");
    }
    
    // ======================= Tests for compareTo() =======================
    
    @Test
    @DisplayName("compareTo: Comparing a note to itself returns 0")
    void testCompareToSameInstance() {
        Note note = createNote(5.0f, 1, 1, 0, 2);
        assertEquals(0, note.compareTo(note), "compareTo should return 0 when comparing the same instance.");
    }
    
    @Test
    @DisplayName("compareTo: Note with an earlier _time comes before a note with a later _time")
    void testCompareToDifferentTime() {
        Note earlierNote = createNote(4.0f, 1, 1, 0, 2);
        Note laterNote = createNote(5.0f, 1, 1, 0, 2);
        assertTrue(earlierNote.compareTo(laterNote) < 0, "A note with an earlier time should compare as less than a note with a later time.");
        assertTrue(laterNote.compareTo(earlierNote) > 0, "A note with a later time should compare as greater than a note with an earlier time.");
    }
    
    @Test
    @DisplayName("compareTo: When _time values are equal, comparison is based on _type")
    void testCompareToEqualTimeDifferentType() {
        Note note1 = createNote(5.0f, 1, 1, 0, 2);
        Note note2 = createNote(5.0f, 1, 1, 1, 2);
        int expected = Float.compare(0, 1); // should be negative since 0 < 1
        assertEquals(expected, note1.compareTo(note2),
                "When _time values are equal, the note with a lower _type should compare as less.");
    }
    
    @Test
    @DisplayName("compareTo: When _time and _type are equal, compareTo returns 0 regardless of other fields")
    void testCompareToEqualTimeSameType() {
        // Here, even if grid positions or cut directions differ, compareTo only cares about _time and _type.
        Note note1 = createNote(5.0f, 1, 1, 0, 2);
        Note note2 = createNote(5.0f, 2, 2, 0, 3);
        assertEquals(0, note1.compareTo(note2),
                "When _time and _type are equal, compareTo should return 0.");
    }
}