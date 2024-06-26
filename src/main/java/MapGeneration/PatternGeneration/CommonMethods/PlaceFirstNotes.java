package MapGeneration.PatternGeneration.CommonMethods;

import BeatSaberObjects.Objects.Note;

import java.util.List;

import static DataManager.Parameters.RANDOM;
import static MapGeneration.PatternGeneration.NextLinearNote.nextLinearNote;

public class PlaceFirstNotes {
    /**
     * If there is no note placed yet, then this function will always generate a down-swing note
     *
     * @param _time time specifies on which bpm the note should be placed.
     * @return BeatSaberObjects.Objects.Note
     */
    public static Note firstNotePlacement(float _time) {
        Note n;
        double placement = RANDOM.nextDouble() * 100;

        if (placement < 20) n = new Note(_time, 1, 0, 1, 1);
        else if (placement <= 65) n = new Note(_time, 2, 0, 1, 1);
        else n = new Note(_time, 3, 0, 1, 1);

        return n;
    }

    public static void placeInitialNoteBasedOnPrevNote(List<Note> notes, Note prevNote, float time) {
        if (prevNote == null) notes.add(firstNotePlacement(time));
        else {

            notes.add(nextLinearNote(prevNote, time));
            int counter = 0;
            while (notes.get(notes.size() - 1).isDD(prevNote) && counter <= 300) {
                notes.remove(notes.size() - 1);
                notes.add(nextLinearNote(prevNote, time));
                counter++;
            }
            if (counter >= 300) System.err.println("[ERROR] at beat: " + time + " infinite loop in create placement of Initial Note: \"placeInitialNoteBasedOnPrevNote\"");
        }
    }


}
