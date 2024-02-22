package DataManager.Database;

import DataManager.Parameters;

import javax.persistence.*;

/**
 * This class is an example class to show how to add entities to the database
 * This class is used to persist entities to the database.
 */
public class DatabaseSaveOperations {
    public static boolean persistEntity(Object entity) {
        EntityManagerFactory entityManagerFactory = Persistence.createEntityManagerFactory("default");
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        EntityTransaction transaction = entityManager.getTransaction();

        try {
            transaction.begin();

            entityManager.persist(entity);
            entityManager.flush();

            transaction.commit();
            if (Parameters.verbose) System.out.println("Persisted " + entity + " to database");
        } catch (PersistenceException e) {
            System.err.println("Failed to persist " + entity + " to database");
            if (Parameters.verbose) e.printStackTrace();
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
