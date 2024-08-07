import DataManager.FileManager;
import org.junit.jupiter.api.Test;

import java.util.NoSuchElementException;
import java.util.logging.LogManager;

import static org.junit.jupiter.api.Assertions.*;

class FileManagerTest {

    @Test
    void readFile() {
        String s = FileManager.readFile("src/test/resources/MinimalMapExample.txt").get(0);
        assertEquals("{\"_version\":\"2.2.0\",\"_notes\":[{\"_time\":0,\"_lineIndex\":0,\"_lineLayer\":0,\"_type\":1,\"_cutDirection\":8},{\"_time\":1,\"_lineIndex\":0,\"_lineLayer\":0,\"_type\":3,\"_cutDirection\":1},{\"_time\":2,\"_lineIndex\":0,\"_lineLayer\":0,\"_type\":1,\"_cutDirection\":8},{\"_time\":3,\"_lineIndex\":0,\"_lineLayer\":0,\"_type\":1,\"_cutDirection\":8},{\"_time\":4,\"_lineIndex\":0,\"_lineLayer\":0,\"_type\":1,\"_cutDirection\":8}],\"_obstacles\":[{\"_time\":0,\"_lineIndex\":3,\"_type\":0,\"_duration\":0.043,\"_width\":1},{\"_time\":0.5,\"_lineIndex\":3,\"_type\":0,\"_duration\":1.5,\"_width\":1}],\"_events\":[{\"_time\":0,\"_type\":3,\"_value\":2},{\"_time\":3,\"_type\":2,\"_value\":2},{\"_time\":3,\"_type\":12,\"_value\":0}],\"_waypoints\":[]}",
                s);
        assertThrowsExactly(NoSuchElementException.class, () -> FileManager.readFile("src/test/resources/NonExistentFile.txt"));
    }

    @Test
    void overwriteFile() {
        String s = FileManager.readFile("src/test/resources/MinimalMapExample.txt").get(0);
        FileManager.overwriteFile("src/test/resources/MinimalMapExample.txt", s, true);
        assertEquals(s, FileManager.readFile("src/test/resources/MinimalMapExample.txt").get(0));

        FileManager.overwriteFile("src/test/resources/MinimalMapExample.txt", "a", true);
        assertEquals("a", FileManager.readFile("src/test/resources/MinimalMapExample.txt").get(0));

        FileManager.overwriteFile("src/test/resources/MinimalMapExample.txt", s, true);
        assertEquals(s, FileManager.readFile("src/test/resources/MinimalMapExample.txt").get(0));

        LogManager.getLogManager().reset();
    }
}