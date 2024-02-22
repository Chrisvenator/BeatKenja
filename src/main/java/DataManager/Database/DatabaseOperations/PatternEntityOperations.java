package DataManager.Database.DatabaseOperations;

import DataManager.Database.DatabaseEntities.PatternDescriptionEntity;
import DataManager.Database.DatabaseEntities.PatternEntity;
import DataManager.Database.DatabaseSaveOperations;

import javax.persistence.*;
import java.util.List;
import java.util.logging.Level;

import static DataManager.Parameters.entityManager;
import static DataManager.Parameters.verbose;

public class PatternEntityOperations extends PatternEntity {
    public static void main(String[] args) {
        java.util.logging.Logger.getLogger("org.hibernate").setLevel(Level.WARNING);

        PatternDescriptionEntity desc = PatternDescriptionEntityOperations.getPatternDescription("default", 120, 5, null, null, null);
        System.out.println(getPatternByDescription(desc));
    }

    public static List<PatternEntity> getPatternByDescription(PatternDescriptionEntity description) {

        return (List<PatternEntity>) entityManager.createNamedQuery("PatternEntity.findByPatternDescription")
                .setParameter("id", description.getId())
                .getResultList();
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

    public static boolean saveOrUpdatePattern(PatternEntity entity) {
        EntityManagerFactory entityManagerFactory = Persistence.createEntityManagerFactory("default");
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        EntityTransaction transaction = entityManager.getTransaction();

        try {
            transaction.begin();

            try {
                PatternEntity oldEntity = PatternEntityOperations.getPattern(entity.getPatternDescriptionId(), entity.getNoteId(), entity.getFollowedByNoteId());
                oldEntity.setCount(entity.getCount());
                oldEntity.setNoteId(entity.getNoteId());
                oldEntity.setPatternDescriptionId(entity.getPatternDescriptionId());
                oldEntity.setFollowedByNoteId(entity.getFollowedByNoteId());

                entityManager.merge(oldEntity);
                entityManager.flush();

            } catch (NoResultException e) {
                System.out.println("Count not find pattern to update, creating new pattern...");
                return DatabaseSaveOperations.persistEntity(entity);
            }


            transaction.commit();
        } catch (PersistenceException e) {
            if (verbose) e.printStackTrace();
            return false;
        } finally {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            entityManager.close();
            entityManagerFactory.close();
        }
        return true;
    }

}
