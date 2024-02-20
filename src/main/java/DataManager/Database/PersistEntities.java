package DataManager.Database;

import BeatSaberObjects.Objects.Note;
import DataManager.Database.DatabaseEntities.DifficultyEntity;
import DataManager.Database.DatabaseEntities.GenreEntity;
import DataManager.Database.DatabaseEntities.NoteEntity;
import DataManager.Database.DatabaseEntities.TagEntity;
import DataManager.Database.DatabaseOperations.DifficultyEntityOperations;
import DataManager.Database.DatabaseOperations.GenreEntityOperations;
import DataManager.Database.DatabaseOperations.NoteEntityOperations;
import DataManager.Database.DatabaseOperations.TagEntityOperations;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.logging.Level;

/**
 * This class is an example class to show how to add entities to the database
 * This class is used to persist entities to the database.
 */
public class PersistEntities {
    public static void main(String[] args) {
        java.util.logging.Logger.getLogger("org.hibernate").setLevel(Level.WARNING);

        ArrayList<DifficultyEntity> diffs = DifficultyEntityOperations.getAllDifficulties();
        ArrayList<GenreEntity> genres = GenreEntityOperations.getAllGenres();
        ArrayList<TagEntity> tags = TagEntityOperations.getAllTags();


        diffs.forEach(e -> System.out.println(e.getName()));
        System.out.println();
        genres.forEach(e -> System.out.println(e.getName()));
        System.out.println();
        tags.forEach(e -> System.out.println(e.getName()));
        System.out.println();

        NoteEntity note = NoteEntityOperations.getNote(new Note(0,0,0,0,0));
        System.out.println(note);


    }

    public static boolean persistEntity(Object entity) {
        EntityManagerFactory entityManagerFactory = Persistence.createEntityManagerFactory("default");
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        EntityTransaction transaction = entityManager.getTransaction();

        try {
            transaction.begin();

            entityManager.persist(entity);

            transaction.commit();
        } catch (PersistenceException e) {
            return false;
        } finally {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            entityManager.close();
            entityManagerFactory.close();
        }
        return true;
    }

}
