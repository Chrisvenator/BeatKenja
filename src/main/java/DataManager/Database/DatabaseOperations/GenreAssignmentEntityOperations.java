package DataManager.Database.DatabaseOperations;


import DataManager.Database.DatabaseEntities.GenreAssignmentEntity;
import DataManager.Database.DatabaseEntities.PatternDescriptionEntity;
import DataManager.Records.PatMetadata;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.NoResultException;
import java.util.ArrayList;
import java.util.List;

import static DataManager.Parameters.entityManager;
import static DataManager.Parameters.verbose;

public class GenreAssignmentEntityOperations extends GenreEntityOperations {
    public static ArrayList<GenreAssignmentEntity> getAssignmentEntity(int fkGenreId, int fkPatternDescriptionId) {
        try {
            return (ArrayList<GenreAssignmentEntity>) entityManager.createNamedQuery("GenreAssignment.findGenreAssignment")
                    .setParameter("fkGenreId", fkGenreId)
                    .setParameter("fkPatternDescriptionId", fkPatternDescriptionId)
                    .getResultList();
        } catch (NoResultException e) {
            System.err.println("[ERROR]: Could not find a difficulty");
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
                        if (verbose) System.out.println("[INFO]: Successfully deleted GenreAssignment: " + toRemove);
                    }
                });
                transaction.commit();

            }
        } catch (NoResultException e) {
            transaction.rollback();
            System.out.println("[INFO]: Nothing to delete... GenreAssignments not found in database: " + metadata);
            return false;
        }


        return true;
    }

    public static List<String> getGenresForPatternID(int id) {
        try {
            ArrayList<GenreAssignmentEntity> list = (ArrayList<GenreAssignmentEntity>) entityManager.createNamedQuery("GenreAssignment.findGenreAssignmentByFkPatternDescriptionId")
                    .setParameter("fkPatternDescriptionId", id)
                    .getResultList();
            ArrayList<String> genres = new ArrayList<>();
            list.forEach(entity -> genres.add(GenreEntityOperations.getGenre(entity.getFkGenreId()).getName()));
            return genres;
        } catch (NoResultException e) {
            System.err.println("[ERROR]: Could not find a genre");
            return new ArrayList<>();
        }
    }
}
