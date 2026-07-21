package AppLogic;

public enum AppState {
    /** No map loaded yet. */
    EMPTY,
    /** At least one difficulty loaded and parsed. */
    LOADED,
    /** A generator ran; generated maps exist in memory but are not saved. */
    GENERATED,
    /** The current generated maps were written to disk. */
    SAVED
}