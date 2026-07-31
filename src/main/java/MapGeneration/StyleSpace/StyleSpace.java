package MapGeneration.StyleSpace;

import MapGeneration.GenerationElements.HigherOrderPattern;
import MapGeneration.GenerationElements.Pattern;
import MapGeneration.GenerationElements.PatternProbability;
import BeatSaberObjects.Objects.Note;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Runtime style-space engine.
 *
 * <p>Holds a set of {@link StyleArchetype}s and a current "style coordinate"
 * (a {@link StyleVector}). For each generation slot it produces a
 * {@link PatternProbability} conditioned on the active coordinate by
 * blending the K-nearest archetype {@link HigherOrderPattern}s weighted by
 * inverse distance.
 *
 * <h2>Consistent quirk</h2>
 * {@link #setCoordinate(StyleVector)} is called once per map — the coordinate
 * is fixed for the entire generation run, so every note is drawn from the same
 * identity-filtered pool.
 *
 * <h2>Narrative drift (Variant D)</h2>
 * Call {@link #driftToward(StyleVector, float)} at section boundaries to shift
 * the coordinate by a small step. The drift magnitude is the user "surprise" knob.
 */
public class StyleSpace {

    private static final Logger logger = LogManager.getLogger(StyleSpace.class);

    /** Number of nearest archetypes to blend when conditioning the pattern. */
    private static final int K_BLEND = 3;

    private final List<StyleArchetype> archetypes;
    private StyleVector coordinate;

    public StyleSpace(List<StyleArchetype> archetypes) {
        this.archetypes = List.copyOf(archetypes);
        this.coordinate = archetypes.isEmpty() ? StyleVector.zero()
                                                : archetypes.get(0).centroid;
    }

    // -----------------------------------------------------------------------
    // Coordinate control
    // -----------------------------------------------------------------------

    /** Sets the style coordinate for this map. Call once before generation starts. */
    public void setCoordinate(StyleVector v) {
        this.coordinate = v;
    }

    public StyleVector getCoordinate() { return coordinate; }

    /**
     * Moves the coordinate a fraction {@code t} (0–1) toward {@code target}.
     * Used at section boundaries to produce narrative drift.
     * Small t (0.05–0.15) = subtle; large t = dramatic style shift.
     */
    public void driftToward(StyleVector target, float t) {
        coordinate = StyleVector.lerp(coordinate, target, t);
    }

    /**
     * Convenience: drift toward the style implied by a section intensity tier (0–4).
     * Tier 0 (calm) → flow-near region; tier 4 (peak) → speed/tech-near region.
     * Returns the target used so callers can log or inspect it.
     */
    public StyleVector driftForIntensityTier(int tier, float driftMagnitude) {
        if (archetypes.isEmpty()) return coordinate;
        StyleVector target = styleVectorForTier(tier);
        driftToward(target, driftMagnitude);
        return target;
    }

    // -----------------------------------------------------------------------
    // Pattern lookup
    // -----------------------------------------------------------------------

    /**
     * Returns a {@link PatternProbability} for the current coordinate and context,
     * blending the K-nearest archetypes' higher-order patterns.
     *
     * <p>Falls back to {@code baseline} if no archetype data is available.
     *
     * @param prevPrev  note two steps back (same color); may be null
     * @param prev      note one step back; may be null → baseline used
     * @param beatGap   gap in beats from prev to current slot
     * @param baseline  1st-order fallback Pattern
     * @param time      beat time to stamp on candidates
     * @param isBlue    true=blue channel, false=red
     */
    public PatternProbability getProbability(Note prevPrev, Note prev, float beatGap,
                                             Pattern baseline, float time, boolean isBlue) {
        if (archetypes.isEmpty() || prev == null) {
            return baseline != null ? baseline.getProbabilityOf(prev) : null;
        }

        List<ArchetypeDistance> nearest = kNearest(K_BLEND);
        if (nearest.isEmpty()) return baseline != null ? baseline.getProbabilityOf(prev) : null;

        // Try each archetype in order of proximity; return the first non-null result
        for (ArchetypeDistance ad : nearest) {
            HigherOrderPattern hop = isBlue ? ad.archetype.patternBlue : ad.archetype.patternRed;
            PatternProbability pp  = hop.getProbability(prevPrev, prev, beatGap, baseline, time);
            if (pp != null) return pp;
        }

        return baseline != null ? baseline.getProbabilityOf(prev) : null;
    }

    // -----------------------------------------------------------------------
    // Style-vector helpers
    // -----------------------------------------------------------------------

    /**
     * Returns a random coordinate near a reference archetype, for map-start randomisation.
     * Jitter amplitude controls how far from the centroid the start point can be.
     */
    public StyleVector randomCoordinateNear(int archetypeId, float jitter, java.util.Random rng) {
        StyleArchetype base = archetypes.stream()
                .filter(a -> a.id == archetypeId)
                .findFirst()
                .orElse(archetypes.get(0));
        float[] jittered = new float[StyleVector.DIMENSIONS];
        for (int i = 0; i < StyleVector.DIMENSIONS; i++) {
            jittered[i] = Math.max(0, Math.min(1, base.centroid.axes[i] + (rng.nextFloat() - 0.5f) * 2 * jitter));
        }
        return new StyleVector(jittered);
    }

    public List<StyleArchetype> getArchetypes() { return archetypes; }

    // -----------------------------------------------------------------------
    // Private
    // -----------------------------------------------------------------------

    private List<ArchetypeDistance> kNearest(int k) {
        List<ArchetypeDistance> ranked = new ArrayList<>(archetypes.size());
        for (StyleArchetype a : archetypes) {
            ranked.add(new ArchetypeDistance(a, coordinate.distanceTo(a.centroid)));
        }
        ranked.sort(Comparator.comparingDouble(ad -> ad.dist));
        return ranked.subList(0, Math.min(k, ranked.size()));
    }

    /**
     * Heuristic: maps intensity tier (0=calm … 4=peak) to a style-space target.
     * Calm sections aim at high streamRatio=0, high sparseRatio (tech/slow).
     * Peak sections aim at high streamRatio (speed/dense).
     * This is an initial heuristic — tune against real user feedback.
     */
    private StyleVector styleVectorForTier(int tier) {
        // Find archetype whose streamRatio axis (index 11) best matches tier intensity
        float targetStream = tier / 4.0f;  // 0.0 calm → 1.0 peak
        StyleArchetype best = archetypes.get(0);
        float bestDelta = Math.abs(best.centroid.axes[11] - targetStream);
        for (StyleArchetype a : archetypes) {
            float delta = Math.abs(a.centroid.axes[11] - targetStream);
            if (delta < bestDelta) { bestDelta = delta; best = a; }
        }
        return best.centroid;
    }

    private record ArchetypeDistance(StyleArchetype archetype, float dist) {}
}
