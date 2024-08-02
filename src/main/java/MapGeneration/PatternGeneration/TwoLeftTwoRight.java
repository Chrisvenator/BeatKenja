package MapGeneration.PatternGeneration;

import BeatSaberObjects.Objects.Note;
import BeatSaberObjects.Objects.TimingNote;

import java.util.ArrayList;
import java.util.List;

import static DataManager.Parameters.logger;
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
                logger.warn("at beat:   " + timings.get(i)._time + " Timing note");
                Note errorNote = new TimingNote(timings.get(i)._time);
                notes.add(errorNote); //Adding BeatSaberObjects.Objects.Note
                invalidPlacementsInARow = 0;
                continue;
            } else if (invalidPlacementsInARow >= 500)
                throw new IllegalArgumentException("Infinite Loop while creating map! Please try again. (Error occurred in \"2-2\")");
            if (i >= 2 && notes.get(notes.size() - 1)._cutDirection == 8) {
                try {
                    notes.add(nextNoteAfterTimingNote(notes, timings.get(i)._time, notes.size(), i < 4 ? 2 : 4));
                } catch (Exception e) {
                    e.printStackTrace();
                    return notes;
                }
                continue;
            }


            Note previousNote = notes.get(notes.size() - 3);
            Note note1 = nextLinearNote(previousNote, timings.get(i)._time);

            if (i >= timings.size() - 1) continue;

            Note note2 = nextLinearNote(note1, timings.get(i + 1)._time);

            if (note1.isDD(previousNote) || note2.isDD(note1)) {
                i--;
                invalidPlacementsInARow++;
                continue;
            }

            notes.add(note1);
            notes.add(note2);
        }

        for (int i = 1; i < notes.size(); i++) if (i % 4 == 2 || i % 4 == 3) notes.get(i).invertNote();

        return notes;

    }

}
