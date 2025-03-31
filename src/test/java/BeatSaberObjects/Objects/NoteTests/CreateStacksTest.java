package BeatSaberObjects.Objects.NoteTests;

import BeatSaberObjects.Objects.Note;
import UserInterface.UserInterface;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

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

@DisplayName("Note Test: Create Stacks")
class CreateStacksTest {
    @Test
    void testCreateStackedNoteWithZeroStackedNotes() {
        Note note = new Note(10.5f, 2, 1, 0, 4); // Valid note
        note.amountOfStackedNotes = 0;
        
        Note[] result = note.createStacks();
        assertNotNull(result, "createStackedNote should never return null.");
        assertEquals(1, result.length, "createStackedNote should return the original note when amountOfStackedNotes is 0.");
        assertEquals(note, result[0], "The returned note should be the original note.");
    }
    
    @Test
    void testCreateStackedNoteWithOneStacks() {
        Note note = new Note(10.5f, 2, 1, 0, 4);
        note.amountOfStackedNotes = 1;
        UserInterface ui = new UserInterface();
        UserInterface.currentDiff = "Hard";
        
        Note[] result = note.createStacks();
        assertNotNull(result, "createStackedNote should never return null.");
        assertEquals(1, result.length, "With one stacked note, the array should contain only the original note.");
    }
    
    
    @Test
    void testCreateStackedNoteWithNegativeStackedNotes() {
        Note note = new Note(10.5f, 3, 0, 0, 5);
        note.amountOfStackedNotes = -1;
        
        Note[] result = note.createStacks();
        assertEquals(1, result.length, "The number of notes should match the amountOfStackedNotes.");
    }
    
    
    @Test
    void testCreateStacksWithInvalidLineIndex() {
        Note note = new Note(10.5f, -1, 0, 0, 4); // Invalid line index
        note.amountOfStackedNotes = 1;
        
        assertEquals(1, note.createStacks().length, "createStackedNote should return the original note for invalid line index.");
        assertEquals(note, note.createStacks()[0], "createStackedNote should return the original note for invalid line index.");
    }
    
    @Test
    void testCreateStacksWithInvalidLineLayer() {
        Note note = new Note(10.5f, 2, -1, 0, 4); // Invalid line layer
        note.amountOfStackedNotes = 2;
        
        assertEquals(1, note.createStacks().length, "createStackedNote should return the original note for invalid line index.");
        assertEquals(note, note.createStacks()[0], "createStackedNote should return the original note for invalid line index.");
    }
    
    @ParameterizedTest
    @ValueSource(ints = {-1, 0, 2, 3, 4, 5})
    void testCreateStacksWithVisionBlockGrid(int layer) {
        Note note = new Note(10.5f, 1, layer, 0, 1); // Vision block grid
        note.amountOfStackedNotes = 2;
        
        Note[] result = note.createStacks();
        assertNotNull(result, "createStackedNote should never return null.");
        for (Note stackedNote : result) {
            assertNotEquals(1, stackedNote._lineLayer, "Stacked notes should avoid grid 2-2.");
        }
    }
    
    
    @ParameterizedTest
    @CsvSource({
            "0, 0, 0,      0, 1",
            "0, 1, 4,      1, 2",
            "0, 1, 4,      2, 2",
            "1, 0, 6,      1, 2",
            "1, 0, 6,      2, 2",
            "2, 1, 5,      1, 1",
            "2, 1, 5,      2, 1",
            "2, 2, 7,      1, 2",
            "2, 2, 7,      2, 2",
            "1, 2, 7,      1, 2",
            "1, 2, 7,      2, 2",
            "3, 1, 3,      1, 1",
            "3, 1, 3,      2, 1",
            "3, 1, 1,      1, 2"
    })
    void testCreateStacks_UpToTwoBig(int lineIndex, int lineLayer, int cutDirection, int amountOfStackedNotes, int expectedStackedNoteAmount) {
        Note n = new Note(0, lineIndex, lineLayer, 0, cutDirection);
        n.amountOfStackedNotes = amountOfStackedNotes;
        Note[] result = n.createStacks();
        
        assertEquals(expectedStackedNoteAmount, result.length, "The amount of stacked notes should match the expected value.");
    }
    
