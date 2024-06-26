package MapGeneration.PatternGeneration;

import BeatSaberObjects.Objects.Note;
import MapGeneration.GenerationElements.Pattern;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static MapGeneration.PatternGeneration.CommonMethods.CheckParity.invalidPlacement;
import static MapGeneration.PatternGeneration.CommonMethods.CheckParity.nextNoteAfterTimingNote;
import static MapGeneration.PatternGeneration.CommonMethods.PlaceFirstNotes.firstNotePlacement;
import static MapGeneration.PatternGeneration.CommonMethods.StackPlacements.createStacks;
import static MapGeneration.ComplexPatternFromTemplate.complexPatternFromTemplate;
import static MapGeneration.PatternGeneration.NextLinearNote.nextLinearNote;

public class TwoRightOneLeft {
    /**
     * This methode creates a pattern, where there is one right-hand swing followed by a both-hand swing followed by a right-hand swing.
     * This repeats until the end of timings[] is reached
     *
     * @param timings  where the notes should be placed
     * @param p        p are the probabilities that which note follows which. It must be in the "MapGeneration.GenerationElements.Pattern"-Format
     * @param prevBlue What the previous blue note was
     * @param prevRed  What the previous red note was
     * @param stacks   should stacks be generated?
     * @return A List of all notes that have been generated
     */
    public static List<Note> twoRightOneLeft(List<Note> timings, Pattern p, Note prevBlue, Note prevRed, boolean stacks) throws IllegalArgumentException {
        List<Note> redNotes = new ArrayList<>();

        // Right-hand swings:
        List<Note> complexPattern = complexPatternFromTemplate(timings, p, true, stacks, false,prevBlue, null);

        // Define the previous note that came before this function was called
        if (prevRed == null) firstNotePlacement(timings.get(0)._time);
        redNotes.add(nextLinearNote(prevRed, timings.get(0)._time));

        // Ensure that there is no DD when creating the first note!
        for (int i = 0; i < 100 && prevRed != null; i++) {
            if (prevRed.isDD(redNotes.get(0))) {
                redNotes.remove(0);
                redNotes.add(nextLinearNote(prevRed, timings.get(0)._time));
            }
        }

        // Create left-hand swings:
        int invalidPlacementsInARow = 0;
        for (int i = 2; i < complexPattern.size(); i += 2) {
            // ERROR handling:
            // Try 100 times to place a normal note. If this doesn't work, then place a Timing-BeatSaberObjects.Objects.Note.
            // If this still doesn't work, then throw an exception
            if (invalidPlacementsInARow >= 100) {
                System.err.println("WARN at beat:    " + timings.get(i)._time + " There may be a mismatched Note");
                // Logic for adding TimingNote or alternative note based on even/odd index and prevRed

                if (i % 2 == 0 && prevRed == null) redNotes.add(new Note(complexPattern.get(i)._time, 2, 0, 1, 0));
                else if (i % 2 == 1 && prevRed == null) redNotes.add(new Note(complexPattern.get(i)._time, 2, 0, 1, 1));
                else if (i % 2 == 0) redNotes.add(new Note(complexPattern.get(i)._time, 2, 0, 1, prevRed.isDD(new Note(0, 0, 0, 0, 1)) ? 0 : 1));
                else redNotes.add(new Note(complexPattern.get(i)._time, 2, 0, 1, prevRed.isDD(new Note(0, 0, 0, 0, 1)) ? 1 : 0));

                invalidPlacementsInARow = 0;
                continue;
            }

            // Place a Note that doesn't break parity after the error:
            if (redNotes.get(redNotes.size() - 1)._cutDirection == 8) {
                if (i >= 4) {
                    redNotes.add(nextNoteAfterTimingNote(redNotes, timings.get(i)._time, redNotes.size(), 1));
                } else redNotes.add(new Note(complexPattern.get(i)._time, 2, 0, 1, (i % 2 == 0 ? 1 : 0)));

                continue;
            }

            // Create note:
            Note n = nextLinearNote(redNotes.get(redNotes.size() - 1), complexPattern.get(i)._time);

            // If the Notes are placed inside each other or too close to one another, then try again
            if (i >= 2 && (complexPattern.get(i)._lineIndex == n.getInverted()._lineIndex && complexPattern.get(i)._lineLayer == n._lineLayer || complexPattern.get(i - 1)._lineIndex == n.getInverted()._lineIndex && complexPattern.get(i - 1)._lineLayer == n._lineLayer)) {
                i -= 2;
                invalidPlacementsInARow++;
                continue;
            }

            // Transfer the information about if the note is a stack into the new list
            if (i < timings.size() - 1) n.amountOfStackedNotes = timings.get(i).amountOfStackedNotes;
            redNotes.add(n);

            if (invalidPlacement(redNotes, redNotes.size() - 1, true)) {
                i -= 2;
                invalidPlacementsInARow++;
                redNotes.remove(n);
            }
        }

        // Inverting all red Notes so that they are actually red notes LUL
        for (Note n : redNotes) n.invertNote();

        // Creating a list of all notes that should be returned
        // and merging the red notes and the blue notes
        List<Note> allNotes = new ArrayList<>();
        allNotes.addAll(redNotes);
        allNotes.addAll(complexPattern);
        Collections.sort(allNotes);

        // Creating the stacks and adding all notes to the final List
        if (stacks) allNotes = createStacks(allNotes);

        return allNotes;
    }

}
