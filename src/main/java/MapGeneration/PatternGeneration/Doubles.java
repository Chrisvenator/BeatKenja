package MapGeneration.PatternGeneration;

import BeatSaberObjects.Objects.Note;
import BeatSaberObjects.Objects.TimingNote;

import java.util.ArrayList;
import java.util.List;

import static DataManager.Parameters.logger;
import static MapGeneration.PatternGeneration.CommonMethods.CheckParity.nextNoteAfterTimingNote;
import static MapGeneration.PatternGeneration.CommonMethods.PlaceFirstNotes.placeInitialNoteBasedOnPrevNote;
import static MapGeneration.PatternGeneration.NextLinearNote.nextLinearNote;

public class Doubles {
    public static List<Note> createDoubles(List<Note> timings, Note prevBlue, Note prevRed) {
        List<Note> notes = new ArrayList<>();


        //make red note blue, so that it can be handled
        if (prevRed != null) prevRed.invertNote();

        placeInitialNoteBasedOnPrevNote(notes, prevBlue, timings.get(0)._time);
        placeInitialNoteBasedOnPrevNote(notes, prevRed, timings.get(0)._time);

        //revert former red note back into a red note
        if (prevRed != null) prevRed.invertNote();

        int invalidPlacementsInARow = 0;
        for (int i = 1; i < timings.size(); i++) {
            // ERROR handling:
            // Try 100 times to place a normal note. If this doesn't work, then place a Timing-BeatSaberObjects.Objects.Note.
            // If this still doesn't work, then throw an exception
            if (i >= 4 && invalidPlacementsInARow >= 100) {
                logger.warn("at beat:   " + timings.get(i)._time + " Timing note");
                Note errorNote = new TimingNote(timings.get(i)._time);
                notes.add(errorNote); //Adding blue BeatSaberObjects.Objects.Note
                notes.add(errorNote); //Adding red BeatSaberObjects.Objects.Note
                invalidPlacementsInARow = 0;
                continue;
            } else if (invalidPlacementsInARow >= 500)
                throw new IllegalArgumentException("Infinite Loop while creating map! Please try again. (Error occurred in \"doubles\"");
            //Place a BeatSaberObjects.Objects.Note that doesn't break parity after the error:
            if (i >= 1 && notes.get(notes.size() - 1)._cutDirection == 8) {
                notes.add(nextNoteAfterTimingNote(notes, timings.get(i)._time, notes.size(), 2));
                notes.add(nextNoteAfterTimingNote(notes, timings.get(i)._time, notes.size() - 1, 2));
                continue;
            }


            Note blue = nextLinearNote(notes.get(notes.size() - 2), timings.get(i)._time);
            Note red = nextLinearNote(notes.get(notes.size() - 1), timings.get(i)._time);

            if (blue.equalNotePlacement(red)) {
                red._lineIndex -= 1;
            }

            if (red.equalNotePlacement(blue)) {
                throw new IllegalArgumentException("hä?");
            }


            if (blue.isDD(notes.get(notes.size() - 2)) || red.isDD(notes.get(notes.size() - 1))) {
                i--;
                invalidPlacementsInARow++;
                continue;
            }

            notes.add(blue);
            notes.add(red);
        }

        for (int i = 1; i < notes.size(); i += 2) notes.get(i).invertNote();
        for (int i = 1; i < notes.size() - 1; i++)
            if (notes.get(i).equalNotePlacement(notes.get(i + 1)) && notes.get(i)._time == notes.get(i + 1)._time)
                notes.get(i)._lineIndex--;


        return notes;
    }

}
