package AppLogic;

/** Map generators the UI can offer. Labels/descriptions are what the generator cards show. */
public enum GeneratorType {
    LINEAR("Linear", "Simple alternating swings. No DDs, no resets — gets boring fast, but always safe."),
    COMPLEX("Complex", "Varied patterns based on the loaded .pat file. May contain DDs/resets (you get warned)."),
    SECTIONED("Sectioned (bookmarks)", "Uses bookmarks in the map to switch styles per section: linear | complex | 1-2 | 2-2 | jumps | doubles …"),
    RANDOM("Random", "Chaos. Notes everywhere. Mostly useful for stress-testing."),
    RANDOM_V2("Random V2", "Random placement guided by the loaded pattern template.");

    public final String label;
    public final String description;

    GeneratorType(String label, String description) {
        this.label = label;
        this.description = description;
    }
}