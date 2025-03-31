package BeatSaberObjects.Objects.NoteTests;

import BeatSaberObjects.Objects.Note;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

/*
  Visual reference:

  Index - Layer:          Cut direction:
  |---|---|---|---|       |---|---|---|
  |0-2|1-2|2-2|3-2|       | 4 | 0 | 5 |
  |---|---|---|---|       |---|---|---|
  |0-1|1-1|2-1|3-1|       | 2 | 8 | 3 |
  |---|---|---|---|       |---|---|---|
  |0-0|1-0|2-0|3-0|       | 6 | 1 | 7 |
  |---|---|---|---|       |---|---|---|

  Example provided:
    A note at 1-1 with cut direction 6 cuts from 3-2 into 1-0.

  Assumed mappings for the tests below:
    • Cut direction 6: from offset (+2, +1), into offset (0, -1).
    • Cut direction 0: from offset (0, +1), into offset (0, -1).
    • Cut direction 4: from offset (+1, 0), into offset (0, -1).
    • Cut direction 7: from offset (0, +1), into offset (-1, 0).
    • Cut direction 8 (invented case): from offset (+1, 0), into offset (-1, 0).

  These tests also cover non-integer positions, negative values, and positions beyond the standard grid.
*/

@DisplayName("Note Test: cut into/from mapping functions")
class NoteCutFromIntoTest {
    
    // Parameterized test for various cut directions with double positions.
    @ParameterizedTest(name = "Note(%.2f-%.2f) with cutDirection {3} -> cut-from: (%.2f-%.2f), cut-into: (%.2f-%.2f)")
    @CsvSource({
            // Format: time, lineIndex, lineLayer, cutDirection, expectedCutFromIndex, expectedCutFromLayer, expectedCutIntoIndex, expectedCutIntoLayer
            
            // Provided example:
            // For note at 1.0-1.0 with cutDirection 6, expected cut-from: 2.0-2.0, cut-into: 0.0-0.0.
            "10.5, 1.0, 1.0, 6, 2.0, 2.0, 0.0, 0.0",
            
            "10.5, 2.0, 1.0, 0, 2.0, 0.0, 2.0, 2.0",
            "10.5, 0.0, 2.0, 4, 1.0, 1.0, -1.0, 3.0",
            "10.5, 3.0, 0.0, 7, 2.0, 1.0, 4.0, -1.0",
            "10.5, 1.5, 1.5, 2, 2.5, 1.5, 0.5, 1.5"
    })
    @DisplayName("Mapping test for various cut directions")
    void testCutMapping(float time, double lineIndex, double lineLayer, int cutDirection,
                        double expectedFromIndex, double expectedFromLayer,
                        double expectedIntoIndex, double expectedIntoLayer) {
        Note note = new Note(time, lineIndex, lineLayer, 0, cutDirection);
        double delta = 0.0001;
        
        double actualFromIndex = note.whichLineIndexWillNoteCutFrom();
        double actualFromLayer = note.whichLineLayerWillNoteCutFrom();
        double actualIntoIndex = note.whichLineIndexWillNoteCutInto();
        double actualIntoLayer = note.whichLineLayerWillNoteCutInto();
        
        assertEquals(expectedFromIndex, actualFromIndex, delta,
                String.format("For note at (%.2f, %.2f) with cutDirection %d, expected cut-from index %.2f but got %.2f",
                        lineIndex, lineLayer, cutDirection, expectedFromIndex, actualFromIndex));
        assertEquals(expectedFromLayer, actualFromLayer, delta,
                String.format("For note at (%.2f, %.2f) with cutDirection %d, expected cut-from layer %.2f but got %.2f",
                        lineIndex, lineLayer, cutDirection, expectedFromLayer, actualFromLayer));
        assertEquals(expectedIntoIndex, actualIntoIndex, delta,
                String.format("For note at (%.2f, %.2f) with cutDirection %d, expected cut-into index %.2f but got %.2f",
                        lineIndex, lineLayer, cutDirection, expectedIntoIndex, actualIntoIndex));
        assertEquals(expectedIntoLayer, actualIntoLayer, delta,
                String.format("For note at (%.2f, %.2f) with cutDirection %d, expected cut-into layer %.2f but got %.2f",
                        lineIndex, lineLayer, cutDirection, expectedIntoLayer, actualIntoLayer));
    }
    
