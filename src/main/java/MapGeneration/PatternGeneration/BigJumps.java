package MapGeneration.PatternGeneration;

import BeatSaberObjects.Objects.Note;

import java.util.List;

import static MapGeneration.PatternGeneration.NormalJumps.createNormalJumps;

public class BigJumps {
    /**
     * This function creates a jump-pattern where the jumps are huge. Use with caution!
     *
     * @param timings   List of note timings
     * @param oneHanded should the jumps be one handed?
     * @param prevBlue  previous blue note so that there is no parity break
     * @param prevRed   previous blue note so that there is no parity break
     * @return returns a jump-pattern with not-so-big swings
     */
    public static List<Note> createBigJumps(List<Note> timings, boolean oneHanded, Note prevBlue, Note prevRed) {
        List<Note> notes = createNormalJumps(timings, oneHanded, prevBlue, prevRed);
        for (Note n : notes) {
            if (n._lineLayer == 2) n._lineLayer--;
            if (n._type == 1) {
                if (n._cutDirection == 1) {
                    n._lineIndex = 0;
                    n._cutDirection = 6;
                } else {
                    n._lineIndex = 3;
                    n._cutDirection = 5;
                }
            } else if (n._type == 0) {
                if (n._cutDirection == 1) {
                    n._lineIndex = 3;
                    n._cutDirection = 7;
                } else {
                    n._lineIndex = 0;
                    n._cutDirection = 4;
                }
            }
        }

        return notes;
    }

}
