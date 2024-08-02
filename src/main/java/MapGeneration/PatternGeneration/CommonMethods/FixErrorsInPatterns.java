package MapGeneration.PatternGeneration.CommonMethods;

import BeatSaberObjects.Objects.Note;

import java.util.Comparator;
import java.util.List;

import static DataManager.Parameters.logger;

public class FixErrorsInPatterns {
    public static void fixSimpleMappingErrors(List<Note> notes) {
        notes.sort(Comparator.comparingDouble(n -> n._time));
        //We need to run this twice, because the first run might create new errors
        for (int i = 0; i < 2; i++) {
            fixNoteOutsideOfGrid(notes);
            fixNoteInNote(notes);
            fixNoteNextToEachOtherInBottomLane(notes);
            fixSwingPathDoubles(notes);
            fixSwingPathAboveEachOther(notes);
            fixSwingPath(notes);
        }
        hardFixNoteOutsideOfGrid(notes);
    }

    /**
     * Fixes notes that are outside the grid after the first run of the error fixing. Because of the other fixes, the notes might be outside the grid again.
     * This function just shifts all the notes of that specific time, where the note os outside the grid, back into the grid.
     * Errors can still occur, but this function should fix most of them.
     *
     * @param notes List of notes
     */
    private static void hardFixNoteOutsideOfGrid(List<Note> notes) {
        for (Note note : notes) {
            if (note._lineIndex < 0) notes.stream().filter(n -> n._time == note._time).forEach(n -> n._lineIndex++);
            if (note._lineIndex > 3) notes.stream().filter(n -> n._time == note._time).forEach(n -> n._lineIndex--);
            if (note._lineLayer < 0) notes.stream().filter(n -> n._time == note._time).forEach(n -> n._lineLayer++);
            if (note._lineLayer > 2) notes.stream().filter(n -> n._time == note._time).forEach(n -> n._lineLayer--);
        }
    }

    /**
     * Fixes notes that are outside the grid
     * The note will be moved to the closest position inside the grid.
     * The grid is 4x3, so the note will be moved to the closest position inside the grid.
     *
     * @param notes List of notes
     */
    private static void fixNoteOutsideOfGrid(List<Note> notes) {
        for (Note note : notes) {
            if (note._lineIndex < 0) note._lineIndex = 0;
            if (note._lineIndex > 3) note._lineIndex = 3;
            if (note._lineLayer < 0) note._lineLayer = 0;
            if (note._lineLayer > 2) note._lineLayer = 2;
        }
    }

    /**
     * Fixes notes that are inside another note
     * The note that is inside another note will be moved to the left or right depending on the color of the note.
     * If the note is red, it will be moved to the left, if it is blue, it will be moved to the right.
     *
     * @param notes List of notes
     */
    private static void fixNoteInNote(List<Note> notes) {
        for (int i = 0; i < notes.size(); i++) {
            if (i >= 1 && notes.get(i).equalNotePlacement(notes.get(i - 1)) && notes.get(i)._time == notes.get(i - 1)._time) {

                logger.debug("Detected Note inside another Note: " + notes.get(i)._time + " " + notes.get(i)._lineIndex + " " + notes.get(i)._lineLayer + " " + notes.get(i)._type + " " + notes.get(i)._cutDirection);
                logger.debug("Detected Note inside another Note: " + notes.get(i - 1)._time + " " + notes.get(i - 1)._lineIndex + " " + notes.get(i - 1)._lineLayer + " " + notes.get(i - 1)._type + " " + notes.get(i - 1)._cutDirection);

                if (notes.get(i)._lineIndex > 0) {
                    //check for vision block
                    if (notes.get(i)._lineLayer == 1 && (notes.get(i)._lineIndex + 1 == 1 || notes.get(i)._lineIndex + 1 == 2)) {
                        //Adjust: put the red note on the left and the blue on the right
                        if (notes.get(i)._type == 1 && notes.get(i)._lineIndex == 0) notes.get(i)._lineIndex = 3;
                        if (notes.get(i)._type == 0 && notes.get(i)._lineIndex == 3) notes.get(i)._lineIndex = 0;
                    } else notes.get(i)._lineIndex--;
                } else {
                    //Special case, if it results in a vision block
                    if (notes.get(i)._lineLayer == 1 && (notes.get(i)._lineIndex + 1 == 1 || notes.get(i)._lineIndex + 1 == 2)) {
                        //Special case, if there is a vision block: put the red note on the left and the blue on the right
                        if (notes.get(i)._type == 1 && notes.get(i)._lineIndex == 0) notes.get(i)._lineIndex = 3;
                        if (notes.get(i)._type == 0 && notes.get(i)._lineIndex == 3) notes.get(i)._lineIndex = 0;
                    } else notes.get(i)._lineIndex++;
                }

                logger.debug("Fixed Note inside another Note:    " + notes.get(i)._time + " " + notes.get(i)._lineIndex + " " + notes.get(i)._lineLayer + " " + notes.get(i)._type + " " + notes.get(i)._cutDirection);
                logger.debug("Fixed Note inside another Note:    " + notes.get(i - 1)._time + " " + notes.get(i - 1)._lineIndex + " " + notes.get(i - 1)._lineLayer + " " + notes.get(i - 1)._type + " " + notes.get(i - 1)._cutDirection);
            }
        }
    }

