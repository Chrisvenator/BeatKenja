package AppLogic;

import AppLogic.SectionAnalysisService;
import BeatSaberObjects.Objects.BeatSaberMap;
import BeatSaberObjects.Objects.Bookmark;
import BeatSaberObjects.Objects.Enums.BeatmapCharacteristic;
import BeatSaberObjects.Objects.Enums.ParityErrorEnum;
import BeatSaberObjects.Objects.Note;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import DataManager.FileManager;
import DataManager.Parameters;
import MapGeneration.CharacteristicGeneration.LawlessGenerator;
import MapGeneration.StyleSpace.StyleSpace;
import MapGeneration.CharacteristicGeneration.RotationEventGenerator;
import MapGeneration.GenerationElements.Pattern;
import MapGeneration.PatternGeneration.CommonMethods.CheckParity;
import MapGeneration.PatternGeneration.CommonMethods.Parser;
import javafx.util.Pair;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static DataManager.Parameters.DEFAULT_PATTERN_METADATA;
import static DataManager.Parameters.MAP_FILE_FORMAT;
import static DataManager.Parameters.PARITY_ERRORS_COLORS_MAP;
import static DataManager.Parameters.PARITY_ERRORS_LIST;
import static DataManager.Parameters.SAVE_PARITY_ERRORS_AS_BOOKMARKS_WILL_OVERWRITE_BOOKMARKS;
import static DataManager.Parameters.logger;

/**
 * UI-independent application service: owns the loaded map session and implements the
 * operations that were previously buried in Swing button classes (load, accept generated
 * maps, parity bookkeeping, save with backup).
 *
 * State changes are published to listeners instead of being polled, so any frontend
 * (Swing today, JavaFX later, CLI) can react without knowing about the others.
 * Listeners are called on the thread that triggered the change; UI implementations
 * must marshal to their own UI thread themselves.
 */
public class AppController {

    public interface Listener {
        default void onStateChanged(AppState state) {}

        default void onBpmChanged(double bpm) {}

        default void onActiveDiffChanged(DiffSession activeDiff) {}
    }

    private final MapSession session = new MapSession();
    private final List<Listener> listeners = new CopyOnWriteArrayList<>();
    private AppState state = AppState.EMPTY;
    private DiffSession activeDiff;

    public MapSession session() {
        return session;
    }

    public AppState state() {
        return state;
    }

    public List<BeatSaberMap> maps() {
        return session.maps();
    }

    public Pattern getPattern() {
        return session.getPattern();
    }

    public void setPattern(Pattern pattern) {
        session.setPattern(pattern);
    }

    public void addListener(Listener listener) {
        listeners.add(listener);
    }

    private void setState(AppState newState) {
        state = newState;
        listeners.forEach(l -> l.onStateChanged(newState));
    }

    /**
     * Loads one difficulty file or every difficulty in a folder into a fresh session.
     * Also extracts the global BPM from the map's info.dat if present.
     *
     * @param path a .dat/.json difficulty file or a map folder
     * @return the names of the loaded difficulty files
     * @throws IOException if the path yields no valid difficulty
     */
    public List<String> loadMapFileOrFolder(File path) throws IOException {
        session.maps().clear();
        PARITY_ERRORS_LIST.clear();
        setState(AppState.EMPTY);

        List<String> loaded = new ArrayList<>();
        if (path.isDirectory()) {
            File[] files = path.listFiles(MAP_FILE_FORMAT);
            if (files == null || files.length == 0) throw new IOException("Could not find valid difficulty files in folder: " + path.getAbsolutePath());

            session.setMapFolderPath(path.getAbsolutePath());
            for (File f : files) loaded.add(loadSingleDiff(f));
        } else {
            if (path.getName().equalsIgnoreCase("info.dat") || !path.getName().contains(".dat")) throw new IOException("Wrong file type: " + path.getName());

            session.setMapFolderPath(path.getParent());
            loaded.add(loadSingleDiff(path));
        }

        extractAndPublishBpm(new File(session.getMapFolderPath()));
        setActiveDiff(session.diffs().isEmpty() ? null : session.diffs().get(0));
        setState(AppState.LOADED);
        return loaded;
    }

    public DiffSession getActiveDiff() {
        return activeDiff;
    }

    /** Selects the diff the step views operate on and notifies listeners (tab switching). */
    public void setActiveDiff(DiffSession diff) {
        this.activeDiff = diff;
        listeners.forEach(l -> l.onActiveDiffChanged(diff));
    }

