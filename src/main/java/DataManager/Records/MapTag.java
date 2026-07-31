package DataManager.Records;

import java.util.Locale;

/**
 * Valid BeatKenja map tags as defined in config/PatMetadata.
 * Tags influence pattern selection: Speed → FAST patterns, Tech → TECH patterns.
 */
public enum MapTag {
    ACCURACY,
    BALANCED,
    CHALLENGE,
    DANCE,
    FITNESS,
    SPEED,
    TECH,
    NULL;

    public static MapTag fromString(String s) {
        if (s == null) return NULL;
        try {
            return MapTag.valueOf(s.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            return NULL;
        }
    }
}
