package MapGeneration;

import BeatSaberObjects.Objects.Note;
import BeatSaberObjects.Objects.TimingNote;
import MapGeneration.GenerationElements.PatternCache;
import MapGeneration.PatternGeneration.CommonMethods.FixErrorsInPatterns;
import MapGeneration.GenerationElements.Pattern;
import MapGeneration.GenerationElements.PatternProbability;

import java.util.*;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;

import static DataManager.Parameters.RANDOM;
import static DataManager.Parameters.ignoreDDs;
import static DataManager.Parameters.verbose;
import static MapGeneration.PatternGeneration.CommonMethods.CheckParity.*;
import static MapGeneration.PatternGeneration.CommonMethods.PlaceFirstNotes.placeInitialNoteBasedOnPrevNote;
import static MapGeneration.PatternGeneration.CommonMethods.StackPlacements.placeStacks;
import static MapGeneration.PatternGeneration.CommonMethods.StackPlacements.removeStacks;
import static MapGeneration.PatternGeneration.NextLinearNote.nextLinearNote;

public class ComplexPatternFromTemplate {

        /*
    Red: 0
    Blue: 1

    Index - Layer:          Cut direction:
    |---|---|---|---|       |---|---|---|
    |   |   |   |   |       | 4 | 0 | 5 |
    |---|---|---|---|       |---|---|---|
    |1-0|1-1|1-2|1-3|       | 2 | 8 | 3 |
    |---|---|---|---|       |---|---|---|
    | 0 | 1 | 2 | 3 |       | 6 | 1 | 7 |
    |---|---|---|---|       |---|---|---|
     */

    private static final int NUMBER_OF_PATTERNS = 4; //must be >= 3
    private static final int LENGTH_OF_PATTERN = 8;
    private static final int NUMBER_OF_NOTES_TO_BE_CHANGED = 4;
    private static PatternCache patternCache;


