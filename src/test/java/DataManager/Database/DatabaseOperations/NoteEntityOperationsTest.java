package DataManager.Database.DatabaseOperations;

import BeatSaberObjects.Objects.Note;
import DataManager.Database.DatabaseEntities.NoteEntity;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class NoteEntityOperationsTest {

    @Test
    void getNote() {
        Note n = new Note(0, 0, 0, 0, 0);
        assertNotNull(NoteEntityOperations.getNote(n));
    }

    @Test
    void getNoteById() {
        Note n = new Note(0, 0, 0, 0, 0);
        NoteEntity noteEntity = NoteEntityOperations.getNote(n);
        assert noteEntity != null;
        assertNotNull(NoteEntityOperations.getNoteById(noteEntity.getId()));
    }

    @Test
    void getAllNotes() {
        ArrayList<NoteEntity> notes = NoteEntityOperations.getAllNotes();
        assertNotNull(notes);
        // 4 indexes, 3 layers, 8 directions, 2 colors
        assertTrue(notes.size() >= 4 * 3 * 8 * 2);
    }
}