package MapGeneration.CharacteristicGeneration;

import AppLogic.SectionAnalysisService;
import BeatSaberObjects.Objects.BeatSaberMap;
import BeatSaberObjects.Objects.Bookmark;
import BeatSaberObjects.Objects.Events;
import BeatSaberObjects.Objects.Note;
import BeatSaberObjects.Objects.Obstacle;
import DataManager.Parameters;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * Generates musically-aware lane-rotation events (type 14/15) for Beat Saber 360° and 90° maps.
 *
 * <p>Rotation candidates are derived from section boundaries (bookmarks) and subdivided by
 * intensity tier. Direction is chosen by looking ahead at note {@code _lineIndex}: notes
 * clustering right → rotate right, left → rotate left. Magnitude is 1 step (15°) by default,
 * 2 steps (30°) allowed on high-tier section boundaries.
 *
 * <p>NINETY mode clamps the cumulative net rotation to ±3 steps (≈±45°) and biases back toward
 * center when near the edge. THREE_SIXTY mode is unconstrained and schedules gentle return
 * rotations during calm sections.
 *
 * <p>Two optional "deluxe" passes are gated by {@link #ENABLE_VISION_BLOCK_CLEANUP} and
 * {@link #ENABLE_TELEGRAPH_WALLS}:
 * <ul>
 *   <li>Vision-block cleanup: shifts outer-lane notes inward after a rotation.
 *   <li>Telegraph walls: inserts a short {@link Obstacle} before 30° turns.
 * </ul>
 *
 * <p>Randomness comes exclusively from {@link Parameters#RANDOM} so {@code -Dbk.seed} reproduces results.
 */
public class RotationEventGenerator {

    /** 90° mode clamps cumulative net steps to this value (±3 ≈ ±45°). */
    private static final int NINETY_MAX_STEPS = 3;

    /** Minimum beat gap between two emitted rotation events. */
    private static final float MIN_GAP_BEATS = 0.5f;

    /**
     * Notes-per-beat threshold above which rotation is suppressed (too dense to turn safely).
     * Measured over a ±1-beat window around the candidate beat.
     */
    private static final float HIGH_DENSITY_THRESHOLD = 3.0f;

    /** Beat lookahead for note-direction analysis. */
    private static final float LOOKAHEAD_BEATS = 2.0f;

    /** Fallback subdivision interval (beats) when fewer than 2 bookmarks exist. */
    private static final float FALLBACK_INTERVAL_BEATS = 4.0f;

    /** Enable vision-block cleanup (shift outer-lane notes inward after a rotation). */
    public static boolean ENABLE_VISION_BLOCK_CLEANUP = true;

    /** Enable telegraph walls (short Obstacle before 30° turns). */
    public static boolean ENABLE_TELEGRAPH_WALLS = true;

    /** Sparse-rotation intervals per intensity tier (0=calm … 4=peak), in beats. */
    private static final float[] TIER_INTERVALS = {8f, 6f, 4f, 3f, 2f};

    // Beat-360fyer-style step→value mapping:
    //   step < 0 : step + 4   (e.g. -1→3, -2→2, -3→1, -4→0)
    //   step > 0 : step + 3   (e.g. +1→4, +2→5, +3→6, +4→7)
    // No 0° value exists in Beat Saber v2.

    public enum RotationMode { NINETY, THREE_SIXTY }

    /**
     * Generates lane-rotation events on the given (already-cloned) map, synced
     * to sections/beats and note position. Merges the new events with the map's
     * existing {@code _events} array and sorts by {@code _time}.
     *
     * @param map  the cloned map to modify in place
     * @param mode NINETY (clamped ±3 steps) or THREE_SIXTY (unbounded)
     */
    public static void generate(BeatSaberMap map, RotationMode mode) {
        if (map._notes == null || map._notes.length == 0) return;

        List<Note> notes = Arrays.asList(map._notes);
        notes.sort(Comparator.comparingDouble(n -> n._time));
        float mapEnd = notes.get(notes.size() - 1)._time;

        List<SectionBeat> candidates = buildCandidates(map.bookmarks, mapEnd);

        List<Events> rotations = new ArrayList<>();
        int netSteps = 0; // cumulative net for NINETY clamping / THREE_SIXTY return scheduling
        float lastEmitted = Float.NEGATIVE_INFINITY;

        for (SectionBeat cb : candidates) {
            // Minimum gap guard
            if (cb.beat - lastEmitted < MIN_GAP_BEATS) continue;

            // Density guard
            if (localDensity(notes, cb.beat) > HIGH_DENSITY_THRESHOLD) continue;

            int step = chooseStep(notes, cb.beat, netSteps, mode, cb.tier, cb.isSectionBoundary);
            if (step == 0) continue;

            netSteps += step;
            lastEmitted = cb.beat;
            int value = stepToValue(step);
            rotations.add(new Events(cb.beat, 15, value));

            // THREE_SIXTY: schedule a gentle return rotation during the next calm beat
            if (mode == RotationMode.THREE_SIXTY && cb.isSectionBoundary && cb.tier == 0 && netSteps != 0) {
                float returnBeat = cb.beat + 2f;
                if (returnBeat <= mapEnd) {
                    int returnStep = netSteps > 0 ? -1 : 1;
                    netSteps += returnStep;
                    rotations.add(new Events(returnBeat, 15, stepToValue(returnStep)));
                    lastEmitted = returnBeat;
                }
            }

            // Deluxe: vision-block cleanup
            if (ENABLE_VISION_BLOCK_CLEANUP) {
                applyVisionBlockCleanup(notes, cb.beat, step);
            }

            // Deluxe: telegraph walls before 30° (2-step) turns
            if (ENABLE_TELEGRAPH_WALLS && Math.abs(step) >= 2) {
                float wallBeat = cb.beat - 0.25f;
                if (wallBeat >= 0 && !noteNear(notes, wallBeat, 0.15f)) {
                    String lane = step > 0 ? "3" : "0"; // turn right → wall on right lane
                    map._obstacles = appendObstacle(map._obstacles,
                            new Obstacle(wallBeat, lane, 0, 0.25f, 1f));
                }
            }
        }

        // Merge with existing events, sort by _time
        List<Events> merged = new ArrayList<>(Arrays.asList(map._events));
        merged.addAll(rotations);
        merged.sort(Comparator.comparingDouble(e -> e._time));
        map._events = merged.toArray(new Events[0]);
    }

    // ─── Candidate building ───────────────────────────────────────────────────

    /** A rotation candidate beat with its associated intensity tier and boundary flag. */
    private record SectionBeat(float beat, int tier, boolean isSectionBoundary) {}

    /**
     * Builds the list of candidate rotation beats from bookmarks. Section starts are always
     * included; long sections are subdivided at an interval scaled by intensity tier.
     * Falls back to a fixed 4-beat grid if fewer than 2 bookmarks exist.
     */
    private static List<SectionBeat> buildCandidates(List<Bookmark> bookmarks, float mapEnd) {
        List<SectionBeat> result = new ArrayList<>();

        if (bookmarks == null || bookmarks.size() < 2) {
            // Fallback: fixed grid
            for (float b = 0; b <= mapEnd; b += FALLBACK_INTERVAL_BEATS) {
                result.add(new SectionBeat(b, 2, b == 0));
            }
            return result;
        }

        List<Bookmark> sorted = new ArrayList<>(bookmarks);
        sorted.sort(Comparator.comparingDouble(bm -> bm._time));

        for (int i = 0; i < sorted.size(); i++) {
            Bookmark bm = sorted.get(i);
            int tier = parseTier(bm._name);
            float sectionEnd = (i + 1 < sorted.size()) ? sorted.get(i + 1)._time : mapEnd;

            // Section boundary itself
            result.add(new SectionBeat(bm._time, tier, true));

            // Subdivisions inside the section
            float interval = TIER_INTERVALS[tier];
            for (float sub = bm._time + interval; sub < sectionEnd - MIN_GAP_BEATS; sub += interval) {
                result.add(new SectionBeat(sub, tier, false));
            }
        }

        result.sort(Comparator.comparingDouble(c -> c.beat));
        return result;
    }

    /** Maps a bookmark name to an intensity tier 0..4 using SectionAnalysisService.TIER_FLAGS. */
    private static int parseTier(String name) {
        if (name == null) return 2;
        String lower = name.toLowerCase();
        String[] flags = SectionAnalysisService.TIER_FLAGS;
        for (int i = flags.length - 1; i >= 0; i--) {
            if (lower.contains(flags[i].toLowerCase())) return i;
        }
        return 2; // neutral
    }

    // ─── Direction / magnitude ────────────────────────────────────────────────

    /**
     * Chooses a signed step value for the rotation at {@code beat}.
     * Positive = right, negative = left.
     * Returns 0 if no rotation should be emitted (e.g. NINETY at hard edge with nowhere to go).
     */
    private static int chooseStep(List<Note> notes, float beat, int netSteps,
                                   RotationMode mode, int tier, boolean isBoundary) {
        // Preferred direction from note look-ahead
        int preferred = preferredDirection(notes, beat);

        // NINETY: clamp and bias toward center
        if (mode == RotationMode.NINETY) {
            if (netSteps >= NINETY_MAX_STEPS) preferred = -1; // must go left
            else if (netSteps <= -NINETY_MAX_STEPS) preferred = 1; // must go right
            else if (netSteps > 1) preferred = -1;             // bias toward center
            else if (netSteps < -1) preferred = 1;
        }

        // Magnitude: 2 steps only on high/peak boundary
        int magnitude = (isBoundary && tier >= 3) ? 2 : 1;

        // NINETY: guard against exceeding clamp with 2-step
        if (mode == RotationMode.NINETY) {
            if (Math.abs(netSteps + preferred * magnitude) > NINETY_MAX_STEPS) {
                magnitude = 1;
            }
            if (Math.abs(netSteps + preferred * magnitude) > NINETY_MAX_STEPS) {
                return 0; // no valid move
            }
        }

        return preferred * magnitude;
    }

    /**
     * Returns +1 (right) if notes in [beat, beat+lookahead) cluster toward the right lanes,
     * -1 (left) if they cluster left. Uses Parameters.RANDOM for tie-breaks.
     */
    private static int preferredDirection(List<Note> notes, float beat) {
        double sum = 0;
        int count = 0;
        for (Note n : notes) {
            if (n._time >= beat && n._time < beat + LOOKAHEAD_BEATS) {
                sum += n._lineIndex;
                count++;
            }
        }
        if (count == 0) return Parameters.RANDOM.nextBoolean() ? 1 : -1;
        double mean = sum / count;
        if (mean > 1.5) return 1;
        if (mean < 1.5) return -1;
        return Parameters.RANDOM.nextBoolean() ? 1 : -1; // tie-break
    }

    /**
     * Converts a signed step to a Beat Saber rotation event value (0..7).
     * Beat-360fyer mapping: step<0 → step+4; step>0 → step+3.
     * (Values: 0=−60°, 1=−45°, 2=−30°, 3=−15°, 4=+15°, 5=+30°, 6=+45°, 7=+60°)
     */
    static int stepToValue(int step) {
        return step < 0 ? step + 4 : step + 3;
    }

    // ─── Density ─────────────────────────────────────────────────────────────

    /** Notes per beat in a ±1-beat window around {@code beat}. */
    private static float localDensity(List<Note> notes, float beat) {
        int count = 0;
        for (Note n : notes) {
            if (n._time >= beat - 1f && n._time <= beat + 1f) count++;
        }
        return count / 2f; // notes per beat over 2-beat window
    }

    // ─── Deluxe helpers ───────────────────────────────────────────────────────

    /**
     * Shifts outer-lane notes inward after a rotation.
     * Right turn: notes at _lineIndex==0 (far left) → 1. Left turn: index==3 → 2.
     */
    private static void applyVisionBlockCleanup(List<Note> notes, float beat, int step) {
        for (Note n : notes) {
            if (n._time < beat || n._time >= beat + LOOKAHEAD_BEATS) continue;
            if (step > 0 && n._lineIndex == 0) n._lineIndex = 1;
            else if (step < 0 && n._lineIndex == 3) n._lineIndex = 2;
        }
    }

    /** Returns true if any note exists within {@code radius} beats of {@code beat}. */
    private static boolean noteNear(List<Note> notes, float beat, float radius) {
        for (Note n : notes) {
            if (Math.abs(n._time - beat) < radius) return true;
        }
        return false;
    }

    private static Obstacle[] appendObstacle(Obstacle[] existing, Obstacle o) {
        Obstacle[] result = Arrays.copyOf(existing, existing.length + 1);
        result[existing.length] = o;
        return result;
    }
}
