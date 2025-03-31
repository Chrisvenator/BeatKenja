package BeatSaberObjects.Objects.NoteTests;

import BeatSaberObjects.Objects.Note;
import BeatSaberObjects.Objects.TimingNote;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Note Test Suite")
class Note_test_ManualBigClassTestPruned {
    
    // Test fixture variables for Note objects used across many tests
    private Note note01, note02;
    
    @BeforeEach
    void setUp() {
        note01 = new Note(255.5f, 1, 1, 0, 1);
        note02 = new Note(256.5f, 2, 1, 0, 2);
    }
    
    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {
        @Test
        @DisplayName("Test single-argument constructor")
        void testSingleArgumentConstructor() {
            Note n1 = new Note(0);
            Note n2 = new Note(10);
            Note n3 = new Note(1.1f);
            
            assertEquals(0, n1._time, "n1 _time should be 0");
            assertEquals(10, n2._time, "n2 _time should be 10");
            assertEquals(1.1f, n3._time, "n3 _time should be 1.1f");
            
            // The defaults are expected for grid positions, type, cutDirection, and amountOfStackedNotes:
            assertEquals(0.0, n1._lineIndex);
            assertEquals(0.0, n1._lineLayer);
            assertEquals(1, n1._type);
            assertEquals(8, n1._cutDirection);
            assertEquals(0, n1.amountOfStackedNotes);
        }
        
        @Test
        @DisplayName("Test five-argument constructor")
        void testFiveArgumentConstructor() {
            Note n1 = new Note(0, 1, 5, 0, 7);
            Note n2 = new Note(10, 1, 5, 0, 7);
            Note n3 = new Note(1.1f, 1, 5, 0, 7);
            
            assertEquals(0, n1._time);
            assertEquals(1, n1._lineIndex);
            assertEquals(5, n1._lineLayer);
            assertEquals(0, n1._type);
            assertEquals(7, n1._cutDirection);
            assertEquals(0, n1.amountOfStackedNotes);
        }
        
        @Test
        @DisplayName("Test copy constructor")
        void testCopyConstructor() {
            Note original = new Note(0, 1, 5, 0, 7);
            Note copy = new Note(original);
            
            assertEquals(original._time, copy._time);
            assertEquals(original._lineIndex, copy._lineIndex);
            assertEquals(original._lineLayer, copy._lineLayer);
            assertEquals(original._type, copy._type);
            assertEquals(original._cutDirection, copy._cutDirection);
            assertEquals(original.amountOfStackedNotes, copy.amountOfStackedNotes);
        }
    }
    
    @Test
    @DisplayName("Test isDD method")
    void testIsDD() {
        // Use a few representative cases
        Note a = new Note(352.0f, 3, 2, 1, 0);
        Note b = new Note(354.0f, 3, 1, 1, 1);
        assertFalse(a.isDD(b), "Expected isDD to return false for these notes");
        
        Note c = new Note(354.0f, 3, 1, 1, 1);
        Note d = new Note(356.0f, 3, 2, 1, 1);
        assertTrue(c.isDD(d), "Expected isDD to return true for these notes");
    }
    
    @Test
    @DisplayName("Test equalPlacement method")
    void testEqualPlacement() {
        // Representative assertions for equalPlacement.
        Note a = new Note(335.0f, 2, 0, 1, 1);
        Note b = new Note(402.0f, 0, 0, 1, 2);
        assertFalse(a.equalPlacement(b), "Different placement should return false");
        
        Note c = new Note(246.0f, 3, 1, 1, 2);
        Note d = new Note(401.0f, 3, 1, 1, 2);
        assertTrue(c.equalPlacement(d), "Same placement should return true");
    }
    
    @Test
    @DisplayName("Test hashCode method")
    void testHashCode() {
        // Checking a few expected hash codes; these values come from the original assertions.
        assertEquals(-143370080, note01.hashCode());
        assertEquals(-821749599, note02.hashCode());
        // Additional assertions for other notes could be added similarly
    }
    
    @Test
    @DisplayName("Test toString method")
    void testToString() {
        assertEquals("{\"_time\":255.5,\"_lineIndex\":1,\"_lineLayer\":1,\"_type\":0,\"_cutDirection\":1}",
                note01.toString().replace("\n", ""));
        // Additional similar assertions for note02, note03, etc., can be added as needed.
    }
    
    @Test
    @DisplayName("Test invertNote method")
    void testInvertNote() {
        // Verify that invertNote returns a Note with expected inverted values.
        Note inverted = note01.invertNote();
        // For example, if the expected inversion changes _lineIndex from 1 to 2, _lineLayer from 1 to 1, and toggles _type to 1:
        assertEquals("{\"_time\":255.5,\"_lineIndex\":2,\"_lineLayer\":1,\"_type\":1,\"_cutDirection\":1}",
                inverted.toString().replace("\n", ""),
                "invertNote did not produce expected output for note01");
        // More representative assertions for other notes can be added.
    }
    
    @Test
    @DisplayName("Test createStacks method")
    void testCreateStacks() {
        // Test various cases by adjusting amountOfStackedNotes and grid positions
        Note a = new Note(0, 0, 0, 0, 0);
        a.amountOfStackedNotes = 3;
        Note[] stacks = a.createStacks();
        assertEquals(3, stacks.length, "Expected one stack for note at (0,0)");
        
        a = new Note(0, 2, 0, 0, 0);
        a.amountOfStackedNotes = 3;
        stacks = a.createStacks();
        assertEquals(2, stacks.length, "Expected two stacks for note at (2,0)");
        
        a = new Note(0, 3, 0, 0, 0);
        a.amountOfStackedNotes = 3;
        stacks = a.createStacks();
        assertEquals(3, stacks.length, "Expected three stacks for note at (3,0)");
    }
    
    @Test
    @DisplayName("Test compareTo method")
    void testCompareTo() {
        // A few representative compareTo assertions
        Note n1 = new Note(1151.0328f, 0, 1, 0, 4);
        Note n2 = new Note(1150.0394f, 3, 0, 1, 7);
        assertEquals(1, n1.compareTo(n2), "n1 should be greater than n2 based on _time");
        
        // Additional representative comparisons can be added similarly.
    }
    
    @Test
    @DisplayName("Test TimingNote constructors")
    void testTimingNote() {
        TimingNote t1 = new TimingNote(0);
        TimingNote t2 = new TimingNote(10);
        TimingNote t3 = new TimingNote(1.1f);
        
        assertEquals(0, t1._time);
        assertEquals(10, t2._time);
        assertEquals(1.1f, t3._time);
        
        // TimingNote should have the default grid positions and other defaults
        assertEquals(0, t1._lineIndex);
        assertEquals(0, t1._lineLayer);
        assertEquals(1, t1._type);
        assertEquals(8, t1._cutDirection);
        assertEquals(0, t1.amountOfStackedNotes);
    }
}
