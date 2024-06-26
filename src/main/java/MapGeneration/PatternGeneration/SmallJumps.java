package MapGeneration.PatternGeneration;

import BeatSaberObjects.Objects.Note;
import BeatSaberObjects.Objects.TimingNote;

import java.util.ArrayList;
import java.util.List;

public class SmallJumps {
    /**
     * This function creates a jump-pattern where the jumps are not that big
     *
     * @param timings   List of note timings
     * @param oneHanded should the jumps be one handed?
     * @param prevBlue  previous blue note so that there is no parity break
     * @param prevRed   previous blue note so that there is no parity break
     * @return returns a jump-pattern with not-so-big swings
     */
    public static List<Note> createSmallJumps(List<Note> timings, boolean oneHanded, Note prevBlue, Note prevRed) {
        List<Note> notes = new ArrayList<>();

        for (int i = 0; i < timings.size(); i++) {

            //If the pattern should be one handed OR every second note
            if (i % 2 == 0 || oneHanded) {
                //When there is an upper cut
                if (prevBlue == null || prevBlue._cutDirection == 0 || prevBlue._cutDirection == 2 || prevBlue._cutDirection == 3 || prevBlue._cutDirection == 4 || prevBlue._cutDirection == 5 || prevBlue._cutDirection == 8)
                    notes.add(new Note(timings.get(i)._time, 2, 0, 1, 1));

                    //When there is a down-cut
                else if (prevBlue._cutDirection == 1 || prevBlue._cutDirection == 6 || prevBlue._cutDirection == 7)
                    notes.add(new Note(timings.get(i)._time, 3, 1, 1, 5));

                    //error catching
                else notes.add(new TimingNote(timings.get(i)._time));


                prevBlue = notes.get(notes.size() - 1);

            } else {
                //Every second note should be red
                if (prevRed == null || prevRed._cutDirection == 0 || prevRed._cutDirection == 2 || prevRed._cutDirection == 3 || prevRed._cutDirection == 4 || prevRed._cutDirection == 5 || prevRed._cutDirection == 8)
                    notes.add(new Note(timings.get(i)._time, 2, 0, 1, 1));

                    //When there is a down-cut
                else if (prevRed._cutDirection == 1 || prevRed._cutDirection == 6 || prevRed._cutDirection == 7)
                    notes.add(new Note(timings.get(i)._time, 3, 1, 1, 5));

                    //error catching
                else notes.add(new TimingNote(timings.get(i)._time));


                //Setting the previous note:
                prevRed = notes.get(notes.size() - 1);

                //making the previous note red:
                notes.get(notes.size() - 1).invertNote();
            }
        }
        return notes;
    }

}
