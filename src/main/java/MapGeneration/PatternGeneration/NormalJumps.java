package MapGeneration.PatternGeneration;

import BeatSaberObjects.Objects.Note;

import java.util.List;

import static MapGeneration.PatternGeneration.SmallJumps.createSmallJumps;

public class NormalJumps {
    /**
     * This function creates a jump-pattern where the jumps are "normally-big". That means, there is 1 air block in between the jumps
     *
     * @param timings   List of note timings
     * @param oneHanded should the jumps be one handed?
     * @param prevBlue  previous blue note so that there is no parity break
     * @param prevRed   previous blue note so that there is no parity break
     * @return returns a jump-pattern with not-so-big swings
     */
    public static List<Note> createNormalJumps(List<Note> timings, boolean oneHanded, Note prevBlue, Note prevRed) {
        List<Note> notes = createSmallJumps(timings, oneHanded, prevBlue, prevRed);
        for (Note n : notes) if (n._lineLayer == 1) n._lineLayer++;

        return notes;
    }

}
