package AppLogic;

import BeatSaberObjects.Objects.BeatSaberMap;
import BeatSaberObjects.Objects.Enums.ParityErrorEnum;
import DataManager.Parameters;
import javafx.util.Pair;

import java.util.ArrayList;
import java.util.List;

/**
 * Per-difficulty working state: the current map (original or generated) plus access to
 * the parity errors recorded for this difficulty.
 *
 * The map reference is replaced when a generator accepts a new result for this diff;
 * the difficulty file name stays stable so saving targets the correct file.
 */
public class DiffSession {
    private final String difficultyFileName;
    private BeatSaberMap map;

    public DiffSession(String difficultyFileName, BeatSaberMap map) {
        this.difficultyFileName = difficultyFileName;
        this.map = map;
        Parameters.PARITY_ERRORS_LIST.putIfAbsent(difficultyFileName, new ArrayList<>());
    }

    public String difficultyFileName() {
        return difficultyFileName;
    }

    public BeatSaberMap map() {
        return map;
    }

    public void setMap(BeatSaberMap map) {
        this.map = map;
    }

    public List<Pair<Float, ParityErrorEnum>> parityErrors() {
        return Parameters.PARITY_ERRORS_LIST.computeIfAbsent(difficultyFileName, k -> new ArrayList<>());
    }
}