    /**
     * Fixes notes that are next to each other and have and one of them is an angled swing
     * The cut direction of the angled swing will be changed to either an upward or downward swing
     *
     * @param notes List of notes
     */
    private static void fixSwingPathDoubles(List<Note> notes) {
        for (int i = 0; i < notes.size(); i++) {
            if (i >= 1) {
                //Check if they at the *same time and layer*
                if (notes.get(i)._time == notes.get(i - 1)._time && notes.get(i)._lineLayer == notes.get(i - 1)._lineLayer) {
                    //Check if they are next to each other
                    if (notes.get(i)._lineIndex - notes.get(i - 1)._lineIndex == 1 || notes.get(i)._lineIndex - notes.get(i - 1)._lineIndex == -1) {
                        //Check and fix cut-direction
                        for (int j = i; j >= i - 1; j--) {
                            Note n = notes.get(j);
                            if (n._cutDirection == 6 || n._cutDirection == 7) {
                                n._cutDirection = 1;
                            } else if (n._cutDirection == 4 || n._cutDirection == 5) {
                                n._cutDirection = 0;
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Fixes notes that are above each other and one of them is pointing away from the other.
     * The note pointing away from the other will be moved to the left or right depending on the color of the note.
     *
     * @param notes List of notes
     */
    private static void fixSwingPathAboveEachOther(List<Note> notes) {
        for (int i = 0; i < notes.size(); i++) {
            if (i >= 1) {
                //Check if they at the same time but different types
                if (notes.get(i)._time == notes.get(i - 1)._time && notes.get(i)._type != notes.get(i - 1)._type) {

                    //Check, if they are above each other
                    if (notes.get(i)._lineIndex == notes.get(i - 1)._lineIndex) {
                        // ignore, when both swings are sideways
                        if (notes.get(i)._cutDirection != 2 && notes.get(i)._cutDirection != 3 && notes.get(i - 1)._cutDirection != 2 && notes.get(i - 1)._cutDirection != 3) {
                            if (notes.get(i)._type == 1 && notes.get(i)._lineIndex < 3) notes.get(i)._lineIndex++;
                            else notes.get(i)._lineIndex--;
                            logger.debug("Fixed fixSwingPathAboveEachOther: " + notes.get(i)._time);
                        }
                    }
                }
            }
        }
    }

    /**
     * Fixes notes where there is one note above the other, and they are next to each other. Like the horse from chess.
     * The note above will be moved to the left or right depending on the position of the note.
     *
     * @param notes List of notes
     */
    private static void fixSwingPath(List<Note> notes) {
        for (int i = 0; i < notes.size(); i++) {
            if (i >= 1) {
                Note c; // Current note (above)
                Note p; // Previous note (below)

                //Flip current and previous, so that current is always the note above
                if (notes.get(i)._lineLayer - notes.get(i - 1)._lineLayer < 0) {
                    c = notes.get(i - 1);
                    p = notes.get(i);
                } else {
                    c = notes.get(i);
                    p = notes.get(i - 1);
                }


                //Check if they at the same time but different types
                if (c._time == p._time && c._type != p._type) {

                    //Check, if they re next to each other
                    if ((c._lineIndex - p._lineIndex == 1 || c._lineIndex - p._lineIndex == -1) && c._lineLayer != p._lineLayer) {
                        //Check, if the swing paths could even collide
                        if ((c._cutDirection == 2 || c._cutDirection == 3) && (p._cutDirection == 2 || p._cutDirection == 3)) continue;

                        //The note above is to the right
                        if (c._lineIndex > p._lineIndex && c._cutDirection == 5) {
                            if (c._lineIndex < 3) c._lineIndex++;
                            else p._lineIndex--;
                            logger.debug("Fixed fixSwingPath: " + c._time);
                        }
                        //The note above is to the left
                        if (c._lineIndex < p._lineIndex && c._cutDirection == 4) {
                            if (c._lineIndex > 0) c._lineIndex--;
                            else p._lineIndex++;
                            logger.debug("Fixed fixSwingPath: " + c._time);
                        }
                    }


                }
            }
        }
    }

    public static void fixNoteNextToEachOtherInBottomLane(List<Note> notes) {
        //Hitbox path fix when both notes are next to each other in the bottom lane
        for (int i = 1; i < notes.size(); i++) {
            Note current = notes.get(i);
            Note prev = notes.get(i - 1);

            if (current._time == prev._time && current._lineLayer == 0 && prev._lineLayer == 0 ) {
                if (current._lineIndex - prev._lineIndex == -1 && current._lineIndex - prev._lineIndex == 1) {
                    if (current._cutDirection == 2 || current._cutDirection == 3) current._cutDirection = 1;
                    if (current._cutDirection == 4 || current._cutDirection == 5) current._cutDirection = 0;
                    if (prev._cutDirection == 2 || prev._cutDirection == 3) prev._cutDirection = 1;
                    if (prev._cutDirection == 4 || prev._cutDirection == 5) prev._cutDirection = 0;
                }

            }
        }
    }
}

    /*
    Red: 0
    Blue: 1

    Index - Layer:          Cut direction:
    |---|---|---|---|       |---|---|---|
    |   |   |   |3-2|       | 4 | 0 | 5 |
    |---|---|---|---|       |---|---|---|
    |   |   |   |3-1|       | 2 | 8 | 3 |
    |---|---|---|---|       |---|---|---|
    |0-0|1-0|2-0|3-0|       | 6 | 1 | 7 |
    |---|---|---|---|       |---|---|---|
     */

