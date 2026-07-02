package AppLogic;

import BeatSaberObjects.Objects.BeatSaberMap;
import BeatSaberObjects.Objects.Bookmark;
import BeatSaberObjects.Objects.Enums.ParityErrorEnum;
import BeatSaberObjects.Objects.Note;
import DataManager.FileManager;
import DataManager.Parameters;
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
    }

    private final MapSession session = new MapSession();
    private final List<Listener> listeners = new CopyOnWriteArrayList<>();
    private AppState state = AppState.EMPTY;

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
        setState(AppState.LOADED);
        return loaded;
    }

    private String loadSingleDiff(File diffFile) {
        BeatSaberMap map = BeatSaberMap.newMapFromJSON(diffFile.getAbsolutePath());
        session.maps().add(map);
        PARITY_ERRORS_LIST.put(diffFile.getName(), new ArrayList<>());
        logger.info("Successfully loaded: {}/{}", diffFile.getParent(), diffFile.getName());
        return diffFile.getName();
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

        if (session.diffs().isEmpty()) unload();
        else setState(state);
    }

    /** Discards the loaded map (diffs, parity errors, folder path) and returns to EMPTY. The loaded pattern is kept. */
    public void unload() {
        session.maps().clear();
        session.setMapFolderPath(null);
        PARITY_ERRORS_LIST.clear();
        GenerationContext.currentDiff = "NULL";
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
        List<BeatSaberMap> maps = session.maps();
        for (int i = 0; i < newMaps.size(); i++) {
            BeatSaberMap generated = newMaps.get(i);

            if (maps.get(i).equals(generated) || new HashSet<>(Arrays.stream(maps.get(i)._notes).toList()).containsAll(Arrays.stream(generated._notes).toList())) {
                logger.error("Map couldn't be loaded!");
                System.err.println("Map couldn't be loaded!");
            }

            logger.info("Checking map: {}", maps.get(i).difficultyFileName);

            String ogJson = maps.get(i).originalJSON;
            String diffName = maps.get(i).difficultyFileName;
            maps.set(i, generated);
            maps.get(i).originalJSON = ogJson;
            maps.get(i).bookmarks = maps.get(i).calculateBookmarks();
            maps.get(i).difficultyFileName = diffName;
            checkMap(maps.get(i));

            if (Parameters.SAVE_PARITY_ERRORS_AS_BOOKMARKS) {
                maps.get(i).bookmarks.addAll(parityErrorsAsBookmarks(diffName));
            }

            logger.info("Newly created map loaded!\n\n");
        }

        GenerationContext.currentDiff = "NULL";
        if (!newMaps.isEmpty()) setState(AppState.GENERATED);
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

        GenerationContext.currentParityErrors().clear();
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