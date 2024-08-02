package DataManager.Database.DatabaseOperations;

import DataManager.Database.DatabaseCommonMethods;
import DataManager.Database.DatabaseEntities.DifficultyEntity;
import DataManager.Database.DatabaseEntities.GenreAssignmentEntity;

import javax.persistence.NoResultException;

import java.util.ArrayList;
import java.util.List;

import static DataManager.Parameters.entityManager;
import static DataManager.Parameters.useDatabase;

public class DifficultyEntityOperations extends DifficultyEntity {
    public static DifficultyEntity getDifficulty(String difficultyName) {
        return (DifficultyEntity) entityManager.createNamedQuery("DifficultyEntity.findDifficulty").setParameter("difficultyName", difficultyName).getSingleResult();
    }

    public static List<String> getAllDifficulties() {
        if (!useDatabase) return getAllDifficultiesNames();
        else
            return DifficultyEntityOperations.getAllDifficultyEntities().stream().map(DifficultyEntity::getName).toList();
    }

    private static ArrayList<DifficultyEntity> getAllDifficultyEntities() {
        try {
            List<?> result = entityManager.createNamedQuery("DifficultyEntity.findAllDifficulties").getResultList();
            List<DifficultyEntity> difficultyEntities = new ArrayList<>();

            return DatabaseCommonMethods.checkCastFromQuery(result, DifficultyEntity.class);
        } catch (NoResultException e) {
            System.err.println("ERROR: Could not find a difficulty");
            return new ArrayList<>();
        }
    }


    public static List<String> getAllDifficultiesNames() {
            List<String> difficultiesNames = new ArrayList<>();
            difficultiesNames.add("Easy");
            difficultiesNames.add("Normal");
            difficultiesNames.add("Hard");
            difficultiesNames.add("Expert");
            difficultiesNames.add("ExpertPlus");

            int diffSize = difficultiesNames.size();
            for (int i = 0; i < diffSize; i++) {
                difficultiesNames.add(difficultiesNames.get(i) + "Standard");
                difficultiesNames.add("Standard" + difficultiesNames.get(i));
            }

            return difficultiesNames;
    }

    public static DifficultyEntity getDifficulty(int id) {
        return (DifficultyEntity) entityManager.createNamedQuery("DifficultyEntity.findDifficultyById").setParameter("id", id).getSingleResult();
    }

}
