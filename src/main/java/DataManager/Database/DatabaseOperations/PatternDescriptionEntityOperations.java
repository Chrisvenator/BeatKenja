package DataManager.Database.DatabaseOperations;

import DataManager.Database.DatabaseEntities.*;
import DataManager.Database.DatabaseSaveOperations;
import DataManager.Parameters;
import DataManager.Records.PatMetadata;

import javax.persistence.NoResultException;
import java.util.List;

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

    /**
     * Saves a pattern description entity based on the provided metadata.
     * If the pattern description already exists in the database, it retrieves and returns it.
     * Otherwise, it creates a new pattern description entity, along with its associated difficulty,
     * genre, and tag assignments, and persists them to the database.
     *
     * @param metadata The PatMetadata Record containing information about the pattern description,
     *                 including name, BPM, NPS, difficulties, genres, and tags.
     * @return A {@link PatternDescriptionEntity} instance that has either been retrieved from the
     * database or newly created and persisted.
     */
    public static PatternDescriptionEntity savePatternDescription(PatMetadata metadata) {
        PatternDescriptionEntity desc;
        try {
            // Attempt to retrieve an existing pattern description from the database
            desc = PatternDescriptionEntityOperations.getPatternDescription(metadata);
        } catch (NoResultException e) {

            // If no existing pattern description is found, create a new one
            desc = new PatternDescriptionEntity();
            desc.setName(metadata.name());
            desc.setBpm(metadata.bpm());
            desc.setNps(metadata.nps());
            if (Parameters.verbose) System.out.println("Persisting " + desc + " to database");
            DatabaseSaveOperations.persistEntity(desc);

            entityManager.getTransaction().begin();

            // Persist difficulty assignments for the pattern description
            for (String diff : metadata.difficulty()) {
                DifficultyAssignmentEntity da = new DifficultyAssignmentEntity();
                da.setFkDifficultyId(DifficultyEntityOperations.getDifficulty(diff).getId());
                da.setFkPatternDescriptionId(desc.getId());
                DatabaseSaveOperations.persistEntity(da);
            }

            // Persist genre assignments for the pattern description
            for (String genre : metadata.genre()) {
                GenreAssignmentEntity ga = new GenreAssignmentEntity();
                ga.setFkGenreId(GenreEntityOperations.getGenre(genre).getId());
                ga.setFkPatternDescriptionId(desc.getId());
                DatabaseSaveOperations.persistEntity(ga);
            }

            // Persist tag assignments for the pattern description
            for (String tag : metadata.tags()) {
                TagAssignmentEntity ta = new TagAssignmentEntity();
                ta.setFkTagId(TagEntityOperations.getTag(tag).getId());
                ta.setFkPatternDescriptionId(desc.getId());
                DatabaseSaveOperations.persistEntity(ta);
            }

            entityManager.getTransaction().commit();
        }


        return desc;
    }
}
