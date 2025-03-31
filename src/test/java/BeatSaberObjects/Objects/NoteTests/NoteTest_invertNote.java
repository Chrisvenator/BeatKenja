package BeatSaberObjects.Objects.NoteTests;

import BeatSaberObjects.Objects.Note;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

/*
 * Note inversion tests for BeatSaberObjects
 * Assumptions:
 *   Red: 0
 *   Blue: 1
 *
 * Index - Layer:          Cut direction:
 * |---|---|---|---|       |---|---|---|
 * |   |   |   |3-2|       | 4 | 0 | 5 |
 * |---|---|---|---|       |---|---|---|
 * |   |   |   |3-1|       | 2 | 8 | 3 |
 * |---|---|---|---|       |---|---|---|
 * |0-0|1-0|2-0|3-0|       | 6 | 1 | 7 |
 * |---|---|---|---|       |---|---|---|
 */

class NoteTest_invertNote {
    
    private static final float DEFAULT_TIME = 10.5f;
    private Note note;
    
    @BeforeEach
    void setup() {
        // This default note can be used or overridden in individual tests
        note = new Note(DEFAULT_TIME, 2, 1, 0, 4);
    }
    
    @Test
    @DisplayName("invertNote returns the same instance and correctly inverts properties")
    void testInvertNoteReturnsSameInstance() {
        Note result = note.invertNote();
        
        // Verify that invertNote returns the same object
        assertSame(note, result, "invertNote should return the same object instance");
        
        // Verify that the note properties were inverted as expected
        assertEquals(1, result._type, "Note type should be inverted from 0 (Red) to 1 (Blue)");
        assertEquals(1, result._lineIndex, "Line index should be inverted from 2 to 1");
        assertEquals(5, result._cutDirection, "Cut direction should be inverted from 4 to 5");
    }
    
    @ParameterizedTest(name = "Given type={3} and lineIndex={1} with cutDirection={4}, inversion yields type={5}, lineIndex={6}, cutDirection={7}")
    @CsvSource({
            // time, lineIndex, lineLayer, type, cutDirection, expected_type, expected_lineIndex, expected_cutDirection
            "10.5, 0, 1, 0, 2, 1, 3, 3",
            "10.5, 1, 1, 1, 3, 0, 2, 2",
            "10.5, 2, 1, 0, 4, 1, 1, 5",
            "10.5, 3, 1, 1, 5, 0, 0, 4",
            "10.5, 0, 1, 0, 6, 1, 3, 7",
            "10.5, 1, 1, 1, 7, 0, 2, 6",
            "10.5, 2, 0, 0, 0, 1, 1, 0",
            "10.5, 3, 0, 1, 1, 0, 0, 1",
            "10.5, 0, 2, 0, 8, 1, 3, 8"
    })
    @DisplayName("Parameterized test for invertNote with multiple values")
    void testInvertNoteParameterized(float time, int lineIndex, int lineLayer, int type,
                                     int cutDirection, int expectedType,
                                     int expectedLineIndex, int expectedCutDirection) {
        Note paramNote = new Note(time, lineIndex, lineLayer, type, cutDirection);
        Note result = paramNote.invertNote();
        
        assertEquals(expectedType, result._type,
                String.format("Type should be inverted from %d to %d", type, expectedType));
        assertEquals(expectedLineIndex, result._lineIndex,
                String.format("LineIndex should be inverted from %d to %d", lineIndex, expectedLineIndex));
        assertEquals(expectedCutDirection, result._cutDirection,
                String.format("CutDirection should be inverted from %d to %d", cutDirection, expectedCutDirection));
        assertEquals(lineLayer, result._lineLayer,
                "LineLayer should remain unchanged");
        assertEquals(time, result._time,
                "Time should remain unchanged");
    }
    
    @Test
    @DisplayName("invertNote preserves additional properties")
    void testInvertNotePreservesAdditionalProperties() {
        note.amountOfStackedNotes = 3;
        Note result = note.invertNote();
        
        assertEquals(3, result.amountOfStackedNotes,
                "invertNote should preserve additional properties like amountOfStackedNotes");
    }
    
    @Test
    @DisplayName("invertNote with invalid values leaves them unchanged")
    void testInvertNoteWithInvalidValues() {
        Note invalidNote = new Note(DEFAULT_TIME, -1, 1, -1, 9);
        Note result = invalidNote.invertNote();
        
        // Verify that invalid values remain unchanged
        assertEquals(4, result._lineIndex, "Invalid lineIndex should remain unchanged");
        assertEquals(-1, result._type, "Invalid type should remain unchanged");
        assertEquals(9, result._cutDirection, "Invalid cutDirection should remain unchanged");
    }
    
    @Test
    @DisplayName("Two successive inversions revert the note to its original state")
    void testMultipleInversions() {
        // Perform inversion twice and verify that the note reverts to original values
        note.invertNote().invertNote();
        
        assertEquals(0, note._type, "Type should revert to original after two inversions");
        assertEquals(2, note._lineIndex, "LineIndex should revert to original after two inversions");
        assertEquals(4, note._cutDirection, "CutDirection should revert to original after two inversions");
    }
}
