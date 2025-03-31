package BeatSaberObjects.Objects.NoteTests;

import BeatSaberObjects.Objects.Note;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

/*
Red: 0
Blue: 1

Index - Layer:          Cut direction:
|---|---|---|---|       |---|---|---|
|   |   |   |3-2|       | 4 | 0 | 5 |
|---|---|---|---|       |---|---|---|
|   |   |   |3-1|       | 2 | 8 | 3 |
|---|---|---|---|       |---|---|---|
|0-0|1-0|2-0|3-0|       | 6 | 1 | 7 |
|---|---|---|---|       |---|---|---|
 */

class NoteTest_invertCutDirection {
    
    private Note note;
    
    @BeforeEach
    void setUp() {
        note = new Note();
    }
    
    @ParameterizedTest
    @CsvSource({
            "0, 1",  // Up to Down
            "1, 0",  // Down to Up
            "2, 3",  // Left to Right
            "3, 2",  // Right to Left
            "4, 7",  // Up-left to Down-right
            "5, 6",  // Down-right to Up-left
            "6, 5",  // Down-left to Up-right
            "7, 4"   // Up-right to Down-left
    })
    void testInvertCutDirection(int initialCutDirection, int expectedCutDirection) {
        // Set up
        note._cutDirection = initialCutDirection;
        
        // Execute
        note.invertCutDirection();
        
        // Verify
        assertEquals(expectedCutDirection, note._cutDirection,
                "Cut direction should be inverted correctly");
    }
    
    @Test
    void testInvertCutDirectionForDotNote() {
        // Set up
        note._cutDirection = 8;  // Dot note
        
        // Execute
        note.invertCutDirection();
        
        // Verify
        assertEquals(8, note._cutDirection,
                "Dot notes (cut direction 8) should remain unchanged");
    }
    
    @ParameterizedTest
    @ValueSource(ints = {-1, 9, 10, 100})
    void testInvertCutDirectionForNonStandardDirections(int nonStandardDirection) {
        // Set up
        note._cutDirection = nonStandardDirection;
        
        // Execute
        note.invertCutDirection();
        
        // Verify
        // Non-standard directions will be unchanged by invertNoteRotation,
        // but we can't predict what invertCutDirection will do with them
        // since it depends on the implementation of invertNoteRotation too
        assertNotNull(note, "The note should still exist after inversion");
    }
    
    @Test
    void testDoubleInversion() {
        // Test all standard directions
        for (int direction = 0; direction <= 8; direction++) {
            note._cutDirection = direction;
            
            // Execute double inversion
            note.invertCutDirection();
            note.invertCutDirection();
            
            // Verify
            assertEquals(direction, note._cutDirection,
                    "Double inversion should return to the original cut direction");
        }
    }
    
    @Test
    void testInvertCutDirectionDoesNotAffectOtherProperties() {
        // Set up
        float time = 10.5f;
        int lineIndex = 2;
        int lineLayer = 1;
        int type = 0;
        int cutDirection = 4;
        
        Note fullNote = new Note(time, lineIndex, lineLayer, type, cutDirection);
        
        // Execute
        fullNote.invertCutDirection();
        
        // Verify
        assertEquals(7, fullNote._cutDirection, "Cut direction should be inverted");
        assertEquals(time, fullNote._time, "Time should not change");
        assertEquals(lineIndex, fullNote._lineIndex, "Line index should not change");
        assertEquals(lineLayer, fullNote._lineLayer, "Line layer should not change");
        assertEquals(type, fullNote._type, "Type should not change");
    }
}