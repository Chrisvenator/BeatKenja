import BeatSaberObjects.Objects.BeatSaberMap;
import BeatSaberObjects.Objects.Note;
import DataManager.FileManager;
import MapGeneration.GenerationElements.Pattern;
import MapGeneration.GenerationElements.PatternProbability;
import com.google.gson.Gson;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PatternProbabilityTest {

    @Test
    void removeNulls() {
        BeatSaberMap map = new Gson().fromJson(FileManager.readFile("test/resources/Template--ISeeFire.txt").get(0), BeatSaberMap.class);
        Pattern p = new Pattern(map._notes, 1);

        for (Note n : new PatternProbability(p.patterns[10], p.probabilities[10]).notes) assertNotNull(n);
        for (Note n : new PatternProbability(p.patterns[100], p.probabilities[100]).notes) assertNotNull(n);
        for (Note n : new PatternProbability(p.patterns[29], p.probabilities[29]).notes) assertNotNull(n);
        for (Note n : new PatternProbability(p.patterns[32], p.probabilities[32]).notes) assertNotNull(n);
        for (Note n : new PatternProbability(p.patterns[46], p.probabilities[46]).notes) assertNotNull(n);
        for (Note n : new PatternProbability(p.patterns[52], p.probabilities[52]).notes) assertNotNull(n);
        for (Note n : new PatternProbability(p.patterns[67], p.probabilities[67]).notes) assertNotNull(n);


    }

    @Test
    void testToString() {
        BeatSaberMap map = new Gson().fromJson(FileManager.readFile("test/resources/Template--ISeeFire.txt").get(0), BeatSaberMap.class);
        Pattern p = new Pattern(map._notes, 1);

        assertEquals("""
                {"_time":0.0,"_lineIndex":0,"_lineLayer":0,"_type":1,"_cutDirection":8}: [
                  {"_time":0.0,"_lineIndex":2,"_lineLayer":0,"_type":1,"_cutDirection":5}: 100.0%
                ]""", new PatternProbability(p.patterns[10], p.probabilities[10]).toString());
    }
}