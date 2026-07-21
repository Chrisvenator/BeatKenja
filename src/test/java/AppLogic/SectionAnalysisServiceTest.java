package AppLogic;

import BeatSaberObjects.Objects.Bookmark;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Unit tests for the pure conversion logic of {@link SectionAnalysisService}.
 */
class SectionAnalysisServiceTest {

    @Test
    void toBookmarksConvertsSecondsToBeatsAndNamesByTier() {
        ArrayList<Double> boundaries = new ArrayList<>(List.of(30.0, 60.0));
        int[] tiers = {0, 2, 4}; // calm, normal, peak
        SectionAnalysisService.SectionAnalysis analysis = new SectionAnalysisService.SectionAnalysis(
                boundaries, tiers, new double[0], new double[0], new double[0], 90.0, 120.0, null);

        List<Bookmark> bookmarks = SectionAnalysisService.toBookmarks(analysis, 120.0); // 2 beats/sec

        assertEquals(3, bookmarks.size());
        assertEquals(0.0f, bookmarks.get(0)._time);
        assertEquals(SectionAnalysisService.TIER_FLAGS[0], bookmarks.get(0)._name);
        assertEquals(60.0f, bookmarks.get(1)._time); // 30 s * 2 beats/s
        assertEquals(SectionAnalysisService.TIER_FLAGS[2], bookmarks.get(1)._name);
        assertEquals(120.0f, bookmarks.get(2)._time);
        assertEquals(SectionAnalysisService.TIER_FLAGS[4], bookmarks.get(2)._name);
    }

    @Test
    void tierTablesStayInSync() {
        assertEquals(SectionAnalysisService.TIER_FLAGS.length, SectionAnalysisService.TIER_COLORS.length);
        assertEquals(AudioAnalysis.FooteSectionDetector.INTENSITY_TIERS, SectionAnalysisService.TIER_FLAGS.length);
    }
}