    public void setActiveDiff(String difficultyFileName) {
        session.diffs().stream()
                .filter(d -> d.difficultyFileName().equals(difficultyFileName))
                .findFirst()
                .ifPresent(this::setActiveDiff);
    }

    private String loadSingleDiff(File diffFile) {
        BeatSaberMap map = BeatSaberMap.newMapFromJSON(diffFile.getAbsolutePath());
        session.maps().add(map);
        PARITY_ERRORS_LIST.put(diffFile.getName(), new ArrayList<>());
        logger.info("Successfully loaded: {}/{}", diffFile.getParent(), diffFile.getName());
        return diffFile.getName();
    }

    /**
     * Adds a single difficulty file to the current session without clearing existing diffs.
     * If a diff with the same filename is already loaded, it is replaced only when
     * {@code overwrite} is true; otherwise the method returns without changing state.
     *
     * @return true if the diff was added/replaced, false if collision and overwrite=false
     */
    public boolean addDiffToSession(File diffFile, boolean overwrite) throws java.io.IOException {
        String name = diffFile.getName();
        boolean collision = session.diffs().stream().anyMatch(d -> d.difficultyFileName().equals(name));
        if (collision) {
            if (!overwrite) return false;
            session.diffs().removeIf(d -> d.difficultyFileName().equals(name));
            PARITY_ERRORS_LIST.remove(name);
        }
        session.setMapFolderPath(diffFile.getParent());
        loadSingleDiff(diffFile);
        extractAndPublishBpm(new File(session.getMapFolderPath()));
        DiffSession added = session.diffs().stream()
                .filter(d -> d.difficultyFileName().equals(name))
                .findFirst().orElseThrow();
        setActiveDiff(added);
        setState(AppState.LOADED);
        return true;
    }

    /**
     * Reads "_beatsPerMinute" from the info.dat inside the map folder and publishes it.
     * Missing info.dat is fine — the previously known BPM stays.
     */
    private void extractAndPublishBpm(File mapFolder) {
        File info = new File(mapFolder, "info.dat");
        if (!info.exists() || !info.isFile() || !info.canRead()) return;

        String searchString = "\"_beatsPerMinute\": ";
        for (String line : FileManager.readFile(info.getAbsolutePath())) {
            if (line.contains(searchString)) {
                double bpm = Parser.parseValue(
                        line.substring(line.indexOf(searchString) + searchString.length(), line.lastIndexOf(",")),
                        "bpm according to info.dat",
                        Double::parseDouble,
                        Parameters.BPM
                );
                session.setBpm(bpm);
                listeners.forEach(l -> l.onBpmChanged(bpm));
            }
        }
    }

    /**
     * Removes a single difficulty from the session. Falls back to a full unload when it
     * was the last one; otherwise re-fires the current state so views refresh.
     */
    public void unloadDiff(String difficultyFileName) {
        session.diffs().removeIf(diff -> diff.difficultyFileName().equals(difficultyFileName));
        PARITY_ERRORS_LIST.remove(difficultyFileName);
        logger.info("Unloaded difficulty: {}", difficultyFileName);

        if (session.diffs().isEmpty()) {
            unload();
        } else {
            if (activeDiff != null && activeDiff.difficultyFileName().equals(difficultyFileName)) setActiveDiff(session.diffs().get(0));
            setState(state);
        }
    }

    /** Discards the loaded map (diffs, parity errors, folder path) and returns to EMPTY. The loaded pattern is kept. */
    public void unload() {
        session.maps().clear();
        session.setMapFolderPath(null);
        PARITY_ERRORS_LIST.clear();
        GenerationContext.currentDiff = "NULL";
        setActiveDiff((DiffSession) null);
        logger.info("Map unloaded");
        setState(AppState.EMPTY);
    }

    /**
     * Prepares the session for a generation run: clears old parity errors, falls back to
     * the default pattern if none was loaded, and resets the current-diff marker.
     * (Formerly UserInterface.manageMap.)
     */
    public void prepareGeneration() {
        PARITY_ERRORS_LIST.keySet().forEach(k -> PARITY_ERRORS_LIST.get(k).clear());
        if (session.getPattern() == null) {
            logger.info("Patterns have not been specified. Proceeding with default patterns");
            session.setPattern(new Pattern(DEFAULT_PATTERN_METADATA));
        }
        GenerationContext.currentDiff = "NULL";
    }

