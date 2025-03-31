package BeatSaberObjects.Objects.NoteTests;

import BeatSaberObjects.Objects.Note;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Tests for Note clone() method")
class NoteTest_Clone {
    
    @Test
    @DisplayName("Clone should produce an equal but distinct object")
    void testCloneProducesEqualButDistinctObject() {
        // Arrange: Create a Note with non-default values.
        Note original = new Note(3.5f, 1.0, 2.0, 0, 7);
        original.amountOfStackedNotes = 5; // set a non-default value for an additional field
        
        // Act: Clone the note.
        Note clone = original.clone();
        
        // Assert: The clone must be a different instance.
        assertNotSame(original, clone, "The clone should be a different object instance.");
        
        // Assert: All primitive fields should be equal.
        assertEquals(original._time, clone._time, "Time should be equal in the cloned note.");
        assertEquals(original._lineIndex, clone._lineIndex, "Line index should be equal in the cloned note.");
        assertEquals(original._lineLayer, clone._lineLayer, "Line layer should be equal in the cloned note.");
        assertEquals(original._type, clone._type, "Type should be equal in the cloned note.");
        assertEquals(original._cutDirection, clone._cutDirection, "Cut direction should be equal in the cloned note.");
        assertEquals(original.amountOfStackedNotes, clone.amountOfStackedNotes, "Amount of stacked notes should be equal in the cloned note.");
        
        // Act: Modify the original note.
        original._time = 7.0f;
        original._lineIndex = 2.0;
        original._lineLayer = 3.0;
        original._type = 1;
        original._cutDirection = 5;
        original.amountOfStackedNotes = 10;
        
        // Assert: The clone remains unchanged despite modifications to the original.
        assertNotEquals(original._time, clone._time, "Changing original time should not affect the clone.");
        assertNotEquals(original._lineIndex, clone._lineIndex, "Changing original line index should not affect the clone.");
        assertNotEquals(original._lineLayer, clone._lineLayer, "Changing original line layer should not affect the clone.");
        assertNotEquals(original._type, clone._type, "Changing original type should not affect the clone.");
        assertNotEquals(original._cutDirection, clone._cutDirection, "Changing original cut direction should not affect the clone.");
        assertNotEquals(original.amountOfStackedNotes, clone.amountOfStackedNotes, "Changing original amountOfStackedNotes should not affect the clone.");
    }
}
