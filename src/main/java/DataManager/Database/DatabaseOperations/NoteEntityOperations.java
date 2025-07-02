package DataManager.Database.DatabaseOperations;

import BeatSaberObjects.Objects.Note;
import DataManager.Database.DatabaseCommonMethods;
import DataManager.Database.DatabaseEntities.NoteEntity;

import javax.persistence.NoResultException;
import java.util.ArrayList;
import java.util.List;

import static DataManager.Parameters.entityManager;
import static DataManager.Parameters.logger;
@Deprecated
public class NoteEntityOperations extends NoteEntity {
    private static List<NoteEntity> notes = getAllNotes();

    public static NoteEntity getNote(Note note) {
        try {
            if (notes == null) notes = getAllNotes();
            for (NoteEntity noteEntity : notes) {
                if (noteEntity.getLineIndex() == note._lineIndex && noteEntity.getLineLayer() == note._lineLayer && noteEntity.getCutDirection() == note._cutDirection && noteEntity.getType() == note._type) {
                    return noteEntity;
                }
            }
            throw new IllegalArgumentException("Note not found: " + note.toString().replace("\n", " "));
        } catch (NoResultException e) {
            return null;
        }
    }

    public static NoteEntity getNoteById(int id) {
        try {
            return (NoteEntity) entityManager.createNamedQuery("NoteEntity.findById")
                    .setParameter("id", id)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    public static ArrayList<NoteEntity> getAllNotes() {
        try {
            List<?> result = entityManager.createNamedQuery("NoteEntity.findAllNotes").getResultList();
            ArrayList<NoteEntity> noteEntities = DatabaseCommonMethods.checkCastFromQuery(result, NoteEntity.class);

            if (notes == null) notes = noteEntities;
            return noteEntities;
        } catch (NoResultException e) {
            logger.error("Could not find a Note");
            System.err.println("ERROR: Could not find a Note");
            return new ArrayList<>();
        }
    }

}
