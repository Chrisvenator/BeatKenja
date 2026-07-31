package AppLogic;

import AppLogic.SectionAnalysisService;
import BeatSaberObjects.Objects.BeatSaberMap;
import DataManager.Parameters;
import MapGeneration.GenerationElements.Pattern;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;

/**
 * Working state of one loaded map (song): its difficulties, the pattern used for
 * generation, and the folder the difficulties were loaded from.
 *
 * Exposes the difficulties both as DiffSession objects (new API) and as a live
 * List&lt;BeatSaberMap&gt; view (legacy API used by the existing Swing buttons).
 * Both views are backed by the same data, so there is no state duplication.
 */
public class MapSession {
    private final List<DiffSession> diffs = new ArrayList<>();
    /** Live legacy view over {@link #diffs}; see {@link LegacyMapListView}. */
    private final List<BeatSaberMap> legacyView = new LegacyMapListView();
    private Pattern pattern;
    private String mapFolderPath;
    private SectionAnalysisService.SectionAnalysis sectionAnalysis;

    public List<DiffSession> diffs() {
        return diffs;
    }

    public List<BeatSaberMap> maps() {
        return legacyView;
    }

    public Pattern getPattern() {
        return pattern;
    }

    public void setPattern(Pattern pattern) {
        this.pattern = pattern;
    }

    public String getMapFolderPath() {
        return mapFolderPath;
    }

    public void setMapFolderPath(String mapFolderPath) {
        this.mapFolderPath = mapFolderPath;
    }

    public SectionAnalysisService.SectionAnalysis getSectionAnalysis() { return sectionAnalysis; }
    public void setSectionAnalysis(SectionAnalysisService.SectionAnalysis a) { this.sectionAnalysis = a; }

    /** BPM is still shared with the generation core via Parameters.BPM; the session only delegates. */
    public double getBpm() {
        return Parameters.BPM;
    }

    public void setBpm(double bpm) {
        Parameters.BPM = bpm;
    }

    /**
     * List&lt;BeatSaberMap&gt; adapter over the DiffSession list.
     * set() keeps the DiffSession (and thereby the difficulty file name and parity errors)
     * and only swaps the map, which is exactly what map generation needs.
     */
    private class LegacyMapListView extends AbstractList<BeatSaberMap> {
        @Override
        public BeatSaberMap get(int index) {
            return diffs.get(index).map();
        }

        @Override
        public BeatSaberMap set(int index, BeatSaberMap element) {
            BeatSaberMap old = diffs.get(index).map();
            diffs.get(index).setMap(element);
            return old;
        }

        @Override
        public boolean add(BeatSaberMap map) {
            diffs.add(new DiffSession(map.difficultyFileName, map));
            return true;
        }

        @Override
        public BeatSaberMap remove(int index) {
            return diffs.remove(index).map();
        }

        @Override
        public void clear() {
            diffs.clear();
        }

        @Override
        public int size() {
            return diffs.size();
        }
    }
}