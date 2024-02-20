package DataManager.Database.DatabaseOperations;

import DataManager.Database.DatabaseEntities.GenreEntity;

import javax.persistence.NoResultException;
import java.util.ArrayList;

import static DataManager.Parameters.entityManager;

public class GenreEntityOperations extends GenreEntity {
    public static GenreEntity getGenre(String GenreName) {
        return (GenreEntity) entityManager.createNamedQuery("GenreEntity.findGenre").setParameter("GenreName", GenreName).getSingleResult();
    }

    public static ArrayList<GenreEntity> getAllGenres() {
        try {
            return (ArrayList<GenreEntity>) entityManager.createNamedQuery("GenreEntity.findAllGenres").getResultList();
        } catch (NoResultException e) {
            System.err.println("ERROR: Could not find a Genre");
            return new ArrayList<>();
        }
    }

}
