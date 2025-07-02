package DataManager.Database;

import DataManager.Parameters;

import javax.persistence.*;

import static DataManager.Parameters.logger;

/**
 * This class is an example class to show how to add entities to the database
 * This class is used to persist entities to the database.
 */
@Deprecated
public class DatabaseSaveOperations {
    public static boolean persistEntity(Object entity) {
        EntityManager entityManager = Parameters.entityManager;
        EntityTransaction transaction = entityManager.getTransaction();

        try {
            transaction.begin();

            entityManager.persist(entity);
            entityManager.flush();

            transaction.commit();
            logger.debug("Persisted {} to database", entity);
        } catch (PersistenceException e) {
            logger.error("Failed to persist {} to database", entity);
            System.err.println("Failed to persist " + entity + " to database");
            if (Parameters.verbose) e.printStackTrace();
            return false;
        } finally {
            if (transaction.isActive()) {
                transaction.rollback();
            }
        }
        return true;
    }

}
