package BeatSaberObjects.Objects.NoteTests;

import BeatSaberObjects.Objects.Note;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Tests for Note Constructors")
public class NoteTest_Constructors {
    
    @Test
    @DisplayName("Default constructor sets expected default values")
    void testDefaultConstructor() {
        Note note = new Note();
        assertEquals(0f, note._time, "Default _time should be 0");
        assertEquals(0.0, note._lineIndex, "Default _lineIndex should be 0");
        assertEquals(0.0, note._lineLayer, "Default _lineLayer should be 0");
        assertEquals(1, note._type, "Default _type should be 1");
        assertEquals(8, note._cutDirection, "Default _cutDirection should be 8");
    }
    
    @Test
    @DisplayName("Single-argument constructor sets time correctly and defaults for others")
    void testSingleArgumentConstructor() {
        float time = 5.0f;
        Note note = new Note(time);
        assertEquals(time, note._time, "The _time should be set to the provided value");
        assertEquals(0.0, note._lineIndex, "Default _lineIndex should be 0");
        assertEquals(0.0, note._lineLayer, "Default _lineLayer should be 0");
        assertEquals(1, note._type, "Default _type should be 1");
        assertEquals(8, note._cutDirection, "Default _cutDirection should be 8");
        
        // Edge case: negative time value
        float negativeTime = -3.0f;
        Note negativeNote = new Note(negativeTime);
        assertEquals(negativeTime, negativeNote._time, "Negative _time should be set as provided");
    }
    
    @Test
    @DisplayName("Constructor with int grid positions sets all fields correctly")
    void testIntConstructor() {
        float time = 3.5f;
        int lineIndex = 1;
        int lineLayer = 2;
        int type = 0;
        int cutDirection = 7;
        Note note = new Note(time, lineIndex, lineLayer, type, cutDirection);
        assertEquals(time, note._time, "The _time should be set correctly");
        // Although the constructor accepts ints, _lineIndex and _lineLayer are doubles.
        assertEquals((double) lineIndex, note._lineIndex, "The _lineIndex should be set correctly");
        assertEquals((double) lineLayer, note._lineLayer, "The _lineLayer should be set correctly");
        assertEquals(type, note._type, "The _type should be set correctly");
        assertEquals(cutDirection, note._cutDirection, "The _cutDirection should be set correctly");
        
        // Edge: extreme values for grid positions and type/cutDirection
        Note extremeNote = new Note(Float.MAX_VALUE, Integer.MIN_VALUE, Integer.MAX_VALUE, -1, 100);
        assertEquals(Float.MAX_VALUE, extremeNote._time, "Extreme _time value should be assigned");
        assertEquals((double) Integer.MIN_VALUE, extremeNote._lineIndex, "Extreme _lineIndex value should be assigned");
        assertEquals((double) Integer.MAX_VALUE, extremeNote._lineLayer, "Extreme _lineLayer value should be assigned");
        assertEquals(-1, extremeNote._type, "Extreme _type value should be assigned");
        assertEquals(100, extremeNote._cutDirection, "Extreme _cutDirection value should be assigned");
    }
    
    @Test
    @DisplayName("Constructor with double grid positions sets all fields correctly")
    void testDoubleConstructor() {
        float time = 4.5f;
        double lineIndex = 1.5;
        double lineLayer = 2.5;
        int type = 1;
        int cutDirection = 8;
        Note note = new Note(time, lineIndex, lineLayer, type, cutDirection);
        assertEquals(time, note._time, "The _time should be set correctly");
        assertEquals(lineIndex, note._lineIndex, "The _lineIndex should be set correctly");
        assertEquals(lineLayer, note._lineLayer, "The _lineLayer should be set correctly");
        assertEquals(type, note._type, "The _type should be set correctly");
        assertEquals(cutDirection, note._cutDirection, "The _cutDirection should be set correctly");
        
        // Edge: non-integer and negative double values
        Note edgeNote = new Note(-1.0f, -0.75, 0.25, 0, -5);
        assertEquals(-1.0f, edgeNote._time, "Negative _time should be set as provided");
        assertEquals(-0.75, edgeNote._lineIndex, "Non-integer negative _lineIndex should be set as provided");
        assertEquals(0.25, edgeNote._lineLayer, "Non-integer _lineLayer should be set as provided");
        assertEquals(0, edgeNote._type, "The _type should be set as provided");
        assertEquals(-5, edgeNote._cutDirection, "Negative _cutDirection should be set as provided");
    }
    
    @Test
    @DisplayName("Copy constructor creates an independent copy with the same field values")
    void testCopyConstructor() {
        // Create an original note with specific values and a non-default amountOfStackedNotes.
        Note original = new Note(6.0f, 2.0, 3.0, 1, 4);
        original.amountOfStackedNotes = 5;
        Note copy = new Note(original);
        // Verify that all fields are equal between the original and the copy.
        assertEquals(original._time, copy._time, "Copy should have the same _time as the original");
        assertEquals(original._lineIndex, copy._lineIndex, "Copy should have the same _lineIndex as the original");
        assertEquals(original._lineLayer, copy._lineLayer, "Copy should have the same _lineLayer as the original");
        assertEquals(original._type, copy._type, "Copy should have the same _type as the original");
        assertEquals(original._cutDirection, copy._cutDirection, "Copy should have the same _cutDirection as the original");
        assertEquals(original.amountOfStackedNotes, copy.amountOfStackedNotes, "Copy should have the same amountOfStackedNotes as the original");
        
        // Verify that the copy is independent by modifying the original.
        original.amountOfStackedNotes = 10;
        assertNotEquals(original.amountOfStackedNotes, copy.amountOfStackedNotes,
                "After modification, the copy's amountOfStackedNotes should remain unchanged");
    }
    
    @Test
    @DisplayName("Copy constructor with null should throw NullPointerException")
    void testCopyConstructorNull() {
        assertThrows(NullPointerException.class, () -> new Note(null),
                "Copy constructor should throw NullPointerException when passed null");
    }
}