    /**
     * Replaces the session's maps with freshly generated ones, preserving the original
     * JSON and difficulty file names, then runs the parity check per diff.
     * (Formerly MySubButton.loadNewlyCreatedMaps.)
     */
    public void acceptGeneratedMaps(List<BeatSaberMap> newMaps) {
        for (int i = 0; i < newMaps.size(); i++) {
            acceptGeneratedMap(session.diffs().get(i), newMaps.get(i));
        }

        GenerationContext.currentDiff = "NULL";
        if (!newMaps.isEmpty()) setState(AppState.GENERATED);
    }

    /** Single-diff version of {@link #acceptGeneratedMaps(List)}: swaps the map inside the DiffSession and checks parity. */
    private void acceptGeneratedMap(DiffSession diff, BeatSaberMap generated) {
        BeatSaberMap old = diff.map();
        if (old.equals(generated) || new HashSet<>(Arrays.stream(old._notes).toList()).containsAll(Arrays.stream(generated._notes).toList())) {
            logger.error("Map couldn't be loaded!");
            System.err.println("Map couldn't be loaded!");
        }

        logger.info("Checking map: {}", diff.difficultyFileName());

        generated.originalJSON = old.originalJSON;
        generated.difficultyFileName = diff.difficultyFileName();
        generated.bookmarks = generated.calculateBookmarks();
        diff.setMap(generated);
        checkMap(generated);

        if (Parameters.SAVE_PARITY_ERRORS_AS_BOOKMARKS) {
            // Two statements on purpose: parityErrorsAsBookmarks may *replace* the map's
            // bookmarks list (overwrite flag), so the addAll target must be resolved afterwards.
            List<Bookmark> parityBookmarks = parityErrorsAsBookmarks(diff.difficultyFileName());
            generated.bookmarks.addAll(parityBookmarks);
        }

        logger.info("Newly created map loaded!\n\n");
    }

    /**
     * Runs a generator for the given diffs (per-diff tabs: active diff or all).
     * Applies each diff's own pattern variance, feeds parity bookkeeping per diff, and
     * fires GENERATED once when at least one diff succeeded.
     *
     * Synchronous — UI callers should run it in a background task and marshal updates.
     *
     * @return per-diff error messages (empty when everything worked)
     */
    public List<String> generateFor(GeneratorType type, boolean oneHanded, List<DiffSession> targets) {
        prepareGeneration();
        List<String> errors = new ArrayList<>();
        boolean anySucceeded = false;

        // Inject section boundaries for style drift if analysis is available
        SectionAnalysisService.SectionAnalysis analysis = session.getSectionAnalysis();
        if (analysis != null && analysis.boundaries() != null && analysis.tiers() != null && Parameters.BPM > 0) {
            double bpm = Parameters.BPM;
            float[] boundaryBeats = new float[analysis.boundaries().size()];
            for (int b = 0; b < analysis.boundaries().size(); b++) {
                boundaryBeats[b] = (float)(analysis.boundaries().get(b) * bpm / 60.0);
            }
            GenerationContext.sectionBoundaryBeats = boundaryBeats;
            GenerationContext.sectionTiers = analysis.tiers();
        } else {
            GenerationContext.sectionBoundaryBeats = null;
            GenerationContext.sectionTiers = null;
        }

        for (DiffSession diff : targets) {
            GenerationContext.currentDiff = diff.difficultyFileName();
            GenerationContext.patternVariance = diff.getPatternVariance() * 10;
            try {
                BeatSaberMap generated = GenerationService.generate(type, diff.map(), Pattern.adjustVariance(session.getPattern()), oneHanded);
                acceptGeneratedMap(diff, generated);
                anySucceeded = true;
            } catch (Exception e) {
                logger.error("Generation failed for {}: {}", diff.difficultyFileName(), e.getMessage());
                errors.add(diff.difficultyFileName() + ": " + e.getMessage());
            }
        }

        GenerationContext.currentDiff = "NULL";
        GenerationContext.patternVariance = 0;
        if (anySucceeded) setState(AppState.GENERATED);
        return errors;
    }

    /**
     * Converts the given diffs to timing notes in place.
     *
     * @param oneColor true = blue-only dot notes bottom-left (the format generators expect);
     *                 false = two-color timing notes (known to be shaky, the old UI warned too)
     */
    public void convertToTimingNotes(boolean oneColor, List<DiffSession> targets) {
        prepareGeneration();
        for (DiffSession diff : targets) {
            GenerationContext.currentDiff = diff.difficultyFileName();
            if (oneColor) diff.map().toBlueLeftBottomRowDotTimings();
            else diff.map().toTimingNotes();
            logger.info("Converted {} to {} timing notes", diff.difficultyFileName(), oneColor ? "1-color" : "2-color");
        }
        GenerationContext.currentDiff = "NULL";
        setState(state); // refresh views
    }

