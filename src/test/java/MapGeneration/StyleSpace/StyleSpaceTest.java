package MapGeneration.StyleSpace;

import BeatSaberObjects.Objects.Note;
import MapGeneration.GenerationElements.HigherOrderPattern;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class StyleSpaceTest {

    private StyleSpace styleSpace;
    private StyleArchetype archFast;
    private StyleArchetype archTech;

    @BeforeEach
    void setUp() {
        // Two archetypes: one fast (high streamRatio axis[11]), one tech (low streamRatio)
        float[] fastAxes = new float[StyleVector.DIMENSIONS];
        fastAxes[11] = 1.0f;  // streamRatio = 1 (all stream)
        StyleVector fastVec = new StyleVector(fastAxes);

        float[] techAxes = new float[StyleVector.DIMENSIONS];
        techAxes[11] = 0.0f;  // streamRatio = 0 (all sparse/tech)
        StyleVector techVec = new StyleVector(techAxes);

        HigherOrderPattern hopBlue = new HigherOrderPattern();
        HigherOrderPattern hopRed  = new HigherOrderPattern();

        archFast = new StyleArchetype(0, "fast", fastVec, hopBlue, hopRed);
        archTech = new StyleArchetype(1, "tech", techVec, hopBlue, hopRed);

        styleSpace = new StyleSpace(List.of(archFast, archTech));
    }

    @Test
    void setCoordinate_updatesCoordinate() {
        StyleVector v = StyleVector.zero();
        styleSpace.setCoordinate(v);
        assertSame(v, styleSpace.getCoordinate());
    }

    @Test
    void driftToward_movesPartway() {
        StyleVector start = StyleVector.zero();
        styleSpace.setCoordinate(start);

        float[] targetAxes = new float[StyleVector.DIMENSIONS];
        for (int i = 0; i < StyleVector.DIMENSIONS; i++) targetAxes[i] = 1.0f;
        StyleVector target = new StyleVector(targetAxes);

        styleSpace.driftToward(target, 0.5f);

        for (float v : styleSpace.getCoordinate().axes) {
            assertEquals(0.5f, v, 1e-5f, "After 50% drift each axis should be 0.5");
        }
    }

    @Test
    void driftForIntensityTier_tier4MovesCoordinateTowardFast() {
        styleSpace.setCoordinate(archTech.centroid);
        float streamBefore = styleSpace.getCoordinate().axes[11];

        styleSpace.driftForIntensityTier(4, 1.0f); // full jump to target

        float streamAfter = styleSpace.getCoordinate().axes[11];
        assertTrue(streamAfter > streamBefore,
                "Peak tier should drift toward higher streamRatio");
    }

    @Test
    void driftForIntensityTier_tier0MovesCoordinateTowardTech() {
        styleSpace.setCoordinate(archFast.centroid);
        float streamBefore = styleSpace.getCoordinate().axes[11];

        styleSpace.driftForIntensityTier(0, 1.0f);

        float streamAfter = styleSpace.getCoordinate().axes[11];
        assertTrue(streamAfter < streamBefore,
                "Calm tier should drift toward lower streamRatio");
    }

    @Test
    void emptyStyleSpace_returnsNullProbability() {
        StyleSpace empty = new StyleSpace(List.of());
        assertNull(empty.getProbability(null, null, 0.5f, null, 1.0f, true));
    }

    @Test
    void getProbability_withNullPrevFallsToBaseline() {
        // No prev → falls back to baseline (null here) → returns null gracefully
        assertNull(styleSpace.getProbability(null, null, 0.5f, null, 1.0f, true));
    }
}
