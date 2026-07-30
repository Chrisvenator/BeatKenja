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
 * Unit tests for RotationEventGenerator using a fixed seed for reproducibility.
 */
class RotationEventGeneratorTest {

    private static final long FIXED_SEED = 133742069L;

    @BeforeEach
    void resetSeed() {
        Parameters.RANDOM = new java.util.Random(FIXED_SEED);
        RotationEventGenerator.ENABLE_VISION_BLOCK_CLEANUP = false;
        RotationEventGenerator.ENABLE_TELEGRAPH_WALLS = false;
    }

    /** Builds a minimal BeatSaberMap with evenly-spaced notes across 32 beats. */
    private BeatSaberMap makeMap(int noteCount, List<Bookmark> bookmarks) {
        Note[] notes = new Note[noteCount];
        for (int i = 0; i < noteCount; i++) {
            Note n = new Note();
            n._time = i * (32f / noteCount);
            n._lineIndex = i % 4;
            n._type = i % 2;
            n._cutDirection = 0;
            notes[i] = n;
        }
        BeatSaberMap map = new BeatSaberMap(new ArrayList<>(), new Events[0]);
        map._notes = notes;
        map._events = new Events[0];
        map.bookmarks = bookmarks != null ? bookmarks : new ArrayList<>();
        return map;
    }

    private List<Bookmark> calmBookmarks() {
        return List.of(
                new Bookmark(0f, "linear", null),
                new Bookmark(16f, "linear", null)
        );
    }

    private List<Bookmark> peakBookmarks() {
        return List.of(
                new Bookmark(0f, "normal_jumps", null),
                new Bookmark(8f, "normal_jumps", null),
                new Bookmark(16f, "normal_jumps", null),
                new Bookmark(24f, "normal_jumps", null)
        );
    }

    @Test
    void generatesType15Events() {
        BeatSaberMap map = makeMap(32, calmBookmarks());
        RotationEventGenerator.generate(map, RotationEventGenerator.RotationMode.THREE_SIXTY);

        assertThat(map._events).isNotEmpty();
        for (Events e : map._events) {
            assertThat(e._type).isEqualTo(15);
        }
    }

    @Test
    void ninetyModeNeverExceedsThreeStepsNet() {
        BeatSaberMap map = makeMap(32, peakBookmarks());
        RotationEventGenerator.generate(map, RotationEventGenerator.RotationMode.NINETY);

        int net = 0;
        Events[] sorted = Arrays.copyOf(map._events, map._events.length);
        Arrays.sort(sorted, (a, b) -> Float.compare(a._time, b._time));
        for (Events e : sorted) {
            if (e._type != 15) continue;
            int step = e._value <= 3 ? e._value - 4 : e._value - 3;
            net += step;
            assertThat(Math.abs(net))
                    .as("cumulative net steps at beat %.2f".formatted(e._time))
                    .isLessThanOrEqualTo(3);
        }
    }

    @Test
    void noTwoEventsCloserThanHalfBeat() {
        BeatSaberMap map = makeMap(32, peakBookmarks());
        RotationEventGenerator.generate(map, RotationEventGenerator.RotationMode.THREE_SIXTY);

        Events[] rotations = Arrays.stream(map._events).filter(e -> e._type == 15)
                .sorted((a, b) -> Float.compare(a._time, b._time))
                .toArray(Events[]::new);

        for (int i = 1; i < rotations.length; i++) {
            float gap = rotations[i]._time - rotations[i - 1]._time;
            assertThat(gap).as("gap between event %d and %d".formatted(i - 1, i))
                    .isGreaterThanOrEqualTo(0.5f - 1e-4f);
        }
    }

    @Test
    void deterministicUnderFixedSeed() {
        Parameters.RANDOM = new java.util.Random(FIXED_SEED);
        BeatSaberMap map1 = makeMap(32, calmBookmarks());
        RotationEventGenerator.generate(map1, RotationEventGenerator.RotationMode.THREE_SIXTY);

        Parameters.RANDOM = new java.util.Random(FIXED_SEED);
        BeatSaberMap map2 = makeMap(32, calmBookmarks());
        RotationEventGenerator.generate(map2, RotationEventGenerator.RotationMode.THREE_SIXTY);

        assertThat(map1._events).hasSameSizeAs(map2._events);
        for (int i = 0; i < map1._events.length; i++) {
            assertThat(map1._events[i]._time).isEqualTo(map2._events[i]._time);
            assertThat(map1._events[i]._value).isEqualTo(map2._events[i]._value);
        }
    }

    @Test
    void peakTierYieldsMoreEventsThanCalmTier() {
        Parameters.RANDOM = new java.util.Random(FIXED_SEED);
        BeatSaberMap calmMap = makeMap(32, calmBookmarks());
        RotationEventGenerator.generate(calmMap, RotationEventGenerator.RotationMode.THREE_SIXTY);
        long calmCount = Arrays.stream(calmMap._events).filter(e -> e._type == 15).count();

        Parameters.RANDOM = new java.util.Random(FIXED_SEED);
        BeatSaberMap peakMap = makeMap(32, peakBookmarks());
        RotationEventGenerator.generate(peakMap, RotationEventGenerator.RotationMode.THREE_SIXTY);
        long peakCount = Arrays.stream(peakMap._events).filter(e -> e._type == 15).count();

        assertThat(peakCount).isGreaterThan(calmCount);
    }

    @Test
    void sourceDiffUntouchedAfterCloneTransform() {
        BeatSaberMap original = makeMap(32, calmBookmarks());
        // Simulate what createCharacteristicDiff does: work on a clone
        Note[] origNotes = Arrays.copyOf(original._notes, original._notes.length);
        Events[] origEvents = Arrays.copyOf(original._events, original._events.length);

        BeatSaberMap clone = new BeatSaberMap(new ArrayList<>(), new Events[0]);
        clone._notes = Arrays.copyOf(original._notes, original._notes.length);
        clone._events = Arrays.copyOf(original._events, original._events.length);
        clone.bookmarks = new ArrayList<>(original.bookmarks);

        RotationEventGenerator.generate(clone, RotationEventGenerator.RotationMode.THREE_SIXTY);

        // original unchanged
        assertThat(original._events).isEqualTo(origEvents);
        assertThat(original._notes).isEqualTo(origNotes);
        // clone got new events
        assertThat(clone._events).isNotEmpty();
    }

    @Test
    void stepToValueRoundTrip() {
        // step < 0: step + 4
        assertThat(RotationEventGenerator.stepToValue(-1)).isEqualTo(3);
        assertThat(RotationEventGenerator.stepToValue(-2)).isEqualTo(2);
        assertThat(RotationEventGenerator.stepToValue(-3)).isEqualTo(1);
        assertThat(RotationEventGenerator.stepToValue(-4)).isEqualTo(0);
        // step > 0: step + 3
        assertThat(RotationEventGenerator.stepToValue(1)).isEqualTo(4);
        assertThat(RotationEventGenerator.stepToValue(2)).isEqualTo(5);
        assertThat(RotationEventGenerator.stepToValue(3)).isEqualTo(6);
        assertThat(RotationEventGenerator.stepToValue(4)).isEqualTo(7);
    }
}
