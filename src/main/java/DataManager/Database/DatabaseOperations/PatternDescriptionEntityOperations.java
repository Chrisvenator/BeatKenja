package DataManager.Database.DatabaseOperations;

import DataManager.Database.DatabaseEntities.*;
import DataManager.Database.DatabaseSaveOperations;
import DataManager.Parameters;
import DataManager.Records.PatMetadata;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.NoResultException;
import java.util.List;

import static DataManager.Parameters.entityManager;
import static DataManager.Parameters.logger;

@Deprecated
public class PatternDescriptionEntityOperations extends PatternDescriptionEntity {

    /**
     * Retrieves a pattern description entity from the database based on the provided metadata.
     *
     * @param name       The name of the pattern description.
     * @param bpm        The BPM of the pattern description.
     * @param nps        The NPS of the pattern description.
     * @param difficulty The list of difficulties associated with the pattern description.
     * @param genres     The list of genres associated with the pattern description.
     * @param tags       The list of tags associated with the pattern description.
     * @return A {@link PatternDescriptionEntity} instance that matches the provided metadata.
     * @warning This method is not safe to use with metadata that may not exist in the database.
     * @warning bpm and genres are not used in the query, and are commented out.
     */
    public static PatternDescriptionEntity getPatternDescription(String name, double bpm, double nps, List<String> difficulty, List<String> genres, List<String> tags) {
        if (difficulty == null || difficulty.isEmpty()) difficulty = Parameters.DIFFICULTIES;
        if (genres == null || genres.isEmpty()) genres = Parameters.MUSIC_GENRES;
        if (tags == null || tags.isEmpty()) tags = Parameters.MAP_TAGS;

        return (PatternDescriptionEntity) entityManager.createNamedQuery("DifficultyEntity.findPatternDescription")
                .setParameter("name", name)
//                .setParameter("bpm", bpm)
                .setParameter("nps", nps)
                .setParameter("difficultyNames", difficulty)
//                .setParameter("genreNames", genres)
                .setParameter("tagNames", tags)
                .getSingleResult();
    }

    public static PatternDescriptionEntity getPatternDescriptionByNameAndNps(String name, double nps, EntityManager entityManager) {
        return (PatternDescriptionEntity) entityManager.createNamedQuery("DifficultyEntity.findPatternDescriptionByNameAndNps")
                .setParameter("name", name)
                .setParameter("nps", nps);
    }

    public static PatternDescriptionEntity getPatternDescription(PatMetadata metadata) {
        return getPatternDescription(metadata.name(), metadata.bpm(), metadata.nps(), metadata.difficulty(), metadata.genre(), metadata.tags());
    }

    public static PatternDescriptionEntity getPatternDescription(int id) {
        return (PatternDescriptionEntity) entityManager.createNamedQuery("DifficultyEntity.findPatternDescriptionById")
                .setParameter("id", id).getSingleResult();
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
            logger.info("Persisting {} to database", desc);
            DatabaseSaveOperations.persistEntity(desc);

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
        }


        return desc;
    }

    public static boolean deletePatternDescriptionEntity(PatMetadata metadata, PatternDescriptionEntity description, EntityManager entityManager) {
        EntityTransaction transaction = entityManager.getTransaction();

        try {
            transaction.begin();
            PatternDescriptionEntity entity = entityManager.find(PatternDescriptionEntity.class, description.getId());
            entityManager.remove(entity);
            transaction.commit();
            logger.info("Successfully deleted PatternDescription: {}", entity);
        } catch (NoResultException e) {
            transaction.rollback();
            e.printStackTrace();
            logger.warn("Nothing to delete... PatternDescription not found in database: {}", metadata);
            System.out.println("[INFO]: Nothing to delete... PatternDescription not found in database: " + metadata);
            return false;
        }

        return true;
    }

}
