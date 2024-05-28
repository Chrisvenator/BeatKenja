package DataManager.Database.DatabaseOperations;

import DataManager.Database.DatabaseEntities.DifficultyAssignmentEntity;
import DataManager.Database.DatabaseEntities.PatternDescriptionEntity;
import DataManager.Records.PatMetadata;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.NoResultException;
import java.util.ArrayList;

import static DataManager.Parameters.entityManager;

public class DifficultyAssignmentEntityOperations extends DifficultyAssignmentEntity {
    public static ArrayList<DifficultyAssignmentEntity> getAssignmentEntity(int fkDifficultyId, int fkPatternDescriptionId) {
        try {
            return (ArrayList<DifficultyAssignmentEntity>) entityManager.createNamedQuery("DifficultyAssignment.findDifficultyAssignment")
                    .setParameter("fkDifficultyId", fkDifficultyId)
                    .setParameter("fkPatternDescriptionId", fkPatternDescriptionId)
                    .getResultList();
        } catch (NoResultException e) {
            System.err.println("[ERROR]: Could not find a difficulty");
            return new ArrayList<>();
        }
    }

    public static boolean deleteAssignmentEntity(PatMetadata metadata, PatternDescriptionEntity description, EntityManager entityManager) {
        EntityTransaction transaction = entityManager.getTransaction();

        try {
            for (String diff : metadata.difficulty()) {

                transaction.begin();
                ArrayList<DifficultyAssignmentEntity> da = DifficultyAssignmentEntityOperations.getAssignmentEntity(DifficultyEntityOperations.getDifficulty(diff).getId(), description.getId());
                da.forEach(entity -> {
                    DifficultyAssignmentEntity toRemove = entityManager.find(DifficultyAssignmentEntity.class, entity.getId());
                    if (toRemove != null) {
                        entityManager.remove(toRemove);
                        System.out.println("[INFO]: Successfully deleted DifficultyAssignment: " + toRemove);
                    }
                });
                transaction.commit();

            }
        } catch (NoResultException e) {
            transaction.rollback();
            System.out.println("[INFO]: Nothing to delete... DifficultyAssignments not found in database: " + metadata);
            return false;
        }

        return true;
    }
}
