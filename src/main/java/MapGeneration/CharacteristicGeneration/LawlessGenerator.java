package MapGeneration.CharacteristicGeneration;

import AppLogic.SectionAnalysisService;
import BeatSaberObjects.Objects.BeatSaberMap;
import BeatSaberObjects.Objects.Bookmark;
import BeatSaberObjects.Objects.Note;
import BeatSaberObjects.Objects.Obstacle;
import DataManager.Parameters;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * Generates a Lawless characteristic map: a musically-aware, intensity-scaled transformation
 * that deliberately violates Standard parity/flow constraints to produce a spicy-but-intentional
 * playing experience.
 *
 * <p>Chaos is proportional to section intensity (tier 0=calm … 4=peak):
 * <ul>
 *   <li>Stacks/doubles: intense/peak sections get same-color stacks or same-beat doubles.
 *   <li>Ghost inserts: peak sections gain extra notes at ½-beat offsets.
 *   <li>Walls: build/intense sections receive sparse short vision-blocking Obstacles.
 *   <li>Same-direction repeats: the DD/parity filter is bypassed — all existing notes kept.
 * </ul>
 *
 * <p>If the map has no notes, the method returns immediately without modifying anything.
 * Randomness comes exclusively from {@link Parameters#RANDOM} so {@code -Dbk.seed} reproduces results.
 */
public class LawlessGenerator {

    /** Minimum beat gap between a ghost note and any existing note. */
    private static final float GHOST_MIN_GAP = 0.25f;

    /** Minimum gap between two obstacles. */
    private static final float WALL_MIN_GAP = 2f;

    /** Tiers at which stacking begins (tier >= STACK_TIER). */
    private static final int STACK_TIER = 2;

    /** Tiers at which ghost inserts begin (tier >= GHOST_TIER). */
    private static final int GHOST_TIER = 4;

    /** Tiers at which walls are spawned (tier >= WALL_TIER). */
    private static final int WALL_TIER = 2;

    /**
     * Applies the Lawless transform to {@code map} in place.
     * Adds stacks, ghost notes, and obstacle chaos scaled per section intensity.
     * Leaves the map unchanged if it has no notes.
     *
     * @param map the (already-cloned) map to transform
     */
    public static void generate(BeatSaberMap map) {
        if (map._notes == null || map._notes.length == 0) return;

        List<Note> notes = new ArrayList<>(Arrays.asList(map._notes));
        notes.sort(Comparator.comparingDouble(n -> n._time));
        float mapEnd = notes.get(notes.size() - 1)._time;

        List<SectionWindow> sections = buildSections(map.bookmarks, mapEnd);

        List<Note> extras = new ArrayList<>();
        List<Obstacle> extraWalls = new ArrayList<>();

        for (SectionWindow sec : sections) {
            List<Note> sectionNotes = notesInWindow(notes, sec.start, sec.end);
            if (sectionNotes.isEmpty()) continue;

            // Stacks/doubles: tier >= 2
            if (sec.tier >= STACK_TIER) {
                addStacks(sectionNotes, sec.tier, extras);
            }

            // Ghost inserts: tier == 4 (peak only)
            if (sec.tier >= GHOST_TIER) {
                addGhosts(sectionNotes, notes, extras);
            }

            // Walls: tier >= 2
            if (sec.tier >= WALL_TIER) {
                addWalls(sec, sectionNotes, extraWalls);
            }
        }

        // Merge and sort notes
        notes.addAll(extras);
        notes.sort(Comparator.comparingDouble(n -> n._time));
        map._notes = notes.toArray(new Note[0]);

        // Merge obstacles (existing first, new walls appended in generation order)
        List<Obstacle> allWalls = new ArrayList<>(Arrays.asList(map._obstacles));
        allWalls.addAll(extraWalls);
        map._obstacles = allWalls.toArray(new Obstacle[0]);
    }

    // ─── Section building ─────────────────────────────────────────────────────

    private record SectionWindow(float start, float end, int tier) {}

    private static List<SectionWindow> buildSections(List<Bookmark> bookmarks, float mapEnd) {
        List<SectionWindow> result = new ArrayList<>();
        if (bookmarks == null || bookmarks.size() < 2) {
            result.add(new SectionWindow(0f, mapEnd, 2)); // mild chaos if no bookmarks
            return result;
        }
        List<Bookmark> sorted = new ArrayList<>(bookmarks);
        sorted.sort(Comparator.comparingDouble(b -> b._time));
        for (int i = 0; i < sorted.size(); i++) {
            float start = sorted.get(i)._time;
            float end = (i + 1 < sorted.size()) ? sorted.get(i + 1)._time : mapEnd;
            result.add(new SectionWindow(start, end, parseTier(sorted.get(i)._name)));
        }
        return result;
    }

    private static int parseTier(String name) {
        if (name == null) return 2;
        String lower = name.toLowerCase();
        String[] flags = SectionAnalysisService.TIER_FLAGS;
        for (int i = flags.length - 1; i >= 0; i--) {
            if (lower.contains(flags[i].toLowerCase())) return i;
        }
        return 2;
    }

    private static List<Note> notesInWindow(List<Note> notes, float start, float end) {
        List<Note> result = new ArrayList<>();
        for (Note n : notes) {
            if (n._time >= start && n._time < end) result.add(n);
        }
        return result;
    }

    // ─── Chaos passes ─────────────────────────────────────────────────────────

    /**
     * Promotes a tier-scaled fraction of notes to stacks: adds a same-time note of the same
     * color one layer above (or at the same layer if already at top), at a nearby lane.
     * Tier 2 → ~15%, 3 → ~30%, 4 → ~50% of notes get a stack partner.
     */
    private static void addStacks(List<Note> sectionNotes, int tier, List<Note> out) {
        // Probability per note of growing a stack
        double prob = switch (tier) {
            case 2 -> 0.15;
            case 3 -> 0.30;
            default -> 0.50; // tier 4
        };

        for (Note n : sectionNotes) {
            if (Parameters.RANDOM.nextDouble() > prob) continue;

            Note stack = new Note();
            stack._time = n._time;
            stack._type = n._type; // same color = parity-breaking stack
            // Mirror lane: place on the opposite side within the same half
            stack._lineIndex = (n._lineIndex <= 1) ? n._lineIndex + 1 : n._lineIndex - 1;
            stack._lineLayer = Math.min(n._lineLayer + 1, 2);
            stack._cutDirection = n._cutDirection; // same cut = explicit parity break
            out.add(stack);
        }
    }

    /**
     * Inserts ghost notes at ½-beat offsets between adjacent existing notes.
     * Only inserted where there is enough space (gap > GHOST_MIN_GAP * 2) and
     * the half-beat position is not already occupied.
     */
    private static void addGhosts(List<Note> sectionNotes, List<Note> allNotes, List<Note> out) {
        for (int i = 0; i + 1 < sectionNotes.size(); i++) {
            Note a = sectionNotes.get(i);
            Note b = sectionNotes.get(i + 1);
            float gap = b._time - a._time;
            if (gap < GHOST_MIN_GAP * 2) continue;

            float ghostTime = a._time + gap / 2f;
            if (noteNear(allNotes, ghostTime, GHOST_MIN_GAP)) continue;
            if (noteNear(out, ghostTime, GHOST_MIN_GAP)) continue;

            // Only 50% chance even in peak to keep it musical
            if (Parameters.RANDOM.nextBoolean()) continue;

            Note ghost = new Note();
            ghost._time = ghostTime;
            // Alternate color for visual contrast
            ghost._type = 1 - a._type;
            ghost._lineIndex = 1 + Parameters.RANDOM.nextInt(2); // lanes 1–2 (readable)
            ghost._lineLayer = Parameters.RANDOM.nextInt(2);
            ghost._cutDirection = 8; // dot = no forced swing direction
            out.add(ghost);
        }
    }

    /**
     * Adds sparse short vision-blocking Obstacles during build/intense sections.
     * Walls are placed no more than once per {@link #WALL_MIN_GAP} beats and only
     * where no note exists in the same lane at that time.
     *
     * <p>Tier 2 → one attempt every 8 beats, tier 3 → 4 beats, tier 4 → 2 beats.
     */
    private static void addWalls(SectionWindow sec, List<Note> sectionNotes, List<Obstacle> out) {
        float interval = switch (sec.tier) {
            case 2 -> 8f;
            case 3 -> 4f;
            default -> 2f; // tier 4
        };

        float lastWall = Float.NEGATIVE_INFINITY;
        for (float t = sec.start; t < sec.end; t += interval) {
            if (t - lastWall < WALL_MIN_GAP) continue;

            final float beatTime = t;

            // Pick a side lane (0 or 3) to avoid blocking the playfield center
            boolean useRightLane = Parameters.RANDOM.nextBoolean();
            String lane = useRightLane ? "3" : "0";
            final double laneIdx = useRightLane ? 3.0 : 0.0;

            // Don't place if a note is in the same lane within ±0.5 beats
            boolean noteConflict = sectionNotes.stream().anyMatch(
                    n -> Math.abs(n._time - beatTime) < 0.5f && Math.abs(n._lineIndex - laneIdx) < 0.5);
            if (noteConflict) continue;

            out.add(new Obstacle(beatTime, lane, 0, 0.5f, 1f));
            lastWall = t;
        }
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────

    private static boolean noteNear(List<Note> notes, float time, float radius) {
        for (Note n : notes) {
            if (Math.abs(n._time - time) < radius) return true;
        }
        return false;
    }
}
