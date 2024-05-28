package DataManager.Database.DatabaseOperations;

import DataManager.Database.DatabaseEntities.PatternDescriptionEntity;
import DataManager.Database.DatabaseEntities.TagAssignmentEntity;
import DataManager.Records.PatMetadata;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.NoResultException;
import java.util.ArrayList;
import java.util.List;

import static DataManager.Parameters.entityManager;
import static DataManager.Parameters.verbose;

public class TagAssignmentEntityOperations extends TagAssignmentEntity {
    public static ArrayList<TagAssignmentEntity> getAssignmentEntity(int fkTagId, int fkPatternDescriptionId) {
        try {
            return (ArrayList<TagAssignmentEntity>) entityManager.createNamedQuery("TagAssignment.findTagAssignment")
                    .setParameter("fkTagId", fkTagId)
                    .setParameter("fkPatternDescriptionId", fkPatternDescriptionId)
                    .getResultList();
        } catch (NoResultException e) {
            System.err.println("[ERROR]: Could not find a difficulty");
            return new ArrayList<>();
        }
    }

    public static boolean deleteTagAssignmentEntity(PatMetadata metadata, PatternDescriptionEntity description, EntityManager entityManager) {
        EntityTransaction transaction = entityManager.getTransaction();

        try {
            for (String tag : metadata.tags()) {

                transaction.begin();
                ArrayList<TagAssignmentEntity> da = TagAssignmentEntityOperations.getAssignmentEntity(TagEntityOperations.getTag(tag).getId(), description.getId());
                da.forEach(entity -> {
                    TagAssignmentEntity toRemove = entityManager.find(TagAssignmentEntity.class, entity.getId());
                    if (toRemove != null) {
                        entityManager.remove(toRemove);
                        if (verbose) System.out.println("[INFO]: Successfully deleted TagAssignment: " + toRemove);
                    }
                });
                transaction.commit();

            }
        } catch (NoResultException e) {
            transaction.rollback();
            System.out.println("[INFO]: Nothing to delete... TagAssignments not found in database: " + metadata);
            return false;
        }

        return true;
    }

    public static List<String> getTagsForPattern(int id) {
        try {
            ArrayList<TagAssignmentEntity> list = (ArrayList<TagAssignmentEntity>) entityManager.createNamedQuery("TagAssignment.findTagAssignmentByFkPatternDescriptionId")
                    .setParameter("fkPatternDescriptionId", id)
                    .getResultList();
            ArrayList<String> tags = new ArrayList<>();
            list.forEach(entity -> tags.add(TagEntityOperations.getTag(entity.getFkTagId()).getName()));
            return tags;
        } catch (NoResultException e) {
            System.err.println("[ERROR]: Could not find a tag");
            return new ArrayList<>();
        }
    }
}
