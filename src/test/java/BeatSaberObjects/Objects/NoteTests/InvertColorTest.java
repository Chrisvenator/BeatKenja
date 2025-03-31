package BeatSaberObjects.Objects.NoteTests;

import BeatSaberObjects.Objects.Note;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
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

@DisplayName("Note Test: Invert Color")
class InvertColorTest {
    
    private Note note;
    
    @BeforeEach
    void setUp() {
        note = new Note();
    }
    
    @ParameterizedTest
    @CsvSource({
            "0, 1", // Red to Blue
            "1, 0"  // Blue to Red
    })
    void testInvertColor(int initialType, int expectedType) {
        // Set up
        note._type = initialType;
        
        // Execute
        note.invertColor();
        
        // Verify
        assertEquals(expectedType, note._type, "Color should be inverted correctly");
    }
    
    @ParameterizedTest
    @ValueSource(ints = {-1, 2, 3, 8, 100})
    void testInvertColorWithNonStandardTypes(int nonStandardType) {
        // Set up
        note._type = nonStandardType;
        
        // Execute
        note.invertColor();
        
        // Verify
        assertEquals(nonStandardType, note._type,
                "Non-standard types should remain unchanged after inversion");
    }
    
    @Test
    void testMultipleInversions() {
        // Set up
        note._type = 0;
        
        // Execute and verify
        note.invertColor();
        assertEquals(1, note._type, "First inversion should change Red (0) to Blue (1)");
        
        note.invertColor();
        assertEquals(0, note._type, "Second inversion should change Blue (1) back to Red (0)");
        
        note.invertColor();
        assertEquals(1, note._type, "Third inversion should change Red (0) to Blue (1) again");
    }
    
    @Test
    void testInvertColorDoesNotAffectOtherProperties() {
        // Set up
        float time = 10.5f;
        int lineIndex = 2;
        int lineLayer = 1;
        int type = 0;
        int cutDirection = 4;
        
        Note fullNote = new Note(time, lineIndex, lineLayer, type, cutDirection);
        
        // Execute
        fullNote.invertColor();
        
        // Verify
        assertEquals(1, fullNote._type, "Type should be inverted");
        assertEquals(time, fullNote._time, "Time should not change");
        assertEquals(lineIndex, fullNote._lineIndex, "Line index should not change");
        assertEquals(lineLayer, fullNote._lineLayer, "Line layer should not change");
        assertEquals(cutDirection, fullNote._cutDirection, "Cut direction should not change");
    }
}