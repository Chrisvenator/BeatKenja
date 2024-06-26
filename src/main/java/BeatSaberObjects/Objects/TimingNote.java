package BeatSaberObjects.Objects;

    /*
    Red: 0
    Blue: 1

    Layer - Index:          Cut direction:
    |---|---|---|---|       |---|---|---|
    |   |   |   |3-2|       | 4 | 0 | 5 |
    |---|---|---|---|       |---|---|---|
    |   |   |   |3-1|       | 2 | 8 | 3 |
    |---|---|---|---|       |---|---|---|
    |0-0|1-0|2-0|3-0|       | 6 | 1 | 7 |
    |---|---|---|---|       |---|---|---|
     */

public class TimingNote extends Note {
    public TimingNote(float time) {
        super(time);
    }

    public static boolean isTimingNote(Note note) {
        if (note instanceof TimingNote) return true;
        if (note._type == 0 && note._lineLayer == 0 && note._lineIndex == 0 && note._cutDirection == 8) return true;

        return false;
    }
}