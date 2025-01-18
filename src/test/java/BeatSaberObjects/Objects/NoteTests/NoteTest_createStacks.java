package BeatSaberObjects.Objects.NoteTests;

import BeatSaberObjects.Objects.Note;
import DataManager.Parameters;
import UserInterface.UserInterface;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

/*
Red: 0
Blue: 1

Layer - Index:          Cut direction:
|---|---|---|---|       |---|---|---|
|   |   |   |3-2|       | 4 | 0 | 5 |
|---|---|---|---|       |---|---|---|
|   |   |   |3-1|       | 2 | 8 | 3 |
|---|---|---|---|       |---|---|---|
|0-0|1-0|2-0|3-0|       | 6 | 1 | 7 |
|---|---|---|---|       |---|---|---|
 */


class NoteTest_createStacks {
    @Test
    void testCreateStackedNoteWithZeroStackedNotes() {
        Note note = new Note(10.5f, 2, 1, 0, 4); // Valid note
        note.amountOfStackedNotes = 0;
        
        Note[] result = note.createStackedNote();
        assertNotNull(result, "createStackedNote should never return null.");
        assertEquals(1, result.length, "createStackedNote should return the original note when amountOfStackedNotes is 0.");
        assertEquals(note, result[0], "The returned note should be the original note.");
    }
    
    @Test
    @Disabled("return value of \"java.util.Map.get(Object)\" is null. Problem with not initializing Parameters.PARITY_ERRORS_LIST.")
    void testCreateStackedNoteWithOneStackedNote() {
        Note note = new Note(10.5f, 2, 1, 0, 4);
        note.amountOfStackedNotes = 1;
        UserInterface ui = new UserInterface();
        UserInterface.currentDiff = "Hard";
        
        Note[] result = note.createStackedNote();
        assertNotNull(result, "createStackedNote should never return null.");
        assertEquals(1, result.length, "With one stacked note, the array should contain only the original note.");
    }
    
    @Test
    @Disabled("Irgendwas stimmt da gerade nicht. Methode muss überarbeitet werden.")
    void testCreateStackedNoteWithMultipleStackedNotes() {
        Note note = new Note(10.5f, 3, 0, 0, 5);
        note.amountOfStackedNotes = 2;
        
        Note[] result = note.createStackedNote();
        assertNotNull(result, "createStackedNote should never return null.");
        assertEquals(2, result.length, "The number of notes should match the amountOfStackedNotes.");
        for (Note stackedNote : result) {
            assertNotNull(stackedNote, "Stacked notes should not be null.");
            assertEquals(note._time, stackedNote._time, "Stacked notes should have the same time as the original note.");
            assertNotEquals(2, stackedNote._lineIndex, "Stacked notes should not be placed in grid 1-2 or 2-2.");
        }
    }
    
    @Test
    void testCreateStackedNoteWithNegativeStackedNotes() {
        Note note = new Note(10.5f, 3, 0, 0, 5);
        note.amountOfStackedNotes = -1;
        
        Note[] result = note.createStackedNote();
        assertEquals(1, result.length, "The number of notes should match the amountOfStackedNotes.");
    }
    
    @Test
    @Disabled("return value of \"java.util.Map.get(Object)\" is null. Problem with not initializing Parameters.PARITY_ERRORS_LIST.")
    void testCreateStackedNoteWithEdgeCaseCutDirection() {
        Note note = new Note(10.5f, 3, 0, 0, 8); // Extreme cut direction value
        note.amountOfStackedNotes = 3;
        
        Note[] result = note.createStackedNote();
        assertNotNull(result, "createStackedNote should never return null.");
        assertEquals(3, result.length, "The number of notes should match the amountOfStackedNotes.");
    }
    
    @Test
    void testCreateStackedNoteWithInvalidLineIndex() {
        Note note = new Note(10.5f, -1, 0, 0, 4); // Invalid line index
        note.amountOfStackedNotes = 1;
        
        assertEquals(1, note.createStackedNote().length, "createStackedNote should return the original note for invalid line index.");
        assertEquals(note, note.createStackedNote()[0], "createStackedNote should return the original note for invalid line index.");
    }
    
    @Test
    void testCreateStackedNoteWithInvalidLineLayer() {
        Note note = new Note(10.5f, 2, -1, 0, 4); // Invalid line layer
        note.amountOfStackedNotes = 2;
        
        assertEquals(1, note.createStackedNote().length, "createStackedNote should return the original note for invalid line index.");
        assertEquals(note, note.createStackedNote()[0], "createStackedNote should return the original note for invalid line index.");
    }
    
    @Test
    @Disabled("return value of \"java.util.Map.get(Object)\" is null. Problem with not initializing Parameters.PARITY_ERRORS_LIST.")
    void testCreateStackedNoteWithVisionBlockGrid() {
        Note note = new Note(10.5f, 1, 2, 0, 4); // Vision block grid
        note.amountOfStackedNotes = 2;
        
        Note[] result = note.createStackedNote();
        assertNotNull(result, "createStackedNote should never return null.");
        for (Note stackedNote : result) {
            assertNotEquals(1, stackedNote._lineIndex, "Stacked notes should avoid grid 1-2.");
            assertNotEquals(2, stackedNote._lineLayer, "Stacked notes should avoid grid 2-2.");
        }
    }
}