package DataManager.Database.DatabaseOperations;

import DataManager.Database.DatabaseCommonMethods;
import DataManager.Database.DatabaseEntities.TagEntity;
import DataManager.Parameters;

import javax.persistence.NoResultException;
import java.util.ArrayList;
import java.util.List;

import static DataManager.Parameters.entityManager;
import static DataManager.Parameters.logger;

@Deprecated
public class TagEntityOperations extends TagEntity {
    public static TagEntity getTag(String TagName) {
        return (TagEntity) entityManager.createNamedQuery("TagEntity.findTag").setParameter("TagName", TagName).getSingleResult();
    }

    public static List<String> getAllTags() {
        if (!Parameters.useDatabase) return getAllTagNames();
        else
            return TagEntityOperations.getAllTagEntities().stream().map(TagEntity::getName).toList();
    }

    private static ArrayList<TagEntity> getAllTagEntities() {
        try {
            List<?> result = entityManager.createNamedQuery("TagEntity.findAllTags").getResultList();
            return DatabaseCommonMethods.checkCastFromQuery(result, TagEntity.class);
        } catch (NoResultException e) {
            logger.error("Could not find a Tag");
            System.err.println("ERROR: Could not find a Tag");
            return new ArrayList<>();
        }
    }

    public static List<String> getAllTagNames() {
            List<String> l = new ArrayList<>();
            l.add("Accuracy");
            l.add("Balanced");
            l.add("Challenge");
            l.add("Dance");
            l.add("Fitness");
            l.add("Speed");
            l.add("Tech");
            l.add("NULL");

            return l;
    }

    public static TagEntity getTag(int fkTagId) {
        return (TagEntity) entityManager.createNamedQuery("TagEntity.findTagById").setParameter("id", fkTagId).getSingleResult();
    }
}
