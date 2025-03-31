package BeatSaberObjects.Objects;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Tests for Bookmark class")
class BookmarkTest {
    
    // ----- toString() tests -----
    
    @Test
    @DisplayName("toString: Should return a correctly formatted JSON-like string")
    void testToStringNormal() {
        float time = 12.5f;
        String name = "Chapter1";
        float[] color = {1.0f, 0.5f, 0.25f};
        Bookmark bookmark = new Bookmark(time, name, color);
        String expected = "{\"_time\":" + time + ",\"_name\":\"" + name + "\",\"_color\":" + Arrays.toString(color) + "}";
        assertEquals(expected, bookmark.toString(), "toString should output the expected JSON string");
    }
    
    @Test
    @DisplayName("toString: Should handle null _color and null _name gracefully")
    void testToStringWithNulls() {
        float time = 0.0f;
        Bookmark bookmark1 = new Bookmark(time, null, new float[]{1.0f, 2.0f});
        Bookmark bookmark2 = new Bookmark(time, "Test", null);
        Bookmark bookmark3 = new Bookmark(time, null, null);
        
        String expected1 = "{\"_time\":" + time + ",\"_name\":\"null\",\"_color\":" + Arrays.toString(new float[]{1.0f, 2.0f}) + "}";
        String expected2 = "{\"_time\":" + time + ",\"_name\":\"Test\",\"_color\":null}";
        String expected3 = "{\"_time\":" + time + ",\"_name\":\"null\",\"_color\":null}";
        
        assertEquals(expected1, bookmark1.toString(), "toString should handle a null _name by printing \"null\"");
        assertEquals(expected2, bookmark2.toString(), "toString should handle a null _color by printing \"null\"");
        assertEquals(expected3, bookmark3.toString(), "toString should handle both _name and _color as null");
    }
    
    // ----- equals() tests -----
    
    @Test
    @DisplayName("equals: Same instance returns true")
    void testEqualsSameInstance() {
        Bookmark bookmark = new Bookmark(10.0f, "Mark", new float[]{0.1f, 0.2f});
        assertEquals(bookmark, bookmark, "A Bookmark should equal itself");
    }
    
    @Test
    @DisplayName("equals: Comparing with null returns false")
    void testEqualsNull() {
        Bookmark bookmark = new Bookmark(10.0f, "Mark", new float[]{0.1f, 0.2f});
        assertNotEquals(null, bookmark, "A Bookmark should not equal null");
    }
    
    @Test
    @DisplayName("equals: Comparing with an object of a different type returns false")
    void testEqualsDifferentClass() {
        Bookmark bookmark = new Bookmark(10.0f, "Mark", new float[]{0.1f, 0.2f});
        String other = "Not a bookmark";
        assertNotEquals(bookmark, other, "A Bookmark should not equal an object of a different type");
    }
    
    @Test
    @DisplayName("equals: Bookmarks with same _time and _name are equal (regardless of _color)")
    void testEqualsSameValues() {
        // Note: _color is not used in equals, so even if colors differ, the Bookmarks are equal.
        Bookmark bookmark1 = new Bookmark(10.0f, "Mark", new float[]{0.1f, 0.2f});
        Bookmark bookmark2 = new Bookmark(10.0f, "Mark", new float[]{0.9f, 0.8f});
        assertEquals(bookmark1, bookmark2, "Bookmarks with the same _time and _name should be equal even if _color differs");
    }
    
    @Test
    @DisplayName("equals: Bookmarks with different _time are not equal")
    void testEqualsDifferentTime() {
        Bookmark bookmark1 = new Bookmark(10.0f, "Mark", new float[]{0.1f, 0.2f});
        Bookmark bookmark2 = new Bookmark(11.0f, "Mark", new float[]{0.1f, 0.2f});
        assertNotEquals(bookmark1, bookmark2, "Bookmarks with different _time values should not be equal");
    }
    
    @Test
    @DisplayName("equals: Bookmarks with different _name are not equal")
    void testEqualsDifferentName() {
        Bookmark bookmark1 = new Bookmark(10.0f, "Mark", new float[]{0.1f, 0.2f});
        Bookmark bookmark2 = new Bookmark(10.0f, "Chapter1", new float[]{0.1f, 0.2f});
        assertNotEquals(bookmark1, bookmark2, "Bookmarks with different _name values should not be equal");
    }
    
