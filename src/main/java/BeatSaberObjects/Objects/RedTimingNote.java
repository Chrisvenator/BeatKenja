package BeatSaberObjects.Objects;

    /*
    Red: 0
    Blue: 1

    Index - Layer:          Cut direction:
    |---|---|---|---|       |---|---|---|
    |   |   |   |3-2|       | 4 | 0 | 5 |
    |---|---|---|---|       |---|---|---|
    |   |   |   |3-1|       | 2 | 8 | 3 |
    |---|---|---|---|       |---|---|---|
    |0-0|1-0|2-0|3-0|       | 6 | 1 | 7 |
    |---|---|---|---|       |---|---|---|
     */

public class RedTimingNote extends Note {
    public RedTimingNote(float time) {
        super(time, 1,0,1,8);
    }

    public static boolean isRedTimingNote(Note note) {
        if (note instanceof RedTimingNote) return true;
        if (note._type == 1 && note._lineLayer == 0 && note._lineIndex == 1 && note._cutDirection == 8) return true;

        return false;
    }
}