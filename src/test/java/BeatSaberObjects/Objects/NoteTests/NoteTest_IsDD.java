package BeatSaberObjects.Objects.NoteTests;

import BeatSaberObjects.Objects.Note;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static BeatSaberObjects.Objects.Enums.ParityMaps.cutDirectionSmallerThanOrEquals90Degrees;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

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
