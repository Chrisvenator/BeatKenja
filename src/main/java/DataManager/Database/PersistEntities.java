package DataManager.Database;

import DataManager.Database.DatabaseEntities.*;
import DataManager.Parameters;

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


            GenreEntity genre = new GenreEntity();
            genre.setGenreName("Pop");
            entityManager.persist(genre);
            //https://www.youtube.com/watch?v=QJddHc41xrM


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
