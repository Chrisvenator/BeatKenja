package MapGeneration.PatternGeneration;

import BeatSaberObjects.Objects.Note;
import DataManager.Parameters;
import MapGeneration.MapGenerator;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static DataManager.Parameters.BPM;
import static DataManager.Parameters.ignoreDDs;
import static DataManager.Parameters.logger;

public class RandomPattern extends MapGenerator {
    /**
     * createRandomPattern is a function that creates random Notes with the timings given
     *
     * @param timings   timings of the notes
     * @param oneHanded is the map one-handed?
     * @return the resulting Notes as a List
     */
    public static List<Note> createRandomPattern(Note[] timings, boolean oneHanded) {
        List<Note> notes = new ArrayList<>();
        Random random = new Random(Parameters.SEED);

        logger.info("Using " + BPM + " BPM");
        System.out.println("Using " + BPM + " BPM");

        for (int i = 0; i < timings.length; i++) {

            Note n = new Note(timings[i]._time);
            //nextInt(5) --> [INCLUSIVE 0, EXCLUSIVE 5)
            n._lineIndex = random.nextInt(4);
            n._lineLayer = random.nextInt(3);
            n._type = oneHanded ? 2 : random.nextInt(2);
            n._cutDirection = random.nextInt(8);

            if (i < timings.length - 1) {
                float time = (float) (timings[i]._time / BPM * 60);
                float timeNext = (float) (timings[i + 1]._time / BPM * 60);

                if (timeNext - time < 0.5) {
                    if (n._lineIndex == 1 && n._lineLayer == 1) n._lineIndex--;
                    if (n._lineIndex == 2 && n._lineLayer == 1) n._lineIndex++;
                }
            }

            if (!ignoreDDs && i >= 1 && n.isDD(notes.get(i - 1))) {
                i--;
            } else notes.add(n);
        }
        return notes;
    }

}
