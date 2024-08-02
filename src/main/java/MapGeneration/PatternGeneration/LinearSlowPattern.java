package MapGeneration.PatternGeneration;

import BeatSaberObjects.Objects.Note;
import BeatSaberObjects.Objects.TimingNote;

import java.util.ArrayList;
import java.util.List;

import static DataManager.Parameters.logger;
import static MapGeneration.PatternGeneration.CommonMethods.CheckParity.invalidPlacement;
import static MapGeneration.PatternGeneration.CommonMethods.CheckParity.nextNoteAfterTimingNote;
import static MapGeneration.PatternGeneration.CommonMethods.PlaceFirstNotes.firstNotePlacement;
import static MapGeneration.PatternGeneration.NextLinearNote.nextLinearNote;

public class LinearSlowPattern {
    /**
     * Creates a really linear two-handed mid-speed pattern.
     *
     * @param timings where the notes should be placed
     * @return List of BeatSaberObjects.Objects.Note
     */
    public static List<Note> linearSlowPattern(List<Note> timings, boolean oneHanded, Note prevBlue, Note prevRed) {
        List<Note> pattern = new ArrayList<>(timings.size());
        for (int i = 0; i < timings.size(); i++) {
            pattern.add(null);
        }

        int j = oneHanded ? 1 : 2;

        // The first 2 notes have to be placed manually to ensure that they are not on some random position
        pattern.set(0, prevBlue == null ? firstNotePlacement(timings.get(0)._time) : nextLinearNote(prevBlue, timings.get(0)._time));
        if (!oneHanded) {
            pattern.set(1, prevRed == null ? firstNotePlacement(timings.get(1)._time) : nextLinearNote(prevRed, timings.get(1)._time));
        }

        for (int i = 0; i < 100; i++) {
            if (!oneHanded) {
                if (prevRed != null && prevRed.isDD(pattern.get(1))) {
                    pattern.set(1, nextLinearNote(prevRed, timings.get(1)._time));
                }
            }
            if (prevBlue != null && prevBlue.isDD(pattern.get(0))) {
                pattern.set(0, nextLinearNote(prevBlue, timings.get(0)._time));
            }
        }

        int invalidPlacesInARow = 0;
        for (int i = j; i < timings.size(); i++) {

            // Manual error handling:
            // When there exists an infinite loop:
            // Then create a new next note
            if ((oneHanded && i >= 2 || i >= 4) && invalidPlacesInARow >= 500) {
                logger.warn("at beat:   " + timings.get(i)._time);
                pattern.set(i, new TimingNote(timings.get(i)._time));
                invalidPlacesInARow = 0;
                continue;
            } else if (invalidPlacesInARow >= 500) {
                throw new IllegalArgumentException("Infinite Loop while creating map! Please try again.");
            }
            if ((oneHanded && i >= 2 || i >= 4) && pattern.get(i - j)._cutDirection == 8) {
                pattern.set(i, nextNoteAfterTimingNote(pattern, timings.get(i)._time, i, j));
                continue;
            }

            // Calculate the next note:
            pattern.set(i, nextLinearNote(pattern.get(i - j), timings.get(i)._time));

            // Check if this note's placement is valid
            if (i >= 4 * j && invalidPlacement(pattern, i, false)) {
                pattern.set(i, null);
                i--;
                invalidPlacesInARow++;
            }
        }

        // Make every second note a red note
        if (!oneHanded) {
            for (int i = 1; i < pattern.size(); i += 2) {
                pattern.get(i).invertNote();
            }
        }

        return pattern;
    }



}
