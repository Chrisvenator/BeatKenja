package BeatSaberObjects.Objects.NoteTests;

import BeatSaberObjects.Objects.Note;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.stream.Stream;

import static BeatSaberObjects.Objects.Enums.ParityMaps.cutDirectionSmallerThanOrEquals90Degrees;
import static org.junit.jupiter.api.Assertions.*;

/*
Red: 0
Blue: 1

Index - Layer:          Cut direction:|---|---|---|---|       |---|---|---|
|   |   |   |3-2|       | 4 | 0 | 5 |
|---|---|---|---|       |---|---|---|
|   |   |   |3-1|       | 2 | 8 | 3 |
|---|---|---|---|       |---|---|---|
|0-0|1-0|2-0|3-0|       | 6 | 1 | 7 |
|---|---|---|---|       |---|---|---|
 */


class NoteTest_IsDD {
    
    @ParameterizedTest
    @MethodSource("provideParameters")
    void testIsDDForLegalBehavior(int cutDirectionPrev, int cutDirectionCurrent) {
        // Set up the notes
        Note previousNote = new Note();
        previousNote._cutDirection = cutDirectionPrev;
        
        Note currentNote = new Note();
        currentNote._cutDirection = cutDirectionCurrent;
        
        // Test the isDD method
        boolean isDD = currentNote.isDD(previousNote);
        boolean shouldBeDD = cutDirectionSmallerThanOrEquals90Degrees.get(cutDirectionPrev).contains(cutDirectionCurrent);
        
        // Assert that isDD should return true for legal behavior
        assertEquals(shouldBeDD, isDD, "isDD returned \"" + isDD + "\" instead of \"" + shouldBeDD + "\" for combination: prev=" + cutDirectionPrev + ", current=" + cutDirectionCurrent);
    }
    
    @Test
    void testIsDD() {
        Note previousNote = new Note();
        previousNote._cutDirection = 0;
        
        Note currentNote = new Note();
        currentNote._cutDirection = 4;
        
        // Verwende die Anforderungen (nicht die Methode), um das erwartete Ergebnis zu bestimmen
        boolean expected = cutDirectionSmallerThanOrEquals90Degrees.getOrDefault(0, List.of()).contains(4);
        
        assertEquals(expected, currentNote.isDD(previousNote));
    }
    
    
    
    @ParameterizedTest
    @CsvSource({
            // DDs
            "0, 4, true",
            "1, 7, true",
            "2, 6, true",
            "3, 5, true",
            "4, 2, true",
            "5, 0, true",
            "7, 7, true",
            "4, 0, true",
            
            // non-DDs
            "0, 1, false",
            "1, 4, false",
            "2, 5, false",
            "3, 6, false",
            "5, 7, false",
            "6, 0, false",
            "7, 2, false",
            "6, 3, false",
            
            // Invalid cuts
            "-1, 4, false",   // Invalid previous cut direction
            "0, -1, false",   // Invalid current cut direction
            "8, 4, false",    // Outside valid range
            "0, 9, false"     // Outside valid range
    })
    void testIsDDWithMultipleParameters(int prev, int current, boolean expected) {
        Note previousNote = new Note();
        previousNote._cutDirection = prev;
        
        Note currentNote = new Note();
        currentNote._cutDirection = current;
        
        // Check isDD method result
        boolean result = currentNote.isDD(previousNote);
        
        // Assert result matches expectation
        assertEquals(expected, result,
                "Unexpected result for isDD with prev=" + prev + " and current=" + current);
    }
    
    @ParameterizedTest
    @CsvSource({
            "-99999, 0",      // Extrem kleiner vorheriger Wert
            "0, -99999",      // Extrem kleiner aktueller Wert
            "99999, 0",       // Extrem großer vorheriger Wert
            "0, 99999",       // Extrem großer aktueller Wert
            "-99999, 99999"   // Beide Werte extrem
    })
    void testIsDDWithExtremeValues(int prev, int current) {
        Note previousNote = new Note();
        previousNote._cutDirection = prev;
        
        Note currentNote = new Note();
        currentNote._cutDirection = current;
        
        // Expect no unhandled exceptions
        try {
            boolean result = currentNote.isDD(previousNote);
            assertFalse(result, "isDD should return false for invalid values: prev=" + prev + ", current=" + current);
        } catch (Exception e) {
            fail("isDD threw an exception for invalid values: prev=" + prev + ", current=" + current + ". Exception: " + e.getMessage());
        }
    }
    
    @Test
    void testIsDDWithNullPreviousNote() {
        Note currentNote = new Note();
        currentNote._cutDirection = 4;
        
        try {
            boolean result = currentNote.isDD(null);
            assertFalse(result, "isDD should return false when previousNote is null");
        } catch (Exception e) {
            fail("isDD threw an exception when previousNote is null. Exception: " + e.getMessage());
        }
    }
    
    
    private static Stream<Arguments> provideParameters() {
        Stream.Builder<Arguments> builder = Stream.builder();
        for (int prev = 0; prev <= 8; prev++) {
            for (int cur = 0; cur <= 8; cur++) {
                builder.accept(Arguments.of(prev, cur));
            }
        }
        return builder.build();
    }
    
    
}
