package DataManager.Database.DatabaseOperations;

import DataManager.Database.DatabaseEntities.PatternDescriptionEntity;
import DataManager.Database.DatabaseEntities.PatternEntity;

import java.util.List;
import java.util.logging.Level;

import static DataManager.Parameters.entityManager;

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
}