    /**
     * This method creates a pattern on the basis of the original Pattern.
     *
     * @param timingsImmutable where the notes should be placed
     * @param p                p are the probabilities that which note follows that. It must be in the "Pattern"-Format
     * @param oneHanded        is the map one-handed?
     * @param prevBlue         What the previous blue note was
     * @param prevRed          What the previous red note was
     * @return A List of all notes that have been generated
     */
    public static List<Note> complexPatternFromTemplate(List<Note> timingsImmutable, Pattern p, boolean oneHanded, boolean stacks, boolean usePatternCache, Note prevBlue, Note prevRed) throws IllegalArgumentException {
        if (timingsImmutable == null || timingsImmutable.isEmpty()) return new ArrayList<>();
        List<Note> timings = new ArrayList<>(timingsImmutable);

        if (verbose) System.out.println("Creating complex pattern from template with " + timings.size() + " notes");
        List<Note> removeStacks = removeStacks(timings);
        if (verbose) System.out.println("Removed " + removeStacks.size() + " stack placements");
        if (verbose) System.out.println("New timings size: " + timings.size());

        List<Note> pattern = new ArrayList<>(timings.size());
        if (usePatternCache) patternCache = new PatternCache(timings, p, NUMBER_OF_PATTERNS, LENGTH_OF_PATTERN, NUMBER_OF_NOTES_TO_BE_CHANGED);


        if (timings.size() == 1) oneHanded = true;
        int j = oneHanded ? 1 : 2;


        placeInitialNoteBasedOnPrevNote(pattern, prevBlue, timings.get(0)._time); // Handling Blue
        if (!oneHanded) placeInitialNoteBasedOnPrevNote(pattern, prevRed, timings.get(1)._time); // Handling Red


        for (int i = j; i < timings.size(); i++) {
            pattern.add(null);
        }


        int blueHorizontalsInARow = 0; // prevent parity breaks for red notes
        int redHorizontalsInARow = 0;  // prevent parity breaks for red notes
        int invalidPlacesInARow = 0;   // prevent infinite loops

        boolean[] palmDirection = {determineInitialPalmDirection(pattern.get(0)), determineInitialPalmDirection(pattern.size() >= 2 ? pattern.get(1) : null)}; //true is up and false is down, [0] is red and [1] is blue
        int[] inversePlacementCount = {0, 0}; //[0] is red and [1] is blue

        List<Note> blueNotesFirstFix = new ArrayList<>();
        for (int i = j; i < timings.size(); i++) {
            boolean inValidPlacement = false;

            // manual error handling:
            // When there exists an infinite loop:
            // Then create a new next note
            if ((oneHanded && i >= 2 || i >= 4) && invalidPlacesInARow >= 500) {
                System.err.println("[ERROR] at beat: " + timings.get(i)._time);
                pattern.set(i, new TimingNote(timings.get(i)._time));
                invalidPlacesInARow = 0;
                continue;
            } else if (invalidPlacesInARow >= 500)
                throw new IllegalArgumentException("Infinite Loop while creating map! Please try again.(Error occurred in \"complex\")");
            if ((oneHanded && i >= 2 || i >= 4) && Objects.requireNonNull(pattern.get(i - j))._cutDirection == 8) {
                pattern.set(i, nextNoteAfterTimingNote(pattern, timings.get(i)._time, i, j));
                continue;
            }

            Note previous = pattern.get(i - j);
            Note next;
            if (usePatternCache) {// alle false noch einmal evaluieren
                next = patternCache.getNext(previous, invalidPlacesInARow, timings.get(i)._time);
            } else
                next = getComplexNote(p, previous, invalidPlacesInARow, timings.get(i)._time);
            pattern.set(i, next);


            if (pattern.get(i) == null) {
                System.err.println("NULL");
            }
            if (previous.isDD(pattern.get(i))) inValidPlacement = true;
            if (!ignoreDDs && previous._cutDirection == pattern.get(i)._cutDirection) inValidPlacement = true;
            if (invalidPlacement(pattern, i, oneHanded)) inValidPlacement = true;
            if (inValidPlacement) {
                pattern.set(i, null);
                i--;
                invalidPlacesInARow++;
                continue;
            } else {
                invalidPlacesInARow = 0;
            }
            if (previous._cutDirection == pattern.get(i)._cutDirection) throw new IllegalArgumentException("hä?");


            // Check if the horizontal placement is correct or if there is a parity break.
            // For further info, have a look at: endHorizontalPlacements()
            if ((redHorizontalsInARow >= 2 || blueHorizontalsInARow >= 2) && (pattern.get(i)._cutDirection != 2 && pattern.get(i)._cutDirection != 3)) {
                Note noteAfterHorizontal = endHorizontalPlacements(pattern, i, j);
                pattern.set(i, noteAfterHorizontal != null ? noteAfterHorizontal : pattern.get(i));
                if (i % 2 == 0) blueHorizontalsInARow = 0;
                if (i % 2 == 1) redHorizontalsInARow = 0;
            }
            if (i % 2 == 0 && (pattern.get(i)._cutDirection == 2 || pattern.get(i)._cutDirection == 3)) blueHorizontalsInARow++;
            if (i % 2 == 1 && (pattern.get(i)._cutDirection == 2 || pattern.get(i)._cutDirection == 3) && !oneHanded)
                redHorizontalsInARow++;

            // creating the flag, so that a stack may be done later;
            pattern.get(i).amountOfStackedNotes = timings.get(i).amountOfStackedNotes;

            //Make it so that a blue swing is always first
            if (!oneHanded && i >= 6 && i + 1 < timings.size()) {
                // The i%2 is necessary, because every second note will be inverted. And we only want to fix blue notes
                if (i % 2 == 0 && pattern.get(i)._time - pattern.get(i - 1)._time >= 0.4 && (
                        ((pattern.get(i)._cutDirection == 6 || pattern.get(i)._cutDirection == 1 || pattern.get(i)._cutDirection == 7) && (pattern.get(i - 1)._cutDirection == 6 || pattern.get(i - 1)._cutDirection == 1 || pattern.get(i - 1)._cutDirection == 7)) ||
                                ((pattern.get(i)._cutDirection == 4 || pattern.get(i)._cutDirection == 0 || pattern.get(i)._cutDirection == 5) && (pattern.get(i - 1)._cutDirection == 4 || pattern.get(i - 1)._cutDirection == 0 || pattern.get(i - 1)._cutDirection == 5))
                )) {

                    Note noteNew = new Note(
                            timings.get(i + 1)._time,
                            pattern.get(i)._lineIndex,
                            pattern.get(i)._lineLayer,
                            pattern.get(i)._type,
                            (pattern.get(i)._cutDirection == 6 || pattern.get(i)._cutDirection == 1 || pattern.get(i)._cutDirection == 7) ? 0 : 1); //cutDirection

                    blueNotesFirstFix.add(pattern.get(i));
                    pattern.set(i, noteNew);
                    System.out.println("Added new Note: " + noteNew);

                    //Only a blue note can be here. So we don't need to check ever statement
                    palmDirection[i % j] = !palmDirection[i % j];
                }
            }

            int checkPalmDirection = checkPalmDirection(palmDirection[i % j], pattern.get(i));
            if (checkPalmDirection == 1) inversePlacementCount[i % j]++; //parity break
            if (checkPalmDirection == 0) inversePlacementCount[i % j] = 0; //everything okay
            if (!ignoreDDs && inversePlacementCount[i % j] >= 1) { //TODO: War 2
//                Note noteNew = new Note(pattern.get(i)._time, pattern.get(i - j)._lineIndex, pattern.get(i - j)._lineLayer, pattern.get(i - j)._type, pattern.get(i - j)._cutDirection);
//                pattern.set(i, noteNew); //If there is a parity break, duplicate the current note. It will be taken care of later :P
                pattern.get(i).invertCutDirection();
                if (verbose) System.out.println("Fixed horizontal parity break at: " + pattern.get(i)._time + "\n");

                inversePlacementCount[i % j] = 0;
                palmDirection[i % j] = !palmDirection[i % j];
                continue;
            }


            //invert wrist position
            palmDirection[i % j] = !palmDirection[i % j];
        }

        // make every second note red:
        if (!oneHanded) {
            for (int i = 1; i < pattern.size(); i += 2) pattern.get(i).invertNote();
        }

        // Check, if one note is inside another note
        List<Note> l = new ArrayList<>(pattern);
        l.addAll(blueNotesFirstFix);
        l.sort(Comparator.comparingDouble(n -> n._time));

        if (stacks) l = placeStacks(l, removeStacks);
        FixErrorsInPatterns.fixSimpleMappingErrors(pattern);
        pattern = checkForMappingErrors(l, true);

        return pattern;
    }

