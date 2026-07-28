package BeatSaberObjects.Objects.Enums;

/**
 * Beat Saber beatmap characteristic (game mode tab).
 * Each constant carries the _beatmapCharacteristicName used in info.dat and the suffix
 * appended to the base difficulty label in the .dat filename (e.g. ExpertPlusOneSaber.dat).
 */
public enum BeatmapCharacteristic {
    STANDARD("Standard",   "Standard",  false),
    ONE_SABER("OneSaber",  "OneSaber",  true),
    NO_ARROWS("NoArrows",  "NoArrows",  true),
    DEGREE_90("90Degree",  "90Degree",  true),
    DEGREE_360("360Degree","360Degree", true),
    LIGHTSHOW("Lightshow", "Lightshow", true),
    LAWLESS("Lawless",     "Lawless",   false),
    LEGACY("Legacy",       "Legacy",    false);

    /** Value used for _beatmapCharacteristicName in info.dat. */
    public final String infoName;
    /** Suffix appended after the base difficulty label in the .dat filename. */
    public final String filenameSuffix;
    /** Whether a note transform for this characteristic is implemented. */
    public final boolean implemented;

    BeatmapCharacteristic(String infoName, String filenameSuffix, boolean implemented) {
        this.infoName = infoName;
        this.filenameSuffix = filenameSuffix;
        this.implemented = implemented;
    }

    /**
     * Extracts the base difficulty label (Easy, Normal, Hard, Expert, ExpertPlus) from a
     * difficultyFileName by stripping the characteristic suffix and .dat extension.
     * Returns the input unchanged (without .dat) if no known suffix matches.
     */
    public static String baseDifficulty(String difficultyFileName) {
        String name = difficultyFileName.endsWith(".dat")
                ? difficultyFileName.substring(0, difficultyFileName.length() - 4)
                : difficultyFileName;
        for (BeatmapCharacteristic c : values()) {
            if (!c.filenameSuffix.isEmpty() && name.endsWith(c.filenameSuffix)) {
                return name.substring(0, name.length() - c.filenameSuffix.length());
            }
        }
        return name;
    }

    /** Returns the _difficultyRank integer for a base difficulty label. */
    public static int difficultyRank(String baseDifficulty) {
        return switch (baseDifficulty) {
            case "Easy" -> 1;
            case "Normal" -> 3;
            case "Hard" -> 5;
            case "Expert" -> 7;
            case "ExpertPlus" -> 9;
            default -> 1;
        };
    }
}
