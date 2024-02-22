package DataManager.Database.DatabaseOperations;

import DataManager.FileManager;
import DataManager.Parameters;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GenreEntityOperationsTest {

    @Test
    void getGenre() {
        List<String> genres = new java.util.ArrayList<>(FileManager.readFile("database/data/genre.csv").stream().map(s -> s.split(",")[1]).toList());
        genres.remove("name");
        for (String genre : genres) {
            assertNotNull(GenreEntityOperations.getGenre(genre));
        }

    }

    @Test
    void getAllGenres() {
        List<String> genres = new java.util.ArrayList<>(FileManager.readFile("database/data/genre.csv").stream().map(s -> s.split(",")[1]).toList());
        genres.remove("name");
        assertTrue(Parameters.MUSIC_GENRES.containsAll(genres));

    }
}