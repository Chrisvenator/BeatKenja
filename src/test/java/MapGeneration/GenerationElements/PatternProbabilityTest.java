package MapGeneration.GenerationElements;

import BeatSaberObjects.Objects.Note;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PatternProbabilityTest {

    private Note[] notes;
    private float[] probabilities;

    @BeforeEach
    void setUp() {
        // Initialize the notes and probabilities arrays
        notes = new Note[]{
                new Note(),  // Assume the Note class has a default constructor
                new Note(),
                null,  // Include a null element
                new Note()
        };

        probabilities = new float[]{0.1f, 0.2f, 0.0f, 0.3f};
    }

    @Test
    void testConstructorAndRemoveNulls() {
        // Act
        PatternProbability patternProbability = new PatternProbability(notes, probabilities);

        // Assert
        assertNotNull(patternProbability.notes, "Notes array should not be null");
        assertNotNull(patternProbability.probabilities, "Probabilities array should not be null");

        assertEquals(2, patternProbability.notes.length, "Notes array should contain only non-null elements");
        assertEquals(2, patternProbability.probabilities.length, "Probabilities array should match the filtered notes length");

        assertEquals(0.1f, patternProbability.probabilities[0], "First probability should be correct");
        assertEquals(0.2f, patternProbability.probabilities[1], "Second probability should be correct");
    }

    @Test
    void testRemoveNulls() {
        // Arrange
        Note[] customNotes = new Note[]{
                new Note(),
                new Note(),
                null,
                null
        };

        float[] customProbabilities = new float[]{0.5f, 0.0f, 0.7f, 0.0f};

        PatternProbability patternProbability = new PatternProbability(customNotes, customProbabilities);

        // Act
        patternProbability.removeNulls();

        // Assert
        assertEquals(2, patternProbability.notes.length, "Notes array should contain only non-null elements");
        assertEquals(2, patternProbability.probabilities.length, "Probabilities array should match the filtered notes length");

        assertEquals(0.5f, patternProbability.probabilities[0], "First probability should be correct");
        assertEquals(0.0f, patternProbability.probabilities[1], "Second probability should be correct");
    }

    @Test
    void testToString() {
        // Arrange
        Note note1 = new Note();  // Assuming Note has a default constructor
        Note note2 = new Note();
        Note note3 = new Note();
        Note[] notesArray = new Note[]{note1, note2, note3};
        float[] probabilitiesArray = new float[]{0.1f, 0.2f, 0.3f};

        PatternProbability patternProbability = new PatternProbability(notesArray, probabilitiesArray);

        // Act
        String result = patternProbability.toString();

        // Assert
        assertTrue(result.contains(note1.toString().replaceAll("\n", "") + ": 0.1%"), "First note string representation should be correct");
        assertTrue(result.contains(note2.toString().replaceAll("\n", "") + ": 0.2%"), "Second note string representation should be correct");
        assertTrue(result.contains(note3.toString().replaceAll("\n", "") + ": 0.3%"), "Third note string representation should be correct");
        assertTrue(result.endsWith("]"), "String representation should end with a closing bracket");
    }

    @Test
    void testEmptyPatternProbability() {
        // Arrange
        Note[] emptyNotes = new Note[0];
        float[] emptyProbabilities = new float[0];

        // Act
        PatternProbability patternProbability = new PatternProbability(emptyNotes, emptyProbabilities);

        // Assert
        assertNotNull(patternProbability.notes, "Notes array should not be null");
        assertNotNull(patternProbability.probabilities, "Probabilities array should not be null");

        assertEquals(0, patternProbability.notes.length, "Notes array should be empty");
        assertEquals(0, patternProbability.probabilities.length, "Probabilities array should be empty");

        assertEquals("]", patternProbability.toString(), "String representation of empty PatternProbability should be just a closing bracket");
    }
}
