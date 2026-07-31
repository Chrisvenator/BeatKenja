package MapGeneration.StyleSpace;

import MapGeneration.GenerationElements.HigherOrderPattern;

import java.io.Serializable;

/**
 * One cluster representative in the style space.
 *
 * <p>Built dev-side by {@link StyleSpaceTrainer} via K-Means over per-map
 * {@link StyleVector}s. Bundled into {@code resources/} and loaded at runtime
 * by {@link StyleSpace}.
 *
 * <p>Each archetype carries:
 * <ul>
 *   <li>A centroid {@link StyleVector} (the cluster centre).</li>
 *   <li>Pre-built {@link HigherOrderPattern}s (blue/red) trained only from maps
 *       nearest to this centroid — the "identity matrix" for this style.</li>
 *   <li>A human-readable label (assigned post-hoc from PatMetadata tags/genres).</li>
 * </ul>
 */
public class StyleArchetype implements Serializable {

    public final int id;
    public final String label;
    public final StyleVector centroid;
    public final HigherOrderPattern patternBlue;
    public final HigherOrderPattern patternRed;

    public StyleArchetype(int id, String label, StyleVector centroid,
                          HigherOrderPattern patternBlue, HigherOrderPattern patternRed) {
        this.id           = id;
        this.label        = label;
        this.centroid     = centroid;
        this.patternBlue  = patternBlue;
        this.patternRed   = patternRed;
    }

    @Override
    public String toString() {
        return "Archetype[" + id + ":" + label + "] obs=" + patternBlue.getTotalObservations();
    }
}
