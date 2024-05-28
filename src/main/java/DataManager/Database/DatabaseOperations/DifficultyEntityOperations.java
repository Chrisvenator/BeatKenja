package DataManager.Database.DatabaseOperations;

import DataManager.Database.DatabaseEntities.DifficultyEntity;
import javax.persistence.NoResultException;

import java.util.ArrayList;

import static DataManager.Parameters.entityManager;

public class DifficultyEntityOperations extends DifficultyEntity {
    public static DifficultyEntity getDifficulty(String difficultyName) {
        return (DifficultyEntity) entityManager.createNamedQuery("DifficultyEntity.findDifficulty").setParameter("difficultyName", difficultyName).getSingleResult();
    }

    public static ArrayList<DifficultyEntity> getAllDifficulties() {
        try {
            return (ArrayList<DifficultyEntity>) entityManager.createNamedQuery("DifficultyEntity.findAllDifficulties").getResultList();
        } catch (NoResultException e) {
            System.err.println("ERROR: Could not find a difficulty");
            return new ArrayList<>();
        }
    }
    }
