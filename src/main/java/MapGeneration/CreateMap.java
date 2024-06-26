package MapGeneration;

import BeatSaberObjects.Objects.BeatSaberMap;
import BeatSaberObjects.Objects.Bookmark;
import BeatSaberObjects.Objects.Note;
import DataManager.Parameters;
import MapGeneration.PatternGeneration.CommonMethods.FixErrorsInPatterns;
import MapGeneration.GenerationElements.Pattern;

import java.util.*;

import static MapGeneration.PatternGeneration.BigJumps.createBigJumps;
import static MapGeneration.PatternGeneration.CommonMethods.CheckParity.checkForMappingErrors;
import static MapGeneration.ComplexPatternFromTemplate.complexPatternFromTemplate;
import static MapGeneration.PatternGeneration.Doubles.createDoubles;
import static MapGeneration.PatternGeneration.NormalJumps.createNormalJumps;
import static MapGeneration.PatternGeneration.LinearSlowPattern.linearSlowPattern;
import static MapGeneration.PatternGeneration.SmallJumps.createSmallJumps;
import static MapGeneration.PatternGeneration.TwoLeftTwoRight.twoLeftTwoRight;
import static MapGeneration.PatternGeneration.TwoRightOneLeft.twoRightOneLeft;



public class CreateMap {


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


    /**
     * This function creates a BeatSaberObjects.Objects.BeatSaberMap from a timings-map.
     *
     * @param map       the map that should be m
     * @param p         input patterns. This is used to calculate the probabilities of the complex patterns
     * @param oneHanded should this map be one handed?
     * @return returns a new Map
     */
    public static BeatSaberMap createMap(BeatSaberMap map, Pattern p, boolean oneHanded, boolean stacks) throws IllegalArgumentException {
        List<Note> notes = new ArrayList<>();
        List<Note> timings = Arrays.asList(map._notes);
        List<Bookmark> bookmarks = map.bookmarks == null ? map.calculateBookmarks() : map.bookmarks;
        String[] supportedTypes = new String[]{"complex", "linear", "1-2", "2-1", "2-2", "small_jumps", "normal_jumps", "big_jumps", "doubles", "pattern"};

        //If the map is one-handed or there are no bookmarks, then there is not that much to do
        if (oneHanded)
            return new BeatSaberMap(complexPatternFromTemplate(List.of(map._notes), p, true, stacks, false,null, null), map.originalJSON);
        if (bookmarks.isEmpty()) {
            Random random = new Random(Parameters.SEED);
            int min = 10;
            int max = 40;

            int randomNumber = random.nextInt(max - min + 1) + min;
            for (int i = 0; i < randomNumber; i++) {
                bookmarks.add(new Bookmark(timings.get(i * (timings.size() / randomNumber))._time, supportedTypes[random.nextInt(supportedTypes.length - 1)], null));
                if (bookmarks.get(bookmarks.size() - 1)._name.contains("jumps")) i--;
            }
//            return new BeatSaberObjects.Objects.BeatSaberMap(complexPatternFromTemplate(map._notes, p, oneHanded, stacks, null, null), map.originalJSON);
        }
        bookmarks.add(new Bookmark(timings.get(timings.size() - 1)._time + 10, "end", new float[]{(float) 0.0, (float) 0.0, (float) 0.0}));


        Note prevBlue = null;
        Note prevRed = null;
        for (int i = 0; i < bookmarks.size() - 1; i++) {
            List<Note> currentNotes = new ArrayList<>();
            System.out.println(bookmarks.get(i)._time + " - " + bookmarks.get(i + 1)._time + ": " + bookmarks.get(i)._name);

            for (Note timing : timings) {
                if (timing._time >= bookmarks.get(i + 1)._time) break;
                if (timing._time >= bookmarks.get(i)._time) currentNotes.add(timing);
            }

            if (bookmarks.get(i)._name.equalsIgnoreCase("end")) break;
            if (currentNotes.isEmpty()) continue; //If there are no notes in the current section, then skip it

            try {
                switch (bookmarks.get(i)._name.toLowerCase()) {
                    case "l", "linear" -> notes.addAll(linearSlowPattern(currentNotes, false, prevBlue, prevRed));
                    case "c", "complex" -> notes.addAll(complexPatternFromTemplate(currentNotes, p, false, stacks, false,prevBlue, prevRed));
                    case "1-2" -> notes.addAll(twoRightOneLeft(currentNotes, p, prevBlue, prevRed, stacks));
                    case "2-1" -> notes.addAll(twoRightOneLeft(currentNotes, p, prevRed, prevBlue, stacks).stream().map(Note::invertNote).toList());
                    case "2-2" -> notes.addAll(twoLeftTwoRight(currentNotes, prevBlue, prevRed));
                    case "sj", "small-jumps", "smalljumps", "small_jumps", "small jumps" -> notes.addAll(createSmallJumps(currentNotes, false, prevBlue, prevRed));
                    case "j", "jumps", "normal jumps", "normal_jumps", "normal-jumps" -> notes.addAll(createNormalJumps(currentNotes, false, prevBlue, prevRed));
                    case "bj", "big-jumps", "bigjumps", "big_jumps", "big jumps" -> notes.addAll(createBigJumps(currentNotes, false, prevBlue, prevRed));
                    case "d", "doubles", "double-handed" -> notes.addAll(createDoubles(currentNotes, prevBlue, prevRed));

                    default -> {
                        System.err.println("There is no such flag as: \"" + bookmarks.get(i)._name + "\" with " + currentNotes.size() + " notes. Please have a look at the supported ones in the README");
                        System.err.println("Supported types: " + Arrays.toString(supportedTypes));
                        notes.addAll(complexPatternFromTemplate(currentNotes, p, false, stacks, false,prevBlue, prevRed));

                    }
                }
            } catch (Exception e) {
//                System.out.println(notes);
                e.printStackTrace();
                throw new RuntimeException("BREAK");
            }
            prevRed = getLast(notes, 0) == null ? prevRed : getLast(notes, 0);
            prevBlue = getLast(notes, 1) == null ? prevBlue : getLast(notes, 1);
        }

        FixErrorsInPatterns.fixSimpleMappingErrors(notes);
        checkForMappingErrors(notes, false);

        checkIfEveryNoteIsPlaced(notes, timings);

        BeatSaberMap newMap = new BeatSaberMap(notes, map.originalJSON);
        newMap.bookmarks = bookmarks;

        return newMap;
    }

    /**
     * returns the last BeatSaberObjects.Objects.Note of type "type" in the list l
     *
     * @param l    the list conatining all the notes
     * @param type what type of note should be returned? 0 or 1?
     * @return the last BeatSaberObjects.Objects.Note of type "type" in lis l
     */
    public static Note getLast(List<Note> l, int type) {
        for (int i = l.size() - 1; i >= 0; i--) {
            if (l.get(i)._type == type) return l.get(i);
        }
        return null;
    }

    /**
     * Checks if every note timing in the list of timings has a corresponding note placed in the list of notes.
     * Logs an error message for each timing that does not have a corresponding placed note.
     *
     * @param notes   the list of placed notes
     * @param timings the list of note timings to check
     */
    private static void checkIfEveryNoteIsPlaced(List<Note> notes, List<Note> timings) {
        for (Note timing : timings) {
            boolean found = false;

            // Iterate through the placed notes to find a matching timing
            for (Note note : notes) {
                if (note._time == timing._time) {
                    found = true;
                    break;
                }
            }

            // Log an error if no matching note was found for the timing
            if (!found) {
                System.err.println("BeatSaberObjects.Objects.Note at " + timing._time + " was not placed!");
            }
        }
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
}