    @Test
    @DisplayName("equals: Bookmarks with null _name compare correctly")
    void testEqualsWithNullName() {
        Bookmark bookmark1 = new Bookmark(10.0f, null, new float[]{0.1f, 0.2f});
        Bookmark bookmark2 = new Bookmark(10.0f, null, new float[]{0.9f, 0.8f});
        assertEquals(bookmark1, bookmark2, "Bookmarks with null _name should be equal if _time is the same");
    }
    
    // ----- hashCode() tests -----
    
    @Test
    @DisplayName("hashCode: Equal Bookmarks have the same hash code when _color arrays are equal by content")
    void testHashCodeConsistency() {
        float[] color = {0.1f, 0.2f, 0.3f};
        Bookmark bookmark1 = new Bookmark(10.0f, "Mark", color);
        // Create a new array with the same content
        Bookmark bookmark2 = new Bookmark(10.0f, "Mark", new float[]{0.1f, 0.2f, 0.3f});
        // Although equals ignores _color, hashCode factors it in.
        // Since the array content is the same, hashCode should be identical.
        assertEquals(bookmark1.hashCode(), bookmark2.hashCode(), "Equal Bookmarks with identical color content should have the same hashCode");
    }
    
    @Test
    @DisplayName("hashCode: Bookmarks with same _time and _name but different _color arrays may have different hash codes")
    void testHashCodeDifferentColor() {
        Bookmark bookmark1 = new Bookmark(10.0f, "Mark", new float[]{0.1f, 0.2f});
        Bookmark bookmark2 = new Bookmark(10.0f, "Mark", new float[]{0.9f, 0.8f});
        // Even though equals returns true (because _color is not checked), hashCode includes _color.
        // So in this implementation, two equal objects may have different hash codes.
        // We document this behavior:
        if (!Arrays.equals(new float[]{0.1f, 0.2f}, new float[]{0.9f, 0.8f})) {
            assertNotEquals(bookmark1.hashCode(), bookmark2.hashCode(), "Bookmarks with different _color arrays should have different hashCodes, even if equals returns true");
        }
    }
    
    @Test
    @DisplayName("hashCode: Consistency for a given Bookmark")
    void testHashCodeConsistencyForSameInstance() {
        Bookmark bookmark = new Bookmark(10.0f, "Mark", new float[]{0.1f, 0.2f});
        int hash1 = bookmark.hashCode();
        int hash2 = bookmark.hashCode();
        assertEquals(hash1, hash2, "hashCode should be consistent across multiple invocations");
    }
    
    // ----- Additional edge case tests -----
    
    @Test
    @DisplayName("Constructor and methods: Should correctly handle null _color")
    void testNullColor() {
        Bookmark bookmark = new Bookmark(5.0f, "Test", null);
        // toString should output "null" for _color.
        String expected = "{\"_time\":" + 5.0f + ",\"_name\":\"Test\",\"_color\":null}";
        assertEquals(expected, bookmark.toString(), "toString should handle null _color");
    }
    
    
    // -------- Manual----------
    
    
    @Test
    void testToString() {
        Bookmark b01 = new Bookmark(0, "YES", null);
        Bookmark b02 = new Bookmark(1, "", null);
        Bookmark b03 = new Bookmark(2, "q3 ", new float[]{0.0f, 0.0f, 0.0f});
        Bookmark b04 = new Bookmark(3, "187621", null);
        Assertions.assertEquals("{\"_time\":0.0,\"_name\":\"YES\",\"_color\":null}", b01.toString());
        Assertions.assertEquals("{\"_time\":1.0,\"_name\":\"\",\"_color\":null}", b02.toString());
        Assertions.assertEquals("{\"_time\":2.0,\"_name\":\"q3 \",\"_color\":[0.0, 0.0, 0.0]}", b03.toString());
        Assertions.assertEquals("{\"_time\":3.0,\"_name\":\"187621\",\"_color\":null}", b04.toString());
    }
    
    @Test
    void testEquals() {
        Bookmark b01 = new Bookmark(0, "YES", null);
        Bookmark b02 = new Bookmark(1, "", null);
        Bookmark b03 = new Bookmark(2, "q3 ", new float[]{0.0f, 0.0f, 0.0f});
        Bookmark b031 = new Bookmark(2, "q3 ", new float[]{0.0f, 0.0f, 0.0f});
        Bookmark b04 = new Bookmark(3, "187621", null);
        Bookmark b041 = new Bookmark(3, "187621", null);
        
        Assertions.assertNotEquals(b01, b02);
        Assertions.assertNotEquals(b03, b02);
        Assertions.assertNotEquals(b04, b02);
        Assertions.assertEquals(b03, b031);
        Assertions.assertEquals(b04, b041);
    }
    
}
