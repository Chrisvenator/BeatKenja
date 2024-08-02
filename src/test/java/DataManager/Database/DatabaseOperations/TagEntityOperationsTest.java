package DataManager.Database.DatabaseOperations;

import DataManager.FileManager;
import DataManager.Parameters;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TagEntityOperationsTest {

    @Test
    void getTag() {
        List<String> tags = new java.util.ArrayList<>(FileManager.readFile("database/data/tag.csv").stream().map(s -> s.split(",")[1]).toList());
        tags.remove("name");
        for (String tag : tags) {
            assertNotNull(TagEntityOperations.getTag(tag));
        }
    }

    @Test
    void getAllTagEntities() {
        List<String> tags = new java.util.ArrayList<>(FileManager.readFile("database/data/tag.csv").stream().map(s -> s.split(",")[1]).toList());
        tags.remove("name");
        assertTrue(Parameters.MAP_TAGS.containsAll(tags));
    }
}