package MapGeneration.PatternGeneration;

import BeatSaberObjects.Objects.Note;
import MapGeneration.GenerationElements.Pattern;
import MapGeneration.PatternGeneration.CommonMethods.FixErrorsInPatterns;

import java.util.ArrayList;
import java.util.List;

import static DataManager.Parameters.ignoreDDs;
import static MapGeneration.PatternGeneration.CommonMethods.StackPlacements.createStacks;
import static MapGeneration.ComplexPatternFromTemplate.complexPatternFromTemplate;
import static MapGeneration.PatternGeneration.RandomPattern.createRandomPattern;

public class RandomV2FromTemplate {
    public static List<Note> randomV2FromTemplate(Note[] timings, Pattern p, boolean stacks, Note prevBlue, Note prevRed) throws IllegalArgumentException {
        List<Note> notes = new ArrayList<>();

        // Creating truly random notes
        List<Note> randomNotes = createRandomPattern(timings, false);

        // Split them into Red and Blue
        List<Note> blueNotes = new ArrayList<>();
        List<Note> redNotes = new ArrayList<>();

        splitNotesByType(randomNotes, blueNotes, redNotes);

        // Create complex notes from the random placements
        List<Note> complexBlue = complexPatternFromTemplate(blueNotes, p, true, false, false, prevBlue, null);
        List<Note> complexRed = complexPatternFromTemplate(redNotes, p, true, false, false, prevRed, null);
        complexRed.forEach(Note::invertNote);

        // Copy the lineIndex and the lineLayer from the randomly generated map and copy them into the complex-generated map
        mergeNotes(blueNotes, complexBlue, notes, 0, 1);
        mergeNotes(redNotes, complexRed, notes, 3, 2);

        if (stacks) notes = createStacks(notes);

        fixInverts(notes);
        FixErrorsInPatterns.fixSimpleMappingErrors(notes);
        return notes;
    }

    /**
     * Splits a list of notes into two separate lists based on their type.
     *
     * @param randomNotes The list of random notes to be split.
     * @param blueNotes The list where all blue notes will be stored.
     * @param redNotes The list where all red notes will be stored.
     */
    private static void splitNotesByType(List<Note> randomNotes, List<Note> blueNotes, List<Note> redNotes) {
        for (Note note : randomNotes) {
            if (note._type == 1) blueNotes.add(note);
            else redNotes.add(note);
        }
    }

    /**
     * Merges notes from the original list with complex notes based on index conditions.
     * Adds them to the main notes list after applying specific index transformations.
     *
     * @param originalNotes The list of original notes from which indexes are derived.
     * @param complexNotes The list of complex pattern notes to be merged.
     * @param notes The main list where all merged notes will be added.
     * @param specialIndex The index in original notes that triggers a replacement.
     * @param replacementIndex The index to replace the special index with in the final note list.
     */
    private static void mergeNotes(List<Note> originalNotes, List<Note> complexNotes, List<Note> notes, int specialIndex, int replacementIndex) {
        for (int i = 0; i < complexNotes.size(); i++) {
            notes.add(new Note(
                    complexNotes.get(i)._time,
                    originalNotes.get(i)._lineIndex == specialIndex ? replacementIndex : originalNotes.get(i)._lineIndex,
                    originalNotes.get(i)._lineLayer,
                    complexNotes.get(i)._type,
                    complexNotes.get(i)._cutDirection
            ));
        }
    }

    /**
     * Fixes inverted note placements in the provided list of notes. This method adjusts the line layer
     * of notes based on their cut direction and the previous note of the same type.
     *
     * @param notes the list of notes to fix
     */
    private static void fixInverts(List<Note> notes) {
        Note prevBlue = null;
        Note prevRed = null;

        for (Note n : notes) {
            // Check for inverted blue notes and adjust the line layer if needed
            if ((n._cutDirection == 1 || n._cutDirection == 6 || n._cutDirection == 7) &&
                    (n._type == 1 && prevBlue != null && (prevBlue._cutDirection == 4 || prevBlue._cutDirection == 0 || prevBlue._cutDirection == 5) && n._lineLayer == 2 && prevBlue._lineLayer == 0) ||
                    (n._type == 0 && prevRed != null && (prevRed._cutDirection == 4 || prevRed._cutDirection == 0 || prevRed._cutDirection == 5) && n._lineLayer == 2 && prevRed._lineLayer == 0)) {
                n._lineLayer = 0;
            }
            // Check for inverted red notes and adjust the line layer if needed
            if ((n._cutDirection == 4 || n._cutDirection == 0 || n._cutDirection == 5) &&
                    (n._type == 1 && prevBlue != null && (prevBlue._cutDirection == 1 || prevBlue._cutDirection == 6 || prevBlue._cutDirection == 7) && n._lineLayer == 0 && prevBlue._lineLayer == 2) ||
                    (n._type == 0 && prevRed != null && (prevRed._cutDirection == 1 || prevRed._cutDirection == 6 || prevRed._cutDirection == 7) && n._lineLayer == 0 && prevRed._lineLayer == 2)) {
                n._lineLayer = 2;
            }

            // Update the previous note of the same type
            if (n._type == 1) prevBlue = n;
            if (n._type == 0) prevRed = n;
        }
    }

}
