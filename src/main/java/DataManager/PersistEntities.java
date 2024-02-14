package DataManager;

import DataManager.Database.DatabaseEntities.*;

import javax.persistence.*;

public class PersistEntities {
    public static void main(String[] args) {
        persistEntities();
    }

    public static void persistEntities() {
        EntityManagerFactory entityManagerFactory = Persistence.createEntityManagerFactory("default");
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        EntityTransaction transaction = entityManager.getTransaction();

        try {
            transaction.begin();

            NoteEntity note = new NoteEntity();
            note.setColor(1);
            note.setCutDirection(1);
            note.setLineLayer(3);
            note.setLineIndex(1);
//            entityManager.persist(note);

            DifficultyEntity difficulty = new DifficultyEntity();
            difficulty.setDifficultyName("Easy");
//            entityManager.persist(difficulty);

            GenreEntity genre = new GenreEntity();
            genre.setGenreName("Pop");
//            entityManager.persist(genre);

            TagEntity tag = new TagEntity();
            tag.setTagName("Speed");
//            entityManager.persist(tag);

            NoteProbabilitiesEntity noteProbabilities = new NoteProbabilitiesEntity();
            noteProbabilities.setBpm(120);
            noteProbabilities.setNps(5);
            noteProbabilities.setCount(1);
            noteProbabilities.setDifficultyFkId(3);
            noteProbabilities.setGenreFkId(2);
            noteProbabilities.setNoteFkId(12);
            noteProbabilities.setFollowedByNoteFkId(14);
            noteProbabilities.setTagsFkId(2);
            entityManager.persist(noteProbabilities);


            transaction.commit();
        } finally {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            entityManager.close();
            entityManagerFactory.close();
        }
    }
}
