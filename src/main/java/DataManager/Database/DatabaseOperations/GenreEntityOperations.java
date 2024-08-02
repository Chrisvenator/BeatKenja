package DataManager.Database.DatabaseOperations;

import DataManager.Database.DatabaseCommonMethods;
import DataManager.Database.DatabaseEntities.GenreAssignmentEntity;
import DataManager.Database.DatabaseEntities.GenreEntity;
import DataManager.Parameters;

import javax.persistence.NoResultException;
import java.util.ArrayList;
import java.util.List;

import static DataManager.Parameters.entityManager;

public class GenreEntityOperations extends GenreEntity {
    public static GenreEntity getGenre(String GenreName) {
        return (GenreEntity) entityManager.createNamedQuery("GenreEntity.findGenre").setParameter("GenreName", GenreName).getSingleResult();
    }

    public static List<String> getAllGenres() {
        if (!Parameters.useDatabase)  return getAllGenreNames();
        else
            return GenreEntityOperations.getAllGenreEntities().stream().map(GenreEntity::getName).toList();
    }

    private static ArrayList<GenreEntity> getAllGenreEntities() {
        try {
            List<?> result = entityManager.createNamedQuery("GenreEntity.findAllGenres").getResultList();
            return DatabaseCommonMethods.checkCastFromQuery(result, GenreEntity.class);
        } catch (NoResultException e) {
            System.err.println("ERROR: Could not find a Genre");
            return new ArrayList<>();
        }
    }


    public static List<String> getAllGenreNames() {
            List<String> genreNames = new ArrayList<>();
            genreNames.add("Alternative");
            genreNames.add("Ambient");
            genreNames.add("Anime");
            genreNames.add("Classical & Orchestral");
            genreNames.add("Comedy & Meme");
            genreNames.add("Dance");
            genreNames.add("Drum and Bass");
            genreNames.add("Dubstep");
            genreNames.add("Electronic");
            genreNames.add("Folk & Acoustic");
            genreNames.add("Funk & Disco");
            genreNames.add("Hardcore");
            genreNames.add("Hip Hop & Rap");
            genreNames.add("Holiday");
            genreNames.add("House");
            genreNames.add("Indie");
            genreNames.add("Instrumental");
            genreNames.add("J-Pop");
            genreNames.add("J-Rock");
            genreNames.add("Jazz");
            genreNames.add("K-Pop");
            genreNames.add("Kids & Family");
            genreNames.add("Metal");
            genreNames.add("Nightcore");
            genreNames.add("Pop");
            genreNames.add("Punk");
            genreNames.add("R&B");
            genreNames.add("Rock");
            genreNames.add("Soul");
            genreNames.add("Speedcore");
            genreNames.add("Swing");
            genreNames.add("TV & Film");
            genreNames.add("Techno");
            genreNames.add("Trance");
            genreNames.add("Video Game");
            genreNames.add("Vocaloid");
            genreNames.add("NULL");

            return genreNames;
    }

    protected static GenreEntity getGenre(int fkGenreId) {
        return (GenreEntity) entityManager.createNamedQuery("GenreEntity.findGenreById").setParameter("id", fkGenreId).getSingleResult();
    }
}
