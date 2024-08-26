package MapGeneration.PatternGeneration;

import BeatSaberObjects.Objects.Note;
import BeatSaberObjects.Objects.TimingNote;
import MapGeneration.MapGenerator;
import org.hibernate.mapping.Map;

import java.util.ArrayList;
import java.util.List;

public class SmallJumps extends NormalJumps {
    /**
     * This function creates a jump-pattern where the jumps are not that big
     *
     * @param timings   List of note timings
     * @param oneHanded should the jumps be one-handed?
     * @param prevBlue  previous blue note so that there is no parity break
     * @param prevRed   previous blue note so that there is no parity break
     * @return returns a jump-pattern with not-so-big swings
     */
    public static List<Note> createSmallJumps(List<Note> timings, boolean oneHanded, Note prevBlue, Note prevRed) {
        List<Note> notes = new ArrayList<>();

        for (int i = 0; i < timings.size(); i++) {
            Note prevNote;

            //If the pattern should be one-handed OR every second note
            boolean isBlueNote = i % 2 == 0 || oneHanded;
            if (isBlueNote) {
                prevNote = prevBlue;
            } else {
                prevNote = prevRed;
            }

            //When there is an upper cut
            if (prevNote == null || prevNote._cutDirection == 0 || prevNote._cutDirection == 2 || prevNote._cutDirection == 3 || prevNote._cutDirection == 4 || prevNote._cutDirection == 5 || prevNote._cutDirection == 8)
                notes.add(new Note(timings.get(i)._time, 2, 0, 1, 1));

            //When there is a down-cut
            else if (prevNote._cutDirection == 1 || prevNote._cutDirection == 6 || prevNote._cutDirection == 7)
                notes.add(new Note(timings.get(i)._time, 3, 1, 1, 5));

            //error catching
            else notes.add(new TimingNote(timings.get(i)._time));

            Note currentNote = notes.get(notes.size() - 1);

            if (isBlueNote) prevBlue = currentNote;
            else {
                prevRed = currentNote;
                currentNote.invertNote();
            }
        }
        return notes;
    }

}
