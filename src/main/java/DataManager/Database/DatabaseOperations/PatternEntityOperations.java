package DataManager.Database.DatabaseOperations;

import DataManager.Database.DatabaseCommonMethods;
import DataManager.Database.DatabaseEntities.PatternDescriptionEntity;
import DataManager.Database.DatabaseEntities.PatternEntity;
import DataManager.Database.DatabaseSaveOperations;

import javax.persistence.*;
import java.util.List;
import java.util.logging.Level;

import static DataManager.Parameters.entityManager;
import static DataManager.Parameters.logger;
import static DataManager.Parameters.verbose;

/**
 * This class provides operations to handle Pattern entities in the database.
 */@Deprecated
public class PatternEntityOperations extends PatternEntity {
    public static void main(String[] args) {
        java.util.logging.Logger.getLogger("org.hibernate").setLevel(Level.WARNING);

        PatternDescriptionEntity desc = PatternDescriptionEntityOperations.getPatternDescription("default", 120, 5, null, null, null);
        logger.info(getPatternByDescription(desc));
        System.out.println(getPatternByDescription(desc));
    }

    /**
     * Retrieves a list of PatternEntity by their description.
     *
     * @param description The pattern description entity to query by.
     * @return A list of matching PatternEntity objects.
     */
    public static List<PatternEntity> getPatternByDescription(PatternDescriptionEntity description) {
        List<?> result = entityManager.createNamedQuery("PatternEntity.findByPatternDescriptionId")
                .setParameter("patternDescriptionId", description.getId())
                .getResultList();

        return DatabaseCommonMethods.checkCastFromQuery(result, PatternEntity.class);
    }

    public static PatternEntity getPatternById(int id) {
        return (PatternEntity) entityManager.createNamedQuery("PatternEntity.findById")
                .setParameter("id", id)
                .getSingleResult();
    }

    public static PatternEntity getPattern(int patternDescriptionId, int noteId, int followedByNoteId) {
        return (PatternEntity) entityManager.createNamedQuery("PatternEntity.find")
                .setParameter("patternDescriptionId", patternDescriptionId)
                .setParameter("noteId", noteId)
                .setParameter("followedByNoteId", followedByNoteId)
                .getSingleResult();
    }

    /**
     * Saves or updates a PatternEntity in the database.
     *
     * @param entity The PatternEntity to save or update.
     * @return true if the operation was successful, false otherwise.
     */
    public static boolean saveOrUpdatePattern(PatternEntity entity, EntityManager entityManager) {
        EntityTransaction transaction = entityManager.getTransaction();

        try {
            try {

                PatternEntity oldEntity = PatternEntityOperations.getPattern(entity.getPatternDescriptionId(), entity.getNoteId(), entity.getFollowedByNoteId());
                oldEntity.setCount(entity.getCount());
                oldEntity.setNoteId(entity.getNoteId());
                oldEntity.setPatternDescriptionId(entity.getPatternDescriptionId());
                oldEntity.setFollowedByNoteId(entity.getFollowedByNoteId());

                transaction.begin();

                entityManager.merge(oldEntity);

                entityManager.flush();
                transaction.commit();
            } catch (NoResultException e) {
                logger.info("Count not find pattern to update, creating new pattern...");
                return DatabaseSaveOperations.persistEntity(entity);
            }

        } catch (PersistenceException e) {
            logger.error(e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            if (transaction.isActive()) {
                transaction.rollback();
            }
        }
        return true;
    }

    /**
     * Deletes a PatternEntity from the database.
     *
     * @param entity The PatternEntity to delete.
     * @return true if the operation was successful, false otherwise.
     */
    public static boolean deletePattern(PatternEntity entity, EntityManager entityManager) {
        EntityTransaction transaction = entityManager.getTransaction();

        try {
            transaction.begin();

            // Retrieve the entity to ensure it exists
            PatternEntity toDelete = entityManager.find(PatternEntity.class, entity.getId());
            if (toDelete == null) {
                logger.warn("Pattern not found, cannot delete.");
                System.out.println("Pattern not found, cannot delete.");
                return false;
            }

            // Delete the entity
            entityManager.remove(toDelete);
            entityManager.flush();

            transaction.commit();
        } catch (PersistenceException e) {
            if (verbose) e.printStackTrace();
            return false;
        } finally {
            if (transaction.isActive()) {
                transaction.rollback();
            }
        }
        return true;
    }


}
