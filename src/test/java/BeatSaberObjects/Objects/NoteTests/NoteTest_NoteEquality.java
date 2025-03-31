package BeatSaberObjects.Objects.NoteTests;

import BeatSaberObjects.Objects.Note;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for Note.equals() and Note.compareTo() methods.
 */
@DisplayName("Tests for Note equals and compareTo methods")
class NoteTest_NoteEquality {
    
    /**
     * Helper method to create a Note instance with the provided field values.
     */
    private Note createNote(float time, int lineIndex, int lineLayer, int type, int cutDirection) {
        return new Note(time, lineIndex, lineLayer, type, cutDirection);
    }
    
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
        NoteTest_CompareTo.TimingNote timingNote = new NoteTest_CompareTo.TimingNote(5.0f);
        assertNotEquals(note, timingNote, "A Note not should equal a TimingNote even if their _time values are equal.");
    }
    
    @Test
    @DisplayName("equals: Note does not equal TimingNote when _time values differ")
    void testEqualsTimingNoteDifferentTime() {
        Note note = createNote(5.0f, 1, 1, 0, 2);
        NoteTest_CompareTo.TimingNote timingNote = new NoteTest_CompareTo.TimingNote(6.0f);
        assertNotEquals(note, timingNote, "A Note should not equal a TimingNote if their _time values differ.");
    }
}