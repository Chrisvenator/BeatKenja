package MapGeneration.PatternGeneration.CommonMethods;

import BeatSaberObjects.Objects.Note;

import java.util.List;

import static DataManager.Parameters.BPM;
import static DataManager.Parameters.logger;

public class NpsBpmConverter {
    public static void convertSecondsToBeats(List<Note> notes) {
        logger.info("Start converting seconds to beats.");

        for (Note n : notes) {
            float before = n._time;
            n._time = (float) (n._time * BPM / 60);
            logger.trace("Before: " + before + " - After: " + n._time);
        }

        logger.debug("End converting seconds to beats.");
    }
    public static void convertBeatsToSeconds(List<Note> notes) {
        logger.info("Start converting beats to seconds.");

        for (Note n : notes) {
            float before = n._time;
            n._time = (float) (n._time  / BPM * 60);
            logger.trace("Before: " + before + " - After: " + n._time);
        }

        logger.debug("End converting beats to seconds.");
    }
}
