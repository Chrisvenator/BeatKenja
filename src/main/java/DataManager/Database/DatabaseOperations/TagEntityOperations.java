package DataManager.Database.DatabaseOperations;

import DataManager.Database.DatabaseEntities.TagEntity;

import javax.persistence.NoResultException;
import java.util.ArrayList;

import static DataManager.Parameters.entityManager;

public class TagEntityOperations extends TagEntity {
    public static TagEntity getTag(String TagName) {
        return (TagEntity) entityManager.createNamedQuery("TagEntity.findTag").setParameter("TagName", TagName).getSingleResult();
    }

    public static ArrayList<TagEntity> getAllTags() {
        try {
            return (ArrayList<TagEntity>) entityManager.createNamedQuery("TagEntity.findAllTags").getResultList();
        } catch (NoResultException e) {
            System.err.println("ERROR: Could not find a Tag");
            return new ArrayList<>();
        }
    }

}
