package AppLogic;

import BeatSaberObjects.Objects.Enums.ParityErrorEnum;
import DataManager.Parameters;
import MapGeneration.GenerationElements.HigherOrderPattern;
import MapGeneration.GenerationElements.Pattern;
import MapGeneration.StyleSpace.StyleSpace;
import javafx.util.Pair;

import java.util.ArrayList;
import java.util.List;

import static DataManager.Parameters.logger;

/**
 * UI-independent home for cross-cutting generation state.
 *
 * These values used to live as statics on the Swing UserInterface class, which made the
 * generation core depend on the UI. They are kept static for now because the generation
 * algorithms access them from deep call stacks without dependency injection; a later stage
 * can move them into per-diff sessions.
 */
public class GenerationContext {
    /** The difficulty currently being generated/checked. Used as key into Parameters.PARITY_ERRORS_LIST. */
    public static volatile String currentDiff = "NULL";

    /** Variance applied to a deep-cloned pattern when generating a map. 0 = use pattern as-is. */
    public static volatile int patternVariance = 0;

    /** Fallback pattern for fast sections. May be null if no easy pattern could be loaded. */
    public static Pattern easyPattern;

    /**
     * 2nd-order Markov tables trained from the corpus.
     * Null = higher-order engine not loaded; generation falls back to original 1st-order path.
     */
    public static HigherOrderPattern higherOrderBlue = null;
    public static HigherOrderPattern higherOrderRed  = null;

    /**
     * Style space loaded from baked archetypes. Null = not loaded; falls back to higher-order
     * or 1st-order baseline. Set via StyleSpaceLoader.load() at application startup.
     */
    public static StyleSpace styleSpace = null;

    /**
     * Magnitude of between-section style drift (0 = no drift, 1 = jump to target immediately).
     * Exposed as a user-facing "surprise" control. Default 0.08 = subtle drift.
     */
    public static float styleDriftMagnitude = 0.08f;

    /**
     * Section boundaries in beats and their intensity tiers. Set before generation when
     * a SectionAnalysis is available so ComplexPattern can trigger style drift.
     * Null = no section-driven drift.
     */
    public static float[] sectionBoundaryBeats = null;
    public static int[]   sectionTiers         = null;

    /**
     * Applies style drift if the current beat crosses the next section boundary.
     * Call from the generation loop whenever a new note beat is processed.
     * Thread-safe enough for single-threaded generation.
     *
     * @param currentBeat beat time of the note just placed
     */
    public static void applyStyleDriftIfNeeded(float currentBeat) {
        if (styleSpace == null || sectionBoundaryBeats == null || sectionTiers == null) return;
        for (int s = 0; s < sectionBoundaryBeats.length; s++) {
            if (currentBeat >= sectionBoundaryBeats[s]) {
                int tier = (s + 1 < sectionTiers.length) ? sectionTiers[s + 1] : sectionTiers[s];
                styleSpace.driftForIntensityTier(tier, styleDriftMagnitude);
                // Null out this boundary so it doesn't fire again
                sectionBoundaryBeats[s] = Float.MAX_VALUE;
                logger.debug("Style drift at beat {} — tier {}", currentBeat, tier);
                break;
            }
        }
    }

    /**
     * Parity error list for the diff currently being generated. Creates the list on first
     * access, so headless runs (CLI, tests) that never registered a diff don't NPE.
     */
    public static List<Pair<Float, ParityErrorEnum>> currentParityErrors() {
        return Parameters.PARITY_ERRORS_LIST.computeIfAbsent(currentDiff, k -> new ArrayList<>());
    }

    private GenerationContext() {
    }
}