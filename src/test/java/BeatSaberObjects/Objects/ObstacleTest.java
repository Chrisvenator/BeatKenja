package BeatSaberObjects.Objects;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Tests for Obstacle class")
class ObstacleTest {
    
    // ----- toString() Tests -----
    
    @Test
    void testToString() {
        Obstacle o = new Obstacle(0, "0", 0, 0, 0);
        Assertions.assertEquals("{\"_time\":0.0,\"_lineIndex\":0,\"_type\":0,\"_duration\":0.0,\"_width\":0.0}", o.toString());
    }
    
    
    @Test
    @DisplayName("toString: Should return a correctly formatted JSON-like string")
    void testToStringNormal() {
        float time = 5.5f;
        String lineIndex = "2";
        int type = 1;
        float duration = 3.0f;
        float width = 2.5f;
        Obstacle obstacle = new Obstacle(time, lineIndex, type, duration, width);
        String expected = "{\"_time\":" + time + ",\"_lineIndex\":" + lineIndex + ",\"_type\":" + type +
                ",\"_duration\":" + duration + ",\"_width\":" + width + "}";
        assertEquals(expected, obstacle.toString(), "toString should produce the expected JSON-like string");
    }
    
    @Test
    @DisplayName("toString: Should handle a null _lineIndex gracefully")
    void testToStringNullLineIndex() {
        float time = 5.5f;
        String lineIndex = null;
        int type = 1;
        float duration = 3.0f;
        float width = 2.5f;
        Obstacle obstacle = new Obstacle(time, lineIndex, type, duration, width);
        String expected = "{\"_time\":" + time + ",\"_lineIndex\":" + lineIndex + ",\"_type\":" + type +
                ",\"_duration\":" + duration + ",\"_width\":" + width + "}";
        assertEquals(expected, obstacle.toString(), "toString should handle null _lineIndex");
    }
    
    // ----- equals() Tests -----
    
    @Test
    @DisplayName("equals: An Obstacle should equal itself (reflexivity)")
    void testEqualsSameInstance() {
        Obstacle obstacle = new Obstacle(5.5f, "2", 1, 3.0f, 2.5f);
        assertEquals(obstacle, obstacle, "An obstacle must equal itself");
    }
    
    @Test
    @DisplayName("equals: An Obstacle should not equal null")
    void testEqualsNull() {
        Obstacle obstacle = new Obstacle(5.5f, "2", 1, 3.0f, 2.5f);
        assertNotEquals(null, obstacle, "An obstacle should not equal null");
    }
    
    @Test
    @DisplayName("equals: An Obstacle should not equal an object of a different type")
    void testEqualsDifferentType() {
        Obstacle obstacle = new Obstacle(5.5f, "2", 1, 3.0f, 2.5f);
        String notAnObstacle = "Not an obstacle";
        assertNotEquals(obstacle, notAnObstacle, "An obstacle should not equal an object of a different type");
    }
    
    @Test
    @DisplayName("equals: Obstacles with identical fields are equal")
    void testEqualsSameValues() {
        Obstacle obstacle1 = new Obstacle(5.5f, "2", 1, 3.0f, 2.5f);
        Obstacle obstacle2 = new Obstacle(5.5f, "2", 1, 3.0f, 2.5f);
        assertEquals(obstacle1, obstacle2, "Obstacles with the same field values should be equal");
        assertEquals(obstacle2, obstacle1, "Equality should be symmetric");
    }
    
    @Test
    @DisplayName("equals: Obstacles with different _time are not equal")
    void testEqualsDifferentTime() {
        Obstacle obstacle1 = new Obstacle(5.5f, "2", 1, 3.0f, 2.5f);
        Obstacle obstacle2 = new Obstacle(6.5f, "2", 1, 3.0f, 2.5f);
        assertNotEquals(obstacle1, obstacle2, "Obstacles with different _time values should not be equal");
    }
    
