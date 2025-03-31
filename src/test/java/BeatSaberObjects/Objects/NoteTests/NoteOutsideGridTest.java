package BeatSaberObjects.Objects.NoteTests;

import BeatSaberObjects.Objects.Note;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Note Test: Outside Grid method")
class NoteOutsideGridTest {
    
    @ParameterizedTest(name = "lineIndex: {0}, lineLayer: {1} -> expected isOutsideGrid: {2}")
    @CsvSource({
            // Valid grid positions: lineIndex in [0,3] and lineLayer in [0,2] should return false
            "0, 0, false",
            "1, 1, false",
            "2, 2, false",
            "3, 0, false",
            "0, 2, false",
            "3, 2, false"
    })
    @DisplayName("Valid grid positions should return false")
    void testIsOutsideGrid_ValidPositions(int lineIndex, int lineLayer, boolean expected) {
        Note note = new Note();
        note._lineIndex = lineIndex;
        note._lineLayer = lineLayer;
        assertEquals(expected, note.isOutsideGrid(),
                "Note with lineIndex " + lineIndex + " and lineLayer " + lineLayer + " should be inside the grid");
    }
    
    @ParameterizedTest(name = "lineIndex: {0}, lineLayer: {1} -> expected isOutsideGrid: {2}")
    @CsvSource({
            // Invalid grid positions: values outside the allowed ranges should return true
            "-1, 1, true",
            "4, 1, true",
            "1, -1, true",
            "1, 3, true",
            "-1, -1, true",
            "4, 3, true"
    })
    @DisplayName("Invalid grid positions should return true")
    void testIsOutsideGrid_InvalidPositions(int lineIndex, int lineLayer, boolean expected) {
        Note note = new Note();
        note._lineIndex = lineIndex;
        note._lineLayer = lineLayer;
        assertEquals(expected, note.isOutsideGrid(),
                "Note with lineIndex " + lineIndex + " and lineLayer " + lineLayer + " should be outside the grid");
    }
}