    /**
     * This function predicts the next note based the probabilities of the pattern
     *
     * @param pattern this is probability of the patterns from a map saved as the PatternProbability class
     * @param time    time specifies on which bpm the note should be placed.
     * @return BeatSaberObjects.Objects.Note
     */
    public static Note predictNextNote(PatternProbability pattern, float time) {
        if (pattern == null) {
            System.err.println("[INFO]: Patten is null!");
            return null;
        }
        if (pattern.notes == null) {
            System.err.println("[INFO]: Notes are null!");
            return null;
        }
        // Check, if there even is a probability in the pattern.
        // This case could appear when the count[][] has been modified and all counts have been set to 0 instead of null.
        if (IntStream.range(0, pattern.probabilities.length).mapToDouble(i -> pattern.probabilities[i]).sum() <= 0) {
            if (verbose) System.out.println("[INFO]: Every probability is 0...");
            return null;
        }

        float currentProbability = 0;
        double placement = RANDOM.nextDouble() * 100;

        for (int i = 0; i < pattern.probabilities.length; i++) {
            if (pattern.notes[i] == null || currentProbability > 100) return null;
            currentProbability += pattern.probabilities[i];
            if (placement <= currentProbability) {
                Note n = pattern.notes[i];
                return new Note(time, n._lineIndex, n._lineLayer, n._type, n._cutDirection);
            }

        }

        System.err.println("[WARN]: Couldn't find a next note. Please have a look at beat: " + time);

        for (int i = 0; i < pattern.probabilities.length; i++) {
            System.out.println(pattern.probabilities[i]);
        }

        return null;
    }

