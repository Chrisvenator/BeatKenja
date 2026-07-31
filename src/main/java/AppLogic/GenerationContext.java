package AppLogic;

import BeatSaberObjects.Objects.Enums.ParityErrorEnum;
import DataManager.Parameters;
import MapGeneration.GenerationElements.HigherOrderPattern;
import MapGeneration.GenerationElements.Pattern;
import javafx.util.Pair;

import java.util.ArrayList;
import java.util.List;

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
     * Parity error list for the diff currently being generated. Creates the list on first
     * access, so headless runs (CLI, tests) that never registered a diff don't NPE.
     */
    public static List<Pair<Float, ParityErrorEnum>> currentParityErrors() {
        return Parameters.PARITY_ERRORS_LIST.computeIfAbsent(currentDiff, k -> new ArrayList<>());
    }

    private GenerationContext() {
    }
}