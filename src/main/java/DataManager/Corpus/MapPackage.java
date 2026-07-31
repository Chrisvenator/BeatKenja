package DataManager.Corpus;

import BeatSaberObjects.Objects.BeatSaberMap;
import BeatSaberObjects.Objects.Note;
import DataManager.Records.MapTag;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * A fully loaded Beat Saber map folder: BPM + all playable difficulties.
 *
 * <p>Skips non-playable characteristics (Lightshow, NoArrows, etc.) and formats that produce
 * empty note arrays (V1/V4 stubs). Computes NPS and reaction time per difficulty so the
 * generation layer can select pattern complexity accordingly:
 * fast maps (high NPS, short reaction time) need simpler patterns than tech/slow maps.
 */
public class MapPackage {

    private static final Logger logger = LogManager.getLogger(MapPackage.class);

    private static final Set<String> SKIP_CHARACTERISTICS =
            Set.of("Lightshow", "NoArrows", "OneSaber", "Lawless");

    /**
     * Speed category derived from NPS.
     * Fast = stream maps, simple patterns required (low reaction time).
     * Tech = slow/complex maps where arm-movement complexity is allowed.
     */
    public enum SpeedCategory { FAST, NORMAL, TECH }

    public record DiffInfo(
        String characteristic,
        String difficulty,
        int difficultyRank,
        double njs,
        double nps,
        double reactionTimeSec,
        SpeedCategory speedCategory,
        /** Tags from PatMetadata (if provided externally); empty when loading raw map folders. */
        List<MapTag> tags,
        BeatSaberMap map
    ) {}

    public final double bpm;
    public final List<DiffInfo> difficulties;

    private MapPackage(double bpm, List<DiffInfo> difficulties) {
        this.bpm = bpm;
        this.difficulties = difficulties;
    }

    /**
     * Loads a map folder: parses Info.dat, then loads each playable difficulty.
     * Difficulties with empty notes (V1/V4 stubs, all-Lightshow maps) are silently skipped.
     */
    public static MapPackage fromFolder(File mapFolder) throws IOException {
        InfoDatLoader.InfoDat info = InfoDatLoader.load(mapFolder);
        List<DiffInfo> diffs = new ArrayList<>();

        for (InfoDatLoader.DifficultyEntry entry : info.difficulties()) {
            if (entry.filename().isEmpty()) continue;
            if (SKIP_CHARACTERISTICS.contains(entry.characteristic())) continue;

            File diffFile = new File(mapFolder, entry.filename());
            BeatSaberMap map = BeatSaberMap.newMapFromJSON(diffFile.getAbsolutePath());
            if (map._notes == null || map._notes.length == 0) continue;

            double nps = computeNps(map._notes, info.bpm());
            double rt = computeReactionTime(entry.njs(), info.bpm(), entry.beatOffset());
            List<MapTag> tags = entry.tags().stream().map(MapTag::fromString).toList();

            diffs.add(new DiffInfo(
                entry.characteristic(), entry.difficulty(), entry.difficultyRank(),
                entry.njs(), nps, rt, categorizeSpeed(nps, tags), tags, map
            ));
        }

        return new MapPackage(info.bpm(), diffs);
    }

    /** NPS over the mapped window (first to last note). */
    private static double computeNps(Note[] notes, double bpm) {
        if (notes.length < 2 || bpm <= 0) return 0.0;
        double durationBeats = notes[notes.length - 1]._time - notes[0]._time;
        if (durationBeats <= 0) return 0.0;
        return notes.length / (durationBeats / bpm * 60.0);
    }

    /**
     * Approximate reaction time using the Beat Saber half-jump duration formula.
     * halfJump starts at 4 beats; halved until NJS * secPerBeat * halfJump fits within 18 m.
     */
    private static double computeReactionTime(double njs, double bpm, double beatOffset) {
        if (bpm <= 0 || njs <= 0) return 0.5;
        double secPerBeat = 60.0 / bpm;
        double halfJump = 4.0;
        while (halfJump > 0.25 && njs * secPerBeat * halfJump >= 18.0) {
            halfJump /= 2.0;
        }
        halfJump = Math.max(halfJump + beatOffset, 0.25);
        return secPerBeat * halfJump;
    }

    /**
     * Categorizes speed using tags first, NPS as fallback.
     * "Speed" tag → FAST (stream-style, simple patterns, short reaction time).
     * "Tech" tag → TECH (complex arm/body movement allowed).
     * NPS heuristic applies only when neither tag is present.
     */
    static SpeedCategory categorizeSpeed(double nps, List<MapTag> tags) {
        if (tags.contains(MapTag.SPEED))    return SpeedCategory.FAST;
        if (tags.contains(MapTag.TECH))     return SpeedCategory.TECH;
        if (nps >= 7.0)                     return SpeedCategory.FAST;
        if (nps >= 3.0)                     return SpeedCategory.NORMAL;
        return SpeedCategory.TECH;
    }
}
