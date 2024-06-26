package MapGeneration.PatternGeneration;

import BeatSaberObjects.Objects.Note;
import MapGeneration.GenerationElements.Pattern;

import java.util.ArrayList;
import java.util.List;

import static DataManager.Parameters.ignoreDDs;
import static MapGeneration.PatternGeneration.CommonMethods.StackPlacements.createStacks;
import static MapGeneration.PatternGeneration.ComplexPatternFromTemplate.complexPatternFromTemplate;
import static MapGeneration.PatternGeneration.RandomPattern.createRandomPattern;

public class RandomV2FromTemplate {
    public static List<Note> randomV2FromTemplate(Note[] timings, Pattern p, boolean stacks, Note prevBlue, Note prevRed) throws IllegalArgumentException {
        List<Note> notes = new ArrayList<>();
        List<Note> randomNotes = createRandomPattern(timings, false);

        List<Note> blueNotes = new ArrayList<>();
        List<Note> redNotes = new ArrayList<>();

        for (Note randomNote : randomNotes) {
            if (randomNote._type == 1) blueNotes.add(randomNote);
            else redNotes.add(randomNote);
        }

        List<Note> blueComplex = complexPatternFromTemplate(blueNotes, p, true, false, prevBlue, null);
        List<Note> redComplex = complexPatternFromTemplate(redNotes, p, true, false, prevRed, null);
        redComplex.forEach(Note::invertNote);

        for (int i = 0; i < blueNotes.size(); i++) {
            notes.add(new Note(
                    blueComplex.get(i)._time,
                    blueNotes.get(i)._lineIndex == 0 ? 1 : blueNotes.get(i)._lineIndex,
                    blueNotes.get(i)._lineLayer,
                    blueComplex.get(i)._type,
                    blueComplex.get(i)._cutDirection
            ));
            if (!ignoreDDs && i >= 1 && notes.get(i).isDD(notes.get(i - 1))) {
                notes.remove(i);
                i--;
            }
        }

        for (int i = 0; i < redComplex.size(); i++) {
            notes.add(new Note(
                    redComplex.get(i)._time,
                    redNotes.get(i)._lineIndex == 3 ? 2 : redNotes.get(i)._lineIndex,
                    redNotes.get(i)._lineLayer,
                    redComplex.get(i)._type,
                    redComplex.get(i)._cutDirection
            ));

            if (!ignoreDDs && i >= 1 && notes.get(i).isDD(notes.get(i - 1))) {
                notes.remove(i);
                i--;
            }
        }

        if (stacks) notes = createStacks(notes);

        return fixInverts(notes);
    }

    /**
     * Fixes inverted note placements in the provided list of notes. This method adjusts the line layer
     * of notes based on their cut direction and the previous note of the same type.
     *
     * @param notes the list of notes to fix
     * @return the list of notes with fixed invert placements
     */
    private static List<Note> fixInverts(List<Note> notes) {
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

        return notes;
    }

}
