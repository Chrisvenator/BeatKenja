package MapGeneration.PatternGeneration;

import BeatSaberObjects.Objects.Note;
import CustomWaveGenerator.Coordinate;

import java.util.ArrayList;
import java.util.List;

public class WavesToMap {
    @Deprecated
    public static List<Note> createMapFromWaves(List<Coordinate> coordinates) {
        List<Note> notes = new ArrayList<>();
        coordinates.forEach(c -> notes.add(new Note(c.x(), 0, c.y(), 1, 8)));

        return notes;
    }

}
