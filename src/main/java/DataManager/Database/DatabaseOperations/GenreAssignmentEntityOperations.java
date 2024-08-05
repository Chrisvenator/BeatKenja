package DataManager.Database.DatabaseOperations;

import DataManager.Database.DatabaseCommonMethods;
import DataManager.Database.DatabaseEntities.GenreAssignmentEntity;
import DataManager.Database.DatabaseEntities.PatternDescriptionEntity;
import DataManager.Records.PatMetadata;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.NoResultException;
import java.util.ArrayList;
import java.util.List;

import static DataManager.Parameters.entityManager;
import static DataManager.Parameters.logger;

public class GenreAssignmentEntityOperations extends GenreEntityOperations {
    public static ArrayList<GenreAssignmentEntity> getAssignmentEntity(int fkGenreId, int fkPatternDescriptionId) {
        try {
            List<?> result = entityManager.createNamedQuery("GenreAssignment.findGenreAssignment")
                    .setParameter("fkGenreId", fkGenreId)
                    .setParameter("fkPatternDescriptionId", fkPatternDescriptionId)
                    .getResultList();

            return DatabaseCommonMethods.checkCastFromQuery(result, GenreAssignmentEntity.class);
        } catch (NoResultException e) {
            logger.error("Could not find a genre");
            System.err.println("[ERROR]: Could not find a genre");
            return new ArrayList<>();
        }
    }

    public static boolean deleteGenreAssignmentEntity(PatMetadata metadata, PatternDescriptionEntity description, EntityManager entityManager) {
        EntityTransaction transaction = entityManager.getTransaction();

        try {
            for (String genre : metadata.genre()) {

                transaction.begin();
                ArrayList<GenreAssignmentEntity> da = GenreAssignmentEntityOperations.getAssignmentEntity(GenreEntityOperations.getGenre(genre).getId(), description.getId());
                da.forEach(entity -> {
                    GenreAssignmentEntity toRemove = entityManager.find(GenreAssignmentEntity.class, entity.getId());
                    if (toRemove != null) {
                        entityManager.remove(toRemove);
                        logger.info("Successfully deleted GenreAssignment: {}", toRemove);
                    }
                });
                transaction.commit();

            }
        } catch (NoResultException e) {
            transaction.rollback();
            logger.info("Nothing to delete... GenreAssignments not found in database: {}", metadata);
            System.out.println("[INFO]: Nothing to delete... GenreAssignments not found in database: " + metadata);
            return false;
        }


        return true;
    }

    public static List<String> getGenresForPatternID(int id) {
        try {
            List<?> result = entityManager.createNamedQuery("GenreAssignment.findGenreAssignmentByFkPatternDescriptionId")
                    .setParameter("fkPatternDescriptionId", id)
                    .getResultList();

            ArrayList<GenreAssignmentEntity> genreAssignmentEntities = DatabaseCommonMethods.checkCastFromQuery(result, GenreAssignmentEntity.class);
            ArrayList<String> genres = new ArrayList<>();
            genreAssignmentEntities.forEach(entity -> genres.add(GenreEntityOperations.getGenre(entity.getFkGenreId()).getName()));
            return genres;
        } catch (NoResultException e) {
            logger.error("[ERROR]: Could not find a genre");
            System.err.println("[ERROR]: Could not find a genre");
            return new ArrayList<>();
        }
    }
}