    /**
     * Map utilities (formerly the Swing "Map Utilities" sub-buttons), applied in place to
     * the given diffs. Each fires a state refresh so views can update.
     */
    public void makeNoArrows(List<DiffSession> targets) {
        for (DiffSession diff : targets) {
            diff.map().makeNoArrows();
            logger.info("{} is now a no arrows map", diff.difficultyFileName());
        }
        setState(state);
    }

    /** Converts all flashing light events into regular "on" events. */
    public void convertFlashingLights(List<DiffSession> targets) {
        for (DiffSession diff : targets) {
            diff.map().convertAllFlashLightsToOnLights();
            logger.info("Removed flashing lights from {}", diff.difficultyFileName());
        }
        setState(state);
    }

    /** Deletes all notes of one color (red: 0, blue: 1), making the diffs one-handed. */
    public void deleteNoteType(List<DiffSession> targets, int noteType) {
        for (DiffSession diff : targets) {
            diff.map().makeOneHanded(noteType);
            logger.info("Removed all notes with type {} from {}", noteType, diff.difficultyFileName());
        }
        setState(state);
    }

    /**
     * Applies the characteristic-specific transform to {@code map} in place.
     * Single source of truth used by both {@code createCharacteristicDiff} and
     * {@code applyCharacteristicInPlace}.
     *
     * @param map        the map to transform (must already be a clone if non-destructive use)
     * @param ch         the target characteristic
     * @param removeType note type to remove for ONE_SABER (0=red, 1=blue); ignored otherwise
     */
    private void applyCharacteristicTransform(BeatSaberMap map, BeatmapCharacteristic ch, int removeType) {
        switch (ch) {
            case NO_ARROWS -> map.makeNoArrows();
            case ONE_SABER -> map.makeOneHanded(removeType);
            case LIGHTSHOW -> map.makeLightshow();
            case DEGREE_360 -> RotationEventGenerator.generate(map, RotationEventGenerator.RotationMode.THREE_SIXTY);
            case DEGREE_90 -> RotationEventGenerator.generate(map, RotationEventGenerator.RotationMode.NINETY);
            case LAWLESS -> LawlessGenerator.generate(map);
            case LEGACY -> logger.info("Created Legacy diff as stub — notes copied as-is, old-format specifics not yet implemented.");
            default -> { /* STANDARD or unknown: no transform */ }
        }
    }

    /**
     * Applies the characteristic transform in place to each diff in {@code targets}.
     * Mirrors the {@code makeNoArrows(List)} pattern: loops, transforms, logs, refreshes state.
     *
     * @param targets    diffs to transform
     * @param ch         the target characteristic
     * @param removeType note type to remove for ONE_SABER; -1 for all others
     */
    public void applyCharacteristicInPlace(List<DiffSession> targets, BeatmapCharacteristic ch, int removeType) {
        for (DiffSession diff : targets) {
            applyCharacteristicTransform(diff.map(), ch, removeType);
            logger.info("Applied {} transform to {} (in place)", ch.infoName, diff.difficultyFileName());
        }
        setState(state);
    }

    /** Creates a new characteristic diff WITH the note transform applied (Characteristics tab). */
    public DiffSession createCharacteristicDiff(DiffSession source, BeatmapCharacteristic characteristic, int removeType, boolean overwrite) {
        return createCharacteristicDiff(source, characteristic, removeType, overwrite, true);
    }

    /**
     * Clones {@code source} into a new in-session diff tagged under {@code characteristic} with NO
     * transform — notes, obstacles and events are copied verbatim. Backs the Utilities relabel tool.
     *
     * @return the new DiffSession, or null on filename collision when overwrite is false
     */
    public DiffSession changeCharacteristic(DiffSession source, BeatmapCharacteristic characteristic, boolean overwrite) {
        return createCharacteristicDiff(source, characteristic, -1, overwrite, false);
    }

    /**
     * Moves {@code source} to a new characteristic: clones it under the new characteristic
     * (notes verbatim, overwriting any existing diff with the target filename) then removes
     * the source diff from the session.
     *
     * @return the new DiffSession, or null if the clone step failed unexpectedly
     */
    public DiffSession renameCharacteristic(DiffSession source, BeatmapCharacteristic characteristic) {
        String sourceFileName = source.difficultyFileName();
        DiffSession created = createCharacteristicDiff(source, characteristic, -1, true, false);
        if (created != null) {
            // unloadDiff fires state listeners so the header refreshes correctly
            unloadDiff(sourceFileName);
        }
        return created;
    }