    /**
     * This function tries to avoid parity breaks when a horizontal segment is coming to an end.
     *
     * @param pattern pattern [] is the array, where the previous notes are saved.
     * @param i       i specifies at which element the last note has been placed.
     * @param j       j... If the pattern is one handed: j = 1. If two handed: j = 2.
     * @return BeatSaberObjects.Objects.Note
     */
    public static Note endHorizontalPlacements(List<Note> pattern, int i, int j) {
        float random = RANDOM.nextFloat() * 100;
//        boolean debug = false;
        int firstHorizontalCutDirection = -1;
        int secondHorizontalCutDirection = -1;
        int horizontalsInARow = 0;
//        if (debug) System.out.println(pattern[i - j - j - j - j].toString().replaceAll("\n", ""));
//        if (debug) System.out.println(pattern[i - j - j - j].toString().replaceAll("\n", ""));
//        if (debug) System.out.println(pattern[i - j - j].toString().replaceAll("\n", ""));
//        if (debug) System.out.println(pattern[i - j].toString().replaceAll("\n", ""));
//        if (debug) System.out.println(pattern[i].toString().replaceAll("\n", ""));

        for (int k = i - j; k >= 0; k -= j) {
            if (pattern.get(k)._cutDirection != 2 && pattern.get(k)._cutDirection != 3) {
                firstHorizontalCutDirection = pattern.get(k)._cutDirection;
                secondHorizontalCutDirection = pattern.get(k + j)._cutDirection;
                break;
            }
            horizontalsInARow++;
        }
//        if (debug) System.out.println("In a row:   " + horizontalsInARow);
//        if (debug) System.out.println("Direction:  " + firstHorizontalCutDirection);
//        if (debug) System.out.println("BeatSaberObjects.Objects.Note (i):   " + i);
//        if (debug) System.out.println();

        if (horizontalsInARow == 0) return null;

        switch (horizontalsInARow % 2) {
            case 0 -> {
                //first: top left swing
                if (firstHorizontalCutDirection == 4 || (firstHorizontalCutDirection == 0 && secondHorizontalCutDirection == 3)) {
                    if (random <= 50) return new Note(pattern.get(i)._time, 3, 0, 1, 7);
                    else return new Note(pattern.get(i)._time, 3, 1, 1, 7);
                }

                //first: top right swing
                if (firstHorizontalCutDirection == 5 || (firstHorizontalCutDirection == 0 && secondHorizontalCutDirection == 2)) {
                    if (random <= 50) return new Note(pattern.get(i)._time, 2, 0, 1, 6);
                    else return new Note(pattern.get(i)._time, 1, 0, 1, 6);
                }

                //first: bottom left swing
                if (firstHorizontalCutDirection == 6 || (firstHorizontalCutDirection == 1 && secondHorizontalCutDirection == 3)) {
                    return new Note(pattern.get(i)._time, 3, 2, 1, 5);
                }

                //first: bottom right swing
                if (firstHorizontalCutDirection == 7 || (firstHorizontalCutDirection == 1 && secondHorizontalCutDirection == 2)) {
                    return new Note(pattern.get(i)._time, 2, 1, 1, 4);
                }
            }
            case 1 -> {
                //first: top left swing
                if (firstHorizontalCutDirection == 4 || (firstHorizontalCutDirection == 0 && secondHorizontalCutDirection == 3)) {
                    return new Note(pattern.get(i)._time, 2, 1, 1, 4);
                }

                //first: top right swing
                if (firstHorizontalCutDirection == 5 || (firstHorizontalCutDirection == 0 && secondHorizontalCutDirection == 2)) {
                    return new Note(pattern.get(i)._time, 3, 2, 1, 5);
                }

                //first: bottom left swing
                if (firstHorizontalCutDirection == 6 || (firstHorizontalCutDirection == 1 && secondHorizontalCutDirection == 3)) {
                    if (random <= 50) return new Note(pattern.get(i)._time, 2, 0, 1, 6);
                    else return new Note(pattern.get(i)._time, 1, 0, 1, 6);
                }

                //first: bottom right swing
                if (firstHorizontalCutDirection == 7 || (firstHorizontalCutDirection == 1 && secondHorizontalCutDirection == 2)) {
                    if (random <= 50) return new Note(pattern.get(i)._time, 3, 0, 1, 7);
                    else return new Note(pattern.get(i)._time, 3, 1, 1, 7);
                }
            }
        }

        System.err.println("Check parity at: " + pattern.get(i)._time);
        return null;
    }

    private static int checkPalmDirection(boolean palmDirection, Note note) {
        if (note == null) return -1;
        if (palmDirection && (note._cutDirection == 6 || note._cutDirection == 1 || note._cutDirection == 7) || !palmDirection && (note._cutDirection == 4 || note._cutDirection == 0 || note._cutDirection == 5))
            return 0; //no errors
        else if (!palmDirection && (note._cutDirection == 6 || note._cutDirection == 1 || note._cutDirection == 7) || palmDirection && (note._cutDirection == 4 || note._cutDirection == 0 || note._cutDirection == 5))
            return 1; //error detected!
        return -1; //error
    }

    private static boolean determineInitialPalmDirection(Note note) {
        if (note == null) return true;
        if (note._cutDirection == 4 || note._cutDirection == 0 || note._cutDirection == 5) return true;
        if (note._cutDirection == 6 || note._cutDirection == 1 || note._cutDirection == 7) return false;

        return true;
    }

        /*
    Red: 0
    Blue: 1

    Index - Layer:          Cut direction:
    |---|---|---|---|       |---|---|---|
    |   |   |   |   |       | 4 | 0 | 5 |
    |---|---|---|---|       |---|---|---|
    |1-0|1-1|1-2|1-3|       | 2 | 8 | 3 |
    |---|---|---|---|       |---|---|---|
    | 0 | 1 | 2 | 3 |       | 6 | 1 | 7 |
    |---|---|---|---|       |---|---|---|
     */

    public static Note getComplexNote(Pattern p, Note previous, int invalidPlacesInARow, float timing) {
        Note next;
        PatternProbability probabilities = p.getProbabilityOf(previous);

        // Generate a note according to the template
        // If there is an infinite loop, then try to place a linear note
        if (probabilities == null || invalidPlacesInARow >= 100) next = nextLinearNote(previous, timing);
        else {
            next = predictNextNote(probabilities, timing);
            if (next == null) {
                if (verbose) System.out.println("Somehow, the next note couldn't be computed. Falling back to BasicLinearNote...");
                next = nextLinearNote(previous, timing);
            }
            if (next == null)
                System.err.println("Error at beat: " + timing + " next note is null. Please have a look at predictNextNote()");
        }

        return next;
    }

}
