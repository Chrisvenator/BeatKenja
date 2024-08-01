package DataManager.Database;

import DataManager.Database.DatabaseEntities.*;
import DataManager.Parameters;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.tool.hbm2ddl.SchemaExport;
import org.hibernate.tool.schema.TargetType;

import java.util.EnumSet;
import java.util.List;

import static DataManager.Parameters.logger;

/**
 * Provides functionality to export the database schema and data into separate files.
 * This class supports exporting entity data for various entities like Genre, Difficulty, Tag, Note, etc.,
 * into CSV format and the database schema into SQL format. The exported files are intended for backup,
 * migration, or analysis purposes.<br>
 *
 * @warning If the database schema is updated, the schema export method should be updated to reflect the changes!!
 */
@SuppressWarnings("unchecked")
public class DatabaseExport {
    /**
     * Initiates the export process for both the schema and data of the database.
     * This method calls other private methods to export data for each entity into CSV format
     * and the database schema into SQL format.
     *
     * @param path The base directory path where the exported files will be saved. Each data entity will be saved
     *             in a subdirectory named "data" with respective CSV files, and the schema will be saved as "schema.sql".
     */
    public static void exportDatabase(String path) {
        logger.debug("Export database to {}", path);
        if (!path.endsWith("/")) path += "/";
        System.out.println("[INFO]: Dumping database...");

//        FileManager.overwriteFile(path + "exported_schema.sql", ""); // Clear the file if it exists
        exportSchema(path + "exported_schema.sql");

        exportGenre(path + "data/genre.csv");
        exportDifficulty(path + "data/difficulty.csv");
        exportTag(path + "data/tag.csv");
        exportNote(path + "data/note.csv");
        exportAssignmentDifficulty(path + "data/assignment_difficulty.csv");
        exportAssignmentGenre(path + "data/assignment_genre.csv");
        exportAssignmentTag(path + "data/assignment_tag.csv");
        exportPatternDescription(path + "data/pattern_description.csv");
        exportPattern(path + "data/pattern.csv");

        System.out.println("[INFO]: Dumped database to " + path + "exported_schema.sql");
        logger.debug("Dumped database to " + path + "exported_schema.sql");
    }

    /**
     * Exports the database schema to the specified path.
     * This method generates SQL statements for creating the database schema and saves them to a file.
     *
     * @param path The file path where the schema SQL should be saved. The path should be a directory,
     *             and "schema.sql" will be appended to this path for the output file.
     */
    private static void exportSchema(String path) {
        logger.info("Export schema to {}", path);
        // Create the ServiceRegistry from the settings
        ServiceRegistry serviceRegistry = new StandardServiceRegistryBuilder()
                .applySettings(Parameters.DATABASE_SETTINGS)
                .build();

        //Save schema
        MetadataSources metadataSources = new MetadataSources(serviceRegistry);
        metadataSources.addAnnotatedClass(GenreEntity.class);
        metadataSources.addAnnotatedClass(TagEntity.class);
        metadataSources.addAnnotatedClass(DifficultyEntity.class);
        metadataSources.addAnnotatedClass(NoteEntity.class);
        metadataSources.addAnnotatedClass(PatternEntity.class);
        metadataSources.addAnnotatedClass(DifficultyAssignmentEntity.class);
        metadataSources.addAnnotatedClass(GenreAssignmentEntity.class);
        metadataSources.addAnnotatedClass(TagAssignmentEntity.class);
        metadataSources.addAnnotatedClass(PatternEntity.class);
        Metadata metadata = metadataSources.buildMetadata();

        SchemaExport schemaExport = new SchemaExport();
        schemaExport.setFormat(true);
        schemaExport.setOutputFile(path);
        schemaExport.createOnly(EnumSet.of(TargetType.SCRIPT), metadata);

        logger.info("Exported schema to {}", path);
    }

    // The following methods document the export process for each entity type.
    // Similar JavaDoc comments are applicable to each method below, adjusted for the specific entity being exported.

    /**
     * Exports data for the Genre entity into a CSV file.
     *
     * @param path The file path where the Genre data should be saved in CSV format.
     */
    private static void exportGenre(String path) {
        logger.debug("Export genre to {}", path);
        StringBuilder csv = new StringBuilder("id,name\n");
        List<GenreEntity> entityList = Parameters.entityManager.createQuery("SELECT d FROM GenreEntity d").getResultList();
        for (GenreEntity entity : entityList) {
            csv.append(entity.getId()).append(",").append(entity.getName()).append("\n");
        }
        DataManager.FileManager.overwriteFile(path, csv.toString());
        logger.debug("Exported genre to {}", path);
    }

    /**
     * Exports data for the Difficulty entity into a CSV file.
     *
     * @param path The file path where the Difficulty data should be saved in CSV format.
     */
    private static void exportDifficulty(String path) {
        logger.debug("Export difficulty to {}", path);
        StringBuilder csv = new StringBuilder("id,name\n");
        List<DifficultyEntity> entityList = Parameters.entityManager.createQuery("SELECT d FROM DifficultyEntity d").getResultList();
        for (DifficultyEntity entity : entityList) {
            csv.append(entity.getId()).append(",").append(entity.getName()).append("\n");
        }
        DataManager.FileManager.overwriteFile(path, csv.toString());
        logger.debug("Exported difficulty to {}", path);
    }