    /**
     * Creates a new in-session difficulty as a clone of {@code source} under the given
     * {@code characteristic}.
     *
     * @param source         the diff to clone
     * @param characteristic the target beatmap characteristic
     * @param removeType     note type to remove for ONE_SABER (0 = red, 1 = blue); ignored otherwise
     * @param overwrite      if true and a diff with the same filename exists, replace it
     * @param applyTransform if true, applies the characteristic-specific note transform (e.g. NO_ARROWS → dots);
     *                       if false, notes are copied verbatim regardless of characteristic
     * @return the new DiffSession, or null if a collision exists and overwrite is false
     */
    private DiffSession createCharacteristicDiff(DiffSession source, BeatmapCharacteristic characteristic, int removeType, boolean overwrite, boolean applyTransform) {
        String base = BeatmapCharacteristic.baseDifficulty(source.difficultyFileName());
        String newFileName = base + characteristic.filenameSuffix + ".dat";

        boolean collision = session.diffs().stream().anyMatch(d -> d.difficultyFileName().equals(newFileName));
        if (collision) {
            if (!overwrite) {
                logger.warn("Characteristic diff {} already exists in session", newFileName);
                return null;
            }
            session.diffs().removeIf(d -> d.difficultyFileName().equals(newFileName));
            PARITY_ERRORS_LIST.remove(newFileName);
            logger.info("Overwriting existing diff {}", newFileName);
        }

        BeatSaberMap clone = cloneMap(source.map());
        clone.difficultyFileName = newFileName;

        if (applyTransform) {
            applyCharacteristicTransform(clone, characteristic, removeType);
        }

        session.maps().add(clone);
        DiffSession newDiff = session.diffs().stream()
                .filter(d -> d.difficultyFileName().equals(newFileName))
                .findFirst().orElseThrow();
        newDiff.setCharacteristic(characteristic);

        setActiveDiff(newDiff);
        setState(AppState.LOADED);
        logger.info("Created characteristic diff {} ({})", newFileName, characteristic.infoName);
        return newDiff;
    }

    /**
     * Deep-clones a BeatSaberMap: notes are copied via the Note copy constructor so that
     * transforms on the clone cannot affect the original. Events and obstacles are
     * shallow-copied (they are not mutated by the available transforms).
     */
    private BeatSaberMap cloneMap(BeatSaberMap src) {
        Note[] clonedNotes = new Note[src._notes != null ? src._notes.length : 0];
        if (src._notes != null) {
            for (int i = 0; i < src._notes.length; i++) clonedNotes[i] = new Note(src._notes[i]);
        }
        BeatSaberMap clone = new BeatSaberMap(clonedNotes);
        clone._version = src._version;
        clone._events = src._events != null ? Arrays.copyOf(src._events, src._events.length) : new BeatSaberObjects.Objects.Events[0];
        clone._obstacles = src._obstacles != null ? Arrays.copyOf(src._obstacles, src._obstacles.length) : new BeatSaberObjects.Objects.Obstacle[0];
        clone.bookmarks = src.bookmarks != null ? new ArrayList<>(src.bookmarks) : new ArrayList<>();
        clone.difficultyFileName = src.difficultyFileName;
        return clone;
    }

    /** Snaps all note placements to 1/precisionDenominator of a beat (e.g. 16 → 1/16). */
    public void fixPlacements(List<DiffSession> targets, double precisionDenominator) {
        for (DiffSession diff : targets) {
            diff.map().fixPlacements(1 / precisionDenominator);
            logger.info("Fixed note placements of {} with a precision of 1/{} of a beat", diff.difficultyFileName(), precisionDenominator);
        }
        setState(state);
    }

    /**
     * Loads a generation pattern from a .pat file or from an existing difficulty (.dat/.json).
     * (Formerly GlobalLoadPatterns.)
     */
    public void loadPatternFromFile(File file) throws Exception {
        if (file.getName().endsWith(".pat")) {
            session.setPattern(new Pattern(file.getAbsolutePath()));
        } else if (file.getName().endsWith(".dat") || file.getName().endsWith(".json")) {
            BeatSaberMap map = BeatSaberMap.newMapFromJSON(file.getAbsolutePath());
            session.setPattern(new Pattern(map._notes, 1));
        } else {
            throw new IllegalArgumentException("Pattern must be a .pat, .dat or .json file");
        }
        logger.info("Pattern loaded from: {}", file.getAbsolutePath());
    }

