package AppLogic;

import BeatSaberObjects.Objects.BeatSaberMap;
import BeatSaberObjects.Objects.Events;
import BeatSaberObjects.Objects.Note;
import DataManager.Parameters;
import MapGeneration.GenerationElements.Pattern;
import MapGeneration.PatternGeneration.CommonMethods.FixSwingTimings;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static DataManager.Parameters.logger;
import static MapGeneration.ComplexPattern.complexPattern;
import static MapGeneration.CreateMap.createMap;
import static MapGeneration.PatternGeneration.LinearSlowPattern.linearSlowPattern;
import static MapGeneration.PatternGeneration.RandomPattern.createRandomPattern;
import static MapGeneration.PatternGeneration.RandomV2FromTemplate.randomV2FromTemplate;

/**
 * Maps GeneratorType to the actual generation-core calls. UI-independent; the logic is
 * lifted 1:1 from the old Swing creator buttons so results stay identical.
 * Callers must set GenerationContext.currentDiff and patternVariance before calling
 * (the AppController's generate orchestration does this per diff).
 */
public final class GenerationService {
    private static final int LINEAR_TIMEOUT_SECONDS = 5;

    private GenerationService() {
    }

    /**
     * Generates a new map from the given source diff.
     *
     * @param type      which generator to run
     * @param source    the loaded diff; it is converted to timing notes in place, exactly like the old UI did
     * @param pattern   the pattern to use (variance already applied via Pattern.adjustVariance)
     * @param oneHanded one-handed variant (only meaningful for LINEAR and COMPLEX)
     * @return the generated map
     * @throws Exception when the generator times out or fails to compute
     */
    public static BeatSaberMap generate(GeneratorType type, BeatSaberMap source, Pattern pattern, boolean oneHanded) throws Exception {
        return switch (type) {
            case LINEAR -> runWithTimeout(
                    () -> {
                        source.toBlueLeftBottomRowDotTimings();
                        return new BeatSaberMap(linearSlowPattern(List.of(source._notes), oneHanded, null, null));
                    },
                    LINEAR_TIMEOUT_SECONDS, TimeUnit.SECONDS);

            case COMPLEX -> oneHanded
                    ? generateOneHandedComplex(source, pattern)
                    : generateComplex(source, pattern);

            case SECTIONED -> {
                source.toBlueLeftBottomRowDotTimings();
                BeatSaberMap map = createMap(source, pattern, false, false);
                if (map.equals(source)) throw new IllegalStateException("Map didn't compute — result is identical to the input");
                yield map;
            }

            case STYLE_AWARE -> {
                MapGeneration.StyleSpace.StyleSpace ss = GenerationContext.styleSpace;
                if (ss == null || ss.getArchetypes().isEmpty()) {
                    throw new IllegalStateException("Style model not loaded — run StyleSpaceTrainer first to produce style_archetypes.ser");
                }
                // Pick a fresh random start coordinate so each generation run has its own identity
                ss.setCoordinate(ss.randomCoordinateNear(
                        Parameters.RANDOM.nextInt(ss.getArchetypes().size()),
                        0.15f, Parameters.RANDOM));
                yield generateComplex(source, pattern);
            }

            case RANDOM -> {
                source.toBlueLeftBottomRowDotTimings();
                yield new BeatSaberMap(createRandomPattern(source._notes, false));
            }

            case RANDOM_V2 -> {
                source.toBlueLeftBottomRowDotTimings();
                yield new BeatSaberMap(randomV2FromTemplate(source._notes, pattern, false, null, null));
            }
        };
    }

    /**
     * Complex generation with the timing-map/template branch from the old CreateComplexMap button:
     * mostly-dot maps are treated as timings; otherwise red/blue positions of the template are reused.
     * BPM-change events survive because bpm changes are stored as events.
     */
    private static BeatSaberMap generateComplex(BeatSaberMap source, Pattern pattern) {
        List<Note> notes = new ArrayList<>();
        List<Note> timings = new ArrayList<>(Parameters.FIX_INCONSISTENT_TIMINGS
                ? FixSwingTimings.fixFastMapTimings(List.of(source._notes))
                : List.of(source._notes));

        boolean isTimingMap = (double) Arrays.stream(source._notes).filter(note -> note._cutDirection == 8).count() / source._notes.length >= 0.8;
        if (isTimingMap) {
            logger.info("Timing Map found. Creating complex map from Pattern...");
            source.toBlueLeftBottomRowDotTimings();
            notes.addAll(complexPattern(timings, pattern, GenerationContext.easyPattern, true, false, false, false, null, null));
        } else {
            logger.info("Map Template found. Creating new map with the position of red & blue notes...");
            notes.addAll(complexPattern(timings.stream().filter(note -> note._type == 1).toList(), pattern, GenerationContext.easyPattern, true, true, false, false, null, null));
            notes.addAll(complexPattern(
                    timings.stream().filter(note -> note._type == 0).toList(), pattern, GenerationContext.easyPattern, true, true, false, false, null, null
            ).stream().peek(Note::invertNote).toList());
        }

        BeatSaberMap map = new BeatSaberMap(notes);
        map._events = Arrays.stream(source._events).filter(event -> event._type == 100 || event._type == 1000 || event._type == 10000).toArray(Events[]::new);
        return map;
    }

    private static BeatSaberMap generateOneHandedComplex(BeatSaberMap source, Pattern pattern) {
        source.toBlueLeftBottomRowDotTimings();
        return new BeatSaberMap(complexPattern(List.of(source._notes), pattern, GenerationContext.easyPattern, true, true, false, false, null, null));
    }

    /** Runs a generator with a timeout so endless loops can't freeze the app. (Formerly MapCreatorSubButton.runWithTimeout.) */
    public static <T> T runWithTimeout(Callable<T> callable, long timeout, TimeUnit unit) throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<T> future = executor.submit(callable);

        try {
            return future.get(timeout, unit);
        } catch (TimeoutException e) {
            future.cancel(true);
            throw e;
        } catch (ExecutionException e) {
            throw new Exception(e.getCause());
        } finally {
            executor.shutdownNow();
        }
    }
}