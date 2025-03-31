package BeatSaberObjects.Objects.NoteTests;

import BeatSaberObjects.Objects.Note;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.IntStream;
import java.util.stream.Stream;

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

@DisplayName("Note Test: Invert Line Index")
class InvertLineIndexTest {
    
    private Note note;
    
    @BeforeEach
    void setUp() {
        note = new Note();
    }
    
    @ParameterizedTest
    @CsvSource({
            "0, 3", // Left edge to right edge
            "1, 2", // Middle-left to middle-right
            "2, 1", // Middle-right to middle-left
            "3, 0", // Right edge to left edge
            "-1, 4", // One step left of left edge
            "-2, 5", // Two steps left of left edge
            "4, -1", // One step right of right edge
            "5, -2",  // Two steps right of right edge
            "-100, 103"  // Two steps right of right edge
    })
    void testInvertLineIndex(int initialLineIndex, int expectedLineIndex) {
        // Set up
        note._lineIndex = initialLineIndex;
        
        // Execute
        note.invertLineIndex();
        
        // Verify
        assertEquals(expectedLineIndex, note._lineIndex,
                "Line index should be inverted correctly");
    }
    
    @ParameterizedTest
    @MethodSource("lineIndexRange")
    void testDoubleInversion(int initialLineIndex) {
        // Set up
        note._lineIndex = initialLineIndex;
        
        // Execute double inversion
        note.invertLineIndex();
        note.invertLineIndex();
        
        // Verify
        assertEquals(initialLineIndex, note._lineIndex, "Double inversion should return to the original line index");
    }
    
    private static Stream<Integer> lineIndexRange() {
        return IntStream.rangeClosed(-100, 100).boxed();
    }
    
    @Test
    void testInvertLineIndexDoesNotAffectOtherProperties() {
        // Set up
        float time = 10.5f;
        int lineIndex = 2;
        int lineLayer = 1;
        int type = 0;
        int cutDirection = 4;
        
        Note fullNote = new Note(time, lineIndex, lineLayer, type, cutDirection);
        
        // Execute
        fullNote.invertLineIndex();
        
        // Verify
        assertEquals(1, fullNote._lineIndex, "Line index should be inverted");
        assertEquals(time, fullNote._time, "Time should not change");
        assertEquals(lineLayer, fullNote._lineLayer, "Line layer should not change");
        assertEquals(type, fullNote._type, "Type should not change");
        assertEquals(cutDirection, fullNote._cutDirection, "Cut direction should not change");
    }
}