    /**
     * Exports the whole map as a playable zip: every file from the loaded map folder
     * (info.dat, song, cover, …) plus the current in-memory state of the loaded diffs,
     * which override their files on disk. Existing zips and diff backups are skipped.
     *
     * One session = one map = one zip; loading always replaces the session, so diffs
     * from two different maps can never be mixed here.
     */
    public void exportMapAsZip(File targetZip) throws IOException {
        String folder = session.getMapFolderPath();
        if (folder == null || folder.isEmpty()) throw new IllegalStateException("No map folder available");

        java.util.Map<String, BeatSaberMap> inMemoryDiffs = new java.util.HashMap<>();
        session.diffs().forEach(diff -> inMemoryDiffs.put(diff.difficultyFileName(), diff.map()));

        // Collect filenames present at the root of the map folder to detect in-session-only diffs later.
        // Use Files.list (non-recursive) — map folders are flat, and a recursive walk would collect names
        // from subdirectories that could shadow a diff filename and cause it to be silently skipped.
        // Case-insensitive scan also resolves Info.dat vs info.dat (Beat Saber uses capital-I on all platforms).
        java.util.Set<String> onDiskFileNames = new java.util.HashSet<>();
        java.nio.file.Path infoDatPath = null;
        java.nio.file.Path root = new File(folder).toPath();
        try (java.util.stream.Stream<java.nio.file.Path> stream = java.nio.file.Files.list(root)) {
            for (java.nio.file.Path p : stream.filter(java.nio.file.Files::isRegularFile).toList()) {
                onDiskFileNames.add(p.getFileName().toString());
                if (p.getFileName().toString().equalsIgnoreCase("info.dat")) infoDatPath = p;
            }
        }

        // Read and patch info.dat with any new characteristic sets
        String infoDatContent = null;
        if (infoDatPath != null) {
            infoDatContent = java.nio.file.Files.readString(infoDatPath, java.nio.charset.StandardCharsets.UTF_8);
            infoDatContent = mergeCharacteristicSets(infoDatContent);
        }

        try (java.util.zip.ZipOutputStream zip = new java.util.zip.ZipOutputStream(new java.io.FileOutputStream(targetZip));
             java.util.stream.Stream<java.nio.file.Path> files = java.nio.file.Files.walk(root)) {

            final String patchedInfoDat = infoDatContent;
            for (java.nio.file.Path path : files.filter(java.nio.file.Files::isRegularFile).toList()) {
                String relative = root.relativize(path).toString().replace('\\', '/');
                if (relative.endsWith(".zip") || path.toFile().equals(targetZip)) continue;

                zip.putNextEntry(new java.util.zip.ZipEntry(relative));
                String fileName = path.getFileName().toString();
                if (fileName.equalsIgnoreCase("info.dat") && patchedInfoDat != null) {
                    zip.write(patchedInfoDat.getBytes(java.nio.charset.StandardCharsets.UTF_8));
                } else {
                    BeatSaberMap inMemory = inMemoryDiffs.get(fileName);
                    if (inMemory != null) {
                        zip.write(inMemory.exportAsMap().getBytes(java.nio.charset.StandardCharsets.UTF_8));
                    } else {
                        java.nio.file.Files.copy(path, zip);
                    }
                }
                zip.closeEntry();
            }

            // Emit in-session diffs that have no backing file on disk (newly created characteristic diffs)
            for (DiffSession diff : session.diffs()) {
                if (!onDiskFileNames.contains(diff.difficultyFileName())) {
                    zip.putNextEntry(new java.util.zip.ZipEntry(diff.difficultyFileName()));
                    zip.write(diff.map().exportAsMap().getBytes(java.nio.charset.StandardCharsets.UTF_8));
                    zip.closeEntry();
                    logger.info("Emitted in-session-only diff: {}", diff.difficultyFileName());
                }
            }
        }
        logger.info("Map exported as zip: {}", targetZip.getAbsolutePath());
    }