    // Test edge case: note positions with non-integer values.
    @Test
    @DisplayName("Edge case: non-integer note positions with cutDirection 0")
    void testNonIntegerPositions() {
        Note note = new Note(10.5F, 2.3, 1.7, 0, 0);
        double delta = 0.0001;
        assertEquals(note._lineIndex, note.whichLineIndexWillNoteCutFrom(), delta, "Cut-from index should equal note index for cutDirection 0");
        assertEquals(0.7, note.whichLineLayerWillNoteCutFrom(), delta, "Cut-from layer should be note layer + 1 for cutDirection 0");
        assertEquals(note._lineIndex, note.whichLineIndexWillNoteCutInto(), delta, "Cut-into index should equal note index for cutDirection 0");
        assertEquals(2.7, note.whichLineLayerWillNoteCutInto(), delta, "Cut-into layer should be note layer - 1 for cutDirection 0");
    }
    
    // Test edge case: negative note positions.
    @Test
    @DisplayName("Edge case: negative note positions with cutDirection 4")
    void testNegativePositions() {
        Note note = new Note(10.5F, -0.5, -0.5, 0, 6);
        double delta = 0.0001;
        assertEquals(0.5, note.whichLineIndexWillNoteCutFrom(), delta, "Cut-from index for negative position with cutDirection 4");
        assertEquals(0.5, note.whichLineLayerWillNoteCutFrom(), delta, "Cut-from layer for negative position with cutDirection 4");
        assertEquals(-1.5, note.whichLineIndexWillNoteCutInto(), delta, "Cut-into index for negative position with cutDirection 4");
        assertEquals(-1.5, note.whichLineLayerWillNoteCutInto(), delta, "Cut-into layer for negative position with cutDirection 4");
    }
    
    // Test edge case: note positions beyond the standard grid boundaries.
    @Test
    @DisplayName("Edge case: note positions beyond grid boundaries with cutDirection 7")
    void testPositionsBeyondBoundaries() {
        // For cut direction 7 we assume: cut-from: (same index, layer + 1) and cut-into: (index - 1, same layer)
        // For note at (3.5, 2.5), expect cut-from: (3.5, 3.5) and cut-into: (2.5, 2.5)
        Note note = new Note(10.5F, 3.5, 2.5, 0, 7);
        double delta = 0.0001;
        assertEquals(2.5, note.whichLineIndexWillNoteCutFrom(), delta, "Cut-from index for note beyond boundaries with cutDirection 7");
        assertEquals(3.5, note.whichLineLayerWillNoteCutFrom(), delta, "Cut-from layer for note beyond boundaries with cutDirection 7");
        assertEquals(4.5, note.whichLineIndexWillNoteCutInto(), delta, "Cut-into index for note beyond boundaries with cutDirection 7");
        assertEquals(1.5, note.whichLineLayerWillNoteCutInto(), delta, "Cut-into layer for note beyond boundaries with cutDirection 7");
    }
    
    // Test that calling these methods on a null note reference throws a NullPointerException.
    @Test
    @DisplayName("Null note instance should throw NullPointerException")
    void testNullNote() {
        Note note = null;
        assertThrows(NullPointerException.class, () -> note.whichLineIndexWillNoteCutFrom());
        assertThrows(NullPointerException.class, () -> note.whichLineLayerWillNoteCutFrom());
        assertThrows(NullPointerException.class, () -> note.whichLineIndexWillNoteCutInto());
        assertThrows(NullPointerException.class, () -> note.whichLineLayerWillNoteCutInto());
    }
}
