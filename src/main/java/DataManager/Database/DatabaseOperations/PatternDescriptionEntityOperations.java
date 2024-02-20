package DataManager.Database.DatabaseOperations;

import DataManager.Database.DatabaseEntities.*;
import DataManager.Parameters;
import DataManager.Records.PatMetadata;

import java.util.List;
import java.util.Objects;
import java.util.logging.Level;

import static DataManager.Parameters.entityManager;

public class PatternDescriptionEntityOperations extends PatternDescriptionEntity {

    public static PatternDescriptionEntity getPatternDescription(String name, double bpm, double nps, List<String> difficulty, List<String> genres, List<String> tags) {
        if (difficulty == null || difficulty.isEmpty()) difficulty = Parameters.DIFFICULTIES;
        if (genres == null || genres.isEmpty()) genres = Parameters.MUSIC_GENRES;
        if (tags == null || tags.isEmpty()) tags = Parameters.MAP_TAGS;

        return (PatternDescriptionEntity) entityManager.createNamedQuery("DifficultyEntity.findPatternDescription")
                .setParameter("name", name)
                .setParameter("bpm", bpm)
                .setParameter("nps", nps)
                .setParameter("difficultyNames", difficulty)
                .setParameter("genreNames", genres)
                .setParameter("tagNames", tags)
                .getSingleResult();
    }

    public static PatternDescriptionEntity getPatternDescription(PatMetadata metadata) {
        return getPatternDescription(metadata.name(), metadata.bpm(), metadata.nps(), metadata.difficulty(), metadata.genre(), metadata.tags());
    }
}