    /**
     * Reads the existing info.dat JSON and injects _difficultyBeatmapSets entries for any
     * in-session diffs whose characteristic is not STANDARD (i.e. diffs created this session).
     * Existing sets are not removed; new sets are added and existing ones extended as needed.
     */
    private String mergeCharacteristicSets(String infoDatJson) {
        List<DiffSession> newDiffs = session.diffs().stream()
                .filter(d -> d.characteristic() != null && d.characteristic() != BeatmapCharacteristic.STANDARD)
                .toList();
        if (newDiffs.isEmpty()) return infoDatJson;

        try {
            // Gson (not org.json) so the original key order is preserved: ArcViewer detects the
            // schema version from the FIRST "version" token in the file, and reordering would push
            // a nested _customData._editors "version" ahead of the top-level _version, breaking load.
            JsonObject info = JsonParser.parseString(infoDatJson).getAsJsonObject();
            JsonArray sets;
            if (info.has("_difficultyBeatmapSets") && info.get("_difficultyBeatmapSets").isJsonArray()) {
                sets = info.getAsJsonArray("_difficultyBeatmapSets");
            } else {
                sets = new JsonArray();
                info.add("_difficultyBeatmapSets", sets);
            }

            for (DiffSession diff : newDiffs) {
                BeatmapCharacteristic ch = diff.characteristic();
                String base = BeatmapCharacteristic.baseDifficulty(diff.difficultyFileName());

                // Find or create the set for this characteristic
                JsonObject targetSet = null;
                for (int i = 0; i < sets.size(); i++) {
                    JsonObject s = sets.get(i).getAsJsonObject();
                    if (ch.infoName.equals(optString(s, "_beatmapCharacteristicName"))) {
                        targetSet = s;
                        break;
                    }
                }
                if (targetSet == null) {
                    targetSet = new JsonObject();
                    targetSet.addProperty("_beatmapCharacteristicName", ch.infoName);
                    targetSet.add("_difficultyBeatmaps", new JsonArray());
                    sets.add(targetSet);
                }

                JsonArray beatmaps = targetSet.getAsJsonArray("_difficultyBeatmaps");
                // Avoid duplicate entries for the same file
                boolean alreadyPresent = false;
                for (int i = 0; i < beatmaps.size(); i++) {
                    if (diff.difficultyFileName().equals(optString(beatmaps.get(i).getAsJsonObject(), "_beatmapFilename"))) {
                        alreadyPresent = true;
                        break;
                    }
                }
                if (!alreadyPresent) {
                    JsonObject template = findStandardTemplate(sets, base);
                    beatmaps.add(buildDifficultyBeatmapEntry(base, diff.difficultyFileName(), template));
                }
            }

            return new GsonBuilder().setPrettyPrinting().create().toJson(info);
        } catch (Exception e) {
            logger.error("Failed to merge characteristic sets into info.dat: {}", e.getMessage());
            return infoDatJson;
        }
    }

    /** Reads a string member, or null if absent/not a string. (Gson has no org.json-style optString.) */
    private static String optString(JsonObject obj, String key) {
        return obj.has(key) && obj.get(key).isJsonPrimitive() ? obj.get(key).getAsString() : null;
    }

    /**
     * Builds a _difficultyBeatmaps entry object.
     * NJS and beat offset are copied from the Standard set's entry for the same base difficulty when
     * present, so the new characteristic plays at the same speed as the source diff. Falls back to
     * NJS 16 / offset 0 when no Standard template entry is found.
     */
    private JsonObject buildDifficultyBeatmapEntry(String baseDifficulty, String beatmapFilename) {
        return buildDifficultyBeatmapEntry(baseDifficulty, beatmapFilename, null);
    }

    /**
     * Builds a _difficultyBeatmaps entry, optionally inheriting NJS/offset from a Standard template entry.
     *
     * @param templateEntry a _difficultyBeatmaps JsonObject from the Standard set for the same difficulty, or null
     */
    private JsonObject buildDifficultyBeatmapEntry(String baseDifficulty, String beatmapFilename, JsonObject templateEntry) {
        int njs    = templateEntry != null && templateEntry.has("_noteJumpMovementSpeed")   ? templateEntry.get("_noteJumpMovementSpeed").getAsInt()      : 16;
        double off = templateEntry != null && templateEntry.has("_noteJumpStartBeatOffset") ? templateEntry.get("_noteJumpStartBeatOffset").getAsDouble() : 0.0;

        JsonObject entry = new JsonObject();
        entry.addProperty("_difficulty", baseDifficulty);
        entry.addProperty("_difficultyRank", BeatmapCharacteristic.difficultyRank(baseDifficulty));
        entry.addProperty("_beatmapFilename", beatmapFilename);
        entry.addProperty("_noteJumpMovementSpeed", njs);
        entry.addProperty("_noteJumpStartBeatOffset", off);
        entry.addProperty("_beatmapColorSchemeIdx", 0);
        entry.addProperty("_environmentNameIdx", 0);
        return entry;
    }

