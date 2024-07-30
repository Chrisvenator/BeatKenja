import BeatSaberObjects.Objects.BeatSaberMap;
import BeatSaberObjects.Objects.Bookmark;
import BeatSaberObjects.Objects.Events;
import BeatSaberObjects.Objects.Obstacle;
import DataManager.FileManager;
import com.google.gson.Gson;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class BeatSaberMapTest {

    @Test
    void constructor() {
        String OGJson = FileManager.readFile("src/test/resources/Template--ISeeFire.txt").get(0);
        BeatSaberMap map = new Gson().fromJson(FileManager.readFile("src/test/resources/Template--ISeeFire.txt").get(0), BeatSaberMap.class);

        Assertions.assertEquals("2.2.0", map._version);
        Assertions.assertEquals(9050, map._events.length);
        Assertions.assertEquals(1117, map._notes.length);
        Assertions.assertEquals(0, map._obstacles.length);
        assertNull(map.bookmarks);


        Assertions.assertEquals(map._notes.length, new BeatSaberMap(map._notes)._notes.length);
        assertArrayEquals(new BeatSaberMap(map._notes)._events, new Events[0]);
        assertArrayEquals(new BeatSaberMap(map._notes)._obstacles, new Obstacle[0]);
        assertArrayEquals(new BeatSaberMap(map._notes).bookmarks.toArray(), new ArrayList<Bookmark>().toArray());

        Assertions.assertArrayEquals(new BeatSaberMap(map._notes)._notes, map._notes);
        Assertions.assertEquals(OGJson, new BeatSaberMap(map._notes, OGJson).originalJSON);

        BeatSaberMap map2 = new Gson().fromJson(FileManager.readFile("src/test/resources/BookmarksExample.txt").get(0), BeatSaberMap.class);
        map2.originalJSON = FileManager.readFile("src/test/resources/BookmarksExample.txt").get(0);
    }

    @Test
    void fixPlacements() {
        BeatSaberMap map = new Gson().fromJson(FileManager.readFile("src/test/resources/Template--ISeeFire.txt").get(0), BeatSaberMap.class);
        for (int i = 0; i < 27; i++) map._notes[i]._time += 0.000000001F;
        map.fixPlacements(0.015625); //1 / 64

        Assertions.assertEquals(6.5, map._notes[0]._time);
        Assertions.assertEquals(6.5, map._notes[1]._time);
        Assertions.assertEquals(6.5, map._notes[2]._time);
        Assertions.assertEquals(6.5, map._notes[3]._time);
        Assertions.assertEquals(10.0, map._notes[4]._time);
        Assertions.assertEquals(10.0625, map._notes[5]._time);
        Assertions.assertEquals(10.125, map._notes[6]._time);
        Assertions.assertEquals(11.0, map._notes[7]._time);
        Assertions.assertEquals(11.0625, map._notes[8]._time);
        Assertions.assertEquals(11.125, map._notes[9]._time);
        Assertions.assertEquals(12.25, map._notes[10]._time);
        Assertions.assertEquals(12.3125, map._notes[11]._time);
        Assertions.assertEquals(12.375, map._notes[12]._time);
        Assertions.assertEquals(14.25, map._notes[13]._time);
        Assertions.assertEquals(14.3125, map._notes[14]._time);
        Assertions.assertEquals(14.375, map._notes[15]._time);
        Assertions.assertEquals(17.25, map._notes[16]._time);
        Assertions.assertEquals(17.3125, map._notes[17]._time);
        Assertions.assertEquals(17.375, map._notes[18]._time);
        Assertions.assertEquals(18.25, map._notes[19]._time);
        Assertions.assertEquals(18.3125, map._notes[20]._time);
        Assertions.assertEquals(20.0, map._notes[21]._time);
        Assertions.assertEquals(20.0625, map._notes[22]._time);
        Assertions.assertEquals(23.5, map._notes[23]._time);
        Assertions.assertEquals(23.5625, map._notes[24]._time);
        Assertions.assertEquals(23.625, map._notes[25]._time);
        Assertions.assertEquals(24.5, map._notes[26]._time);

        for (int i = 0; i < 27; i++) map._notes[i]._time += 0.000000001F;
        map.fixPlacements(0.03125); //1 / 32

        Assertions.assertEquals(6.5, map._notes[0]._time);
        Assertions.assertEquals(6.5, map._notes[1]._time);
        Assertions.assertEquals(6.5, map._notes[2]._time);
        Assertions.assertEquals(6.5, map._notes[3]._time);
        Assertions.assertEquals(10.0, map._notes[4]._time);
        Assertions.assertEquals(10.0625, map._notes[5]._time);
        Assertions.assertEquals(10.125, map._notes[6]._time);
        Assertions.assertEquals(11.0, map._notes[7]._time);
        Assertions.assertEquals(11.0625, map._notes[8]._time);
        Assertions.assertEquals(11.125, map._notes[9]._time);
        Assertions.assertEquals(12.25, map._notes[10]._time);
        Assertions.assertEquals(12.3125, map._notes[11]._time);
        Assertions.assertEquals(12.375, map._notes[12]._time);
        Assertions.assertEquals(14.25, map._notes[13]._time);
        Assertions.assertEquals(14.3125, map._notes[14]._time);
        Assertions.assertEquals(14.375, map._notes[15]._time);
        Assertions.assertEquals(17.25, map._notes[16]._time);
        Assertions.assertEquals(17.3125, map._notes[17]._time);
        Assertions.assertEquals(17.375, map._notes[18]._time);
        Assertions.assertEquals(18.25, map._notes[19]._time);
        Assertions.assertEquals(18.3125, map._notes[20]._time);
        Assertions.assertEquals(20.0, map._notes[21]._time);
        Assertions.assertEquals(20.0625, map._notes[22]._time);
        Assertions.assertEquals(23.5, map._notes[23]._time);
        Assertions.assertEquals(23.5625, map._notes[24]._time);
        Assertions.assertEquals(23.625, map._notes[25]._time);
        Assertions.assertEquals(24.5, map._notes[26]._time);


        for (int i = 0; i < 27; i++) map._notes[i]._time += 0.000000001F;
        map.fixPlacements(0.0625); //1 / 16

        Assertions.assertEquals(6.5, map._notes[0]._time);
        Assertions.assertEquals(6.5, map._notes[1]._time);
        Assertions.assertEquals(6.5, map._notes[2]._time);
        Assertions.assertEquals(6.5, map._notes[3]._time);
        Assertions.assertEquals(10.0, map._notes[4]._time);
        Assertions.assertEquals(10.0625, map._notes[5]._time);
        Assertions.assertEquals(10.125, map._notes[6]._time);
        Assertions.assertEquals(11.0, map._notes[7]._time);
        Assertions.assertEquals(11.0625, map._notes[8]._time);
        Assertions.assertEquals(11.125, map._notes[9]._time);
        Assertions.assertEquals(12.25, map._notes[10]._time);
        Assertions.assertEquals(12.3125, map._notes[11]._time);
        Assertions.assertEquals(12.375, map._notes[12]._time);
        Assertions.assertEquals(14.25, map._notes[13]._time);
        Assertions.assertEquals(14.3125, map._notes[14]._time);
        Assertions.assertEquals(14.375, map._notes[15]._time);
        Assertions.assertEquals(17.25, map._notes[16]._time);
        Assertions.assertEquals(17.3125, map._notes[17]._time);
        Assertions.assertEquals(17.375, map._notes[18]._time);
        Assertions.assertEquals(18.25, map._notes[19]._time);
        Assertions.assertEquals(18.3125, map._notes[20]._time);
        Assertions.assertEquals(20.0, map._notes[21]._time);
        Assertions.assertEquals(20.0625, map._notes[22]._time);
        Assertions.assertEquals(23.5, map._notes[23]._time);
        Assertions.assertEquals(23.5625, map._notes[24]._time);
        Assertions.assertEquals(23.625, map._notes[25]._time);
        Assertions.assertEquals(24.5, map._notes[26]._time);


        for (int i = 0; i < 27; i++) map._notes[i]._time += 0.000000001F;
        map.fixPlacements(0.125); //1 / 8

        Assertions.assertEquals(6.5, map._notes[0]._time);
        Assertions.assertEquals(6.5, map._notes[1]._time);
        Assertions.assertEquals(6.5, map._notes[2]._time);
        Assertions.assertEquals(6.5, map._notes[3]._time);
        Assertions.assertEquals(10.0, map._notes[4]._time);
        Assertions.assertEquals(10.125, map._notes[5]._time);
        Assertions.assertEquals(10.125, map._notes[6]._time);
        Assertions.assertEquals(11.0, map._notes[7]._time);
        Assertions.assertEquals(11.125, map._notes[8]._time);
        Assertions.assertEquals(11.125, map._notes[9]._time);
        Assertions.assertEquals(12.25, map._notes[10]._time);
        Assertions.assertEquals(12.375, map._notes[11]._time);
        Assertions.assertEquals(12.375, map._notes[12]._time);
        Assertions.assertEquals(14.25, map._notes[13]._time);
        Assertions.assertEquals(14.375, map._notes[14]._time);
        Assertions.assertEquals(14.375, map._notes[15]._time);
        Assertions.assertEquals(17.25, map._notes[16]._time);
        Assertions.assertEquals(17.375, map._notes[17]._time);
        Assertions.assertEquals(17.375, map._notes[18]._time);
        Assertions.assertEquals(18.25, map._notes[19]._time);
        Assertions.assertEquals(18.375, map._notes[20]._time);
        Assertions.assertEquals(20.0, map._notes[21]._time);
        Assertions.assertEquals(20.125, map._notes[22]._time);
        Assertions.assertEquals(23.5, map._notes[23]._time);
        Assertions.assertEquals(23.625, map._notes[24]._time);
        Assertions.assertEquals(23.625, map._notes[25]._time);
        Assertions.assertEquals(24.5, map._notes[26]._time);


        for (int i = 0; i < 27; i++) map._notes[i]._time += 0.000000001F;
        map.fixPlacements(0.25); //1 / 4

        Assertions.assertEquals(6.5, map._notes[0]._time);
        Assertions.assertEquals(6.5, map._notes[1]._time);
        Assertions.assertEquals(6.5, map._notes[2]._time);
        Assertions.assertEquals(6.5, map._notes[3]._time);
        Assertions.assertEquals(10.0, map._notes[4]._time);
        Assertions.assertEquals(10.25, map._notes[5]._time);
        Assertions.assertEquals(10.25, map._notes[6]._time);
        Assertions.assertEquals(11.0, map._notes[7]._time);
        Assertions.assertEquals(11.25, map._notes[8]._time);
        Assertions.assertEquals(11.25, map._notes[9]._time);
        Assertions.assertEquals(12.25, map._notes[10]._time);
        Assertions.assertEquals(12.5, map._notes[11]._time);
        Assertions.assertEquals(12.5, map._notes[12]._time);
        Assertions.assertEquals(14.25, map._notes[13]._time);
        Assertions.assertEquals(14.5, map._notes[14]._time);
        Assertions.assertEquals(14.5, map._notes[15]._time);
        Assertions.assertEquals(17.25, map._notes[16]._time);
        Assertions.assertEquals(17.5, map._notes[17]._time);
        Assertions.assertEquals(17.5, map._notes[18]._time);
        Assertions.assertEquals(18.25, map._notes[19]._time);
        Assertions.assertEquals(18.5, map._notes[20]._time);
        Assertions.assertEquals(20.0, map._notes[21]._time);
        Assertions.assertEquals(20.25, map._notes[22]._time);
        Assertions.assertEquals(23.5, map._notes[23]._time);
        Assertions.assertEquals(23.75, map._notes[24]._time);
        Assertions.assertEquals(23.75, map._notes[25]._time);
        Assertions.assertEquals(24.5, map._notes[26]._time);

        for (int i = 0; i < 27; i++) map._notes[i]._time += 0.000000001F;
        map.fixPlacements(0.5); //1 / 2

        Assertions.assertEquals(6.5, map._notes[0]._time);
        Assertions.assertEquals(6.5, map._notes[1]._time);
        Assertions.assertEquals(6.5, map._notes[2]._time);
        Assertions.assertEquals(6.5, map._notes[3]._time);
        Assertions.assertEquals(10.0, map._notes[4]._time);
        Assertions.assertEquals(10.5, map._notes[5]._time);
        Assertions.assertEquals(10.5, map._notes[6]._time);
        Assertions.assertEquals(11.0, map._notes[7]._time);
        Assertions.assertEquals(11.5, map._notes[8]._time);
        Assertions.assertEquals(11.5, map._notes[9]._time);
        Assertions.assertEquals(12.5, map._notes[10]._time);
        Assertions.assertEquals(12.5, map._notes[11]._time);
        Assertions.assertEquals(12.5, map._notes[12]._time);
        Assertions.assertEquals(14.5, map._notes[13]._time);
        Assertions.assertEquals(14.5, map._notes[14]._time);
        Assertions.assertEquals(14.5, map._notes[15]._time);
        Assertions.assertEquals(17.5, map._notes[16]._time);
        Assertions.assertEquals(17.5, map._notes[17]._time);
        Assertions.assertEquals(17.5, map._notes[18]._time);
        Assertions.assertEquals(18.5, map._notes[19]._time);
        Assertions.assertEquals(18.5, map._notes[20]._time);
        Assertions.assertEquals(20.0, map._notes[21]._time);
        Assertions.assertEquals(20.5, map._notes[22]._time);
        Assertions.assertEquals(23.5, map._notes[23]._time);
        Assertions.assertEquals(24.0, map._notes[24]._time);
        Assertions.assertEquals(24.0, map._notes[25]._time);
        Assertions.assertEquals(24.5, map._notes[26]._time);


        for (int i = 0; i < 27; i++) map._notes[i]._time += 0.000000001F;
        map.fixPlacements(1); //1 / 1

        Assertions.assertEquals(7.0, map._notes[0]._time);
        Assertions.assertEquals(7.0, map._notes[1]._time);
        Assertions.assertEquals(7.0, map._notes[2]._time);
        Assertions.assertEquals(7.0, map._notes[3]._time);
        Assertions.assertEquals(10.0, map._notes[4]._time);
        Assertions.assertEquals(11.0, map._notes[5]._time);
        Assertions.assertEquals(11.0, map._notes[6]._time);
        Assertions.assertEquals(11.0, map._notes[7]._time);
        Assertions.assertEquals(12.0, map._notes[8]._time);
        Assertions.assertEquals(12.0, map._notes[9]._time);
        Assertions.assertEquals(13.0, map._notes[10]._time);
        Assertions.assertEquals(13.0, map._notes[11]._time);
        Assertions.assertEquals(13.0, map._notes[12]._time);
        Assertions.assertEquals(15.0, map._notes[13]._time);
        Assertions.assertEquals(15.0, map._notes[14]._time);
        Assertions.assertEquals(15.0, map._notes[15]._time);
        Assertions.assertEquals(18.0, map._notes[16]._time);
        Assertions.assertEquals(18.0, map._notes[17]._time);
        Assertions.assertEquals(18.0, map._notes[18]._time);
        Assertions.assertEquals(19.0, map._notes[19]._time);
        Assertions.assertEquals(19.0, map._notes[20]._time);
        Assertions.assertEquals(20.0, map._notes[21]._time);
        Assertions.assertEquals(21.0, map._notes[22]._time);
        Assertions.assertEquals(24.0, map._notes[23]._time);
        Assertions.assertEquals(24.0, map._notes[24]._time);
        Assertions.assertEquals(24.0, map._notes[25]._time);
        Assertions.assertEquals(25.0, map._notes[26]._time);
    }

    @Test
    void newMapFromJSON() {
        BeatSaberMap map = BeatSaberMap.newMapFromJSON("src/test/resources/BookmarksExample.txt");

        Assertions.assertEquals(887, map._notes.length);
        Assertions.assertEquals(49, map.bookmarks.size());
        Assertions.assertEquals(0, map._events.length);
        Assertions.assertEquals(0, map._obstacles.length);
    }

    @Test
    void testToString() {
        BeatSaberMap map = new Gson().fromJson(FileManager.readFile("src/test/resources/Template--ISeeFire.txt").get(0), BeatSaberMap.class);
        Assertions.assertEquals(85154, map.toString().length());

    }

    @Test
    void exportAsMap() {
        String json1 = FileManager.readFile("src/test/resources/MinimalMapExample.txt").get(0);
        String json2 = FileManager.readFile("src/test/resources/MapExample.txt").get(0).replace(",\"_customData\":{\"_time\":15.022}", "");
        String json3 = FileManager.readFile("src/test/resources/BookmarksExample.txt").get(0).replace("\"_time\":13.492,", "");
        String json4 = FileManager.readFile("src/test/resources/Template--ISeeFire.txt").get(0).replace(",\"_customData\":{\"_time\":0.121}", "");
        String json5 = FileManager.readFile("src/test/resources/TimingsExampleMap.txt").get(0).replace(",\"_customData\":{\"_time\":0.121}", "");

        assertEquals(json1, new Gson().fromJson(json1, BeatSaberMap.class).setOriginalJson(json1).exportAsMap());
        assertEquals(json2, new Gson().fromJson(json2, BeatSaberMap.class).setOriginalJson(json2).exportAsMap());
        assertEquals(json3, new Gson().fromJson(json3, BeatSaberMap.class).setOriginalJson(json3).exportAsMap());
        assertEquals(json4, new Gson().fromJson(json4, BeatSaberMap.class).setOriginalJson(json4).exportAsMap());
        assertEquals(json5, new Gson().fromJson(json5, BeatSaberMap.class).setOriginalJson(json5).exportAsMap());
    }

    @Test
    void setOriginalJson() {
        String json3 = FileManager.readFile("src/test/resources/BookmarksExample.txt").get(0).replace("\"_time\":13.492,", "");
        BeatSaberMap map = new Gson().fromJson(json3, BeatSaberMap.class).setOriginalJson(json3);

        assertEquals(json3, map.setOriginalJson(json3).originalJSON);
        assertEquals(json3, map.setOriginalJson(json3).exportAsMap());
        assertEquals(49, map.bookmarks.size());
        assertEquals("[{\"_time\":0.0,\"_name\":\"complex\",\"_color\":[0.25, 1.0, 0.622, 1.0]}, {\"_time\":4.696,\"_name\":\"doubles\",\"_color\":[0.25, 0.785, 1.0, 1.0]}, {\"_time\":16.771,\"_name\":\"complex\",\"_color\":[0.545, 1.0, 0.25, 1.0]}, {\"_time\":23.479,\"_name\":\"1-2\",\"_color\":[1.0, 0.25, 0.741, 1.0]}, {\"_time\":28.846,\"_name\":\"2-1\",\"_color\":[0.25, 0.56, 1.0, 1.0]}, {\"_time\":32.535,\"_name\":\"1-2\",\"_color\":[1.0, 0.25, 0.838, 1.0]}, {\"_time\":35.89,\"_name\":\"doubles\",\"_color\":[0.25, 1.0, 0.935, 1.0]}, {\"_time\":40.25,\"_name\":\"2-1\",\"_color\":[1.0, 0.925, 0.25, 1.0]}, {\"_time\":44.946,\"_name\":\"1-2\",\"_color\":[1.0, 0.979, 0.25, 1.0]}, {\"_time\":48.635,\"_name\":\"2-1\",\"_color\":[1.0, 0.25, 0.97, 1.0]}, {\"_time\":52.325,\"_name\":\"doubles\",\"_color\":[1.0, 0.25, 0.991, 1.0]}, {\"_time\":56.35,\"_name\":\"complex\",\"_color\":[0.374, 0.25, 1.0, 1.0]}, {\"_time\":79.829,\"_name\":\"1-2\",\"_color\":[1.0, 0.25, 0.285, 1.0]}, {\"_time\":96.6,\"_name\":\"2-1\",\"_color\":[0.318, 1.0, 0.25, 1.0]}, {\"_time\":112.7,\"_name\":\"complex\",\"_color\":[0.841, 0.25, 1.0, 1.0]}, {\"_time\":145.235,\"_name\":\"1-2\",\"_color\":[0.759, 0.25, 1.0, 1.0]}, {\"_time\":164.354,\"_name\":\"doubles\",\"_color\":[0.573, 0.25, 1.0, 1.0]}, {\"_time\":166.702,\"_name\":\"complex\",\"_color\":[1.0, 0.996, 0.25, 1.0]}, {\"_time\":168.715,\"_name\":\"doubles\",\"_color\":[0.523, 0.25, 1.0, 1.0]}, {\"_time\":175.758,\"_name\":\"1-2\",\"_color\":[0.373, 1.0, 0.25, 1.0]}, {\"_time\":183.808,\"_name\":\"1-2\",\"_color\":[1.0, 0.549, 0.25, 1.0]}, {\"_time\":191.858,\"_name\":\"doubles\",\"_color\":[0.25, 1.0, 0.335, 1.0]}, {\"_time\":194.206,\"_name\":\"complex\",\"_color\":[1.0, 0.25, 0.707, 1.0]}, {\"_time\":205.946,\"_name\":\"doubles\",\"_color\":[0.25, 1.0, 0.954, 1.0]}, {\"_time\":207.623,\"_name\":\"1-2\",\"_color\":[0.998, 1.0, 0.25, 1.0]}, {\"_time\":224.394,\"_name\":\"2-1\",\"_color\":[1.0, 0.669, 0.25, 1.0]}, {\"_time\":240.158,\"_name\":\"complex\",\"_color\":[0.25, 1.0, 0.311, 1.0]}, {\"_time\":267.663,\"_name\":\"linear\",\"_color\":[0.25, 1.0, 0.663, 1.0]}, {\"_time\":272.358,\"_name\":\"complex\",\"_color\":[1.0, 0.25, 0.358, 1.0]}, {\"_time\":288.123,\"_name\":\"doubles\",\"_color\":[0.589, 1.0, 0.25, 1.0]}, {\"_time\":292.819,\"_name\":\"1-2\",\"_color\":[0.25, 1.0, 0.574, 1.0]}, {\"_time\":303.888,\"_name\":\"2-1\",\"_color\":[0.924, 1.0, 0.25, 1.0]}, {\"_time\":325.69,\"_name\":\"doubles\",\"_color\":[0.25, 1.0, 0.556, 1.0]}, {\"_time\":328.038,\"_name\":\"linear\",\"_color\":[0.572, 1.0, 0.25, 1.0]}, {\"_time\":331.392,\"_name\":\"doubles\",\"_color\":[1.0, 0.262, 0.25, 1.0]}, {\"_time\":335.752,\"_name\":\"1-2\",\"_color\":[0.25, 1.0, 0.673, 1.0]}, {\"_time\":352.188,\"_name\":\"2-1\",\"_color\":[0.25, 1.0, 0.95, 1.0]}, {\"_time\":368.288,\"_name\":\"2-2\",\"_color\":[0.25, 1.0, 0.762, 1.0]}, {\"_time\":384.723,\"_name\":\"1-2\",\"_color\":[0.72, 1.0, 0.25, 1.0]}, {\"_time\":395.792,\"_name\":\"complex\",\"_color\":[1.0, 0.25, 0.411, 1.0]}, {\"_time\":400.152,\"_name\":\"doubles\",\"_color\":[0.957, 0.25, 1.0, 1.0]}, {\"_time\":401.494,\"_name\":\"2-1\",\"_color\":[1.0, 0.25, 0.819, 1.0]}, {\"_time\":412.562,\"_name\":\"doubles\",\"_color\":[0.25, 1.0, 0.299, 1.0]}, {\"_time\":415.917,\"_name\":\"1-2\",\"_color\":[0.25, 1.0, 0.556, 1.0]}, {\"_time\":420.612,\"_name\":\"2-1\",\"_color\":[0.25, 1.0, 0.275, 1.0]}, {\"_time\":424.302,\"_name\":\"1-2\",\"_color\":[0.975, 1.0, 0.25, 1.0]}, {\"_time\":428.327,\"_name\":\"doubles\",\"_color\":[0.655, 1.0, 0.25, 1.0]}, {\"_time\":431.681,\"_name\":\"2-2\",\"_color\":[0.25, 0.903, 1.0, 1.0]}, {\"_time\":460.192,\"_name\":\"complex\",\"_color\":[0.25, 1.0, 0.847, 1.0]}]",
                map.bookmarks.toString());
    }


    @Test
    void convertAllFlashLightsToOnLights() {
        BeatSaberMap map = new Gson().fromJson(FileManager.readFile("src/test/resources/MapExample.txt").get(0), BeatSaberMap.class);
        map.convertAllFlashLightsToOnLights();
        for (int i = 0; i < map._events.length; i++) {
            Assertions.assertNotEquals(6, map._events[i]._value);
        }
    }

    @Test
    void makeOneHanded() {
        BeatSaberMap map = new Gson().fromJson(FileManager.readFile("src/test/resources/MapExample.txt").get(0), BeatSaberMap.class);
        map.makeOneHanded(0);
        for (int i = 0; i < map._notes.length; i++) {
            Assertions.assertNotEquals(0, map._notes[i]._type);
        }
    }

    @Test
    void makeNoArrows() {
        BeatSaberMap map = new Gson().fromJson(FileManager.readFile("src/test/resources/MapExample.txt").get(0), BeatSaberMap.class);
        map.makeNoArrows();
        for (int i = 0; i < map._notes.length; i++) {
            Assertions.assertEquals(8, map._notes[i]._cutDirection);
        }

    }

    @Test
    void toTimingNotes() {
        BeatSaberMap map = new Gson().fromJson(FileManager.readFile("src/test/resources/MapExample.txt").get(0), BeatSaberMap.class);
        map.makeNoArrows();
        for (int i = 0; i < map._notes.length; i++) {
            Assertions.assertEquals(8, map._notes[i]._cutDirection);
        }
    }

    @Test
    void toBlueLeftBottomRowDotTimings() {
        BeatSaberMap map = new Gson().fromJson(FileManager.readFile("src/test/resources/MapExample.txt").get(0), BeatSaberMap.class);
        map.toBlueLeftBottomRowDotTimings();
        for (int i = 0; i < map._notes.length; i++) {
            Assertions.assertEquals(8, map._notes[i]._cutDirection);
            Assertions.assertEquals(1, map._notes[i]._type);
            Assertions.assertEquals(0, map._notes[i]._lineLayer);
            Assertions.assertEquals(0, map._notes[i]._lineIndex);
            Assertions.assertEquals(878, map._notes.length);
        }
    }

    @Test
    void calculateBookmarks() {
        BeatSaberMap map = new Gson().fromJson(FileManager.readFile("src/test/resources/BookmarksExample.txt").get(0), BeatSaberMap.class);
        map.originalJSON = FileManager.readFile("src/test/resources/BookmarksExample.txt").get(0);
        map.calculateBookmarks();
        Assertions.assertEquals(49, map.bookmarks.size());
        Assertions.assertEquals("[{\"_time\":0.0,\"_name\":\"complex\",\"_color\":[0.25, 1.0, 0.622, 1.0]}, {\"_time\":4.696,\"_name\":\"doubles\",\"_color\":[0.25, 0.785, 1.0, 1.0]}, {\"_time\":16.771,\"_name\":\"complex\",\"_color\":[0.545, 1.0, 0.25, 1.0]}, {\"_time\":23.479,\"_name\":\"1-2\",\"_color\":[1.0, 0.25, 0.741, 1.0]}, {\"_time\":28.846,\"_name\":\"2-1\",\"_color\":[0.25, 0.56, 1.0, 1.0]}, {\"_time\":32.535,\"_name\":\"1-2\",\"_color\":[1.0, 0.25, 0.838, 1.0]}, {\"_time\":35.89,\"_name\":\"doubles\",\"_color\":[0.25, 1.0, 0.935, 1.0]}, {\"_time\":40.25,\"_name\":\"2-1\",\"_color\":[1.0, 0.925, 0.25, 1.0]}, {\"_time\":44.946,\"_name\":\"1-2\",\"_color\":[1.0, 0.979, 0.25, 1.0]}, {\"_time\":48.635,\"_name\":\"2-1\",\"_color\":[1.0, 0.25, 0.97, 1.0]}, {\"_time\":52.325,\"_name\":\"doubles\",\"_color\":[1.0, 0.25, 0.991, 1.0]}, {\"_time\":56.35,\"_name\":\"complex\",\"_color\":[0.374, 0.25, 1.0, 1.0]}, {\"_time\":79.829,\"_name\":\"1-2\",\"_color\":[1.0, 0.25, 0.285, 1.0]}, {\"_time\":96.6,\"_name\":\"2-1\",\"_color\":[0.318, 1.0, 0.25, 1.0]}, {\"_time\":112.7,\"_name\":\"complex\",\"_color\":[0.841, 0.25, 1.0, 1.0]}, {\"_time\":145.235,\"_name\":\"1-2\",\"_color\":[0.759, 0.25, 1.0, 1.0]}, {\"_time\":164.354,\"_name\":\"doubles\",\"_color\":[0.573, 0.25, 1.0, 1.0]}, {\"_time\":166.702,\"_name\":\"complex\",\"_color\":[1.0, 0.996, 0.25, 1.0]}, {\"_time\":168.715,\"_name\":\"doubles\",\"_color\":[0.523, 0.25, 1.0, 1.0]}, {\"_time\":175.758,\"_name\":\"1-2\",\"_color\":[0.373, 1.0, 0.25, 1.0]}, {\"_time\":183.808,\"_name\":\"1-2\",\"_color\":[1.0, 0.549, 0.25, 1.0]}, {\"_time\":191.858,\"_name\":\"doubles\",\"_color\":[0.25, 1.0, 0.335, 1.0]}, {\"_time\":194.206,\"_name\":\"complex\",\"_color\":[1.0, 0.25, 0.707, 1.0]}, {\"_time\":205.946,\"_name\":\"doubles\",\"_color\":[0.25, 1.0, 0.954, 1.0]}, {\"_time\":207.623,\"_name\":\"1-2\",\"_color\":[0.998, 1.0, 0.25, 1.0]}, {\"_time\":224.394,\"_name\":\"2-1\",\"_color\":[1.0, 0.669, 0.25, 1.0]}, {\"_time\":240.158,\"_name\":\"complex\",\"_color\":[0.25, 1.0, 0.311, 1.0]}, {\"_time\":267.663,\"_name\":\"linear\",\"_color\":[0.25, 1.0, 0.663, 1.0]}, {\"_time\":272.358,\"_name\":\"complex\",\"_color\":[1.0, 0.25, 0.358, 1.0]}, {\"_time\":288.123,\"_name\":\"doubles\",\"_color\":[0.589, 1.0, 0.25, 1.0]}, {\"_time\":292.819,\"_name\":\"1-2\",\"_color\":[0.25, 1.0, 0.574, 1.0]}, {\"_time\":303.888,\"_name\":\"2-1\",\"_color\":[0.924, 1.0, 0.25, 1.0]}, {\"_time\":325.69,\"_name\":\"doubles\",\"_color\":[0.25, 1.0, 0.556, 1.0]}, {\"_time\":328.038,\"_name\":\"linear\",\"_color\":[0.572, 1.0, 0.25, 1.0]}, {\"_time\":331.392,\"_name\":\"doubles\",\"_color\":[1.0, 0.262, 0.25, 1.0]}, {\"_time\":335.752,\"_name\":\"1-2\",\"_color\":[0.25, 1.0, 0.673, 1.0]}, {\"_time\":352.188,\"_name\":\"2-1\",\"_color\":[0.25, 1.0, 0.95, 1.0]}, {\"_time\":368.288,\"_name\":\"2-2\",\"_color\":[0.25, 1.0, 0.762, 1.0]}, {\"_time\":384.723,\"_name\":\"1-2\",\"_color\":[0.72, 1.0, 0.25, 1.0]}, {\"_time\":395.792,\"_name\":\"complex\",\"_color\":[1.0, 0.25, 0.411, 1.0]}, {\"_time\":400.152,\"_name\":\"doubles\",\"_color\":[0.957, 0.25, 1.0, 1.0]}, {\"_time\":401.494,\"_name\":\"2-1\",\"_color\":[1.0, 0.25, 0.819, 1.0]}, {\"_time\":412.562,\"_name\":\"doubles\",\"_color\":[0.25, 1.0, 0.299, 1.0]}, {\"_time\":415.917,\"_name\":\"1-2\",\"_color\":[0.25, 1.0, 0.556, 1.0]}, {\"_time\":420.612,\"_name\":\"2-1\",\"_color\":[0.25, 1.0, 0.275, 1.0]}, {\"_time\":424.302,\"_name\":\"1-2\",\"_color\":[0.975, 1.0, 0.25, 1.0]}, {\"_time\":428.327,\"_name\":\"doubles\",\"_color\":[0.655, 1.0, 0.25, 1.0]}, {\"_time\":431.681,\"_name\":\"2-2\",\"_color\":[0.25, 0.903, 1.0, 1.0]}, {\"_time\":460.192,\"_name\":\"complex\",\"_color\":[0.25, 1.0, 0.847, 1.0]}]",
                map.bookmarks.toString());
    }

}