package DataManager.Database.DatabaseOperations;

import DataManager.Database.DatabaseEntities.DifficultyEntity;

import javax.persistence.NoResultException;

import java.util.ArrayList;
import java.util.List;

import static DataManager.Parameters.entityManager;
import static DataManager.Parameters.useDatabase;

public class DifficultyEntityOperations extends DifficultyEntity {
    public static DifficultyEntity getDifficulty(String difficultyName) {
        return (DifficultyEntity) entityManager.createNamedQuery("DifficultyEntity.findDifficulty").setParameter("difficultyName", difficultyName).getSingleResult();
    }

    private static ArrayList<DifficultyEntity> getAllDifficulties() {
        try {
            return (ArrayList<DifficultyEntity>) entityManager.createNamedQuery("DifficultyEntity.findAllDifficulties").getResultList();
        } catch (NoResultException e) {
            System.err.println("ERROR: Could not find a difficulty");
            return new ArrayList<>();
        }
    }

    public static List<String> getAllDifficultiesNames() {
        if (!useDatabase){
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

        return DifficultyEntityOperations.getAllDifficulties().stream().map(DifficultyEntity::getName).toList();
    }

    public static DifficultyEntity getDifficulty(int id) {
        return (DifficultyEntity) entityManager.createNamedQuery("DifficultyEntity.findDifficultyById").setParameter("id", id).getSingleResult();
    }

}
