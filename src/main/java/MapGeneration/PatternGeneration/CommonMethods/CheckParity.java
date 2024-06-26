package MapGeneration.PatternGeneration.CommonMethods;

import BeatSaberObjects.Objects.Note;

import java.util.Collections;
import java.util.List;

import static MapGeneration.PatternGeneration.CommonMethods.PlaceFirstNotes.firstNotePlacement;

public class CheckParity {
    /**
     * This function checks parity and prints an error, if there is a dd somewhere
     *
     * @param notes List of notes that should be checked
     * @param quiet Should this function display error messages?
     */
    public static void checkBasicParity(List<Note> notes, boolean quiet) {
        Note red = null;
        Note blue = null;

        //ignore the rest, if the map is a no-arrow-map
        for (Note n : notes) {
            if (n._cutDirection != 8) break;
            if (n.equals(notes.get(notes.size() - 1))) return;
        }

        for (Note n : notes) {

            //set red and blue notes:
            if (red == null && n._type == 0) {
                red = n;
                continue;
            } else if (blue == null && n._type == 1) {
                blue = n;
                continue;
            } else if (blue == null || red == null) continue;

            if (n._type == 0 && red._time == n._time) continue;
            if (n._type == 1 && blue._time == n._time) continue;


            //Hitbox path fix when both notes are next to each other in the bottom lane
            if (blue._lineLayer == 0 && red._lineLayer == 0 && blue._lineIndex - red._lineIndex == -1 && blue._lineIndex - red._lineIndex == 1) {
                if (blue._cutDirection == 2 || blue._cutDirection == 3) blue._cutDirection = 1;
                if (blue._cutDirection == 4 || blue._cutDirection == 5) blue._cutDirection = 0;
                if (red._cutDirection == 2 || red._cutDirection == 3) red._cutDirection = 1;
                if (red._cutDirection == 4 || red._cutDirection == 5) red._cutDirection = 0;
            }

            //Exclude this at dd-checking:
            if (n._type == 0 && (red._cutDirection == 6 && n._cutDirection == 4 || red._cutDirection == 4 && n._cutDirection == 6 || red._cutDirection == 7 && n._cutDirection == 5 || red._cutDirection == 5 && n._cutDirection == 7))
                System.err.println("WARN at beat:    " + n._time + ": sharp angle");
                //check if red has a dd:
            else if (n._type == 0 && (n._cutDirection == red._cutDirection
                    || (red._cutDirection == 6 || red._cutDirection == 1 || red._cutDirection == 7) && (n._cutDirection == 6 || n._cutDirection == 1 || n._cutDirection == 7)
                    || (red._cutDirection == 7 || red._cutDirection == 3 || red._cutDirection == 5) && (n._cutDirection == 7 || n._cutDirection == 3 || n._cutDirection == 5)
                    || (red._cutDirection == 4 || red._cutDirection == 0 || red._cutDirection == 5) && (n._cutDirection == 4 || n._cutDirection == 0 || n._cutDirection == 5)
                    || (red._cutDirection == 4 || red._cutDirection == 2 || red._cutDirection == 6) && (n._cutDirection == 4 || n._cutDirection == 2 || n._cutDirection == 6))) {
                if (!quiet) System.err.println("[ERROR] at beat:   " + n._time + ": Parity break!");
            }

            //Exclude this at dd-checking:
            if (n._type == 1 && (blue._cutDirection == 6 && n._cutDirection == 4 || blue._cutDirection == 4 && n._cutDirection == 6 || blue._cutDirection == 7 && n._cutDirection == 5 || blue._cutDirection == 5 && n._cutDirection == 7))
                System.err.println("WARN at beat:    " + n._time + ": sharp angle");
                //check if blue has a dd:
            else if (n._type == 1 && (n._cutDirection == blue._cutDirection
                    || (blue._cutDirection == 6 || blue._cutDirection == 1 || blue._cutDirection == 7) && (n._cutDirection == 6 || n._cutDirection == 1 || n._cutDirection == 7)
                    || (blue._cutDirection == 7 || blue._cutDirection == 3 || blue._cutDirection == 5) && (n._cutDirection == 7 || n._cutDirection == 3 || n._cutDirection == 5)
                    || (blue._cutDirection == 4 || blue._cutDirection == 0 || blue._cutDirection == 5) && (n._cutDirection == 4 || n._cutDirection == 0 || n._cutDirection == 5)
                    || (blue._cutDirection == 4 || blue._cutDirection == 2 || blue._cutDirection == 6) && (n._cutDirection == 4 || n._cutDirection == 2 || n._cutDirection == 6))) {
                if (!quiet) System.err.println("[ERROR] at beat:   " + n._time + ": Parity break!");
            }


            if (n._type == 0) red = n;
            else if (n._type == 1) blue = n;
        }
    }