    /**
     * Exports data for the Tag entity into a CSV file.
     *
     * @param path The file path where the Tag data should be saved in CSV format.
     */
    private static void exportTag(String path) {
        logger.debug("Export tag to {}", path);
        StringBuilder csv = new StringBuilder("id,name\n");
        List<TagEntity> entityList = Parameters.entityManager.createQuery("SELECT d FROM TagEntity d").getResultList();
        for (TagEntity entity : entityList) {
            csv.append(entity.getId()).append(",").append(entity.getName()).append("\n");
        }
        DataManager.FileManager.overwriteFile(path, csv.toString());
        logger.debug("Exported tag to {}", path);
    }

    /**
     * Exports data for the Tag entity into a CSV file.
     *
     * @param path The file path where the Tag data should be saved in CSV format.
     */
    private static void exportNote(String path) {
        logger.debug("Export note to {}", path);
        StringBuilder csv = new StringBuilder("id,line_index,line_layer,cut_direction,type\n");
        List<NoteEntity> entityList = Parameters.entityManager.createQuery("SELECT d FROM NoteEntity d").getResultList();
        for (NoteEntity entity : entityList) {
            csv.append(entity.getId()).append(",").append(entity.getLineIndex()).append(",").append(entity.getLineLayer()).append(",").append(entity.getCutDirection()).append(",").append(entity.getType()).append("\n");
        }

        DataManager.FileManager.overwriteFile(path, csv.toString());
        logger.debug("Exported note to {}", path);
    }

    /**
     * Exports data for the DifficultyAssignment entity into a CSV file.
     *
     * @param path The file path where the DifficultyAssignment data should be saved in CSV format.
     */
    private static void exportAssignmentDifficulty(String path) {
        logger.debug("Export assignment difficulty to {}", path);
        StringBuilder csv = new StringBuilder("id,fk_difficulty_id,fk_pattern_description_id\n");
        List<DifficultyAssignmentEntity> entityList = Parameters.entityManager.createQuery("SELECT d FROM DifficultyAssignmentEntity d").getResultList();
        for (DifficultyAssignmentEntity entity : entityList) {
            csv.append(entity.getId()).append(",").append(entity.getFkDifficultyId()).append(",").append(entity.getFkPatternDescriptionId()).append("\n");
        }

        DataManager.FileManager.overwriteFile(path, csv.toString());
        logger.debug("Exported assignment difficulty to {}", path);
    }

    /**
     * Exports data for the GenreAssignment entity into a CSV file.
     *
     * @param path The file path where the GenreAssignment data should be saved in CSV format.
     */
    private static void exportAssignmentGenre(String path) {
        logger.debug("Export assignment genre to {}", path);
        StringBuilder csv = new StringBuilder("id,fk_genre_id,fk_pattern_description_id\n");
        List<GenreAssignmentEntity> entityList = Parameters.entityManager.createQuery("SELECT d FROM GenreAssignmentEntity d").getResultList();
        for (GenreAssignmentEntity entity : entityList) {
            csv.append(entity.getId()).append(",").append(entity.getFkGenreId()).append(",").append(entity.getFkPatternDescriptionId()).append("\n");
        }

        DataManager.FileManager.overwriteFile(path, csv.toString());
        logger.debug("Exported assignment genre to {}", path);
    }

    /**
     * Exports data for the TagAssignment entity into a CSV file.
     *
     * @param path The file path where the TagAssignment data should be saved in CSV format.
     */
    private static void exportAssignmentTag(String path) {
        logger.debug("Export assignment tag to {}", path);
        StringBuilder csv = new StringBuilder("id,fk_tag_id,fk_pattern_description_id\n");
        List<TagAssignmentEntity> entityList = Parameters.entityManager.createQuery("SELECT d FROM TagAssignmentEntity d").getResultList();
        for (TagAssignmentEntity entity : entityList) {
            csv.append(entity.getId()).append(",").append(entity.getFkTagId()).append(",").append(entity.getFkPatternDescriptionId()).append("\n");
        }

        DataManager.FileManager.overwriteFile(path, csv.toString());
        logger.debug("Exported assignment tag to {}", path);
    }

    /**
     * Exports data for the PatternDescription entity into a CSV file.
     *
     * @param path The file path where the PatternDescription data should be saved in CSV format.
     */
    private static void exportPatternDescription(String path) {
        logger.debug("Export pattern description to {}", path);
        StringBuilder csv = new StringBuilder("id,name,bpm,nps\n");
        List<PatternDescriptionEntity> entityList = Parameters.entityManager.createQuery("SELECT d FROM PatternDescriptionEntity d").getResultList();
        for (PatternDescriptionEntity entity : entityList) {
            csv.append(entity.getId()).append(",").append(entity.getName()).append(",").append(entity.getBpm()).append(",").append(entity.getNps()).append("\n");
        }

        DataManager.FileManager.overwriteFile(path, csv.toString());
        logger.debug("Exported pattern description to {}", path);
    }

    /**
     * Exports data for the Pattern entity into a CSV file.
     *
     * @param path The file path where the Pattern data should be saved in CSV format.
     */
    private static void exportPattern(String path) {
        logger.debug("Export pattern to {}", path);
        StringBuilder csv = new StringBuilder("id,note_id,followed_by_note_id,count,pattern_description_id\n");
        List<PatternEntity> entityList = Parameters.entityManager.createQuery("SELECT d FROM PatternEntity d").getResultList();
        for (PatternEntity entity : entityList) {
            csv.append(entity.getId()).append(",").append(entity.getNoteId()).append(",").append(entity.getFollowedByNoteId()).append(",").append(entity.getCount()).append(",").append(entity.getPatternDescriptionId()).append("\n");
        }

        DataManager.FileManager.overwriteFile(path, csv.toString());
        logger.debug("Exported pattern to {}", path);
    }


}
