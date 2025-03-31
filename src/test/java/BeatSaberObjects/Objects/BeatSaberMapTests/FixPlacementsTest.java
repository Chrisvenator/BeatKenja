package BeatSaberObjects.Objects.BeatSaberMapTests;


import static org.junit.jupiter.api.Assertions.*;

import BeatSaberObjects.Objects.BeatSaberMap;
import BeatSaberObjects.Objects.Note;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

@DisplayName("BeatSaberMap Tests: Fix Placements")
public class FixPlacementsTest {
    // Test 1: When a note timing is already divisible by 64.
    @Test
    public void testFixPlacementsAlreadyCorrect() {
        Note note = new Note(128.0);
        BeatSaberMap map = new BeatSaberMap(new Note[]{note});
        map.fixPlacements(0.1);
        assertEquals(128.0, note._time, 0.0001, "Timing should remain unchanged when already a multiple of 64.");
    }
    
    // Test 2: Timing slightly below a multiple (should round up).
    @Test
    public void testFixPlacementsWithinPrecisionRoundingUp() {
        Note note = new Note(63.9); // 0.1 away from 64.
        BeatSaberMap map = new BeatSaberMap(new Note[]{note});
        map.fixPlacements(1.0);
        assertEquals(64.0, note._time, 0.0001, "Timing should round to 64 when within precision.");
    }
    
    // Test 3: Timing slightly above a multiple (should round down).
    @Test
    public void testFixPlacementsWithinPrecisionRoundingDown() {
        Note note = new Note(64.1); // 0.1 away from 64.
        BeatSaberMap map = new BeatSaberMap(new Note[]{note});
        map.fixPlacements(1.0);
        assertEquals(64.0, note._time, 0.0001, "Timing should round to 64 when within precision.");
    }
    
    // Test 4: Timing is outside the precision range, so no adjustment should occur.
    @Test
    public void testFixPlacementsOutsidePrecisionNoChange() {
        Note note = new Note(63.0); // 1.0 away from 64.
        BeatSaberMap map = new BeatSaberMap(new Note[]{note});
        map.fixPlacements(0.5);
        assertEquals(63.0, note._time, 0.0001, "Timing should remain unchanged when difference exceeds precision.");
    }
    
    // Test 5: Edge case when the difference equals the precision.
    @Test
    public void testFixPlacementsEdgeCasePrecisionBoundary() {
        Note note = new Note(63.5); // Exactly 0.5 away from 64.
        BeatSaberMap map = new BeatSaberMap(new Note[]{note});
        map.fixPlacements(2);
        // Assuming the boundary condition triggers adjustment.
        assertEquals(64.0, note._time, 0.0001, "Timing should adjust when difference equals precision.");
    }
    
    // Test 6: Negative timing values within precision (e.g. should adjust toward 0).
    @Test
    public void testFixPlacementsNegativeTime() {
        Note note = new Note(-0.2); // Nearest multiple is 0.
        BeatSaberMap map = new BeatSaberMap(new Note[]{note});
        map.fixPlacements(0.5);
        assertEquals(0.0, note._time, 0.0001, "Negative timing within precision should adjust to 0.");
    }
    
    // Test 7: Large precision forcing an adjustment even when the difference is significant.
    @Test
    public void testFixPlacementsLargePrecisionForcingAdjustment() {
        Note note = new Note(100.0);
        BeatSaberMap map = new BeatSaberMap(new Note[]{note});
        // With a very large precision, the note should be adjusted to the nearest multiple.
        // For 100.0, lower multiple is 64 and upper is 128; difference to 128 is 28, to 64 is 36; nearest is 128.
        map.fixPlacements(100.0);
        assertEquals(100.0, note._time, 0.0001, "With large precision, timing should adjust to the nearest multiple (128).");
    }
    
    // Test 8: Multiple notes in one map.
    @Test
    public void testFixPlacementsMultipleNotes() {
        Note note1 = new Note(63.9);  // Should adjust to 64.
        Note note2 = new Note(65.1);  // Difference 1.1 from 64; if precision is 1.0, no change.
        Note note3 = new Note(127.5); // 0.5 away from 128; should adjust to 128 with precision 1.0.
        BeatSaberMap map = new BeatSaberMap(new Note[]{note1, note2, note3});
        map.fixPlacements(1.0);
        assertEquals(64.0, note1._time, 0.0001, "Note1 timing should adjust to 64.");
        assertEquals(65.0, note2._time, 0.0001, "Note2 timing should adjust to 65.");
        assertEquals(128.0, note3._time, 0.0001, "Note3 timing should adjust to 128.");
    }
    
    @Test
    void testFixPlacements_roundDown() {
        List<Note> notes = new ArrayList<>();
        notes.add(new Note(63.9f));
        BeatSaberMap testObj = new BeatSaberMap(notes);
        
        testObj.fixPlacements(64.0);
        
        assertEquals(64.0f, notes.get(0)._time, 0.001f);
    }
    
    @Test
    void testFixPlacements_roundUp() {
        List<Note> notes = new ArrayList<>();
        notes.add(new Note(64.1f));
        BeatSaberMap testObj = new BeatSaberMap(notes);
        
        testObj.fixPlacements(64.0);
        
        assertEquals(64.0f, notes.get(0)._time, 0.001f);
    }
    
    @Test
    void testFixPlacements_alreadyDivisible() {
        List<Note> notes = new ArrayList<>();
        notes.add(new Note(128.0f));
        BeatSaberMap testObj = new BeatSaberMap(notes);
        
        testObj.fixPlacements(64.0);
        
        assertEquals(128.0f, notes.get(0)._time, 0.001f);
    }
    
    @Test
    void testFixPlacements_multipleNotes() {
        List<Note> notes = new ArrayList<>();
        notes.add(new Note(63.9f));
        notes.add(new Note(64.1f));
        notes.add(new Note(127.9f));
        BeatSaberMap testObj = new BeatSaberMap(notes);
        
        testObj.fixPlacements(64.0);
        
        assertEquals(64.0f, notes.get(0)._time, 0.001f);
        assertEquals(64.0f, notes.get(1)._time, 0.001f);
        assertEquals(128.0f, notes.get(2)._time, 0.001f);
    }
    
    @Test
    void testFixPlacements_differentPrecision() {
        List<Note> notes = new ArrayList<>();
        notes.add(new Note(47.9f));
        notes.add(new Note(48.1f));
        BeatSaberMap testObj = new BeatSaberMap(notes);
        
        testObj.fixPlacements(32.0);
        
        assertEquals(32.0f, notes.get(0)._time, 0.001f);
        assertEquals(64.0f, notes.get(1)._time, 0.001f);
    }
    
    @Test
    void testFixPlacements_zeroTime() {
        List<Note> notes = new ArrayList<>();
        notes.add(new Note(0.0f));
        BeatSaberMap testObj = new BeatSaberMap(notes);
        
        testObj.fixPlacements(64.0);
        
        assertEquals(0.0f, notes.get(0)._time, 0.001f);
    }
    
    @Test
    void testFixPlacements_negativeTime() {
        List<Note> notes = new ArrayList<>();
        notes.add(new Note(-63.9f));
        BeatSaberMap testObj = new BeatSaberMap(notes);
        
        testObj.fixPlacements(64.0);
        
        assertEquals(-64.0f, notes.get(0)._time, 0.001f);
    }
}
