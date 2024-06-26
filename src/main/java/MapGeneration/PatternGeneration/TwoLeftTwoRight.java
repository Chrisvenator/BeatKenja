package MapGeneration.PatternGeneration;

import BeatSaberObjects.Objects.Note;
import BeatSaberObjects.Objects.TimingNote;

import java.util.ArrayList;
import java.util.List;

import static MapGeneration.PatternGeneration.CommonMethods.CheckParity.nextNoteAfterTimingNote;
import static MapGeneration.PatternGeneration.CommonMethods.PlaceFirstNotes.placeInitialNoteBasedOnPrevNote;
import static MapGeneration.PatternGeneration.NextLinearNote.nextLinearNote;

public class TwoLeftTwoRight {
    public static List<Note> twoLeftTwoRight(List<Note> timings, Note prevBlue, Note prevRed) {
        List<Note> notes = new ArrayList<>();

        placeInitialNoteBasedOnPrevNote(notes, prevBlue, timings.get(0)._time);
        notes.add(nextLinearNote(notes.get(0), timings.get(1)._time));

        placeInitialNoteBasedOnPrevNote(notes, prevRed, timings.get(2)._time);
        notes.add(nextLinearNote(notes.get(2), timings.get(3)._time));


        int invalidPlacementsInARow = 0;
        for (int i = 4; i < timings.size(); i += 2) {
            // ERROR handling:
            // Try 100 times to place a normal note. If this doesn't work, then place a Timing-BeatSaberObjects.Objects.Note.
            // If this still doesn't work, then throw an exception
            //Place a BeatSaberObjects.Objects.Note that doesn't break parity after the error:
            if (i >= 4 && invalidPlacementsInARow >= 100) {
                System.err.println("_ERROR at beat:   " + timings.get(i)._time + " Timing note");
                Note errorNote = new TimingNote(timings.get(i)._time);
                notes.add(errorNote); //Adding BeatSaberObjects.Objects.Note
                invalidPlacementsInARow = 0;
                continue;
            } else if (invalidPlacementsInARow >= 500)
                throw new IllegalArgumentException("Infinite Loop while creating map! Please try again. (Error occured in \"2-2\")");
            if (i >= 2 && notes.get(notes.size() - 1)._cutDirection == 8) {
                try {
                    notes.add(nextNoteAfterTimingNote(notes, timings.get(i)._time, notes.size(), i < 4 ? 2 : 4));
                } catch (Exception e) {
                    e.printStackTrace();
                    return notes;
                }
                continue;
            }


            if (i % 4 == 0) {
                Note blue1 = nextLinearNote(notes.get(notes.size() - 3), timings.get(i)._time);
                if (i >= timings.size() - 1) continue;
                Note blue2 = nextLinearNote(blue1, timings.get(i + 1)._time);
                if (blue1.isDD(notes.get(notes.size() - 3)) || blue2.isDD(blue1)) {
                    i--;
                    invalidPlacementsInARow++;
                    continue;
                }
                notes.add(blue1);
                notes.add(blue2);
            } else {
                Note red1 = nextLinearNote(notes.get(notes.size() - 3), timings.get(i)._time);
                if (i >= timings.size() - 1) continue;
                Note red2 = nextLinearNote(red1, timings.get(i + 1)._time);
                if (red1.isDD(notes.get(notes.size() - 3)) || red2.isDD(red1)) {
                    i--;
                    invalidPlacementsInARow++;
                    continue;
                }
                notes.add(red1);
                notes.add(red2);
            }
        }

        for (int i = 1; i < notes.size(); i++) if (i % 4 == 2 || i % 4 == 3) notes.get(i).invertNote();

        return notes;

    }

}
