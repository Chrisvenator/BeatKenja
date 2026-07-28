package MapGeneration.CharacteristicGeneration;

import BeatSaberObjects.Objects.BeatSaberMap;
import BeatSaberObjects.Objects.Bookmark;
import BeatSaberObjects.Objects.Events;
import BeatSaberObjects.Objects.Note;
import DataManager.Parameters;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for LawlessGenerator using a fixed seed for reproducibility.
 */
class LawlessGeneratorTest {

    private static final long FIXED_SEED = 133742069L;

    @BeforeEach
    void resetSeed() {
        Parameters.RANDOM = new java.util.Random(FIXED_SEED);
    }

    private BeatSaberMap makeMap(int noteCount, List<Bookmark> bookmarks) {
        Note[] notes = new Note[noteCount];
        for (int i = 0; i < noteCount; i++) {
            Note n = new Note();
            n._time = i * (32f / noteCount);
            n._lineIndex = i % 4;
            n._type = i % 2;
            n._cutDirection = i % 9;
            notes[i] = n;
        }
        BeatSaberMap map = new BeatSaberMap(new ArrayList<>(), new Events[0]);
        map._notes = notes;
        map._events = new Events[0];
        map._obstacles = new BeatSaberObjects.Objects.Obstacle[0];
        map.bookmarks = bookmarks != null ? bookmarks : new ArrayList<>();
        return map;
    }

    private List<Bookmark> peakBookmarks() {
        return List.of(
                new Bookmark(0f, "normal_jumps", null),
                new Bookmark(16f, "normal_jumps", null)
        );
    }

    private List<Bookmark> calmBookmarks() {
        return List.of(
                new Bookmark(0f, "linear", null),
                new Bookmark(16f, "linear", null)
        );
    }

    @Test
    void noteCountIncreasesOnIntenseMap() {
        BeatSaberMap map = makeMap(32, peakBookmarks());
        int before = map._notes.length;

        LawlessGenerator.generate(map);

        assertThat(map._notes.length).isGreaterThan(before);
    }

    @Test
    void noNotesMapIsUnchanged() {
        BeatSaberMap map = makeMap(0, peakBookmarks());
        map._notes = new Note[0];
        int before = map._notes.length;

        LawlessGenerator.generate(map);

        assertThat(map._notes).hasSize(before);
    }

    @Test
    void deterministicUnderFixedSeed() {
        Parameters.RANDOM = new java.util.Random(FIXED_SEED);
        BeatSaberMap map1 = makeMap(32, peakBookmarks());
        LawlessGenerator.generate(map1);
        int count1 = map1._notes.length;
        double[] times1 = Arrays.stream(map1._notes).mapToDouble(n -> n._time).toArray();

        Parameters.RANDOM = new java.util.Random(FIXED_SEED);
        BeatSaberMap map2 = makeMap(32, peakBookmarks());
        LawlessGenerator.generate(map2);
        int count2 = map2._notes.length;
        double[] times2 = Arrays.stream(map2._notes).mapToDouble(n -> n._time).toArray();

        assertThat(count1).isEqualTo(count2);
        assertThat(times1).isEqualTo(times2);
    }

    @Test
    void sourceDiffUntouchedAfterCloneTransform() {
        BeatSaberMap original = makeMap(32, peakBookmarks());
        int originalCount = original._notes.length;
        double[] originalTimes = Arrays.stream(original._notes).mapToLong(n -> (long)(n._time * 1000))
                .mapToDouble(l -> l / 1000.0).toArray();

        // Simulate what createCharacteristicDiff does: work on a clone
        BeatSaberMap clone = new BeatSaberMap(new ArrayList<>(), new Events[0]);
        clone._notes = Arrays.copyOf(original._notes, original._notes.length);
        clone._events = Arrays.copyOf(original._events, original._events.length);
        clone._obstacles = Arrays.copyOf(original._obstacles, original._obstacles.length);
        clone.bookmarks = new ArrayList<>(original.bookmarks);

        LawlessGenerator.generate(clone);

        // original untouched
        assertThat(original._notes).hasSize(originalCount);
        double[] afterTimes = Arrays.stream(original._notes).mapToLong(n -> (long)(n._time * 1000))
                .mapToDouble(l -> l / 1000.0).toArray();
        assertThat(afterTimes).isEqualTo(originalTimes);
        // clone changed
        assertThat(clone._notes.length).isGreaterThanOrEqualTo(originalCount);
    }

    @Test
    void notesRemainTimeSorted() {
        BeatSaberMap map = makeMap(32, peakBookmarks());
        LawlessGenerator.generate(map);

        for (int i = 1; i < map._notes.length; i++) {
            assertThat(map._notes[i]._time)
                    .isGreaterThanOrEqualTo(map._notes[i - 1]._time);
        }
    }
}
