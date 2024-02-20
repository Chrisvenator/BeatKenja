package DataManager.Database.DatabaseOperations;

import BeatSaberObjects.Objects.Note;
import DataManager.Database.DatabaseEntities.NoteEntity;

import javax.persistence.NoResultException;
import java.util.ArrayList;
import java.util.List;

import static DataManager.Parameters.entityManager;

public class NoteEntityOperations extends NoteEntity {
    private static List<NoteEntity> notes = getAllNotes();

    public static NoteEntity getNote(Note note) {
        try {
            if (notes == null) notes = getAllNotes();
            if (notes == null) return null;
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

    public static ArrayList<NoteEntity> getAllNotes() {
        try {
            ArrayList<NoteEntity> noteEntities = (ArrayList<NoteEntity>) entityManager.createNamedQuery("NoteEntity.findAllNotes").getResultList();
            if (notes == null) notes = noteEntities;
            return noteEntities;
        } catch (NoResultException e) {
            System.err.println("ERROR: Could not find a Note");
            return new ArrayList<>();
        }
    }

}