    /**
     * Finds the _difficultyBeatmaps entry in the Standard set that matches {@code baseDifficulty}.
     * Returns null if the Standard set or a matching entry does not exist.
     */
    private JsonObject findStandardTemplate(JsonArray sets, String baseDifficulty) {
        for (int i = 0; i < sets.size(); i++) {
            JsonObject set = sets.get(i).getAsJsonObject();
            if ("Standard".equals(optString(set, "_beatmapCharacteristicName"))) {
                if (!set.has("_difficultyBeatmaps") || !set.get("_difficultyBeatmaps").isJsonArray()) return null;
                JsonArray beatmaps = set.getAsJsonArray("_difficultyBeatmaps");
                for (int j = 0; j < beatmaps.size(); j++) {
                    JsonObject bm = beatmaps.get(j).getAsJsonObject();
                    if (baseDifficulty.equals(optString(bm, "_difficulty"))) return bm;
                }
            }
        }
        return null;
    }

    /**
     * Zips the map folder and opens the configured web previewer plus the folder,
     * so the zip can be dragged into the browser. (Formerly GlobalOpenMapInBrowser.)
     */
    public void openMapInBrowserPreviewer() throws Exception {
        String folder = session.getMapFolderPath();
        if (folder == null || folder.isEmpty()) throw new IllegalStateException("No map folder available");

        String zipFileName = folder + "/output.zip";
        FileManager.createZipFileFromDirectory(folder, zipFileName);
        logger.info("Zip created: {}", zipFileName);

        java.awt.Desktop desktop = java.awt.Desktop.getDesktop();
        desktop.browse(new java.net.URI(Parameters.mapViewerURL));
        desktop.open(new File(folder));
    }

    /**
     * Converts the parity errors recorded for a difficulty into editor bookmarks.
     * (Formerly UserInterface.parityErrorsAsBookmarks.)
     */
    public List<Bookmark> parityErrorsAsBookmarks(String diffName) {
        if (SAVE_PARITY_ERRORS_AS_BOOKMARKS_WILL_OVERWRITE_BOOKMARKS) session.maps().forEach(b -> b.bookmarks = new ArrayList<>());

        List<Bookmark> bookmarks = new ArrayList<>();
        for (Pair<Float, ParityErrorEnum> err : PARITY_ERRORS_LIST.get(diffName)) {
            float[] color = new float[3];
            color[0] = PARITY_ERRORS_COLORS_MAP.get(err.getValue()).getRed();
            color[1] = PARITY_ERRORS_COLORS_MAP.get(err.getValue()).getGreen();
            color[2] = PARITY_ERRORS_COLORS_MAP.get(err.getValue()).getBlue();

            bookmarks.add(new Bookmark(err.getKey(), err.getValue().toString(), color));
        }

        // Deliberately not clearing the parity list here: the Review view still needs it,
        // and prepareGeneration() resets all lists before the next run anyway.
        return bookmarks;
    }

    /** Runs the basic parity/mapping-error check on a map. (Formerly UserInterface.checkMap.) */
    public void checkMap(BeatSaberMap map) {
        List<Note> notes = new ArrayList<>();
        Collections.addAll(notes, map._notes);

        CheckParity.checkAndFixBasicMappingErrors(notes, false);
        logger.warn("There have been {} mapping errors", GenerationContext.currentParityErrors().size());
    }

    /**
     * Writes one map to the given path, optionally renaming an existing file to a
     * numbered backup first. (Formerly GlobalSaveMapAs internals.)
     *
     * @return true if the write succeeded
     */
    public boolean saveMap(BeatSaberMap map, String targetPath, boolean backup) {
        if (backup && !backupExisting(targetPath)) {
            logger.error("Could not create backup for: {}", targetPath);
            return false;
        }

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(targetPath))) {
            bw.write(map.exportAsMap());
            logger.info("Map saved successfully at: {}", targetPath);
            return true;
        } catch (IOException e) {
            logger.error("There was an error while saving the map at {}: {}", targetPath, e.getMessage());
            return false;
        }
    }

    /** Marks the current generation result as saved (fired after a successful save run). */
    public void markSaved() {
        setState(AppState.SAVED);
    }

    /** Renames an existing file at path to the first free "path&lt;n&gt;" name. No-op if the file doesn't exist. */
    private boolean backupExisting(String path) {
        File f = new File(path);
        if (!f.exists()) return true;

        int i = 1;
        File backup = new File(path + i);
        while (backup.exists()) {
            i++;
            backup = new File(path + i);
        }
        return f.renameTo(backup);
    }
}