    @Test
    @DisplayName("equals: Obstacles with different _lineIndex are not equal")
    void testEqualsDifferentLineIndex() {
        Obstacle obstacle1 = new Obstacle(5.5f, "2", 1, 3.0f, 2.5f);
        Obstacle obstacle2 = new Obstacle(5.5f, "3", 1, 3.0f, 2.5f);
        assertNotEquals(obstacle1, obstacle2, "Obstacles with different _lineIndex values should not be equal");
    }
    
    @Test
    @DisplayName("equals: Obstacles with different _type are not equal")
    void testEqualsDifferentTypeField() {
        Obstacle obstacle1 = new Obstacle(5.5f, "2", 1, 3.0f, 2.5f);
        Obstacle obstacle2 = new Obstacle(5.5f, "2", 2, 3.0f, 2.5f);
        assertNotEquals(obstacle1, obstacle2, "Obstacles with different _type values should not be equal");
    }
    
    @Test
    @DisplayName("equals: Obstacles with different _duration are not equal")
    void testEqualsDifferentDuration() {
        Obstacle obstacle1 = new Obstacle(5.5f, "2", 1, 3.0f, 2.5f);
        Obstacle obstacle2 = new Obstacle(5.5f, "2", 1, 4.0f, 2.5f);
        assertNotEquals(obstacle1, obstacle2, "Obstacles with different _duration values should not be equal");
    }
    
    @Test
    @DisplayName("equals: Obstacles with different _width are not equal")
    void testEqualsDifferentWidth() {
        Obstacle obstacle1 = new Obstacle(5.5f, "2", 1, 3.0f, 2.5f);
        Obstacle obstacle2 = new Obstacle(5.5f, "2", 1, 3.0f, 3.0f);
        assertNotEquals(obstacle1, obstacle2, "Obstacles with different _width values should not be equal");
    }
    
    @Test
    @DisplayName("equals: Obstacles with null _lineIndex compare correctly")
    void testEqualsWithNullLineIndex() {
        Obstacle obstacle1 = new Obstacle(5.5f, null, 1, 3.0f, 2.5f);
        Obstacle obstacle2 = new Obstacle(5.5f, null, 1, 3.0f, 2.5f);
        assertEquals(obstacle1, obstacle2, "Obstacles with null _lineIndex should be equal if other fields match");
    }
    
    // ----- hashCode() Tests -----
    
    @Test
    @DisplayName("hashCode: Equal obstacles must produce the same hash code")
    void testHashCodeConsistency() {
        Obstacle obstacle1 = new Obstacle(5.5f, "2", 1, 3.0f, 2.5f);
        Obstacle obstacle2 = new Obstacle(5.5f, "2", 1, 3.0f, 2.5f);
        assertEquals(obstacle1.hashCode(), obstacle2.hashCode(), "Equal obstacles should have identical hash codes");
    }
    
    @Test
    @DisplayName("hashCode: Changing any field should affect the hash code")
    void testHashCodeDifference() {
        Obstacle base = new Obstacle(5.5f, "2", 1, 3.0f, 2.5f);
        Obstacle diffTime = new Obstacle(6.5f, "2", 1, 3.0f, 2.5f);
        Obstacle diffLineIndex = new Obstacle(5.5f, "3", 1, 3.0f, 2.5f);
        Obstacle diffType = new Obstacle(5.5f, "2", 2, 3.0f, 2.5f);
        Obstacle diffDuration = new Obstacle(5.5f, "2", 1, 4.0f, 2.5f);
        Obstacle diffWidth = new Obstacle(5.5f, "2", 1, 3.0f, 3.0f);
        
        // Note: While hash code collisions can happen, for our purposes these differences should result in different hash codes.
        assertNotEquals(base.hashCode(), diffTime.hashCode(), "Different _time should yield a different hash code");
        assertNotEquals(base.hashCode(), diffLineIndex.hashCode(), "Different _lineIndex should yield a different hash code");
        assertNotEquals(base.hashCode(), diffType.hashCode(), "Different _type should yield a different hash code");
        assertNotEquals(base.hashCode(), diffDuration.hashCode(), "Different _duration should yield a different hash code");
        assertNotEquals(base.hashCode(), diffWidth.hashCode(), "Different _width should yield a different hash code");
    }
}
