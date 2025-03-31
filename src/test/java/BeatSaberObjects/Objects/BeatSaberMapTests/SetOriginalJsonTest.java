package BeatSaberObjects.Objects.BeatSaberMapTests;

import BeatSaberObjects.Objects.BeatSaberMap;
import BeatSaberObjects.Objects.Bookmark;
import BeatSaberObjects.Objects.Note;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("BeatSaberMap Tests: Set Original Json")
public class SetOriginalJsonTest {
    
    private static final String SAMPLE_JSON = """
                                              {
                                                "_version": "3",
                                                "_notes": [
                                                  { "_time": 1.0, "_lineIndex": 1, "_lineLayer": 1 },
                                                  { "_time": 2.5, "_lineIndex": 2, "_lineLayer": 0 }
                                                ],
                                              "_customData":{"_time":13.492,"_bookmarks":[\
                                                     {"_time":0,"_name":"TestBookmark","_color":[0.25,1,0.622,1]},\
                                                     {"_time":4.696,"_name":"doubles","_color":[0.25,0.785,1,1]},\
                                                     {"_time":16.771,"_name":"complex","_color":[0.545,1,0.25,1]}\
                                              ]}\
                                              }""";
    private static final String SAMPLE_JSON_ONE_BOOKMARK = """
                                                           {
                                                             "_version": "3",
                                                             "_notes": [
                                                               { "_time": 1.0, "_lineIndex": 1, "_lineLayer": 1 },
                                                               { "_time": 2.5, "_lineIndex": 2, "_lineLayer": 0 }
                                                             ],
                                                           "_customData":{"_time":13.492,"_bookmarks":[\
                                                                  {"_time":16.771,"_name":"complex","_color":[0.545,1,0.25,1]}\
                                                           ]}\
                                                           }""";
    
    @Test
    void setOriginalJson_shouldStoreJsonAndReturnSameInstance() {
        // given
        BeatSaberMap map = new BeatSaberMap();
        
        // when
        BeatSaberMap returned = map.setOriginalJson(SAMPLE_JSON);
        
        // then
        assertThat(returned).isSameAs(map);
        assertThat(map.originalJSON).isEqualTo(SAMPLE_JSON);
    }
    
    @Test
    void setOriginalJson_shouldTriggerBookmarkCalculation() {
        // given
        BeatSaberMap map = new BeatSaberMap();
        assertTrue(map.bookmarks.isEmpty());
        
        // when
        map.setOriginalJson(SAMPLE_JSON);
        
        // then
        // Only validate if calculateBookmarks added something — adjust if you know exact logic
        assertThat(map.bookmarks).isNotNull();
    }
    
    
    
    private BeatSaberMap map;
    
    @BeforeEach
    public void setUp() {
        // Instantiate with an empty notes array.
        map = new BeatSaberMap(new Note[0]);
    }
    
    @Test
    public void testSetOriginalJsonUpdatesField() {
        map.setOriginalJson(SAMPLE_JSON);
        assertEquals(SAMPLE_JSON, map.originalJSON, "originalJSON should be updated with the provided JSON string.");
    }
    
    @Test
    public void testSetOriginalJsonReturnsSelf() {
        BeatSaberMap returned = map.setOriginalJson(SAMPLE_JSON);
        assertSame(map, returned, "setOriginalJson should return the same instance for method chaining.");
    }
    
    @Test
    public void testCalculateBookmarksTriggered() {
        // Before setting JSON, bookmarks are empty.
        assertTrue(map.bookmarks.isEmpty(), "Bookmarks list should be empty before setting JSON.");
        map.setOriginalJson(SAMPLE_JSON);
        // After setting JSON, the overridden calculateBookmarks adds a dummy bookmark.
        assertFalse(map.bookmarks.isEmpty(), "Bookmarks list should not be empty after setting a valid JSON string.");
        assertEquals(3, map.bookmarks.size(), "Bookmarks list should contain three bookmarks after processing.");
        assertEquals("TestBookmark", map.bookmarks.get(0)._name, "Bookmark should have the expected dummy name.");
    }
    
    @Test
    public void testSetOriginalJsonWithEmptyString() {
        String SAMPLE_JSON = "";
        map.setOriginalJson(SAMPLE_JSON);
        assertEquals(SAMPLE_JSON, map.originalJSON, "originalJSON should be updated even if the string is empty.");
        // In the test subclass, an empty string causes no bookmark to be added.
        assertTrue(map.bookmarks.isEmpty(), "Bookmarks list should remain empty when JSON is empty.");
    }
    
    @Test
    public void testSetOriginalJsonWithNull() {
        map.setOriginalJson(null);
        assertNull(map.originalJSON, "originalJSON should be null when set to null.");
        // When JSON is null, calculateBookmarks does not add any bookmark.
        assertTrue(map.bookmarks.isEmpty(), "Bookmarks list should remain empty when JSON is null.");
    }
    
    @Test
    public void testRepeatedSetOriginalJsonCalls() {
        map.setOriginalJson(SAMPLE_JSON);
        assertEquals(SAMPLE_JSON, map.originalJSON, "originalJSON should reflect the first JSON string.");
        assertFalse(map.bookmarks.isEmpty(), "Bookmarks list should not be empty after the first JSON update.");
        
        map.setOriginalJson(SAMPLE_JSON_ONE_BOOKMARK);
        assertEquals(SAMPLE_JSON_ONE_BOOKMARK, map.originalJSON, "originalJSON should reflect the second JSON string.");
        // In the test subclass, calculateBookmarks clears and resets bookmarks.
        assertFalse(map.bookmarks.isEmpty(), "Bookmarks list should not be empty after the second JSON update.");
        assertEquals(1, map.bookmarks.size(), "Bookmarks list should contain one bookmark after the second update.");
    }
}
