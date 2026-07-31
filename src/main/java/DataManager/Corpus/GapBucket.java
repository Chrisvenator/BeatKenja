package DataManager.Corpus;

/**
 * Beat-gap category between consecutive same-color notes.
 * Used as part of the 2nd-order Markov state key so the pattern engine
 * distinguishes stream contexts (must stay simple) from tech/sparse contexts
 * (arm-movement complexity allowed).
 */
public enum GapBucket {
    /** ≤0.25 beats — 16th-note stream spacing. Simple patterns, short reaction time. */
    STREAM,
    /** 0.25–1.0 beats — typical 8th/quarter note flow. */
    NORMAL,
    /** >1.0 beats — slow or sparse sections. Tech complexity permitted. */
    SPARSE;

    public static GapBucket fromBeatGap(float gap) {
        if (gap <= 0.25f) return STREAM;
        if (gap <= 1.0f)  return NORMAL;
        return SPARSE;
    }
}
