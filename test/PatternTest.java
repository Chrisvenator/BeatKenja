import BeatSaberObjects.Objects.BeatSaberMap;
import BeatSaberObjects.Objects.Note;
import DataManager.FileManager;
import MapGeneration.GenerationElements.Pattern;
import com.google.gson.Gson;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PatternTest {

    @Test
    void analyzePattern() {
        BeatSaberMap map = new Gson().fromJson(FileManager.readFile("test/resources/Template--ISeeFire.txt").get(0), BeatSaberMap.class);
        Pattern p = new Pattern(map._notes, 2);

        p.count = new int[108][108];
        p.patterns = new Note[108][108];

        p.analyzePattern(map._notes, 1);

//        System.out.println(p.patterns[0][0].toString());
//        System.out.println(p.patterns[1][1].toString());
//        System.out.println(p.patterns[2][2].toString());
//        System.out.println(p.patterns[3][3].toString());
//        System.out.println(p.patterns[4][4]);
//        System.out.println(p.patterns[5][5]);
//        System.out.println(p.patterns[100][100]);

        assertEquals("{\"_time\":6.5,\"_lineIndex\":2,\"_lineLayer\":0,\"_type\":1,\"_cutDirection\":1}\n", p.patterns[0][0].toString());
        assertEquals("{\"_time\":10.0,\"_lineIndex\":3,\"_lineLayer\":0,\"_type\":1,\"_cutDirection\":0}\n", p.patterns[1][1].toString());
        assertEquals("{\"_time\":20.0,\"_lineIndex\":3,\"_lineLayer\":1,\"_type\":1,\"_cutDirection\":1}\n", p.patterns[2][2].toString());
        assertEquals("{\"_time\":104.25,\"_lineIndex\":3,\"_lineLayer\":0,\"_type\":1,\"_cutDirection\":4}\n", p.patterns[3][3].toString());
        assertNull(p.patterns[4][4]);
        assertEquals("{\"_time\":301.5,\"_lineIndex\":1,\"_lineLayer\":2,\"_type\":1,\"_cutDirection\":4}\n", p.patterns[5][5].toString());
        assertNull(p.patterns[100][100]);
    }

    @Test
    void testToString() {
        BeatSaberMap map = new Gson().fromJson(FileManager.readFile("test/resources/Template--ISeeFire.txt").get(0), BeatSaberMap.class);
        Pattern p = new Pattern(map._notes, 1);

        String replace = p.toString()
                .replace("\n", "")
                .replace(" ", "");

        assertEquals(jsonExample.replace("\n", "").replace(" ", ""), replace);


        Pattern p2 = new Pattern("test/resources/Template--ISeeFire.txt");
        assertEquals(replace, p2.toString().replace("\n", "").replace(" ", ""));
    }

    @Test
    void TestExportInPatFormatAndReadInPatFormat() {
        String path = "test/resources/Template--ISeeFire.txt";
        BeatSaberMap map = new Gson().fromJson(FileManager.readFile(path).get(0), BeatSaberMap.class);

        Pattern p = new Pattern(map._notes, 1);
        Pattern p2 = new Pattern(path.replace(".txt", ".pat"));
        assertEquals(p.toString(), p2.toString());
    }

    @Test
    void removeXTimes() {
        BeatSaberMap map = new Gson().fromJson(FileManager.readFile("test/resources/Template--ISeeFire.txt").get(0), BeatSaberMap.class);
        Pattern p = new Pattern(map._notes, 1);
        p.removeXTimes(1);
        assertEquals(26, p.asList().size());
        p.removeXTimes(3);
        assertEquals(11, p.asList().size());
        p.removeXTimes(4);
        assertEquals(10, p.asList().size());
        p.removeXTimes(5);
        assertEquals(10, p.asList().size());
        p.removeXTimes(6);
        assertEquals(9, p.asList().size());
        p.removeXTimes(7);
        assertEquals(9, p.asList().size());
        p.removeXTimes(8);
        assertEquals(9, p.asList().size());
        p.removeXTimes(9);
        assertEquals(9, p.asList().size());
        p.removeXTimes(10);
        assertEquals(5, p.asList().size());
        assertEquals(2, p.asList().get(1).size());
        assertEquals(2, p.asList().get(2).size());
        assertEquals(3, p.asList().get(3).size());
        assertEquals(2, p.asList().get(4).size());
    }


    @Test
    void computeProbabilities() {
        BeatSaberMap map = new Gson().fromJson(FileManager.readFile("test/resources/Template--ISeeFire.txt").get(0), BeatSaberMap.class);
        Pattern p = new Pattern(map._notes, 1);

        p.computeProbabilities();
        p.analyzePattern(map._notes, 1);

//        System.out.println(p.probabilities[0][0]);
//        System.out.println(p.probabilities[1][1]);
//        System.out.println(p.probabilities[2][2]);
//        System.out.println(p.probabilities[3][3]);
//        System.out.println(p.probabilities[4][4]);
//        System.out.println(p.probabilities[5][5]);
//        System.out.println(p.probabilities[100][100]);

        assertEquals(0.0, p.probabilities[0][0]);
        assertEquals(33.333335876464844, p.probabilities[1][1]);
        assertEquals(30.000001907348633, p.probabilities[2][2]);
        assertEquals(10.0, p.probabilities[3][3]);
        assertEquals(0.0, p.probabilities[4][4]);
        assertEquals(25.0, p.probabilities[5][5]);
        assertEquals(0.0, p.probabilities[100][100]);
    }

    @Test
    void getProbabilityOf() {
        BeatSaberMap map = new Gson().fromJson(FileManager.readFile("test/resources/Template--ISeeFire.txt").get(0), BeatSaberMap.class);
        Pattern p = new Pattern(map._notes, 1);

        assertEquals("""
                {"_time":0.0,"_lineIndex":0,"_lineLayer":0,"_type":1,"_cutDirection":8}: [
                  {"_time":0.0,"_lineIndex":2,"_lineLayer":0,"_type":1,"_cutDirection":5}: 100.0%
                ]""", p.getProbabilityOf(new Note(0)).toString());
    }

    @Test
    void testMergePatterns() {
        BeatSaberMap map = new Gson().fromJson(FileManager.readFile("test/resources/Template--ISeeFire.txt").get(0), BeatSaberMap.class);
        Pattern p1 = new Pattern(map._notes, 1);
        Pattern p2 = new Pattern(map._notes, 1);
        p1.merge(p2);

        for (int i = 0; i < p2.count.length; i++) {
            if (p2.count[i] == null) break;
            for (int j = 0; j < p2.count[i].length; j++) {
                if (p2.count[i][j] == 0) continue;
                assertEquals((2 * p2.count[i][j]), p1.count[i][j]);
            }
        }

        Pattern pattern1 = new Pattern("./test/resources/patterns/test1.pat");
        Pattern pattern2 = new Pattern("./test/resources/patterns/test2.pat");
        Pattern pattern3 = new Pattern("./test/resources/patterns/test3.pat");

        pattern1.merge(pattern2);
        for (int i = 0; i < pattern2.count.length; i++) {
            if (pattern2.count[i] == null) break;
            for (int j = 0; j < pattern2.count[i].length; j++) {
                if (pattern2.count[i][j] == 0) continue;
                assertEquals((2 * pattern2.count[i][j]), pattern1.count[i][j]);
            }
        }
        assertEquals(pattern1.toString(), """
                {"_time":0.0,"_lineIndex":3,"_lineLayer":2,"_type":1,"_cutDirection":6}: [
                  {"_time":0.0,"_lineIndex":2,"_lineLayer":2,"_type":1,"_cutDirection":0}: 2 times = 100.0% ,\s
                ]\s
                {"_time":0.0,"_lineIndex":2,"_lineLayer":1,"_type":1,"_cutDirection":1}: [
                  {"_time":0.0,"_lineIndex":2,"_lineLayer":0,"_type":1,"_cutDirection":1}: 2 times = 100.0% ,\s
                ]\s
                """);


        pattern1.merge(pattern3);
        assertEquals(pattern1.toString(), """
                {"_time":0.0,"_lineIndex":3,"_lineLayer":2,"_type":1,"_cutDirection":6}: [
                  {"_time":0.0,"_lineIndex":2,"_lineLayer":2,"_type":1,"_cutDirection":0}: 3 times = 75.0% ,\s
                  {"_time":0.0,"_lineIndex":2,"_lineLayer":2,"_type":2,"_cutDirection":0}: 1 times = 25.0% ,\s
                ]\s
                {"_time":0.0,"_lineIndex":2,"_lineLayer":1,"_type":1,"_cutDirection":1}: [
                  {"_time":0.0,"_lineIndex":2,"_lineLayer":0,"_type":1,"_cutDirection":1}: 2 times = 100.0% ,\s
                ]\s
                {"_time":0.0,"_lineIndex":1,"_lineLayer":2,"_type":1,"_cutDirection":2}: [
                  {"_time":0.0,"_lineIndex":3,"_lineLayer":2,"_type":1,"_cutDirection":3}: 1 times = 100.0% ,\s
                ]\s
                {"_time":0.0,"_lineIndex":3,"_lineLayer":2,"_type":1,"_cutDirection":3}: [
                  {"_time":0.0,"_lineIndex":2,"_lineLayer":2,"_type":1,"_cutDirection":2}: 1 times = 100.0% ,\s
                ]\s
                {"_time":0.0,"_lineIndex":2,"_lineLayer":2,"_type":1,"_cutDirection":2}: [
                  {"_time":0.0,"_lineIndex":3,"_lineLayer":1,"_type":1,"_cutDirection":7}: 1 times = 100.0% ,\s
                ]\s
                """);

    }

    @Test
    void asList() {
        BeatSaberMap map = new Gson().fromJson(FileManager.readFile("test/resources/Template--ISeeFire.txt").get(0), BeatSaberMap.class);
        Pattern p = new Pattern(map._notes, 1);
        assertEquals("""
                [[{"_time":0.0,"_lineIndex":2,"_lineLayer":0,"_type":1,"_cutDirection":1}
                , {"_time":0.0,"_lineIndex":2,"_lineLayer":2,"_type":1,"_cutDirection":1}
                , {"_time":0.0,"_lineIndex":1,"_lineLayer":0,"_type":1,"_cutDirection":5}
                , {"_time":0.0,"_lineIndex":3,"_lineLayer":0,"_type":1,"_cutDirection":7}
                , {"_time":0.0,"_lineIndex":2,"_lineLayer":2,"_type":1,"_cutDirection":0}
                , {"_time":0.0,"_lineIndex":3,"_lineLayer":2,"_type":1,"_cutDirection":0}
                , {"_time":0.0,"_lineIndex":3,"_lineLayer":1,"_type":1,"_cutDirection":5}
                , {"_time":0.0,"_lineIndex":3,"_lineLayer":1,"_type":1,"_cutDirection":0}
                , {"_time":0.0,"_lineIndex":1,"_lineLayer":2,"_type":1,"_cutDirection":4}
                , {"_time":0.0,"_lineIndex":3,"_lineLayer":2,"_type":1,"_cutDirection":5}
                , {"_time":0.0,"_lineIndex":1,"_lineLayer":0,"_type":1,"_cutDirection":0}
                , {"_time":0.0,"_lineIndex":3,"_lineLayer":0,"_type":1,"_cutDirection":0}
                , {"_time":0.0,"_lineIndex":2,"_lineLayer":0,"_type":1,"_cutDirection":0}
                , {"_time":0.0,"_lineIndex":3,"_lineLayer":0,"_type":1,"_cutDirection":1}
                , {"_time":0.0,"_lineIndex":2,"_lineLayer":0,"_type":1,"_cutDirection":1}
                , {"_time":0.0,"_lineIndex":1,"_lineLayer":0,"_type":1,"_cutDirection":1}
                ], [{"_time":0.0,"_lineIndex":2,"_lineLayer":2,"_type":1,"_cutDirection":1}
                , {"_time":0.0,"_lineIndex":3,"_lineLayer":0,"_type":1,"_cutDirection":0}
                , {"_time":0.0,"_lineIndex":2,"_lineLayer":0,"_type":1,"_cutDirection":1}
                , {"_time":0.0,"_lineIndex":2,"_lineLayer":1,"_type":1,"_cutDirection":1}
                ], [{"_time":0.0,"_lineIndex":3,"_lineLayer":2,"_type":1,"_cutDirection":8}
                , {"_time":0.0,"_lineIndex":3,"_lineLayer":2,"_type":1,"_cutDirection":1}
                , {"_time":0.0,"_lineIndex":3,"_lineLayer":1,"_type":1,"_cutDirection":1}
                , {"_time":0.0,"_lineIndex":3,"_lineLayer":1,"_type":1,"_cutDirection":7}
                , {"_time":0.0,"_lineIndex":1,"_lineLayer":0,"_type":1,"_cutDirection":6}
                , {"_time":0.0,"_lineIndex":3,"_lineLayer":0,"_type":1,"_cutDirection":1}
                ], [{"_time":0.0,"_lineIndex":3,"_lineLayer":0,"_type":1,"_cutDirection":8}
                , {"_time":0.0,"_lineIndex":3,"_lineLayer":0,"_type":1,"_cutDirection":0}
                , {"_time":0.0,"_lineIndex":2,"_lineLayer":0,"_type":1,"_cutDirection":0}
                , {"_time":0.0,"_lineIndex":3,"_lineLayer":0,"_type":1,"_cutDirection":4}
                , {"_time":0.0,"_lineIndex":3,"_lineLayer":1,"_type":1,"_cutDirection":0}
                ], [{"_time":0.0,"_lineIndex":0,"_lineLayer":2,"_type":1,"_cutDirection":8}
                , {"_time":0.0,"_lineIndex":3,"_lineLayer":0,"_type":1,"_cutDirection":7}
                ], [{"_time":0.0,"_lineIndex":3,"_lineLayer":0,"_type":1,"_cutDirection":7}
                , {"_time":0.0,"_lineIndex":3,"_lineLayer":0,"_type":1,"_cutDirection":0}
                , {"_time":0.0,"_lineIndex":2,"_lineLayer":2,"_type":1,"_cutDirection":4}
                , {"_time":0.0,"_lineIndex":3,"_lineLayer":1,"_type":1,"_cutDirection":0}
                , {"_time":0.0,"_lineIndex":2,"_lineLayer":2,"_type":1,"_cutDirection":0}
                , {"_time":0.0,"_lineIndex":1,"_lineLayer":2,"_type":1,"_cutDirection":4}
                , {"_time":0.0,"_lineIndex":2,"_lineLayer":0,"_type":1,"_cutDirection":4}
                ], [{"_time":0.0,"_lineIndex":2,"_lineLayer":0,"_type":1,"_cutDirection":0}
                , {"_time":0.0,"_lineIndex":2,"_lineLayer":2,"_type":1,"_cutDirection":0}
                , {"_time":0.0,"_lineIndex":3,"_lineLayer":1,"_type":1,"_cutDirection":7}
                , {"_time":0.0,"_lineIndex":1,"_lineLayer":0,"_type":1,"_cutDirection":1}
                , {"_time":0.0,"_lineIndex":2,"_lineLayer":0,"_type":1,"_cutDirection":1}
                , {"_time":0.0,"_lineIndex":3,"_lineLayer":0,"_type":1,"_cutDirection":1}
                , {"_time":0.0,"_lineIndex":1,"_lineLayer":0,"_type":1,"_cutDirection":6}
                ], [{"_time":0.0,"_lineIndex":2,"_lineLayer":2,"_type":1,"_cutDirection":0}
                , {"_time":0.0,"_lineIndex":2,"_lineLayer":0,"_type":1,"_cutDirection":6}
                , {"_time":0.0,"_lineIndex":3,"_lineLayer":1,"_type":1,"_cutDirection":6}
                , {"_time":0.0,"_lineIndex":3,"_lineLayer":2,"_type":1,"_cutDirection":1}
                , {"_time":0.0,"_lineIndex":2,"_lineLayer":2,"_type":1,"_cutDirection":1}
                , {"_time":0.0,"_lineIndex":2,"_lineLayer":0,"_type":1,"_cutDirection":1}
                , {"_time":0.0,"_lineIndex":3,"_lineLayer":1,"_type":1,"_cutDirection":7}
                , {"_time":0.0,"_lineIndex":3,"_lineLayer":0,"_type":1,"_cutDirection":1}
                , {"_time":0.0,"_lineIndex":3,"_lineLayer":0,"_type":1,"_cutDirection":7}
                , {"_time":0.0,"_lineIndex":3,"_lineLayer":1,"_type":1,"_cutDirection":1}
                , {"_time":0.0,"_lineIndex":1,"_lineLayer":0,"_type":1,"_cutDirection":1}
                , {"_time":0.0,"_lineIndex":1,"_lineLayer":0,"_type":1,"_cutDirection":6}
                ], [{"_time":0.0,"_lineIndex":2,"_lineLayer":0,"_type":1,"_cutDirection":6}
                , {"_time":0.0,"_lineIndex":3,"_lineLayer":1,"_type":1,"_cutDirection":6}
                , {"_time":0.0,"_lineIndex":3,"_lineLayer":1,"_type":1,"_cutDirection":5}
                , {"_time":0.0,"_lineIndex":3,"_lineLayer":2,"_type":1,"_cutDirection":5}
                , {"_time":0.0,"_lineIndex":2,"_lineLayer":2,"_type":1,"_cutDirection":0}
                , {"_time":0.0,"_lineIndex":3,"_lineLayer":2,"_type":1,"_cutDirection":0}
                , {"_time":0.0,"_lineIndex":3,"_lineLayer":1,"_type":1,"_cutDirection":3}
                , {"_time":0.0,"_lineIndex":2,"_lineLayer":0,"_type":1,"_cutDirection":5}
                , {"_time":0.0,"_lineIndex":3,"_lineLayer":1,"_type":1,"_cutDirection":0}
                ], [{"_time":0.0,"_lineIndex":3,"_lineLayer":1,"_type":1,"_cutDirection":6}
                , {"_time":0.0,"_lineIndex":1,"_lineLayer":0,"_type":1,"_cutDirection":5}
                , {"_time":0.0,"_lineIndex":3,"_lineLayer":2,"_type":1,"_cutDirection":5}
                ], [{"_time":0.0,"_lineIndex":0,"_lineLayer":0,"_type":1,"_cutDirection":8}
                , {"_time":0.0,"_lineIndex":2,"_lineLayer":0,"_type":1,"_cutDirection":5}
                ], [{"_time":0.0,"_lineIndex":2,"_lineLayer":0,"_type":1,"_cutDirection":5}
                , {"_time":0.0,"_lineIndex":3,"_lineLayer":1,"_type":1,"_cutDirection":5}
                , {"_time":0.0,"_lineIndex":3,"_lineLayer":0,"_type":1,"_cutDirection":1}
                ], [{"_time":0.0,"_lineIndex":3,"_lineLayer":1,"_type":1,"_cutDirection":5}
                , {"_time":0.0,"_lineIndex":3,"_lineLayer":2,"_type":1,"_cutDirection":1}
                , {"_time":0.0,"_lineIndex":2,"_lineLayer":0,"_type":1,"_cutDirection":6}
                , {"_time":0.0,"_lineIndex":2,"_lineLayer":0,"_type":1,"_cutDirection":1}
                , {"_time":0.0,"_lineIndex":1,"_lineLayer":0,"_type":1,"_cutDirection":6}
                , {"_time":0.0,"_lineIndex":3,"_lineLayer":1,"_type":1,"_cutDirection":1}
                , {"_time":0.0,"_lineIndex":0,"_lineLayer":0,"_type":1,"_cutDirection":6}
                , {"_time":0.0,"_lineIndex":3,"_lineLayer":1,"_type":1,"_cutDirection":6}
                , {"_time":0.0,"_lineIndex":1,"_lineLayer":1,"_type":1,"_cutDirection":6}
                ], [{"_time":0.0,"_lineIndex":1,"_lineLayer":2,"_type":1,"_cutDirection":8}
                , {"_time":0.0,"_lineIndex":2,"_lineLayer":2,"_type":1,"_cutDirection":1}
                ], [{"_time":0.0,"_lineIndex":1,"_lineLayer":0,"_type":1,"_cutDirection":5}
                , {"_time":0.0,"_lineIndex":3,"_lineLayer":2,"_type":1,"_cutDirection":5}
                , {"_time":0.0,"_lineIndex":1,"_lineLayer":0,"_type":1,"_cutDirection":6}
                , {"_time":0.0,"_lineIndex":2,"_lineLayer":0,"_type":1,"_cutDirection":1}
                ], [{"_time":0.0,"_lineIndex":3,"_lineLayer":2,"_type":1,"_cutDirection":5}
                , {"_time":0.0,"_lineIndex":1,"_lineLayer":0,"_type":1,"_cutDirection":6}
                , {"_time":0.0,"_lineIndex":3,"_lineLayer":0,"_type":1,"_cutDirection":1}
                , {"_time":0.0,"_lineIndex":2,"_lineLayer":0,"_type":1,"_cutDirection":6}
                , {"_time":0.0,"_lineIndex":2,"_lineLayer":0,"_type":1,"_cutDirection":1}
                , {"_time":0.0,"_lineIndex":3,"_lineLayer":1,"_type":1,"_cutDirection":1}
                , {"_time":0.0,"_lineIndex":0,"_lineLayer":1,"_type":1,"_cutDirection":2}
                , {"_time":0.0,"_lineIndex":0,"_lineLayer":0,"_type":1,"_cutDirection":6}
                ], [{"_time":0.0,"_lineIndex":1,"_lineLayer":0,"_type":1,"_cutDirection":6}
                , {"_time":0.0,"_lineIndex":3,"_lineLayer":2,"_type":1,"_cutDirection":6}
                , {"_time":0.0,"_lineIndex":2,"_lineLayer":0,"_type":1,"_cutDirection":5}
                , {"_time":0.0,"_lineIndex":3,"_lineLayer":0,"_type":1,"_cutDirection":5}
                , {"_time":0.0,"_lineIndex":3,"_lineLayer":1,"_type":1,"_cutDirection":5}
                , {"_time":0.0,"_lineIndex":2,"_lineLayer":2,"_type":1,"_cutDirection":5}
                , {"_time":0.0,"_lineIndex":3,"_lineLayer":2,"_type":1,"_cutDirection":5}
                , {"_time":0.0,"_lineIndex":1,"_lineLayer":0,"_type":1,"_cutDirection":5}
                , {"_time":0.0,"_lineIndex":2,"_lineLayer":0,"_type":1,"_cutDirection":0}
                , {"_time":0.0,"_lineIndex":2,"_lineLayer":2,"_type":1,"_cutDirection":0}
                , {"_time":0.0,"_lineIndex":3,"_lineLayer":1,"_type":1,"_cutDirection":3}
                ], [{"_time":0.0,"_lineIndex":3,"_lineLayer":2,"_type":1,"_cutDirection":6}
                , {"_time":0.0,"_lineIndex":2,"_lineLayer":2,"_type":1,"_cutDirection":0}
                ], [{"_time":0.0,"_lineIndex":2,"_lineLayer":1,"_type":1,"_cutDirection":1}
                , {"_time":0.0,"_lineIndex":2,"_lineLayer":0,"_type":1,"_cutDirection":1}
                ], [{"_time":0.0,"_lineIndex":2,"_lineLayer":2,"_type":1,"_cutDirection":4}
                , {"_time":0.0,"_lineIndex":2,"_lineLayer":0,"_type":1,"_cutDirection":1}
                , {"_time":0.0,"_lineIndex":3,"_lineLayer":1,"_type":1,"_cutDirection":3}
                , {"_time":0.0,"_lineIndex":3,"_lineLayer":2,"_type":1,"_cutDirection":7}
                , {"_time":0.0,"_lineIndex":3,"_lineLayer":1,"_type":1,"_cutDirection":1}
                ], [{"_time":0.0,"_lineIndex":3,"_lineLayer":2,"_type":1,"_cutDirection":0}
                , {"_time":0.0,"_lineIndex":1,"_lineLayer":0,"_type":1,"_cutDirection":6}
                , {"_time":0.0,"_lineIndex":2,"_lineLayer":0,"_type":1,"_cutDirection":1}
                , {"_time":0.0,"_lineIndex":3,"_lineLayer":0,"_type":1,"_cutDirection":1}
                , {"_time":0.0,"_lineIndex":2,"_lineLayer":0,"_type":1,"_cutDirection":6}
                , {"_time":0.0,"_lineIndex":3,"_lineLayer":1,"_type":1,"_cutDirection":1}
                ], [{"_time":0.0,"_lineIndex":3,"_lineLayer":1,"_type":1,"_cutDirection":7}
                , {"_time":0.0,"_lineIndex":1,"_lineLayer":2,"_type":1,"_cutDirection":4}
                , {"_time":0.0,"_lineIndex":2,"_lineLayer":2,"_type":1,"_cutDirection":4}
                , {"_time":0.0,"_lineIndex":3,"_lineLayer":1,"_type":1,"_cutDirection":0}
                , {"_time":0.0,"_lineIndex":3,"_lineLayer":1,"_type":1,"_cutDirection":4}
                , {"_time":0.0,"_lineIndex":2,"_lineLayer":2,"_type":1,"_cutDirection":0}
                ], [{"_time":0.0,"_lineIndex":1,"_lineLayer":2,"_type":1,"_cutDirection":4}
                , {"_time":0.0,"_lineIndex":2,"_lineLayer":0,"_type":1,"_cutDirection":1}
                , {"_time":0.0,"_lineIndex":3,"_lineLayer":0,"_type":1,"_cutDirection":7}
                , {"_time":0.0,"_lineIndex":3,"_lineLayer":1,"_type":1,"_cutDirection":7}
                ], [{"_time":0.0,"_lineIndex":3,"_lineLayer":1,"_type":1,"_cutDirection":0}
                , {"_time":0.0,"_lineIndex":3,"_lineLayer":0,"_type":1,"_cutDirection":1}
                , {"_time":0.0,"_lineIndex":3,"_lineLayer":2,"_type":1,"_cutDirection":0}
                , {"_time":0.0,"_lineIndex":2,"_lineLayer":0,"_type":1,"_cutDirection":6}
                , {"_time":0.0,"_lineIndex":1,"_lineLayer":0,"_type":1,"_cutDirection":6}
                , {"_time":0.0,"_lineIndex":2,"_lineLayer":0,"_type":1,"_cutDirection":1}
                , {"_time":0.0,"_lineIndex":1,"_lineLayer":0,"_type":1,"_cutDirection":1}
                , {"_time":0.0,"_lineIndex":3,"_lineLayer":1,"_type":1,"_cutDirection":1}
                ], [{"_time":0.0,"_lineIndex":3,"_lineLayer":0,"_type":1,"_cutDirection":1}
                , {"_time":0.0,"_lineIndex":2,"_lineLayer":2,"_type":1,"_cutDirection":0}
                , {"_time":0.0,"_lineIndex":3,"_lineLayer":1,"_type":1,"_cutDirection":0}
                , {"_time":0.0,"_lineIndex":3,"_lineLayer":2,"_type":1,"_cutDirection":0}
                , {"_time":0.0,"_lineIndex":3,"_lineLayer":0,"_type":1,"_cutDirection":0}
                , {"_time":0.0,"_lineIndex":3,"_lineLayer":1,"_type":1,"_cutDirection":5}
                , {"_time":0.0,"_lineIndex":2,"_lineLayer":0,"_type":1,"_cutDirection":1}
                , {"_time":0.0,"_lineIndex":3,"_lineLayer":1,"_type":1,"_cutDirection":1}
                , {"_time":0.0,"_lineIndex":2,"_lineLayer":0,"_type":1,"_cutDirection":0}
                ], [{"_time":0.0,"_lineIndex":3,"_lineLayer":1,"_type":1,"_cutDirection":3}
                , {"_time":0.0,"_lineIndex":1,"_lineLayer":2,"_type":1,"_cutDirection":2}
                , {"_time":0.0,"_lineIndex":0,"_lineLayer":1,"_type":1,"_cutDirection":2}
                , {"_time":0.0,"_lineIndex":1,"_lineLayer":0,"_type":1,"_cutDirection":6}
                , {"_time":0.0,"_lineIndex":2,"_lineLayer":0,"_type":1,"_cutDirection":6}
                , {"_time":0.0,"_lineIndex":0,"_lineLayer":0,"_type":1,"_cutDirection":6}
                ], [{"_time":0.0,"_lineIndex":1,"_lineLayer":2,"_type":1,"_cutDirection":2}
                , {"_time":0.0,"_lineIndex":3,"_lineLayer":2,"_type":1,"_cutDirection":3}
                ], [{"_time":0.0,"_lineIndex":3,"_lineLayer":2,"_type":1,"_cutDirection":3}
                , {"_time":0.0,"_lineIndex":2,"_lineLayer":2,"_type":1,"_cutDirection":2}
                ], [{"_time":0.0,"_lineIndex":2,"_lineLayer":2,"_type":1,"_cutDirection":2}
                , {"_time":0.0,"_lineIndex":3,"_lineLayer":1,"_type":1,"_cutDirection":7}
                ], [{"_time":0.0,"_lineIndex":3,"_lineLayer":0,"_type":1,"_cutDirection":5}
                , {"_time":0.0,"_lineIndex":1,"_lineLayer":0,"_type":1,"_cutDirection":6}
                , {"_time":0.0,"_lineIndex":2,"_lineLayer":0,"_type":1,"_cutDirection":1}
                ], [{"_time":0.0,"_lineIndex":2,"_lineLayer":2,"_type":1,"_cutDirection":5}
                , {"_time":0.0,"_lineIndex":1,"_lineLayer":0,"_type":1,"_cutDirection":6}
                , {"_time":0.0,"_lineIndex":0,"_lineLayer":1,"_type":1,"_cutDirection":6}
                , {"_time":0.0,"_lineIndex":1,"_lineLayer":0,"_type":1,"_cutDirection":1}
                , {"_time":0.0,"_lineIndex":0,"_lineLayer":0,"_type":1,"_cutDirection":6}
                ], [{"_time":0.0,"_lineIndex":3,"_lineLayer":1,"_type":1,"_cutDirection":1}
                , {"_time":0.0,"_lineIndex":3,"_lineLayer":2,"_type":1,"_cutDirection":0}
                , {"_time":0.0,"_lineIndex":3,"_lineLayer":1,"_type":1,"_cutDirection":0}
                , {"_time":0.0,"_lineIndex":2,"_lineLayer":2,"_type":1,"_cutDirection":0}
                , {"_time":0.0,"_lineIndex":3,"_lineLayer":2,"_type":1,"_cutDirection":1}
                , {"_time":0.0,"_lineIndex":3,"_lineLayer":1,"_type":1,"_cutDirection":5}
                , {"_time":0.0,"_lineIndex":1,"_lineLayer":2,"_type":1,"_cutDirection":4}
                ], [{"_time":0.0,"_lineIndex":3,"_lineLayer":1,"_type":1,"_cutDirection":8}
                , {"_time":0.0,"_lineIndex":1,"_lineLayer":0,"_type":1,"_cutDirection":6}
                , {"_time":0.0,"_lineIndex":3,"_lineLayer":2,"_type":1,"_cutDirection":0}
                , {"_time":0.0,"_lineIndex":2,"_lineLayer":0,"_type":1,"_cutDirection":6}
                ], [{"_time":0.0,"_lineIndex":3,"_lineLayer":0,"_type":1,"_cutDirection":0}
                , {"_time":0.0,"_lineIndex":3,"_lineLayer":0,"_type":1,"_cutDirection":1}
                , {"_time":0.0,"_lineIndex":2,"_lineLayer":0,"_type":1,"_cutDirection":1}
                , {"_time":0.0,"_lineIndex":1,"_lineLayer":0,"_type":1,"_cutDirection":6}
                ], [{"_time":0.0,"_lineIndex":0,"_lineLayer":1,"_type":1,"_cutDirection":6}
                , {"_time":0.0,"_lineIndex":2,"_lineLayer":0,"_type":1,"_cutDirection":5}
                ], [{"_time":0.0,"_lineIndex":1,"_lineLayer":0,"_type":1,"_cutDirection":1}
                , {"_time":0.0,"_lineIndex":2,"_lineLayer":2,"_type":1,"_cutDirection":5}
                , {"_time":0.0,"_lineIndex":1,"_lineLayer":0,"_type":1,"_cutDirection":0}
                , {"_time":0.0,"_lineIndex":3,"_lineLayer":2,"_type":1,"_cutDirection":5}
                , {"_time":0.0,"_lineIndex":1,"_lineLayer":2,"_type":1,"_cutDirection":0}
                , {"_time":0.0,"_lineIndex":3,"_lineLayer":0,"_type":1,"_cutDirection":0}
                ], [{"_time":0.0,"_lineIndex":0,"_lineLayer":0,"_type":1,"_cutDirection":6}
                , {"_time":0.0,"_lineIndex":3,"_lineLayer":2,"_type":1,"_cutDirection":5}
                , {"_time":0.0,"_lineIndex":3,"_lineLayer":1,"_type":1,"_cutDirection":5}
                ], [{"_time":0.0,"_lineIndex":1,"_lineLayer":0,"_type":1,"_cutDirection":0}
                , {"_time":0.0,"_lineIndex":1,"_lineLayer":0,"_type":1,"_cutDirection":6}
                , {"_time":0.0,"_lineIndex":2,"_lineLayer":0,"_type":1,"_cutDirection":1}
                , {"_time":0.0,"_lineIndex":3,"_lineLayer":1,"_type":1,"_cutDirection":1}
                ], [{"_time":0.0,"_lineIndex":3,"_lineLayer":1,"_type":1,"_cutDirection":4}
                , {"_time":0.0,"_lineIndex":3,"_lineLayer":1,"_type":1,"_cutDirection":7}
                , {"_time":0.0,"_lineIndex":2,"_lineLayer":0,"_type":1,"_cutDirection":6}
                ], [{"_time":0.0,"_lineIndex":3,"_lineLayer":2,"_type":1,"_cutDirection":7}
                , {"_time":0.0,"_lineIndex":2,"_lineLayer":2,"_type":1,"_cutDirection":4}
                ], [{"_time":0.0,"_lineIndex":0,"_lineLayer":1,"_type":1,"_cutDirection":2}
                , {"_time":0.0,"_lineIndex":3,"_lineLayer":1,"_type":1,"_cutDirection":3}
                ], [{"_time":0.0,"_lineIndex":2,"_lineLayer":0,"_type":1,"_cutDirection":4}
                , {"_time":0.0,"_lineIndex":2,"_lineLayer":0,"_type":1,"_cutDirection":1}
                ], [{"_time":0.0,"_lineIndex":1,"_lineLayer":2,"_type":1,"_cutDirection":0}
                , {"_time":0.0,"_lineIndex":2,"_lineLayer":0,"_type":1,"_cutDirection":1}
                ], [{"_time":0.0,"_lineIndex":3,"_lineLayer":2,"_type":1,"_cutDirection":1}
                , {"_time":0.0,"_lineIndex":3,"_lineLayer":1,"_type":1,"_cutDirection":5}
                , {"_time":0.0,"_lineIndex":3,"_lineLayer":2,"_type":1,"_cutDirection":0}
                , {"_time":0.0,"_lineIndex":2,"_lineLayer":0,"_type":1,"_cutDirection":5}
                , {"_time":0.0,"_lineIndex":3,"_lineLayer":2,"_type":1,"_cutDirection":5}
                ], [{"_time":0.0,"_lineIndex":2,"_lineLayer":2,"_type":1,"_cutDirection":8}
                , {"_time":0.0,"_lineIndex":3,"_lineLayer":0,"_type":1,"_cutDirection":7}
                ], [{"_time":0.0,"_lineIndex":2,"_lineLayer":0,"_type":1,"_cutDirection":8}
                , {"_time":0.0,"_lineIndex":3,"_lineLayer":1,"_type":1,"_cutDirection":5}
                ], [{"_time":0.0,"_lineIndex":1,"_lineLayer":1,"_type":1,"_cutDirection":6}
                , {"_time":0.0,"_lineIndex":3,"_lineLayer":2,"_type":1,"_cutDirection":5}
                ]]""", p.asList().toString());
    }

    String jsonExample = """
            {"_time":0.0,"_lineIndex":2,"_lineLayer":0,"_type":1,"_cutDirection":1}: [
              {"_time":0.0,"_lineIndex":2,"_lineLayer":2,"_type":1,"_cutDirection":1}: 1 times = 1.8867924% ,\s
              {"_time":0.0,"_lineIndex":1,"_lineLayer":0,"_type":1,"_cutDirection":5}: 1 times = 1.8867924% ,\s
              {"_time":0.0,"_lineIndex":3,"_lineLayer":0,"_type":1,"_cutDirection":7}: 1 times = 1.8867924% ,\s
              {"_time":0.0,"_lineIndex":2,"_lineLayer":2,"_type":1,"_cutDirection":0}: 13 times = 24.528303% ,\s
              {"_time":0.0,"_lineIndex":3,"_lineLayer":2,"_type":1,"_cutDirection":0}: 5 times = 9.433963% ,\s
              {"_time":0.0,"_lineIndex":3,"_lineLayer":1,"_type":1,"_cutDirection":5}: 5 times = 9.433963% ,\s
              {"_time":0.0,"_lineIndex":3,"_lineLayer":1,"_type":1,"_cutDirection":0}: 2 times = 3.7735848% ,\s
              {"_time":0.0,"_lineIndex":1,"_lineLayer":2,"_type":1,"_cutDirection":4}: 2 times = 3.7735848% ,\s
              {"_time":0.0,"_lineIndex":3,"_lineLayer":2,"_type":1,"_cutDirection":5}: 5 times = 9.433963% ,\s
              {"_time":0.0,"_lineIndex":1,"_lineLayer":0,"_type":1,"_cutDirection":0}: 2 times = 3.7735848% ,\s
              {"_time":0.0,"_lineIndex":3,"_lineLayer":0,"_type":1,"_cutDirection":0}: 2 times = 3.7735848% ,\s
              {"_time":0.0,"_lineIndex":2,"_lineLayer":0,"_type":1,"_cutDirection":0}: 7 times = 13.207547% ,\s
              {"_time":0.0,"_lineIndex":3,"_lineLayer":0,"_type":1,"_cutDirection":1}: 2 times = 3.7735848% ,\s
              {"_time":0.0,"_lineIndex":2,"_lineLayer":0,"_type":1,"_cutDirection":1}: 4 times = 7.5471697% ,\s
              {"_time":0.0,"_lineIndex":1,"_lineLayer":0,"_type":1,"_cutDirection":1}: 1 times = 1.8867924% ,\s
            ]\s
            {"_time":0.0,"_lineIndex":2,"_lineLayer":2,"_type":1,"_cutDirection":1}: [
              {"_time":0.0,"_lineIndex":3,"_lineLayer":0,"_type":1,"_cutDirection":0}: 1 times = 33.333336% ,\s
              {"_time":0.0,"_lineIndex":2,"_lineLayer":0,"_type":1,"_cutDirection":1}: 1 times = 33.333336% ,\s
              {"_time":0.0,"_lineIndex":2,"_lineLayer":1,"_type":1,"_cutDirection":1}: 1 times = 33.333336% ,\s
            ]\s
            {"_time":0.0,"_lineIndex":3,"_lineLayer":2,"_type":1,"_cutDirection":8}: [
              {"_time":0.0,"_lineIndex":3,"_lineLayer":2,"_type":1,"_cutDirection":1}: 2 times = 20.0% ,\s
              {"_time":0.0,"_lineIndex":3,"_lineLayer":1,"_type":1,"_cutDirection":1}: 3 times = 30.000002% ,\s
              {"_time":0.0,"_lineIndex":3,"_lineLayer":1,"_type":1,"_cutDirection":7}: 1 times = 10.0% ,\s
              {"_time":0.0,"_lineIndex":1,"_lineLayer":0,"_type":1,"_cutDirection":6}: 1 times = 10.0% ,\s
              {"_time":0.0,"_lineIndex":3,"_lineLayer":0,"_type":1,"_cutDirection":1}: 3 times = 30.000002% ,\s
            ]\s
            {"_time":0.0,"_lineIndex":3,"_lineLayer":0,"_type":1,"_cutDirection":8}: [
              {"_time":0.0,"_lineIndex":3,"_lineLayer":0,"_type":1,"_cutDirection":0}: 3 times = 30.000002% ,\s
              {"_time":0.0,"_lineIndex":2,"_lineLayer":0,"_type":1,"_cutDirection":0}: 3 times = 30.000002% ,\s
              {"_time":0.0,"_lineIndex":3,"_lineLayer":0,"_type":1,"_cutDirection":4}: 1 times = 10.0% ,\s
              {"_time":0.0,"_lineIndex":3,"_lineLayer":1,"_type":1,"_cutDirection":0}: 3 times = 30.000002% ,\s
            ]\s
            {"_time":0.0,"_lineIndex":0,"_lineLayer":2,"_type":1,"_cutDirection":8}: [
              {"_time":0.0,"_lineIndex":3,"_lineLayer":0,"_type":1,"_cutDirection":7}: 1 times = 100.0% ,\s
            ]\s
            {"_time":0.0,"_lineIndex":3,"_lineLayer":0,"_type":1,"_cutDirection":7}: [
              {"_time":0.0,"_lineIndex":3,"_lineLayer":0,"_type":1,"_cutDirection":0}: 1 times = 12.5% ,\s
              {"_time":0.0,"_lineIndex":2,"_lineLayer":2,"_type":1,"_cutDirection":4}: 1 times = 12.5% ,\s
              {"_time":0.0,"_lineIndex":3,"_lineLayer":1,"_type":1,"_cutDirection":0}: 2 times = 25.0% ,\s
              {"_time":0.0,"_lineIndex":2,"_lineLayer":2,"_type":1,"_cutDirection":0}: 1 times = 12.5% ,\s
              {"_time":0.0,"_lineIndex":1,"_lineLayer":2,"_type":1,"_cutDirection":4}: 2 times = 25.0% ,\s
              {"_time":0.0,"_lineIndex":2,"_lineLayer":0,"_type":1,"_cutDirection":4}: 1 times = 12.5% ,\s
            ]\s
            {"_time":0.0,"_lineIndex":2,"_lineLayer":0,"_type":1,"_cutDirection":0}: [
              {"_time":0.0,"_lineIndex":2,"_lineLayer":2,"_type":1,"_cutDirection":0}: 3 times = 25.0% ,\s
              {"_time":0.0,"_lineIndex":3,"_lineLayer":1,"_type":1,"_cutDirection":7}: 2 times = 16.666668% ,\s
              {"_time":0.0,"_lineIndex":1,"_lineLayer":0,"_type":1,"_cutDirection":1}: 2 times = 16.666668% ,\s
              {"_time":0.0,"_lineIndex":2,"_lineLayer":0,"_type":1,"_cutDirection":1}: 2 times = 16.666668% ,\s
              {"_time":0.0,"_lineIndex":3,"_lineLayer":0,"_type":1,"_cutDirection":1}: 2 times = 16.666668% ,\s
              {"_time":0.0,"_lineIndex":1,"_lineLayer":0,"_type":1,"_cutDirection":6}: 1 times = 8.333334% ,\s
            ]\s
            {"_time":0.0,"_lineIndex":2,"_lineLayer":2,"_type":1,"_cutDirection":0}: [
              {"_time":0.0,"_lineIndex":2,"_lineLayer":0,"_type":1,"_cutDirection":6}: 1 times = 3.4482758% ,\s
              {"_time":0.0,"_lineIndex":3,"_lineLayer":1,"_type":1,"_cutDirection":6}: 1 times = 3.4482758% ,\s
              {"_time":0.0,"_lineIndex":3,"_lineLayer":2,"_type":1,"_cutDirection":1}: 1 times = 3.4482758% ,\s
              {"_time":0.0,"_lineIndex":2,"_lineLayer":2,"_type":1,"_cutDirection":1}: 1 times = 3.4482758% ,\s
              {"_time":0.0,"_lineIndex":2,"_lineLayer":0,"_type":1,"_cutDirection":1}: 10 times = 34.482758% ,\s
              {"_time":0.0,"_lineIndex":3,"_lineLayer":1,"_type":1,"_cutDirection":7}: 2 times = 6.8965516% ,\s
              {"_time":0.0,"_lineIndex":3,"_lineLayer":0,"_type":1,"_cutDirection":1}: 3 times = 10.344828% ,\s
              {"_time":0.0,"_lineIndex":3,"_lineLayer":0,"_type":1,"_cutDirection":7}: 4 times = 13.793103% ,\s
              {"_time":0.0,"_lineIndex":3,"_lineLayer":1,"_type":1,"_cutDirection":1}: 1 times = 3.4482758% ,\s
              {"_time":0.0,"_lineIndex":1,"_lineLayer":0,"_type":1,"_cutDirection":1}: 1 times = 3.4482758% ,\s
              {"_time":0.0,"_lineIndex":1,"_lineLayer":0,"_type":1,"_cutDirection":6}: 4 times = 13.793103% ,\s
            ]\s
            {"_time":0.0,"_lineIndex":2,"_lineLayer":0,"_type":1,"_cutDirection":6}: [
              {"_time":0.0,"_lineIndex":3,"_lineLayer":1,"_type":1,"_cutDirection":6}: 2 times = 7.1428576% ,\s
              {"_time":0.0,"_lineIndex":3,"_lineLayer":1,"_type":1,"_cutDirection":5}: 10 times = 35.714287% ,\s
              {"_time":0.0,"_lineIndex":3,"_lineLayer":2,"_type":1,"_cutDirection":5}: 7 times = 25.0% ,\s
              {"_time":0.0,"_lineIndex":2,"_lineLayer":2,"_type":1,"_cutDirection":0}: 1 times = 3.5714288% ,\s
              {"_time":0.0,"_lineIndex":3,"_lineLayer":2,"_type":1,"_cutDirection":0}: 3 times = 10.714286% ,\s
              {"_time":0.0,"_lineIndex":3,"_lineLayer":1,"_type":1,"_cutDirection":3}: 1 times = 3.5714288% ,\s
              {"_time":0.0,"_lineIndex":2,"_lineLayer":0,"_type":1,"_cutDirection":5}: 2 times = 7.1428576% ,\s
              {"_time":0.0,"_lineIndex":3,"_lineLayer":1,"_type":1,"_cutDirection":0}: 2 times = 7.1428576% ,\s
            ]\s
            {"_time":0.0,"_lineIndex":3,"_lineLayer":1,"_type":1,"_cutDirection":6}: [
              {"_time":0.0,"_lineIndex":1,"_lineLayer":0,"_type":1,"_cutDirection":5}: 1 times = 33.333336% ,\s
              {"_time":0.0,"_lineIndex":3,"_lineLayer":2,"_type":1,"_cutDirection":5}: 2 times = 66.66667% ,\s
            ]\s
            {"_time":0.0,"_lineIndex":0,"_lineLayer":0,"_type":1,"_cutDirection":8}: [
              {"_time":0.0,"_lineIndex":2,"_lineLayer":0,"_type":1,"_cutDirection":5}: 1 times = 100.0% ,\s
            ]\s
            {"_time":0.0,"_lineIndex":2,"_lineLayer":0,"_type":1,"_cutDirection":5}: [
              {"_time":0.0,"_lineIndex":3,"_lineLayer":1,"_type":1,"_cutDirection":5}: 1 times = 50.0% ,\s
              {"_time":0.0,"_lineIndex":3,"_lineLayer":0,"_type":1,"_cutDirection":1}: 1 times = 50.0% ,\s
            ]\s
            {"_time":0.0,"_lineIndex":3,"_lineLayer":1,"_type":1,"_cutDirection":5}: [
              {"_time":0.0,"_lineIndex":3,"_lineLayer":2,"_type":1,"_cutDirection":1}: 1 times = 2.777778% ,\s
              {"_time":0.0,"_lineIndex":2,"_lineLayer":0,"_type":1,"_cutDirection":6}: 16 times = 44.444447% ,\s
              {"_time":0.0,"_lineIndex":2,"_lineLayer":0,"_type":1,"_cutDirection":1}: 4 times = 11.111112% ,\s
              {"_time":0.0,"_lineIndex":1,"_lineLayer":0,"_type":1,"_cutDirection":6}: 9 times = 25.0% ,\s
              {"_time":0.0,"_lineIndex":3,"_lineLayer":1,"_type":1,"_cutDirection":1}: 2 times = 5.555556% ,\s
              {"_time":0.0,"_lineIndex":0,"_lineLayer":0,"_type":1,"_cutDirection":6}: 2 times = 5.555556% ,\s
              {"_time":0.0,"_lineIndex":3,"_lineLayer":1,"_type":1,"_cutDirection":6}: 1 times = 2.777778% ,\s
              {"_time":0.0,"_lineIndex":1,"_lineLayer":1,"_type":1,"_cutDirection":6}: 1 times = 2.777778% ,\s
            ]\s
            {"_time":0.0,"_lineIndex":1,"_lineLayer":2,"_type":1,"_cutDirection":8}: [
              {"_time":0.0,"_lineIndex":2,"_lineLayer":2,"_type":1,"_cutDirection":1}: 1 times = 100.0% ,\s
            ]\s
            {"_time":0.0,"_lineIndex":1,"_lineLayer":0,"_type":1,"_cutDirection":5}: [
              {"_time":0.0,"_lineIndex":3,"_lineLayer":2,"_type":1,"_cutDirection":5}: 1 times = 25.0% ,\s
              {"_time":0.0,"_lineIndex":1,"_lineLayer":0,"_type":1,"_cutDirection":6}: 1 times = 25.0% ,\s
              {"_time":0.0,"_lineIndex":2,"_lineLayer":0,"_type":1,"_cutDirection":1}: 2 times = 50.0% ,\s
            ]\s
            {"_time":0.0,"_lineIndex":3,"_lineLayer":2,"_type":1,"_cutDirection":5}: [
              {"_time":0.0,"_lineIndex":1,"_lineLayer":0,"_type":1,"_cutDirection":6}: 16 times = 42.105263% ,\s
              {"_time":0.0,"_lineIndex":3,"_lineLayer":0,"_type":1,"_cutDirection":1}: 7 times = 18.421053% ,\s
              {"_time":0.0,"_lineIndex":2,"_lineLayer":0,"_type":1,"_cutDirection":6}: 6 times = 15.789473% ,\s
              {"_time":0.0,"_lineIndex":2,"_lineLayer":0,"_type":1,"_cutDirection":1}: 6 times = 15.789473% ,\s
              {"_time":0.0,"_lineIndex":3,"_lineLayer":1,"_type":1,"_cutDirection":1}: 1 times = 2.631579% ,\s
              {"_time":0.0,"_lineIndex":0,"_lineLayer":1,"_type":1,"_cutDirection":2}: 1 times = 2.631579% ,\s
              {"_time":0.0,"_lineIndex":0,"_lineLayer":0,"_type":1,"_cutDirection":6}: 1 times = 2.631579% ,\s
            ]\s
            {"_time":0.0,"_lineIndex":1,"_lineLayer":0,"_type":1,"_cutDirection":6}: [
              {"_time":0.0,"_lineIndex":3,"_lineLayer":2,"_type":1,"_cutDirection":6}: 1 times = 2.0833335% ,\s
              {"_time":0.0,"_lineIndex":2,"_lineLayer":0,"_type":1,"_cutDirection":5}: 2 times = 4.166667% ,\s
              {"_time":0.0,"_lineIndex":3,"_lineLayer":0,"_type":1,"_cutDirection":5}: 4 times = 8.333334% ,\s
              {"_time":0.0,"_lineIndex":3,"_lineLayer":1,"_type":1,"_cutDirection":5}: 16 times = 33.333336% ,\s
              {"_time":0.0,"_lineIndex":2,"_lineLayer":2,"_type":1,"_cutDirection":5}: 3 times = 6.25% ,\s
              {"_time":0.0,"_lineIndex":3,"_lineLayer":2,"_type":1,"_cutDirection":5}: 16 times = 33.333336% ,\s
              {"_time":0.0,"_lineIndex":1,"_lineLayer":0,"_type":1,"_cutDirection":5}: 3 times = 6.25% ,\s
              {"_time":0.0,"_lineIndex":2,"_lineLayer":0,"_type":1,"_cutDirection":0}: 1 times = 2.0833335% ,\s
              {"_time":0.0,"_lineIndex":2,"_lineLayer":2,"_type":1,"_cutDirection":0}: 1 times = 2.0833335% ,\s
              {"_time":0.0,"_lineIndex":3,"_lineLayer":1,"_type":1,"_cutDirection":3}: 1 times = 2.0833335% ,\s
            ]\s
            {"_time":0.0,"_lineIndex":3,"_lineLayer":2,"_type":1,"_cutDirection":6}: [
              {"_time":0.0,"_lineIndex":2,"_lineLayer":2,"_type":1,"_cutDirection":0}: 1 times = 100.0% ,\s
            ]\s
            {"_time":0.0,"_lineIndex":2,"_lineLayer":1,"_type":1,"_cutDirection":1}: [
              {"_time":0.0,"_lineIndex":2,"_lineLayer":0,"_type":1,"_cutDirection":1}: 1 times = 100.0% ,\s
            ]\s
            {"_time":0.0,"_lineIndex":2,"_lineLayer":2,"_type":1,"_cutDirection":4}: [
              {"_time":0.0,"_lineIndex":2,"_lineLayer":0,"_type":1,"_cutDirection":1}: 1 times = 25.0% ,\s
              {"_time":0.0,"_lineIndex":3,"_lineLayer":1,"_type":1,"_cutDirection":3}: 1 times = 25.0% ,\s
              {"_time":0.0,"_lineIndex":3,"_lineLayer":2,"_type":1,"_cutDirection":7}: 1 times = 25.0% ,\s
              {"_time":0.0,"_lineIndex":3,"_lineLayer":1,"_type":1,"_cutDirection":1}: 1 times = 25.0% ,\s
            ]\s
            {"_time":0.0,"_lineIndex":3,"_lineLayer":2,"_type":1,"_cutDirection":0}: [
              {"_time":0.0,"_lineIndex":1,"_lineLayer":0,"_type":1,"_cutDirection":6}: 4 times = 12.5% ,\s
              {"_time":0.0,"_lineIndex":2,"_lineLayer":0,"_type":1,"_cutDirection":1}: 6 times = 18.75% ,\s
              {"_time":0.0,"_lineIndex":3,"_lineLayer":0,"_type":1,"_cutDirection":1}: 11 times = 34.375% ,\s
              {"_time":0.0,"_lineIndex":2,"_lineLayer":0,"_type":1,"_cutDirection":6}: 2 times = 6.25% ,\s
              {"_time":0.0,"_lineIndex":3,"_lineLayer":1,"_type":1,"_cutDirection":1}: 9 times = 28.125% ,\s
            ]\s
            {"_time":0.0,"_lineIndex":3,"_lineLayer":1,"_type":1,"_cutDirection":7}: [
              {"_time":0.0,"_lineIndex":1,"_lineLayer":2,"_type":1,"_cutDirection":4}: 1 times = 12.5% ,\s
              {"_time":0.0,"_lineIndex":2,"_lineLayer":2,"_type":1,"_cutDirection":4}: 2 times = 25.0% ,\s
              {"_time":0.0,"_lineIndex":3,"_lineLayer":1,"_type":1,"_cutDirection":0}: 1 times = 12.5% ,\s
              {"_time":0.0,"_lineIndex":3,"_lineLayer":1,"_type":1,"_cutDirection":4}: 2 times = 25.0% ,\s
              {"_time":0.0,"_lineIndex":2,"_lineLayer":2,"_type":1,"_cutDirection":0}: 2 times = 25.0% ,\s
            ]\s
            {"_time":0.0,"_lineIndex":1,"_lineLayer":2,"_type":1,"_cutDirection":4}: [
              {"_time":0.0,"_lineIndex":2,"_lineLayer":0,"_type":1,"_cutDirection":1}: 3 times = 50.0% ,\s
              {"_time":0.0,"_lineIndex":3,"_lineLayer":0,"_type":1,"_cutDirection":7}: 1 times = 16.666668% ,\s
              {"_time":0.0,"_lineIndex":3,"_lineLayer":1,"_type":1,"_cutDirection":7}: 2 times = 33.333336% ,\s
            ]\s
            {"_time":0.0,"_lineIndex":3,"_lineLayer":1,"_type":1,"_cutDirection":0}: [
              {"_time":0.0,"_lineIndex":3,"_lineLayer":0,"_type":1,"_cutDirection":1}: 6 times = 25.0% ,\s
              {"_time":0.0,"_lineIndex":3,"_lineLayer":2,"_type":1,"_cutDirection":0}: 5 times = 20.833332% ,\s
              {"_time":0.0,"_lineIndex":2,"_lineLayer":0,"_type":1,"_cutDirection":6}: 3 times = 12.5% ,\s
              {"_time":0.0,"_lineIndex":1,"_lineLayer":0,"_type":1,"_cutDirection":6}: 3 times = 12.5% ,\s
              {"_time":0.0,"_lineIndex":2,"_lineLayer":0,"_type":1,"_cutDirection":1}: 3 times = 12.5% ,\s
              {"_time":0.0,"_lineIndex":1,"_lineLayer":0,"_type":1,"_cutDirection":1}: 2 times = 8.333334% ,\s
              {"_time":0.0,"_lineIndex":3,"_lineLayer":1,"_type":1,"_cutDirection":1}: 2 times = 8.333334% ,\s
            ]\s
            {"_time":0.0,"_lineIndex":3,"_lineLayer":0,"_type":1,"_cutDirection":1}: [
              {"_time":0.0,"_lineIndex":2,"_lineLayer":2,"_type":1,"_cutDirection":0}: 5 times = 13.157895% ,\s
              {"_time":0.0,"_lineIndex":3,"_lineLayer":1,"_type":1,"_cutDirection":0}: 10 times = 26.31579% ,\s
              {"_time":0.0,"_lineIndex":3,"_lineLayer":2,"_type":1,"_cutDirection":0}: 7 times = 18.421053% ,\s
              {"_time":0.0,"_lineIndex":3,"_lineLayer":0,"_type":1,"_cutDirection":0}: 3 times = 7.8947363% ,\s
              {"_time":0.0,"_lineIndex":3,"_lineLayer":1,"_type":1,"_cutDirection":5}: 1 times = 2.631579% ,\s
              {"_time":0.0,"_lineIndex":2,"_lineLayer":0,"_type":1,"_cutDirection":1}: 3 times = 7.8947363% ,\s
              {"_time":0.0,"_lineIndex":3,"_lineLayer":1,"_type":1,"_cutDirection":1}: 8 times = 21.052631% ,\s
              {"_time":0.0,"_lineIndex":2,"_lineLayer":0,"_type":1,"_cutDirection":0}: 1 times = 2.631579% ,\s
            ]\s
            {"_time":0.0,"_lineIndex":3,"_lineLayer":1,"_type":1,"_cutDirection":3}: [
              {"_time":0.0,"_lineIndex":1,"_lineLayer":2,"_type":1,"_cutDirection":2}: 1 times = 16.666668% ,\s
              {"_time":0.0,"_lineIndex":0,"_lineLayer":1,"_type":1,"_cutDirection":2}: 2 times = 33.333336% ,\s
              {"_time":0.0,"_lineIndex":1,"_lineLayer":0,"_type":1,"_cutDirection":6}: 1 times = 16.666668% ,\s
              {"_time":0.0,"_lineIndex":2,"_lineLayer":0,"_type":1,"_cutDirection":6}: 1 times = 16.666668% ,\s
              {"_time":0.0,"_lineIndex":0,"_lineLayer":0,"_type":1,"_cutDirection":6}: 1 times = 16.666668% ,\s
            ]\s
            {"_time":0.0,"_lineIndex":1,"_lineLayer":2,"_type":1,"_cutDirection":2}: [
              {"_time":0.0,"_lineIndex":3,"_lineLayer":2,"_type":1,"_cutDirection":3}: 1 times = 100.0% ,\s
            ]\s
            {"_time":0.0,"_lineIndex":3,"_lineLayer":2,"_type":1,"_cutDirection":3}: [
              {"_time":0.0,"_lineIndex":2,"_lineLayer":2,"_type":1,"_cutDirection":2}: 1 times = 100.0% ,\s
            ]\s
            {"_time":0.0,"_lineIndex":2,"_lineLayer":2,"_type":1,"_cutDirection":2}: [
              {"_time":0.0,"_lineIndex":3,"_lineLayer":1,"_type":1,"_cutDirection":7}: 1 times = 100.0% ,\s
            ]\s
            {"_time":0.0,"_lineIndex":3,"_lineLayer":0,"_type":1,"_cutDirection":5}: [
              {"_time":0.0,"_lineIndex":1,"_lineLayer":0,"_type":1,"_cutDirection":6}: 3 times = 75.0% ,\s
              {"_time":0.0,"_lineIndex":2,"_lineLayer":0,"_type":1,"_cutDirection":1}: 1 times = 25.0% ,\s
            ]\s
            {"_time":0.0,"_lineIndex":2,"_lineLayer":2,"_type":1,"_cutDirection":5}: [
              {"_time":0.0,"_lineIndex":1,"_lineLayer":0,"_type":1,"_cutDirection":6}: 2 times = 40.0% ,\s
              {"_time":0.0,"_lineIndex":0,"_lineLayer":1,"_type":1,"_cutDirection":6}: 1 times = 20.0% ,\s
              {"_time":0.0,"_lineIndex":1,"_lineLayer":0,"_type":1,"_cutDirection":1}: 1 times = 20.0% ,\s
              {"_time":0.0,"_lineIndex":0,"_lineLayer":0,"_type":1,"_cutDirection":6}: 1 times = 20.0% ,\s
            ]\s
            {"_time":0.0,"_lineIndex":3,"_lineLayer":1,"_type":1,"_cutDirection":1}: [
              {"_time":0.0,"_lineIndex":3,"_lineLayer":2,"_type":1,"_cutDirection":0}: 10 times = 40.0% ,\s
              {"_time":0.0,"_lineIndex":3,"_lineLayer":1,"_type":1,"_cutDirection":0}: 6 times = 24.0% ,\s
              {"_time":0.0,"_lineIndex":2,"_lineLayer":2,"_type":1,"_cutDirection":0}: 3 times = 12.0% ,\s
              {"_time":0.0,"_lineIndex":3,"_lineLayer":2,"_type":1,"_cutDirection":1}: 4 times = 16.0% ,\s
              {"_time":0.0,"_lineIndex":3,"_lineLayer":1,"_type":1,"_cutDirection":5}: 1 times = 4.0% ,\s
              {"_time":0.0,"_lineIndex":1,"_lineLayer":2,"_type":1,"_cutDirection":4}: 1 times = 4.0% ,\s
            ]\s
            {"_time":0.0,"_lineIndex":3,"_lineLayer":1,"_type":1,"_cutDirection":8}: [
              {"_time":0.0,"_lineIndex":1,"_lineLayer":0,"_type":1,"_cutDirection":6}: 1 times = 25.0% ,\s
              {"_time":0.0,"_lineIndex":3,"_lineLayer":2,"_type":1,"_cutDirection":0}: 1 times = 25.0% ,\s
              {"_time":0.0,"_lineIndex":2,"_lineLayer":0,"_type":1,"_cutDirection":6}: 2 times = 50.0% ,\s
            ]\s
            {"_time":0.0,"_lineIndex":3,"_lineLayer":0,"_type":1,"_cutDirection":0}: [
              {"_time":0.0,"_lineIndex":3,"_lineLayer":0,"_type":1,"_cutDirection":1}: 3 times = 50.0% ,\s
              {"_time":0.0,"_lineIndex":2,"_lineLayer":0,"_type":1,"_cutDirection":1}: 2 times = 33.333336% ,\s
              {"_time":0.0,"_lineIndex":1,"_lineLayer":0,"_type":1,"_cutDirection":6}: 1 times = 16.666668% ,\s
            ]\s
            {"_time":0.0,"_lineIndex":0,"_lineLayer":1,"_type":1,"_cutDirection":6}: [
              {"_time":0.0,"_lineIndex":2,"_lineLayer":0,"_type":1,"_cutDirection":5}: 1 times = 100.0% ,\s
            ]\s
            {"_time":0.0,"_lineIndex":1,"_lineLayer":0,"_type":1,"_cutDirection":1}: [
              {"_time":0.0,"_lineIndex":2,"_lineLayer":2,"_type":1,"_cutDirection":5}: 2 times = 28.57143% ,\s
              {"_time":0.0,"_lineIndex":1,"_lineLayer":0,"_type":1,"_cutDirection":0}: 1 times = 14.285715% ,\s
              {"_time":0.0,"_lineIndex":3,"_lineLayer":2,"_type":1,"_cutDirection":5}: 2 times = 28.57143% ,\s
              {"_time":0.0,"_lineIndex":1,"_lineLayer":2,"_type":1,"_cutDirection":0}: 1 times = 14.285715% ,\s
              {"_time":0.0,"_lineIndex":3,"_lineLayer":0,"_type":1,"_cutDirection":0}: 1 times = 14.285715% ,\s
            ]\s
            {"_time":0.0,"_lineIndex":0,"_lineLayer":0,"_type":1,"_cutDirection":6}: [
              {"_time":0.0,"_lineIndex":3,"_lineLayer":2,"_type":1,"_cutDirection":5}: 4 times = 80.0% ,\s
              {"_time":0.0,"_lineIndex":3,"_lineLayer":1,"_type":1,"_cutDirection":5}: 1 times = 20.0% ,\s
            ]\s
            {"_time":0.0,"_lineIndex":1,"_lineLayer":0,"_type":1,"_cutDirection":0}: [
              {"_time":0.0,"_lineIndex":1,"_lineLayer":0,"_type":1,"_cutDirection":6}: 1 times = 33.333336% ,\s
              {"_time":0.0,"_lineIndex":2,"_lineLayer":0,"_type":1,"_cutDirection":1}: 1 times = 33.333336% ,\s
              {"_time":0.0,"_lineIndex":3,"_lineLayer":1,"_type":1,"_cutDirection":1}: 1 times = 33.333336% ,\s
            ]\s
            {"_time":0.0,"_lineIndex":3,"_lineLayer":1,"_type":1,"_cutDirection":4}: [
              {"_time":0.0,"_lineIndex":3,"_lineLayer":1,"_type":1,"_cutDirection":7}: 1 times = 50.0% ,\s
              {"_time":0.0,"_lineIndex":2,"_lineLayer":0,"_type":1,"_cutDirection":6}: 1 times = 50.0% ,\s
            ]\s
            {"_time":0.0,"_lineIndex":3,"_lineLayer":2,"_type":1,"_cutDirection":7}: [
              {"_time":0.0,"_lineIndex":2,"_lineLayer":2,"_type":1,"_cutDirection":4}: 1 times = 100.0% ,\s
            ]\s
            {"_time":0.0,"_lineIndex":0,"_lineLayer":1,"_type":1,"_cutDirection":2}: [
              {"_time":0.0,"_lineIndex":3,"_lineLayer":1,"_type":1,"_cutDirection":3}: 3 times = 100.0% ,\s
            ]\s
            {"_time":0.0,"_lineIndex":2,"_lineLayer":0,"_type":1,"_cutDirection":4}: [
              {"_time":0.0,"_lineIndex":2,"_lineLayer":0,"_type":1,"_cutDirection":1}: 1 times = 100.0% ,\s
            ]\s
            {"_time":0.0,"_lineIndex":1,"_lineLayer":2,"_type":1,"_cutDirection":0}: [
              {"_time":0.0,"_lineIndex":2,"_lineLayer":0,"_type":1,"_cutDirection":1}: 1 times = 100.0% ,\s
            ]\s
            {"_time":0.0,"_lineIndex":3,"_lineLayer":2,"_type":1,"_cutDirection":1}: [
              {"_time":0.0,"_lineIndex":3,"_lineLayer":1,"_type":1,"_cutDirection":5}: 1 times = 25.0% ,\s
              {"_time":0.0,"_lineIndex":3,"_lineLayer":2,"_type":1,"_cutDirection":0}: 1 times = 25.0% ,\s
              {"_time":0.0,"_lineIndex":2,"_lineLayer":0,"_type":1,"_cutDirection":5}: 1 times = 25.0% ,\s
              {"_time":0.0,"_lineIndex":3,"_lineLayer":2,"_type":1,"_cutDirection":5}: 1 times = 25.0% ,\s
            ]\s
            {"_time":0.0,"_lineIndex":2,"_lineLayer":2,"_type":1,"_cutDirection":8}: [
              {"_time":0.0,"_lineIndex":3,"_lineLayer":0,"_type":1,"_cutDirection":7}: 1 times = 100.0% ,\s
            ]\s
            {"_time":0.0,"_lineIndex":2,"_lineLayer":0,"_type":1,"_cutDirection":8}: [
              {"_time":0.0,"_lineIndex":3,"_lineLayer":1,"_type":1,"_cutDirection":5}: 1 times = 100.0% ,\s
            ]\s
            {"_time":0.0,"_lineIndex":1,"_lineLayer":1,"_type":1,"_cutDirection":6}: [
              {"_time":0.0,"_lineIndex":3,"_lineLayer":2,"_type":1,"_cutDirection":5}: 1 times = 100.0% ,\s
            ]""";
}