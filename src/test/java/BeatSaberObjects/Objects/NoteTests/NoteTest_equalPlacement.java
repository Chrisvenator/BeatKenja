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


class NoteTest_equalPlacement {
    
    @Test
    void testEqualPlacementWithSameObject() {
        Note note = new Note(10.5f, 2, 1, 0, 4);
        assertTrue(note.equalPlacement(note), "equalPlacement should return true for the same object.");
    }
    
    @Test
    void testEqualPlacementWithNull() {
        Note note = new Note(10.5f, 2, 1, 0, 4);
        assertFalse(note.equalPlacement(null), "equalPlacement should return false when comparing with null.");
    }
    
    @Test
    void testEqualPlacementWithDifferentClass() {
        Note note = new Note(10.5f, 2, 1, 0, 4);
        String differentObject = "Not a Note";
        assertFalse(note.equalPlacement(differentObject), "equalPlacement should return false for a different class.");
    }
    
    @Test
    void testEqualPlacementWithEqualPlacementValues() {
        Note note1 = new Note(10.5f, 2, 1, 0, 4);
        Note note2 = new Note(20.0f, 2, 1, 0, 4); // Different time
        assertTrue(note1.equalPlacement(note2), "equalPlacement should return true for notes with equal placement values.");
    }
    
    @Test
    void testEqualPlacementWithDifferentPlacementValues() {
        Note note1 = new Note(10.5f, 2, 1, 0, 4);
        Note note2 = new Note(10.5f, 3, 1, 0, 4); // Different lineIndex
        assertFalse(note1.equalPlacement(note2), "equalPlacement should return false for notes with different placement values.");
    }
    
    @Test
    void testEqualPlacementWithEdgeCaseValues() {
        Note note1 = new Note(Float.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE);
        Note note2 = new Note(Float.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE);
        assertFalse(note1.equalPlacement(note2), "equalPlacement should handle edge cases correctly.");
    }
    
    @Test
    void testEqualPlacementWithPartiallyOverlappingValues() {
        Note note1 = new Note(10.5f, 2, 1, 0, 4);
        Note note2 = new Note(10.5f, 2, 1, 1, 4); // Different type
        assertFalse(note1.equalPlacement(note2), "equalPlacement should return false for partially overlapping values.");
    }
    
    @Test
    void testEqualPlacementWithAllZeroValues() {
        Note note1 = new Note(0, 0, 0, 0, 0);
        Note note2 = new Note(0, 0, 0, 0, 0);
        assertTrue(note1.equalPlacement(note2), "equalPlacement should return true for notes with all zero values.");
    }
    
    @Test
    void testEqualPlacementWithNegativeValues() {
        Note note1 = new Note(-10.5f, -2, -1, -1, -4);
        Note note2 = new Note(-10.5f, -2, -1, -1, -4);
        assertTrue(note1.equalPlacement(note2), "equalPlacement should handle negative values correctly.");
    }
    
    @Test
    void testEqualPlacementWithFloatingPointPrecision() {
        Note note1 = new Note(10.5f, 2, 1, 0, 4);
        Note note2 = new Note(10.5f, 2.00001, 1, 0, 4); // Slightly different lineIndex
        assertFalse(note1.equalPlacement(note2), "equalPlacement should correctly handle floating-point precision differences.");
    }
}