    /**
     * This function checks the list allNotes if there are 2 or more notes inside one another. If this is true, the red BeatSaberObjects.Objects.Note
     * will be placed moved one line to the right
     *
     * @param allNotes input List where all the notes have been saved
     * @param quiet    Should this function output errors?
     * @return a List without notes inside other notes
     */
    public static List<Note> checkForMappingErrors(List<Note> allNotes, boolean quiet) {
        if (allNotes.size() <= 1) return allNotes;
        Collections.sort(allNotes);
        for (int i = 0; i < allNotes.size() - 1; i++) {
            if (allNotes.get(i)._time == allNotes.get(i + 1)._time && allNotes.get(i).equalNotePlacement(allNotes.get(i + 1))) {
                if (allNotes.get(i)._type == 0) {
                    if (allNotes.get(i)._lineIndex != 0) allNotes.get(i)._lineIndex--;
                    else allNotes.get(i)._lineLayer = 2;
                }
            }
            Note n = allNotes.get(i);

            //Checking, if there is a downswing note in the top left or right corner
            if (n._type == 0 && n._lineIndex == 3 && n._lineLayer == 2 && n._cutDirection == 1) {
                n._lineLayer = 0;
                n._lineIndex = 1;
            }
            if (n._type == 1 && n._lineIndex == 0 && n._lineLayer == 2 && n._cutDirection == 1) {
                n._lineLayer = 0;
                n._lineIndex = 2;
            }

            if (n._lineIndex < 0 || n._lineIndex >= 4 || n._lineLayer < 0 || n._lineLayer >= 3)
                if (!quiet) System.err.println("WARNING at beat: " + n._time + " note outside the grid!");

        }

        //Checking, if some notes inside other notes were missed:
        for (int i = 0; i < allNotes.size() - 1; i++) {
            if (allNotes.get(i)._time == allNotes.get(i + 1)._time && allNotes.get(i).equalNotePlacement(allNotes.get(i + 1))) {
                if (!quiet)
                    System.err.println("[ERROR] at beat:   " + allNotes.get(i)._time + ": note inside another Note!");
            }
        }

        checkBasicParity(allNotes, quiet);

        return allNotes;
    }

    /**
     * If there was an error, a timing note is being placed.
     * This function tries to see which note came before the error and places a note accordingly, which does not break parity.
     *
     * @param pattern pattern is the list where the previous notes are saved.
     * @param time    time specifies on which bpm the note should be placed.
     * @param i       i specifies at which element the last note has been placed.
     * @param j       j... If the pattern is one handed: j = 1. If two handed: j = 2.
     * @return BeatSaberObjects.Objects.Note
     */
    public static Note nextNoteAfterTimingNote(List<Note> pattern, float time, int i, int j) {
        Note toReturn = firstNotePlacement(time);

        // When second last note was an up swing:
        if (pattern.get(i - 2 * j)._cutDirection == 6 || pattern.get(i - 2 * j)._cutDirection == 1 || pattern.get(i - 2 * j)._cutDirection == 7)
            toReturn._cutDirection = 1;

        // When second last note was a down swing:
        if (pattern.get(i - 2 * j)._cutDirection == 4 || pattern.get(i - 2 * j)._cutDirection == 0 || pattern.get(i - 2 * j)._cutDirection == 5)
            toReturn._cutDirection = 0;

        // When second last note was a horizontal swing:
        if (pattern.get(i - j)._cutDirection == 2 || pattern.get(i - j)._cutDirection == 3)
            toReturn._cutDirection = 1;

        return toReturn;
    }

    /**
     * This function checks if the note on position "i" has a valid placement there
     * currently supports:
     * - Double Directional
     * - Vision blocks
     * - placing note, if the previous note is not placed directly in front of it
     * <p>
     * read further for more information
     *
     * @param notes     notes is the list where the previous notes are saved.
     * @param i         i specifies at which element the last note has been placed.
     * @param oneHanded is the map a one handed map.
     * @return boolean
     */
    public static boolean invalidPlacement(List<Note> notes, int i, boolean oneHanded) {
        if (notes.size() <= 2) return false;
        if (i < 4) return false;
        if (notes.get(i - 1) == null || notes.get(i) == null) return true;

        int j = 2;
        if (oneHanded) j = 1;

        if (notes.get(i)._lineIndex < 0 || notes.get(i)._lineIndex >= 4 || notes.get(i)._lineLayer < 0 || notes.get(i)._lineLayer >= 3)
            return false;

        // DD:
        if (notes.get(i - j)._cutDirection == notes.get(i)._cutDirection
                || (notes.get(i - j)._cutDirection == 6 || notes.get(i - j)._cutDirection == 1 || notes.get(i - j)._cutDirection == 7) && (notes.get(i)._cutDirection == 6 || notes.get(i)._cutDirection == 1 || notes.get(i)._cutDirection == 7)
                || (notes.get(i - j)._cutDirection == 7 || notes.get(i - j)._cutDirection == 3 || notes.get(i - j)._cutDirection == 5) && (notes.get(i)._cutDirection == 7 || notes.get(i)._cutDirection == 3 || notes.get(i)._cutDirection == 5)
                || (notes.get(i - j)._cutDirection == 4 || notes.get(i - j)._cutDirection == 0 || notes.get(i - j)._cutDirection == 5) && (notes.get(i)._cutDirection == 4 || notes.get(i)._cutDirection == 0 || notes.get(i)._cutDirection == 5)
                || (notes.get(i - j)._cutDirection == 4 || notes.get(i - j)._cutDirection == 2 || notes.get(i - j)._cutDirection == 6) && (notes.get(i)._cutDirection == 4 || notes.get(i)._cutDirection == 2 || notes.get(i)._cutDirection == 6)
        ) return true;

        // weird top row notes
        if (notes.get(i - j)._cutDirection == 0 && notes.get(i)._cutDirection == 6 && notes.get(i - j)._lineLayer == 2 && notes.get(i)._lineLayer >= 1 && notes.get(i - j)._lineIndex <= 2 && notes.get(i)._lineLayer >= 2)
            return true;

        // Vision block
        if (notes.get(i)._lineIndex == 2 && notes.get(i)._lineLayer == 1) return true;

        // Only place the note, if the previous note is not placed directly in front of it
        // If one-handed it true, then we can just skip this step.
        if (oneHanded) return false;
        return notes.get(i - 1)._lineIndex == notes.get(i).getInverted()._lineIndex && notes.get(i - 1)._lineLayer == notes.get(i)._lineLayer;
    }


}
