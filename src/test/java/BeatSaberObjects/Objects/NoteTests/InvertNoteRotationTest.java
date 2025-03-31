package BeatSaberObjects.Objects.NoteTests;

import BeatSaberObjects.Objects.Note;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

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

@DisplayName("Note Test: Invert Note Rotation")
class InvertNoteRotationTest {
    
    @Test
    void testInvertNoteRotation_BasicCase() {
        Note note = new Note();
        note._cutDirection = 4;
        
        note.invertNoteRotation();
        
        assertEquals(5, note._cutDirection, "Cut direction 4 should be inverted to 5");
    }
    
    @Test
    void testInvertNoteRotationTwice_ReturnsOriginal() {
        Note note = new Note();
        note._cutDirection = 6;
        
        note.invertNoteRotation();
        note.invertNoteRotation();
        
        assertEquals(6, note._cutDirection, "Inverting a note's rotation twice should return to the original direction");
    }
    
    @ParameterizedTest
    @CsvSource({
            "2, 3", // Left to Right
            "3, 2", // Right to Left
            "4, 5", // Down to Up
            "5, 4", // Up to Down
            "6, 7", // Down-left to Down-right
            "7, 6"  // Down-right to Down-left
    })
    void testInvertNoteRotation_ParameterizedTest(int inputDirection, int expectedDirection) {
        Note note = new Note();
        note._cutDirection = inputDirection;
        
        note.invertNoteRotation();
        
        assertEquals(expectedDirection, note._cutDirection,
                String.format("Cut direction %d should be inverted to %d", inputDirection, expectedDirection));
    }
    
    @ParameterizedTest
    @CsvSource({
            "0, 0", // Up
            "1, 1", // Down
            "8, 8", // Dot
            "-1, -1",
            "9, 9"
    })
    void testInvertNoteRotation_UnchangedDirections(int inputDirection, int expectedDirection) {
        Note note = new Note();
        note._cutDirection = inputDirection;
        
        note.invertNoteRotation();
        
        assertEquals(expectedDirection, note._cutDirection,
                "Some cut directions should remain unchanged after inversion");
    }
}