package DataManager.Database.DatabaseOperations;

import DataManager.FileManager;
import DataManager.Parameters;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

class DifficultyEntityOperationsTest {

    @Test
    void getDifficulty() {
        List<String> difficulties = new java.util.ArrayList<>(FileManager.readFile("database/data/difficulty.csv").stream().map(s -> s.split(",")[1]).toList());
        difficulties.remove("name");
        for (String difficulty : difficulties) {
            assertNotNull(DifficultyEntityOperations.getDifficulty(difficulty));
        }
    }

    @Test
    void getAllDifficulties() {
        List<String> difficulties = new java.util.ArrayList<>(FileManager.readFile("database/data/difficulty.csv").stream().map(s -> s.split(",")[1]).toList());
        difficulties.remove("name");
        assertTrue(Parameters.DIFFICULTIES.containsAll(difficulties));
    }
}