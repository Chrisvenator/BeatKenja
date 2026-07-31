package MapGeneration;

import BeatSaberObjects.Objects.Note;
import DataManager.Corpus.GapBucket;
import MapGeneration.GenerationElements.HigherOrderPattern;
import MapGeneration.GenerationElements.Pattern;
import MapGeneration.GenerationElements.PatternProbability;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class HigherOrderPatternTest {

    private HigherOrderPattern hop;
    private Note a, b, c, d;

    @BeforeEach
    void setUp() {
        hop = new HigherOrderPattern();
        // Blue notes (type=1) with valid placements
        a = new Note(0, 1, 0, 1, 1);  // bottom-center down
        b = new Note(1, 2, 0, 1, 0);  // bottom-right up
        c = new Note(2, 1, 1, 1, 1);  // mid-center down
        d = new Note(3, 0, 0, 1, 0);  // bottom-left up
    }

    @Test
    void observe_increasesObservationCount() {
        hop.observe(a, b, c, 0.5f, 1);
        assertEquals(1, hop.getTotalObservations());
    }

    @Test
    void trainFrom_countsTriples() {
        Note[] notes = {a, b, c, d};
        hop.trainFrom(notes, 1, 1);
        // Two triples: (a,b,c) and (b,c,d)
        assertEquals(2, hop.getTotalObservations());
        assertEquals(2, hop.getSecondOrderStateCount());
    }

    @Test
    void getProbability_returnsSecondOrderWhenSufficientData() {
        // Observe the same triple 10× so it clears BACKOFF_THRESHOLD=5
        for (int i = 0; i < 10; i++) hop.observe(a, b, c, 0.5f, 1);

        PatternProbability pp = hop.getProbability(a, b, 0.5f, null, 5.0f);

        assertNotNull(pp, "Should return 2nd-order result");
        assertEquals(1, pp.notes.length, "Only one successor seen");
        assertEquals(1, pp.notes[0]._cutDirection, "Successor cut direction should match c");
    }

    @Test
    void getProbability_fallsBackTo1stOrderWhenInsufficient2nd() {
        // Only 2 observations for this triple — below threshold
        hop.observe(a, b, c, 0.5f, 2);
        // But 10 observations at 1st-order (b→c via different prevPrev)
        Note other = new Note(0, 0, 2, 1, 5);
        for (int i = 0; i < 10; i++) hop.observe(other, b, c, 0.5f, 1);

        PatternProbability pp = hop.getProbability(a, b, 0.5f, null, 5.0f);
        // 2nd-order too sparse; should use 1st-order (b→c, 12 total via b)
        assertNotNull(pp);
    }

    @Test
    void getProbability_fallsBackToBaselineWhenNothingElse() {
        // No training at all; provide a baseline Pattern
        try {
            Note[] train = {a, b, c, d, a, b, c, d, a, b, c, d};
            Pattern baseline = new Pattern(train, 1);

            PatternProbability pp = hop.getProbability(null, b, 0.5f, baseline, 5.0f);
            // baseline.getProbabilityOf(b) should return something
            assertNotNull(pp, "Should fall through to baseline");
        } catch (Exception e) {
            fail("Unexpected exception: " + e.getMessage());
        }
    }

    @Test
    void toIndex_roundTrips() {
        Note n = new Note(0, 2, 1, 1, 3);
        int idx = HigherOrderPattern.toIndex(n);
        assertTrue(idx >= 0 && idx < 108);
        Note back = HigherOrderPattern.indexToNote(idx, 1);
        assertNotNull(back);
        assertEquals((int) n._lineIndex,  (int) back._lineIndex);
        assertEquals((int) n._lineLayer,  (int) back._lineLayer);
        assertEquals(n._cutDirection,      back._cutDirection);
    }

    @Test
    void gapBucket_streamThreshold() {
        assertEquals(GapBucket.STREAM, GapBucket.fromBeatGap(0.0f));
        assertEquals(GapBucket.STREAM, GapBucket.fromBeatGap(0.25f));
        assertEquals(GapBucket.NORMAL, GapBucket.fromBeatGap(0.5f));
        assertEquals(GapBucket.SPARSE, GapBucket.fromBeatGap(1.5f));
    }
}
