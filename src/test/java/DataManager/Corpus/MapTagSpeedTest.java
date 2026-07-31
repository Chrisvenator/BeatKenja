package DataManager.Corpus;

import DataManager.Records.MapTag;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MapTagSpeedTest {

    @Test
    void mapTag_fromString_knownValues() {
        assertEquals(MapTag.SPEED,    MapTag.fromString("Speed"));
        assertEquals(MapTag.TECH,     MapTag.fromString("Tech"));
        assertEquals(MapTag.ACCURACY, MapTag.fromString("Accuracy"));
        assertEquals(MapTag.NULL,     MapTag.fromString("NULL"));
    }

    @Test
    void mapTag_fromString_caseInsensitive() {
        assertEquals(MapTag.SPEED, MapTag.fromString("speed"));
        assertEquals(MapTag.TECH,  MapTag.fromString("TECH"));
    }

    @Test
    void mapTag_fromString_unknownReturnsNull() {
        assertEquals(MapTag.NULL, MapTag.fromString("SomethingUnknown"));
        assertEquals(MapTag.NULL, MapTag.fromString(null));
    }

    @Test
    void categorizeSpeed_tagSpeedOverridesLowNps() {
        // NPS 1.0 would normally be TECH, but Speed tag forces FAST
        List<MapTag> tags = List.of(MapTag.SPEED);
        assertEquals(MapPackage.SpeedCategory.FAST, MapPackage.categorizeSpeed(1.0, tags));
    }

    @Test
    void categorizeSpeed_tagTechOverridesHighNps() {
        // NPS 10.0 would normally be FAST, but Tech tag forces TECH
        List<MapTag> tags = List.of(MapTag.TECH);
        assertEquals(MapPackage.SpeedCategory.TECH, MapPackage.categorizeSpeed(10.0, tags));
    }

    @Test
    void categorizeSpeed_noTagsFallsBackToNps() {
        List<MapTag> noTags = List.of();
        assertEquals(MapPackage.SpeedCategory.FAST,   MapPackage.categorizeSpeed(8.0, noTags));
        assertEquals(MapPackage.SpeedCategory.NORMAL, MapPackage.categorizeSpeed(5.0, noTags));
        assertEquals(MapPackage.SpeedCategory.TECH,   MapPackage.categorizeSpeed(1.0, noTags));
    }

    @Test
    void categorizeSpeed_speedTagTakesPrecedenceOverTech() {
        // Speed tag listed first → FAST
        List<MapTag> both = List.of(MapTag.SPEED, MapTag.TECH);
        assertEquals(MapPackage.SpeedCategory.FAST, MapPackage.categorizeSpeed(5.0, both));
    }
}
