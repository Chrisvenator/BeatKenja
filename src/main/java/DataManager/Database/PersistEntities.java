package DataManager.Database;

import javax.persistence.*;

/**
 * This class is an example class to show how to add entities to the database
 * This class is used to persist entities to the database.
 */
public class PersistEntities {
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