    @ParameterizedTest
    @CsvSource({
            "0,0,0,   0,1,0",
            "0,1,4,   1,0,4",
            "1,0,6,   3,2,6",
            "2,2,7,   3,1,7",
            "1,2,7,   3,0,7",
            "3,1,1,   3,0,1",
            "3,0,1,   3,1,1",
            "3,2,1,   3,1,1",
    })
    void testTryCreatingStackedNote(int lineIndex, int lineLayer, int cutDirection, int expectedLineIndex, int expectedLineLayer, int expectedCutDirection) {
        Note n = new Note(0, lineIndex, lineLayer, 0, cutDirection);
        Note ex = new Note(0, expectedLineIndex, expectedLineLayer, 0, expectedCutDirection);
        n.amountOfStackedNotes = 1;
        List<Note> stack = n.tryCreatingStackedNote();
        
        assertNotNull(stack, "The stack should not be null.");
        assertNotNull(stack.get(0), "The stack should not be empty.");
        assertEquals(ex.toString(), stack.get(0).toString(), "The stacked note should have the same placement as the expected note.");
    }
    
    
    @ParameterizedTest
    @CsvSource({
            "0, 6, -1",  // Downward cut decreases lineLayer
            "0, 4, 1",   // Upward cut increases lineLayer
            "1, 8, 1"    // Dot cut keeps lineLayer unchanged
    })
    void testWhichLineLayerWillNoteCutInto(int lineLayer, int cutDirection, int expectedLineLayer) {
        Note n = new Note();
        n._lineLayer = lineLayer;
        n._cutDirection = cutDirection;
        
        assertEquals(expectedLineLayer, n.whichLineLayerWillNoteCutInto(),
                "The calculated lineLayer should match the expected value.");
    }
    
    @ParameterizedTest
    @CsvSource({
            "2, 6, 1",  // Left cut decreases lineIndex
            "2, 5, 3",  // Right cut increases lineIndex
            "2, 8, 2"   // Dot cut keeps lineIndex unchanged
    })
    void testWhichLineIndexWillNoteCutInto(int lineIndex, int cutDirection, int expectedLineIndex) {
        Note n = new Note();
        n._lineIndex = lineIndex;
        n._cutDirection = cutDirection;
        
        assertEquals(expectedLineIndex, n.whichLineIndexWillNoteCutInto(),
                "The calculated lineIndex should match the expected value.");
    }
    
    @Test
    void testTryCreatingStackedNote_AvoidsVisionBlocksShouldNotPlace() {
        Note n = new Note(0, 2, 1, 0, 4); // Vision block position
        n.amountOfStackedNotes = 1;
        
        List<Note> stack = n.tryCreatingStackedNote();
        
        assertNotNull(stack, "The stack should not be null.");
        assertEquals(0, stack.size(), "The stack should contain two notes.");
    }
    
    
    @ParameterizedTest
    @ValueSource(ints = {1, 2})
    void testCreateStacksWithEdgeCaseCutDirection(int amountOfStackedNotes) {
        Note note = new Note(10.5f, 3, 1, 0, 8); // Extreme cut direction value
        note.amountOfStackedNotes = amountOfStackedNotes;
        
        Note[] result = note.createStacks();
        assertNotNull(result, "createStackedNote should never return null.");
        assertEquals(amountOfStackedNotes + 1, result.length, "The number of notes should match the amountOfStackedNotes.");
    }
    
    
    
    @ParameterizedTest
    @CsvSource({
            "3, 2, 1,      2, 3",
            "3, 1, 1,      2, 3",
            "3, 0, 1,      2, 3",
            "0, 2, 1,      2, 3",
            "0, 1, 1,      2, 3",
            "0, 0, 1,      2, 3",
            "2, 2, 3,      2, 3"
    })
    void testCreateStacks_ThreeBig(int lineIndex, int lineLayer, int cutDirection, int amountOfStackedNotes, int expectedStackedNoteAmount) {
        Note n = new Note(0, lineIndex, lineLayer, 0, cutDirection);
        n.amountOfStackedNotes = amountOfStackedNotes;
        Note[] result = n.createStacks();
        
        assertEquals(3, result.length, "The amount of stacked notes should match the expected value.");
    }
}

/*
Red: 0
Blue: 1

Index - Layer:          Cut direction:
|---|---|---|---|       |---|---|---|
|0-2|1-2|2-2|3-2|       | 4 | 0 | 5 |
|---|---|---|---|       |---|---|---|
|0-1|1-1|2-1|3-1|       | 2 | 8 | 3 |
|---|---|---|---|       |---|---|---|
|0-0|1-0|2-0|3-0|       | 6 | 1 | 7 |
|---|---|---|---|       |---|---|---|
*/