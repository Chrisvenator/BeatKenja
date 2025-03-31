package BeatSaberObjects.Objects.BeatSaberMapTests;

import BeatSaberObjects.Objects.BeatSaberMap;
import DataManager.FileManager;
import com.google.gson.Gson;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mockStatic;

import org.junit.jupiter.api.DisplayName;

@DisplayName("BeatSaberMap Tests: New Map from Json")
public class NewMapFromJsonTest {
    private static final String EMPTY_FILE_PATH = "src/test/resources/BeatSaberMapTests/EmptyMap_ISeeFire.txt";
    private static final String FILE_PATH = "src/test/resources/BeatSaberMapTests/EmptyMap_ISeeFire.txt";
    
    @Test
    void shouldReturnEmptyMap_whenFileNotFound() {
            BeatSaberMap result = BeatSaberMap.newMapFromJSON("Filepath does not exist");
            
            assertThat(result).isNotNull();
            assertThat(result._notes).isEmpty();
            assertThat(result.bookmarks).isEmpty();
    }
    
    // Test case: File not found returns an empty map.
    @Test
    public void testFileNotFound() {
        // Provide a file path that does not exist.
        BeatSaberMap map = BeatSaberMap.newMapFromJSON("non_existent_file.json");
        assertNotNull(map, "Returned map should not be null");
        assertEquals(0, map._notes.length, "Map should be empty if file is not found.");
    }
    
    @Test
    void shouldReturnEmptyMap_whenMapIsEmpty() {
            BeatSaberMap result = BeatSaberMap.newMapFromJSON(EMPTY_FILE_PATH);
            
            assertThat(result).isNotNull();
            assertThat(result.getNotes()).isEmpty();
    }
    
    // Test case: Unsupported version "1" returns an empty map.
    @Test
    public void testUnsupportedVersion1(@TempDir Path tempDir) throws IOException {
        String jsonContent = "{ \"_version\": \"1\", \"_notes\": [ { \"time\": 1.0 } ] }";
        Path tempFile = tempDir.resolve("map_v1.json");
        Files.writeString(tempFile, jsonContent);
        BeatSaberMap map = BeatSaberMap.newMapFromJSON(tempFile.toString());
        assertEquals(0, map._notes.length, "Map should be empty for unsupported version 1.");
    }
    
    // Test case: Unsupported version "4" returns an empty map.
    @Test
    public void testUnsupportedVersion4(@TempDir Path tempDir) throws IOException {
        String jsonContent = "{ \"_version\": \"4\", \"_notes\": [ { \"time\": 2.0 } ] }";
        Path tempFile = tempDir.resolve("map_v4.json");
        Files.writeString(tempFile, jsonContent);
        BeatSaberMap map = BeatSaberMap.newMapFromJSON(tempFile.toString());
        assertEquals(0, map._notes.length, "Map should be empty for unsupported version 4.");
    }
    
    // Test case: Unknown version (e.g. "5") returns an empty map.
    @Test
    public void testUnknownVersion(@TempDir Path tempDir) throws IOException {
        String jsonContent = "{ \"_version\": \"5\", \"_notes\": [ { \"time\": 3.0 } ] }";
        Path tempFile = tempDir.resolve("map_v5.json");
        Files.writeString(tempFile, jsonContent);
        BeatSaberMap map = BeatSaberMap.newMapFromJSON(tempFile.toString());
        assertEquals(0, map._notes.length, "Map should be empty for an unknown version.");
    }
    
    // Test case: Valid version 2 JSON should parse entire JSON.
    @Test
    public void testVersion2(@TempDir Path tempDir) throws IOException {
        String jsonContent = "{\n" +
                "  \"_version\": \"2\",\n" +
                "  \"_notes\": [ { \"time\": 1.0 }, { \"time\": 2.0 } ]\n" +
                "}";
        Path tempFile = tempDir.resolve("map_v2.json");
        Files.writeString(tempFile, jsonContent);
        BeatSaberMap map = BeatSaberMap.newMapFromJSON(tempFile.toString());
        // Verify that _notes were parsed.
        assertEquals(2, map._notes.length, "Version 2 JSON should be parsed with 2 notes.");
        // Check that originalJSON is set.
        assertEquals(jsonContent.replaceAll("\n", ""), map.originalJSON, "originalJSON should match the file content.");
    }
    
    // Test case: Valid version 3 JSON should parse only color notes and sort them.
    @Test
    public void testVersion3(@TempDir Path tempDir) throws IOException {
        // Provide unsorted color notes.
        String jsonContent = "{\n" +
                "  \"_version\": \"3\",\n" +
                "  \"colorNotes\": [ " +
                "{\"b\":5.0,\"x\":1,\"y\":0,\"c\":0,\"d\":1}," +
                "{\"b\":3.0,\"x\":1,\"y\":0,\"c\":0,\"d\":1}," +
                "{\"b\":1.0,\"x\":1,\"y\":0,\"c\":0,\"d\":1}" +
                " ]\n" +
                "}";
        Path tempFile = tempDir.resolve("map_v3.json");
        Files.writeString(tempFile, jsonContent);
        BeatSaberMap map = BeatSaberMap.newMapFromJSON(tempFile.toString());
        // The notes should be sorted by time.
        assertEquals(3, map._notes.length, "Version 3 JSON should have 3 color notes.");
        assertEquals(1.0, map._notes[0]._time, 0.0001, "First note should have time 1.0 after sorting.");
        assertEquals(3.0, map._notes[1]._time, 0.0001, "Second note should have time 3.0 after sorting.");
        assertEquals(5.0, map._notes[2]._time, 0.0001, "Third note should have time 5.0 after sorting.");
        // Verify originalJSON.
        assertEquals(jsonContent.replaceAll("\n", ""), map.originalJSON, "originalJSON should match the file content.");
    }
    
    // Test case: Invalid JSON content returns an empty map.
    @Test
    public void testParsingError(@TempDir Path tempDir) throws IOException {
        String invalidJson = "This is not JSON!";
        Path tempFile = tempDir.resolve("invalid.json");
        Files.writeString(tempFile, invalidJson);
        BeatSaberMap map = BeatSaberMap.newMapFromJSON(tempFile.toString());
        assertEquals(0, map._notes.length, "Invalid JSON should result in an empty map.");
    }
}
