package DataManager.Database.DatabaseOperations;

import DataManager.Database.DatabaseEntities.TagEntity;
import DataManager.Parameters;

import javax.persistence.NoResultException;
import java.util.ArrayList;
import java.util.List;

import static DataManager.Parameters.entityManager;

public class TagEntityOperations extends TagEntity {
    public static TagEntity getTag(String TagName) {
        return (TagEntity) entityManager.createNamedQuery("TagEntity.findTag").setParameter("TagName", TagName).getSingleResult();
    }

    private static ArrayList<TagEntity> getAllTags() {
        try {
            return (ArrayList<TagEntity>) entityManager.createNamedQuery("TagEntity.findAllTags").getResultList();
        } catch (NoResultException e) {
            System.err.println("ERROR: Could not find a Tag");
            return new ArrayList<>();
        }
    }

    public static List<String> getAllTagNames() {
        if (!Parameters.useDatabase){
            List<String> l = new ArrayList<>();
            l.add("Accuracy");
            l.add("Balanced");
            l.add("Challenge");
            l.add("Dance");
            l.add("Fitness");
            l.add("Speed");
            l.add("Tech");

            return l;
        }
        return TagEntityOperations.getAllTags().stream().map(TagEntity::getName).toList();
    }

    public static TagEntity getTag(int fkTagId) {
        return (TagEntity) entityManager.createNamedQuery("TagEntity.findTagById").setParameter("id", fkTagId).getSingleResult();
    }
}
