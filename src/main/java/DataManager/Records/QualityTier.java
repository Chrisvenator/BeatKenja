package DataManager.Records;

/** Quality tier for corpus maps, derived from the train-folder subfolder name. */
public enum QualityTier {
    RANKED(10),
    CURATED(5),
    VERIFIED(3),
    NORMAL(1);

    public final int weight;

    QualityTier(int weight) {
        this.weight = weight;
    }

    /** Maps a train subfolder name to its tier. Handles the "Verfied" typo. */
    public static QualityTier fromFolderName(String folderName) {
        return switch (folderName.toLowerCase().trim()) {
            case "ranked"              -> RANKED;
            case "curated"             -> CURATED;
            case "verified", "verfied" -> VERIFIED;
            default                    -> NORMAL;
        };
    }
}
