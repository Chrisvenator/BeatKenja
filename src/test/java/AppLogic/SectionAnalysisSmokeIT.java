package AppLogic;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/** One-shot smoke test of the Song Map service on a real corpus song (needs local corpus). */
@Tag("benchmark")
class SectionAnalysisSmokeIT {

    @Test
    void analyzesARealSong() throws Exception {
        File wav = new File("data/ground_truth/.wav_cache/1a32a.wav"); // Oyasumi
        assumeTrue(wav.exists(), "corpus wav cache missing");

        SectionAnalysisService.SectionAnalysis analysis = SectionAnalysisService.analyze(wav);

        System.out.printf("duration %.1fs, BPM %.1f, %d onsets, %d sections%n",
                analysis.durationSeconds(), analysis.estimatedBpm(),
                analysis.onsetTimesSeconds().length, analysis.tiers().length);
        for (int s = 0; s < analysis.tiers().length; s++) {
            double start = s == 0 ? 0 : analysis.boundaries().get(s - 1);
            System.out.printf("  section %d: %.1fs tier %d (%s)%n", s, start, analysis.tiers()[s],
                    SectionAnalysisService.TIER_FLAGS[analysis.tiers()[s]]);
        }

        assertTrue(analysis.tiers().length >= 2, "should find some structure");
        assertFalse(SectionAnalysisService.toBookmarks(analysis, 200.0).isEmpty());
    }
}
