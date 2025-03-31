package BeatSaberObjects.Objects.NoteTests;

import BeatSaberObjects.Objects.Note;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

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


class NoteTest_equalNotePlacement {
    @Test
    void testEqualNotePlacementWithSameObject() {
        Note note = new Note(10.5f, 2, 1, 0, 4);
        assertTrue(note.equalNotePlacement(note), "equalNotePlacement should return true for the same object.");
    }
    
    @Test
    void testEqualNotePlacementWithNull() {
        Note note = new Note(10.5f, 2, 1, 0, 4);
        assertFalse(note.equalNotePlacement(null), "equalNotePlacement should return false when comparing with null.");
    }
    
    @Test
    void testEqualNotePlacementWithEqualPlacementValues() {
        Note note1 = new Note(10.5f, 2, 1, 0, 4);
        Note note2 = new Note(20.0f, 2, 1, 1, 8); // Different time and direction
        assertTrue(note1.equalNotePlacement(note2), "equalNotePlacement should return true for notes with equal placement values.");
    }
    
    @Test
    void testEqualNotePlacementWithDifferentLineIndex() {
        Note note1 = new Note(10.5f, 2, 1, 0, 4);
        Note note2 = new Note(10.5f, 3, 1, 0, 4); // Different lineIndex
        assertFalse(note1.equalNotePlacement(note2), "equalNotePlacement should return false for notes with different lineIndex.");
    }
    
    @Test
    void testEqualNotePlacementWithDifferentLineLayer() {
        Note note1 = new Note(10.5f, 2, 1, 0, 4);
        Note note2 = new Note(10.5f, 2, 2, 0, 4); // Different lineLayer
        assertFalse(note1.equalNotePlacement(note2), "equalNotePlacement should return false for notes with different lineLayer.");
    }
    
    @Test
    void testEqualNotePlacementWithAllZeroValues() {
        Note note1 = new Note(0, 0, 0, 0, 0);
        Note note2 = new Note(0, 0, 0, 0, 0);
        assertTrue(note1.equalNotePlacement(note2), "equalNotePlacement should return true for notes with all zero values.");
    }
    
    @Test
    void testEqualNotePlacementWithNegativeValues() {
        Note note1 = new Note(-10.5f, -2, -1, -1, -4);
        Note note2 = new Note(-10.5f, -2, -1, -2, -4); // Different type but same placement
        assertTrue(note1.equalNotePlacement(note2), "equalNotePlacement should handle negative values correctly.");
    }
    
    @Test
    void testEqualNotePlacementWithFloatingPointPrecision() {
        Note note1 = new Note(10.5f, 2, 1, 0, 4);
        Note note2 = new Note(10.5f, 2.00001, 1, 0, 4); // Slightly different lineIndex
        assertFalse(note1.equalNotePlacement(note2), "equalNotePlacement should correctly handle floating-point precision differences.");
    }
    
    @Test
    void testEqualNotePlacementWithEdgeCaseValues() {
        Note note1 = new Note(Float.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE);
        Note note2 = new Note(Float.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE);
        assertFalse(note1.equalNotePlacement(note2), "equalNotePlacement should handle edge cases correctly.");
    }
    
    @Test
    void testEqualNotePlacementWithNullLineIndexOrLayer() {
        Note note1 = new Note(10.5f, 2, 1, 0, 4);
        Note note2 = new Note(10.5f, 0, 0, 0, 4);
        assertFalse(note1.equalNotePlacement(note2), "equalNotePlacement should return false when lineIndex or lineLayer are zero and don't match.");
    }
}