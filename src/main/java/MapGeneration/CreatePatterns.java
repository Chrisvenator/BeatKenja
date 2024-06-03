package MapGeneration;

import BeatSaberObjects.Objects.BeatSaberMap;
import BeatSaberObjects.Objects.Bookmark;
import BeatSaberObjects.Objects.Note;
import BeatSaberObjects.Objects.TimingNote;
import CustomWaveGenerator.Coordinate;
import DataManager.FileManager;
import DataManager.Parameters;
import DataManager.Records.PatMetadata;
import MapGeneration.GenerationElements.Pattern;
import MapGeneration.GenerationElements.PatternProbability;

import java.util.*;

import static DataManager.Parameters.*;

public class CreatePatterns {
    //TODO: stack placement is breaking everything. YAY
    /*
    Red: 0
    Blue: 1

    Layer - Index:          Cut direction:
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
            return new BeatSaberMap(complexPatternFromTemplate(map._notes, p, true, stacks, null, null), map.originalJSON);
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
//            try {
            switch (bookmarks.get(i)._name.toLowerCase()) {
                case "complex" -> {
                    Note[] complexNotes = complexPatternFromTemplate(currentNotes.toArray(new Note[0]), p, false, stacks, prevBlue, prevRed);
                    notes.addAll(Arrays.stream(complexNotes).toList());
                }
                case "linear" -> {
                    Note[] linearNotes = linearSlowPattern(currentNotes.toArray(new Note[0]), false, prevBlue, prevRed);
                    notes.addAll(Arrays.stream(linearNotes).toList());
                }
                case "1-2" -> notes.addAll(twoRightOneLeft(currentNotes.toArray(new Note[0]), p, prevBlue, prevRed, stacks));

                case "2-1" -> {
                    //prevRed and prevBlue must be inverted right here because in the next line we invert all the notes again.
                    List<Note> toAdd = twoRightOneLeft(currentNotes.toArray(new Note[0]), p, prevRed, prevBlue, stacks);
                    for (Note n : toAdd) n.invertNote();
                    notes.addAll(toAdd);
                }

                case "2-2" -> notes.addAll(twoLeftTwoRight(currentNotes.toArray(new Note[0]), prevBlue, prevRed));
                case "small-jumps", "smalljumps", "small_jumps", "small jumps" -> notes.addAll(createSmallJumps(currentNotes, false, prevBlue, prevRed));
                case "jumps", "normal jumps", "normal_jumps", "normal-jumps" -> notes.addAll(createJumps(currentNotes, false, prevBlue, prevRed));
                case "big-jumps", "bigjumps", "big_jumps", "big jumps" -> notes.addAll(createBigJumps(currentNotes, false, prevBlue, prevRed));
                case "doubles", "double-handed" -> notes.addAll(createDoubles(currentNotes.toArray(new Note[0]), prevBlue, prevRed));

                default -> {
                    System.err.println("There is no such flag as: \"" + bookmarks.get(i)._name + "\" with " + currentNotes.size() + " notes. Please have a look at the supported ones in the README");
                    System.err.println("Supported types: " + Arrays.toString(supportedTypes));
                    Note[] complexNotes = complexPatternFromTemplate(currentNotes.toArray(new Note[0]), p, false, stacks, prevBlue, prevRed);
                    notes.addAll(Arrays.stream(complexNotes).toList());
                }
            }
//            } catch (Exception e){
//                System.out.println(notes);
//                e.printStackTrace();
//                throw new RuntimeException("BREAK");
//            }
            prevRed = getLast(notes, 0) == null ? prevRed : getLast(notes, 0);
            prevBlue = getLast(notes, 1) == null ? prevBlue : getLast(notes, 1);
        }

        checkForMappingErrors(notes, false);

        checkIfEveryNoteIsPlaced(notes, timings);

        BeatSaberMap newMap = new BeatSaberMap(notes, map.originalJSON);
        newMap.bookmarks = bookmarks;

        return newMap;
    }

    /**
     * Creates an advanced complex pattern for a Beat Saber map based on the provided notes, pattern, and metadata.
     * This method processes the notes in chunks to generate a complex pattern, calculates notes per second (NPS),
     * and handles potential pattern creation exceptions.
     *
     * @param timingsImmutable the list of immutable note timings
     * @param fallbackPattern  the fallback pattern to use if pattern creation fails
     * @param oneHanded        whether the pattern should be one-handed
     * @param stacks           whether to include stack placements
     * @param prevBlue         the previous blue note, for reference in pattern creation
     * @param prevRed          the previous red note, for reference in pattern creation
     * @param metadata         the metadata containing information such as BPM, NPS, difficulty, genre, and tags
     * @return a list of notes forming the advanced complex pattern
     * @throws IllegalArgumentException if there is an issue creating the patterns
     */
    public static List<Note> createAdvancedComplexPattern(List<Note> timingsImmutable, Pattern fallbackPattern, boolean oneHanded, boolean stacks, Note prevBlue, Note prevRed, PatMetadata metadata) throws IllegalArgumentException {
        List<Note> notes = new ArrayList<>();
        List<Note> timings = new ArrayList<>(timingsImmutable);
        timings.sort(Comparator.comparingDouble(n -> n._time));

        // Remove all the stack placements because they break everything YAY. They can be added later again
        // Remove all stack placements as they can cause issues, to be added back later if needed
        List<Note> stackPlacements = removeStacks(timings);


        int nps; // Notes per second
        // Process the timings in chunks of four notes at a time
        for (int i = 0; i < timings.size(); i += 4) {
            // Check if there are enough notes to process a complete chunk
            if (i + 3 < timings.size()) {
                double t1 = timings.get(i)._time * 60 / metadata.bpm();
                double t4 = timings.get(i + 3)._time * 60 / metadata.bpm();
                nps = (int) Math.round(4 / (t4 - t1));
            } else {
                // Handle remaining notes if less than a chunk of four
                if (i == timings.size() - 1) {
                    notes.add(nextLinearNote(
                            notes.get(i - 1),
                            timings.get(i)._time
                    ));
                } else {
                    // Create a new PatMetadata instance with the calculated NPS
                    notes.addAll(Arrays.stream(complexPatternFromTemplate(
                            timings.subList(i, timings.size()).toArray(new Note[0]),
                            fallbackPattern,
                            oneHanded,
                            stacks,
                            prevBlue,
                            prevRed
                    )).toList());
                    System.out.println("i" + i + " " + timings.size());
                }
                break;
            }

            // Create a new PatMetadata instance with the calculated NPS
            PatMetadata newMetadata = new PatMetadata(metadata.name(), metadata.bpm(), nps, metadata.difficulty(), metadata.genre(), metadata.tags());
            Pattern p;
            try {
                // Try to create a new pattern with the new metadata
                p = new Pattern(newMetadata);
            } catch (IllegalArgumentException e1) {
                try {
                    // If creating the new pattern fails, revert to the original metadata
                    p = new Pattern(metadata);
                } catch (IllegalArgumentException e2) {
                    // If that also fails, use the fallback pattern
                    p = fallbackPattern;
                }
            }

            // Add the generated notes to the list
            notes.addAll(Arrays.stream(complexPatternFromTemplate(
                    timings.subList(i, i + 4).toArray(new Note[0]),
                    p,
                    oneHanded,
                    stacks,
                    prevBlue,
                    prevRed
            )).toList());
        }

        // If stacks are enabled, re-add the stack placements to the notes
        if (stacks) notes = placeStacks(notes, stackPlacements);

        return notes;
    }


    /**
     * This methode creates a pattern on basis of the original MapGeneration.GenerationElements.Pattern.
     *
     * @param timings   where the notes should be placed
     * @param p         p are the probabilities that which note follows which. It must be in the "MapGeneration.GenerationElements.Pattern"-Format
     * @param oneHanded is the map one handed?
     * @param prevBlue  What the previous blue note was
     * @param prevRed   What the previous red note was
     * @return A List of all notes that have been generated
     */
    public static Note[] complexPatternFromTemplate(Note[] timings, Pattern p, boolean oneHanded, boolean stacks, Note prevBlue, Note prevRed) throws IllegalArgumentException {
        Note[] pattern = new Note[timings.length];
        if (timings.length == 1) oneHanded = true;
        int j = oneHanded ? 1 : 2;


        //Placing the first notes manually:
        pattern[0] = prevBlue != null ? nextLinearNote(prevBlue, timings[0]._time) : firstNotePlacement(timings[0]._time);
        int counter = 0;
        while (pattern[0].isDD(prevBlue) && prevBlue != null && counter <= 300) {
            pattern[0] = nextLinearNote(prevBlue, timings[0]._time);
            counter++;
        }
        if (counter >= 300)
            System.err.println("[ERROR] at beat: " + timings[0]._time + "infinite loop in create complex (blue)");

        if (!oneHanded) pattern[1] = prevRed != null ? nextLinearNote(prevRed, timings[1]._time) : firstNotePlacement(timings[1]._time);
        counter = 0;
        if (!oneHanded) while (pattern[1].isDD(prevRed) && prevRed != null && counter <= 300) {
            pattern[1] = nextLinearNote(prevRed, timings[1]._time);
            counter++;
        }
        if (counter >= 300)
            System.err.println("[ERROR] at beat: " + timings[0]._time + "infinite loop in create complex (red)");

        int blueHorizontalsInARow = 0; //prevent parity breaks for red notes
        int redHorizontalsInARow = 0; //prevent parity breaks for red notes
        int invalidPlacesInARow = 0; //prevent infinite loops
        for (int i = j; i < timings.length; i++) {
            boolean inValidPlacement = false;

            //manual error handling:
            //When there exists an infinite loop:
            //Then create a new next note
            if ((oneHanded && i >= 2 || i >= 4) && invalidPlacesInARow >= 500) {
                System.err.println("[ERROR] at beat:   " + timings[i]._time);
                pattern[i] = new TimingNote(timings[i]._time);
                invalidPlacesInARow = 0;
                continue;
            } else if (invalidPlacesInARow >= 500)
                throw new IllegalArgumentException("Infinite Loop while creating map! Please try again.(Error occured in \"complex\")");
            if ((oneHanded && i >= 2 || i >= 4) && Objects.requireNonNull(pattern[i - j])._cutDirection == 8) {
                pattern[i] = nextNoteAfterTimingNote(pattern, timings[i]._time, i, j);
                continue;
            } //<-- next note after the error


            Note previous = pattern[i - j];
            PatternProbability probabilities = p.getProbabilityOf(previous);

            //Generate a note according to the template
            //If there is an infinite loop,then try to place a linear note
            if (probabilities == null || invalidPlacesInARow >= 100)
                pattern[i] = nextLinearNote(previous, timings[i]._time);
            else pattern[i] = predictNextNote(probabilities, timings[i]._time);


            //check, if the placement is valid (example: dd)
            if (previous.isDD(pattern[i])) inValidPlacement = true;
            if (previous._cutDirection == pattern[i]._cutDirection) inValidPlacement = true;
            if (invalidPlacement(pattern, i, oneHanded)) inValidPlacement = true;
            if (inValidPlacement) {
                pattern[i] = null;
                i--;
                invalidPlacesInARow++;
                continue;
            } else invalidPlacesInARow = 0;
            if (previous._cutDirection == pattern[i]._cutDirection) throw new IllegalArgumentException("hä?");


            //check if the horizontal placement is correct or if there is a parity break.
            //For further info have a look at: endHorizontalPlacements()
            if ((redHorizontalsInARow >= 2 || blueHorizontalsInARow >= 2) && (pattern[i]._cutDirection != 2 && pattern[i]._cutDirection != 3)) {
                Note noteAfterHorizontal = endHorizontalPlacements(pattern, i, j);
                pattern[i] = noteAfterHorizontal != null ? noteAfterHorizontal : pattern[i];
                if (i % 2 == 0) blueHorizontalsInARow = 0;
                if (i % 2 == 1) redHorizontalsInARow = 0;
            }
            if (i % 2 == 0 && (pattern[i]._cutDirection == 2 || pattern[i]._cutDirection == 3)) blueHorizontalsInARow++;
            if (i % 2 == 1 && (pattern[i]._cutDirection == 2 || pattern[i]._cutDirection == 3) && !oneHanded)
                redHorizontalsInARow++;

            //creating the flag, so that a stack may be done later;
            pattern[i].amountOfStackedNotes = timings[i].amountOfStackedNotes;
        }

        //make every second note red:
        if (!oneHanded) for (int i = 1; i < pattern.length; i += 2) pattern[i].invertNote();

        //Check, if one note is inside another note
        List<Note> l = Arrays.asList(pattern);
        if (stacks) l = createStacks(l);
        pattern = checkForMappingErrors(l, true).toArray(new Note[0]);

        return pattern;
    }

    @Deprecated
    public static List<Note> createMapFromWaves(List<Coordinate> coordinates) {
        List<Note> notes = new ArrayList<>();
        coordinates.forEach(c -> notes.add(new Note(c.x(), 0, c.y(), 1, 8)));

        return notes;
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

    /**
     * createRandomPattern is a function that creates random Notes with the timings given
     *
     * @param timings   timings of the notes
     * @param oneHanded is the map one-handed?
     * @return the resulting Notes as a List
     */
    public static List<Note> createRandomPattern(Note[] timings, boolean oneHanded) {
        List<Note> notes = new ArrayList<>();
        Random random = new Random(Parameters.SEED);

        double bpm = 120;
        try {
            List<String> list = FileManager.readFile(Parameters.filePath + "/info.dat");
            for (String s : list) {
                if (s.contains("\"_beatsPerMinute\" : ")) {
                    bpm = Double.parseDouble(s.substring(s.indexOf(":") + 1, s.indexOf(",")));
                    break;
                }
            }
        } catch (Exception ignored) {
        }

        System.out.println("Using " + bpm + " bpm");

        for (int i = 0; i < timings.length; i++) {

            Note n = new Note(timings[i]._time);
            //nextInt(5) --> [INCLUSIVE 0, EXCLUSIVE 5)
            n._lineIndex = random.nextInt(4);
            n._lineLayer = random.nextInt(3);
            n._type = oneHanded ? 2 : random.nextInt(2);
            n._cutDirection = random.nextInt(8);

            if (i < timings.length - 1) {
                float time = (float) (timings[i]._time / bpm * 60);
                float timeNext = (float) (timings[i + 1]._time / bpm * 60);

                if (timeNext - time < 0.5) {
                    if (n._lineIndex == 1 && n._lineLayer == 1) n._lineIndex--;
                    if (n._lineIndex == 2 && n._lineLayer == 1) n._lineIndex++;
                }
            }

            if (!ignoreDDs && i >= 1 && n.isDD(notes.get(i - 1))) {
                i--;
            } else notes.add(n);
        }
        return notes;
    }

    public static List<Note> createDoubles(Note[] timings, Note prevBlue, Note prevRed) {
        List<Note> notes = new ArrayList<>();


        if (prevRed != null) prevRed.invertNote();

        notes.add(prevBlue != null ? nextLinearNote(prevBlue, timings[0]._time) : firstNotePlacement(timings[0]._time));
        int counter = 0;
        while (notes.get(0).isDD(prevBlue) && counter <= 300) {
            notes.remove(0);
            notes.add(nextLinearNote(prevBlue, timings[0]._time));
            counter++;
        }
        if (counter >= 300)
            System.err.println("[ERROR] at beat: " + timings[0]._time + " infinite loop in create doubles");

        notes.add(prevRed != null ? nextLinearNote(prevRed, timings[0]._time) : firstNotePlacement(timings[0]._time));
        counter = 0;
        while (notes.get(1).isDD(prevRed)) {
            notes.remove(1);
            notes.add(nextLinearNote(prevRed, timings[0]._time));
            counter++;
        }
        if (prevRed != null) prevRed.invertNote();
        if (counter >= 300)
            System.err.println("[ERROR] at beat: " + timings[0]._time + " infinite loop in create doubles");

        int invalidPlacementsInARow = 0;
        for (int i = 1; i < timings.length; i++) {
            // ERROR handling:
            // Try 100 times to place a normal note. If this doesn't work, then place a Timing-BeatSaberObjects.Objects.Note.
            // If this still doesn't work, then throw an exception
            if (i >= 4 && invalidPlacementsInARow >= 100) {
                System.err.println("_ERROR at beat:   " + timings[i]._time + " Timing note");
                Note errorNote = new TimingNote(timings[i]._time);
                notes.add(errorNote); //Adding blue BeatSaberObjects.Objects.Note
                notes.add(errorNote); //Adding red BeatSaberObjects.Objects.Note
                invalidPlacementsInARow = 0;
                continue;
            } else if (invalidPlacementsInARow >= 500)
                throw new IllegalArgumentException("Infinite Loop while creating map! Please try again. (Error occured in \"doubles\"");
            //Place a BeatSaberObjects.Objects.Note that doesn't break parity after the error:
            if (i >= 1 && notes.get(notes.size() - 1)._cutDirection == 8) {
                notes.add(nextNoteAfterTimingNote(notes.toArray(notes.toArray(new Note[0])), timings[i]._time, notes.size(), 2));
                notes.add(nextNoteAfterTimingNote(notes.toArray(notes.toArray(new Note[0])), timings[i]._time, notes.size() - 1, 2));
                continue;
            }


            Note blue = nextLinearNote(notes.get(notes.size() - 2), timings[i]._time);
            Note red = nextLinearNote(notes.get(notes.size() - 1), timings[i]._time);

            if (blue.equalNotePlacement(red)) {
                red._lineIndex -= 1;
            }

            if (red.equalNotePlacement(blue)) {
                throw new IllegalArgumentException("hä?");
            }


            if (blue.isDD(notes.get(notes.size() - 2)) || red.isDD(notes.get(notes.size() - 1))) {
                i--;
                invalidPlacementsInARow++;
                continue;
            }

            notes.add(blue);
            notes.add(red);
        }

        for (int i = 1; i < notes.size(); i += 2) notes.get(i).invertNote();
        for (int i = 1; i < notes.size() - 1; i++)
            if (notes.get(i).equalNotePlacement(notes.get(i + 1)) && notes.get(i)._time == notes.get(i + 1)._time)
                notes.get(i)._lineIndex--;


        return notes;
    }

    public static List<Note> twoLeftTwoRight(Note[] timings, Note prevBlue, Note prevRed) {
        List<Note> notes = new ArrayList<>();

        notes.add(prevBlue != null ? nextLinearNote(prevBlue, timings[0]._time) : firstNotePlacement(timings[0]._time));
        int counter = 0;
        while (notes.get(0).isDD(prevBlue) && counter <= 300) {
            notes.remove(0);
            notes.add(nextLinearNote(prevBlue, timings[0]._time));
            counter++;
        }
        if (counter >= 300)
            System.err.println("[ERROR] at beat: " + timings[0]._time + " infinite loop in create doubles");
        notes.add(nextLinearNote(notes.get(0), timings[1]._time));

        notes.add(prevRed != null ? nextLinearNote(prevRed, timings[2]._time) : firstNotePlacement(timings[2]._time));
        counter = 0;
        while (notes.get(2).isDD(prevRed)) {
            notes.remove(2);
            notes.add(nextLinearNote(prevRed, timings[2]._time));
            counter++;
        }
        if (counter >= 300)
            System.err.println("[ERROR] at beat: " + timings[3]._time + " infinite loop in create doubles");
        notes.add(nextLinearNote(notes.get(2), timings[3]._time));


        int invalidPlacementsInARow = 0;
        for (int i = 4; i < timings.length; i += 2) {
            // ERROR handling:
            // Try 100 times to place a normal note. If this doesn't work, then place a Timing-BeatSaberObjects.Objects.Note.
            // If this still doesn't work, then throw an exception
            //Place a BeatSaberObjects.Objects.Note that doesn't break parity after the error:
            if (i >= 4 && invalidPlacementsInARow >= 100) {
                System.err.println("_ERROR at beat:   " + timings[i]._time + " Timing note");
                Note errorNote = new TimingNote(timings[i]._time);
                notes.add(errorNote); //Adding BeatSaberObjects.Objects.Note
                invalidPlacementsInARow = 0;
                continue;
            } else if (invalidPlacementsInARow >= 500)
                throw new IllegalArgumentException("Infinite Loop while creating map! Please try again. (Error occured in \"2-2\")");
            if (i >= 2 && notes.get(notes.size() - 1)._cutDirection == 8) {
                notes.add(nextNoteAfterTimingNote(notes.toArray(notes.toArray(new Note[0])), timings[i]._time, notes.size(), i < 4 ? 2 : 4));
                continue;
            }


            if (i % 4 == 0) {
                Note blue1 = nextLinearNote(notes.get(notes.size() - 3), timings[i]._time);
                if (i >= timings.length - 1) continue;
                Note blue2 = nextLinearNote(notes.get(notes.size() - 1), timings[i + 1]._time);
                if (blue1.isDD(notes.get(notes.size() - 3)) || blue2.isDD(blue1)) {
                    i--;
                    invalidPlacementsInARow++;
                    continue;
                }
                notes.add(blue1);
                notes.add(blue2);
            } else {
                Note red1 = nextLinearNote(notes.get(notes.size() - 3), timings[i]._time);
                if (i >= timings.length - 1) continue;
                Note red2 = nextLinearNote(notes.get(notes.size() - 1), timings[i + 1]._time);
                if (red1.isDD(notes.get(notes.size() - 3)) || red2.isDD(red1)) {
                    i--;
                    invalidPlacementsInARow++;
                    continue;
                }
                notes.add(red1);
                notes.add(red2);
            }
        }

        for (int i = 1; i < notes.size(); i++) if (i % 4 == 2 || i % 4 == 3) notes.get(i).invertNote();

        return notes;

    }

    public static List<Note> randomV2FromTemplate(Note[] timings, Pattern p, boolean stacks, Note prevBlue, Note prevRed) throws IllegalArgumentException {
        List<Note> notes = new ArrayList<>();
        List<Note> randomNotes = createRandomPattern(timings, false);

        List<Note> blueNotes = new ArrayList<>();
        List<Note> redNotes = new ArrayList<>();

        for (Note randomNote : randomNotes) {
            if (randomNote._type == 1) blueNotes.add(randomNote);
            else redNotes.add(randomNote);
        }

        List<Note> blueComplex = Arrays.stream(complexPatternFromTemplate(blueNotes.toArray(new Note[0]), p, true, false, prevBlue, null)).toList();
        List<Note> redComplex = Arrays.stream(complexPatternFromTemplate(redNotes.toArray(new Note[0]), p, true, false, prevRed, null)).toList();
        redComplex.forEach(Note::invertNote);

        for (int i = 0; i < blueNotes.size(); i++) {
            notes.add(new Note(
                    blueComplex.get(i)._time,
                    blueNotes.get(i)._lineIndex == 0 ? 1 : blueNotes.get(i)._lineIndex,
                    blueNotes.get(i)._lineLayer,
                    blueComplex.get(i)._type,
                    blueComplex.get(i)._cutDirection
            ));
            if (!ignoreDDs && i >= 1 && notes.get(i).isDD(notes.get(i - 1))) {
                notes.remove(i);
                i--;
            }
        }

        for (int i = 0; i < redComplex.size(); i++) {
            notes.add(new Note(
                    redComplex.get(i)._time,
                    redNotes.get(i)._lineIndex == 3 ? 2 : redNotes.get(i)._lineIndex,
                    redNotes.get(i)._lineLayer,
                    redComplex.get(i)._type,
                    redComplex.get(i)._cutDirection
            ));

            if (!ignoreDDs && i >= 1 && notes.get(i).isDD(notes.get(i - 1))) {
                notes.remove(i);
                i--;
            }
        }

        if (stacks) notes = createStacks(notes);

        return fixInverts(notes);
    }

    /**
     * Fixes inverted note placements in the provided list of notes. This method adjusts the line layer
     * of notes based on their cut direction and the previous note of the same type.
     *
     * @param notes the list of notes to fix
     * @return the list of notes with fixed invert placements
     */
    private static List<Note> fixInverts(List<Note> notes) {
        Note prevBlue = null;
        Note prevRed = null;

        for (Note n : notes) {
            // Check for inverted blue notes and adjust the line layer if needed
            if ((n._cutDirection == 1 || n._cutDirection == 6 || n._cutDirection == 7) &&
                    (n._type == 1 && prevBlue != null && (prevBlue._cutDirection == 4 || prevBlue._cutDirection == 0 || prevBlue._cutDirection == 5) && n._lineLayer == 2 && prevBlue._lineLayer == 0) ||
                    (n._type == 0 && prevRed != null && (prevRed._cutDirection == 4 || prevRed._cutDirection == 0 || prevRed._cutDirection == 5) && n._lineLayer == 2 && prevRed._lineLayer == 0)) {
                n._lineLayer = 0;
            }
            // Check for inverted red notes and adjust the line layer if needed
            if ((n._cutDirection == 4 || n._cutDirection == 0 || n._cutDirection == 5) &&
                    (n._type == 1 && prevBlue != null && (prevBlue._cutDirection == 1 || prevBlue._cutDirection == 6 || prevBlue._cutDirection == 7) && n._lineLayer == 0 && prevBlue._lineLayer == 2) ||
                    (n._type == 0 && prevRed != null && (prevRed._cutDirection == 1 || prevRed._cutDirection == 6 || prevRed._cutDirection == 7) && n._lineLayer == 0 && prevRed._lineLayer == 2)) {
                n._lineLayer = 2;
            }

            // Update the previous note of the same type
            if (n._type == 1) prevBlue = n;
            if (n._type == 0) prevRed = n;
        }

        return notes;
    }

    /**
     * This methode creates a pattern, where there is one right-hand swing followed by a both-hand swing followed by a right-hand swing.
     * This repeats until the end of timings[] is reached
     *
     * @param timings  where the notes should be placed
     * @param p        p are the probabilities that which note follows which. It must be in the "MapGeneration.GenerationElements.Pattern"-Format
     * @param prevBlue What the previous blue note was
     * @param prevRed  What the previous red note was
     * @param stacks   should stacks be generated?
     * @return A List of all notes that have been generated
     */
    public static List<Note> twoRightOneLeft(Note[] timings, Pattern p, Note prevBlue, Note prevRed, boolean stacks) throws IllegalArgumentException {
        List<Note> redNotes = new ArrayList<>();


        //Right-hand swings:
        Note[] complexPattern = complexPatternFromTemplate(timings, p, true, stacks, prevBlue, null);

        //Define the previous note that came before this function was called
        if (prevRed == null) firstNotePlacement(timings[0]._time);
        redNotes.add(nextLinearNote(prevRed, timings[0]._time));

        //Ensure that there is no DD when creating the first note!
        for (int i = 0; i < 100 && prevRed != null; i++) {
            if (prevRed.isDD(redNotes.get(0))) {
                redNotes.remove(0);
                redNotes.add(nextLinearNote(prevRed, timings[0]._time));
            }
        }

        //Create left-hand swings:
        int invalidPlacementsInARow = 0;
        for (int i = 2; i < complexPattern.length; i += 2) {
            // ERROR handling:
            // Try 100 times to place a normal note. If this doesn't work, then place a Timing-BeatSaberObjects.Objects.Note.
            // If this still doesn't work, then throw an exception
            if (invalidPlacementsInARow >= 100) {
                System.err.println("WARN at beat:    " + timings[i]._time + " There may be a mismatched Note");
//                redNotes.add(new BeatSaberObjects.Objects.TimingNote(timings[i]._time));

                if (i % 2 == 0 && prevRed == null) redNotes.add(new Note(complexPattern[i]._time, 2, 0, 1, 0));
                else if (i % 2 == 1 && prevRed == null) redNotes.add(new Note(complexPattern[i]._time, 2, 0, 1, 1));
                else if (i % 2 == 0)
                    redNotes.add(new Note(complexPattern[i]._time, 2, 0, 1, prevRed.isDD(new Note(0, 0, 0, 0, 1)) ? 0 : 1));
                else
                    redNotes.add(new Note(complexPattern[i]._time, 2, 0, 1, prevRed.isDD(new Note(0, 0, 0, 0, 1)) ? 1 : 0));

                invalidPlacementsInARow = 0;
                continue;
            }
//            else if (invalidPlacementsInARow >= 500)
//                throw new IllegalArgumentException("Infinite Loop while creating map! Please try again.(Error occured in \"1-2 or 2-1\")");

            //Place a BeatSaberObjects.Objects.Note that doesn't break parity after the error:
            if (redNotes.get(redNotes.size() - 1)._cutDirection == 8) {
                if (i >= 4) {
                    redNotes.add(nextNoteAfterTimingNote(redNotes.toArray(redNotes.toArray(new Note[0])), timings[i]._time, redNotes.size(), 1));
                } else redNotes.add(new Note(complexPattern[i]._time, 2, 0, 1, (i % 2 == 0 ? 1 : 0)));

                continue;
            }


            //create note:
            Note n = nextLinearNote(redNotes.get(redNotes.size() - 1), complexPattern[i]._time);

            //If the Notes are placed inside each other or too close to one another, then try again
            if (i >= 2 && (complexPattern[i]._lineIndex == n.getInverted()._lineIndex && complexPattern[i]._lineLayer == n._lineLayer || complexPattern[i - 1]._lineIndex == n.getInverted()._lineIndex && complexPattern[i - 1]._lineLayer == n._lineLayer)) {
                i -= 2;
                invalidPlacementsInARow++;
                continue;
            }

            //Transfer the information about if the note is a stack into the new array
            if (i < timings.length - 1) n.amountOfStackedNotes = timings[i].amountOfStackedNotes;
            redNotes.add(n);


            if (invalidPlacement(redNotes.toArray(new Note[0]), redNotes.size() - 1, true)) {
                i -= 2;
                invalidPlacementsInARow++;
                redNotes.remove(n);
            }
        }


        //Inverting all red Notes so that they are actually red notes LUL
        for (Note n : redNotes) n.invertNote();


        //Creating a list of all notes that should be returned
        //and merging the red notes and the blue notes
        List<Note> allNotes = new ArrayList<>();
        allNotes.addAll(redNotes);
        allNotes.addAll(Arrays.asList(complexPattern));
        Collections.sort(allNotes);

        //Creating the stacks and adding all notes to the final List
        if (stacks) allNotes = createStacks(allNotes);


        return allNotes;
    }

    /**
     * creates a really linear two handed mid-speed pattern
     *
     * @param timings where the notes should be placed
     * @return BeatSaberObjects.Objects.Note []
     */
    public static Note[] linearSlowPattern(Note[] timings, boolean oneHanded, Note prevBlue, Note prevRed) {

        Note[] pattern = new Note[timings.length];
        int j = oneHanded ? 1 : 2;

        //The first 2 notes have to placed manually to ensure that they are not on some random position
        pattern[0] = prevBlue == null ? firstNotePlacement(timings[0]._time) : nextLinearNote(prevBlue, timings[0]._time);
        if (!oneHanded)
            pattern[1] = prevRed == null ? firstNotePlacement(timings[1]._time) : nextLinearNote(prevRed, timings[1]._time);

        for (int i = 0; i < 100; i++) {
            if (!oneHanded)
                if (prevRed != null && prevRed.isDD(pattern[1])) pattern[1] = nextLinearNote(prevRed, timings[1]._time);
            if (prevBlue != null && prevBlue.isDD(pattern[0])) pattern[0] = nextLinearNote(prevBlue, timings[0]._time);
        }

        int invalidPlacesInARow = 0;
        for (int i = j; i < timings.length; i++) {

            //manual error handling:
            //When there exists an infinite loop:
            //Then create a new next note
            if ((oneHanded && i >= 2 || i >= 4) && invalidPlacesInARow >= 500) {
                System.err.println("[ERROR] at beat:   " + timings[i]._time);
                pattern[i] = new TimingNote(timings[i]._time);
                invalidPlacesInARow = 0;
                continue;
            } else if (invalidPlacesInARow >= 500)
                throw new IllegalArgumentException("Infinite Loop while creating map! Please try again.");
            if ((oneHanded && i >= 2 || i >= 4) && pattern[i - j]._cutDirection == 8) {
                pattern[i] = nextNoteAfterTimingNote(pattern, timings[i]._time, i, j);
                continue;
            } //<-- next note after the error


            //calculate note:
            pattern[i] = nextLinearNote(pattern[i - j], timings[i]._time);

            //Check if this note's placement valid
            if (i >= 4 * j && invalidPlacement(pattern, i, false)) {
                pattern[i] = null;
                i--;
                invalidPlacesInARow++;
            }
        }

        //make every second note a red note
        if (!oneHanded) for (int i = 1; i < pattern.length; i += 2) pattern[i].invertNote();


        return pattern;
    }

    /**
     * This function creates a jump-pattern where the jumps are not that big
     *
     * @param timings   List of note timings
     * @param oneHanded should the jumps be one handed?
     * @param prevBlue  previous blue note so that there is no parity break
     * @param prevRed   previous blue note so that there is no parity break
     * @return returns a jump-pattern with not-so-big swings
     */
    public static List<Note> createSmallJumps(List<Note> timings, boolean oneHanded, Note prevBlue, Note prevRed) {
        List<Note> notes = new ArrayList<>();

        for (int i = 0; i < timings.size(); i++) {

            //If the pattern should be one handed OR every second note
            if (i % 2 == 0 || oneHanded) {
                //When there is an upper cut
                if (prevBlue == null || prevBlue._cutDirection == 0 || prevBlue._cutDirection == 2 || prevBlue._cutDirection == 3 || prevBlue._cutDirection == 4 || prevBlue._cutDirection == 5 || prevBlue._cutDirection == 8)
                    notes.add(new Note(timings.get(i)._time, 2, 0, 1, 1));

                    //When there is a down-cut
                else if (prevBlue._cutDirection == 1 || prevBlue._cutDirection == 6 || prevBlue._cutDirection == 7)
                    notes.add(new Note(timings.get(i)._time, 3, 1, 1, 5));

                    //error catching
                else notes.add(new TimingNote(timings.get(i)._time));


                prevBlue = notes.get(notes.size() - 1);

            } else {
                //Every second note should be red
                if (prevRed == null || prevRed._cutDirection == 0 || prevRed._cutDirection == 2 || prevRed._cutDirection == 3 || prevRed._cutDirection == 4 || prevRed._cutDirection == 5 || prevRed._cutDirection == 8)
                    notes.add(new Note(timings.get(i)._time, 2, 0, 1, 1));

                    //When there is a down-cut
                else if (prevRed._cutDirection == 1 || prevRed._cutDirection == 6 || prevRed._cutDirection == 7)
                    notes.add(new Note(timings.get(i)._time, 3, 1, 1, 5));

                    //error catching
                else notes.add(new TimingNote(timings.get(i)._time));


                //Setting the previous note:
                prevRed = notes.get(notes.size() - 1);

                //making the previous note red:
                notes.get(notes.size() - 1).invertNote();
            }
        }
        return notes;
    }

    /**
     * This function creates a jump-pattern where the jumps are "normally-big". That means, there is 1 air block in between the jumps
     *
     * @param timings   List of note timings
     * @param oneHanded should the jumps be one handed?
     * @param prevBlue  previous blue note so that there is no parity break
     * @param prevRed   previous blue note so that there is no parity break
     * @return returns a jump-pattern with not-so-big swings
     */
    public static List<Note> createJumps(List<Note> timings, boolean oneHanded, Note prevBlue, Note prevRed) {
        List<Note> notes = createSmallJumps(timings, oneHanded, prevBlue, prevRed);
        for (Note n : notes) if (n._lineLayer == 1) n._lineLayer++;

        return notes;
    }

    /**
     * This function creates a jump-pattern where the jumps are huge. Use with caution!
     *
     * @param timings   List of note timings
     * @param oneHanded should the jumps be one handed?
     * @param prevBlue  previous blue note so that there is no parity break
     * @param prevRed   previous blue note so that there is no parity break
     * @return returns a jump-pattern with not-so-big swings
     */
    public static List<Note> createBigJumps(List<Note> timings, boolean oneHanded, Note prevBlue, Note prevRed) {
        List<Note> notes = createJumps(timings, oneHanded, prevBlue, prevRed);
        for (Note n : notes) {
            if (n._lineLayer == 2) n._lineLayer--;
            if (n._type == 1) {
                if (n._cutDirection == 1) {
                    n._lineIndex = 0;
                    n._cutDirection = 6;
                } else {
                    n._lineIndex = 3;
                    n._cutDirection = 5;
                }
            } else if (n._type == 0) {
                if (n._cutDirection == 1) {
                    n._lineIndex = 3;
                    n._cutDirection = 7;
                } else {
                    n._lineIndex = 0;
                    n._cutDirection = 4;
                }
            }
        }

        return notes;
    }

    /**
     * This function checks parity and prints an error, if there is a dd somewhere
     *
     * @param notes List of notes that should be checked
     * @param quiet Should this function display error messages?
     */
    public static void checkParity(List<Note> notes, boolean quiet) {
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
     * This function creates stacks for every note in notes. Stacks will only be placed if the flag has been set.
     * The flag can be set with: note.amountOfStackedNotes = 2
     *
     * @param notes all the notes that should be looked at to create stacks. Doesn't guarantee that a stack will be placed!
     * @return a List of all notes including stacks
     */
    public static List<Note> createStacks(List<Note> notes) {
        List<Note> toReturn = new ArrayList<>();
        for (Note n : notes) {
            toReturn.addAll(List.of(n.createStackedNote()));
        }

        //Check if there is a note inside another note
        return checkForMappingErrors(toReturn, true);
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

        checkParity(allNotes, quiet);

        return allNotes;
    }

    /**
     * This function tries to avoid parity breaks when a horizontal segment is coming to an end.
     *
     * @param pattern pattern [] is the array, where the previous notes are saved.
     * @param i       i specifies at which element the last note has been placed.
     * @param j       j... If the pattern is one handed: j = 1. If two handed: j = 2.
     * @return BeatSaberObjects.Objects.Note
     */
    public static Note endHorizontalPlacements(Note[] pattern, int i, int j) {
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
            if (pattern[k]._cutDirection != 2 && pattern[k]._cutDirection != 3) {
                firstHorizontalCutDirection = pattern[k]._cutDirection;
                secondHorizontalCutDirection = pattern[k + j]._cutDirection;
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
//                    if (debug) System.out.println("Before-dir: " + firstHorizontalCutDirection);
                    if (random <= 50) return new Note(pattern[i]._time, 3, 0, 1, 7);
                    else return new Note(pattern[i]._time, 3, 1, 1, 7);
                }

                //first: top right swing
                if (firstHorizontalCutDirection == 5 || (firstHorizontalCutDirection == 0 && secondHorizontalCutDirection == 2)) {
//                    if (debug) System.out.println("Before-dir: " + firstHorizontalCutDirection);
                    if (random <= 50) return new Note(pattern[i]._time, 2, 0, 1, 6);
                    else return new Note(pattern[i]._time, 1, 0, 1, 6);
                }

                //first: bottom left swing
                if (firstHorizontalCutDirection == 6 || (firstHorizontalCutDirection == 1 && secondHorizontalCutDirection == 3)) {
//                    if (debug) System.out.println("Before-dir: " + firstHorizontalCutDirection);
                    return new Note(pattern[i]._time, 3, 2, 1, 5);
                }

                //first: bottom right swing
                if (firstHorizontalCutDirection == 7 || (firstHorizontalCutDirection == 1 && secondHorizontalCutDirection == 2)) {
//                    if (debug) System.out.println("Before-dir: " + firstHorizontalCutDirection);
                    return new Note(pattern[i]._time, 2, 1, 1, 4);
                }
            }
            case 1 -> {
                //first: top left swing
                if (firstHorizontalCutDirection == 4 || (firstHorizontalCutDirection == 0 && secondHorizontalCutDirection == 3)) {
//                    if (debug) System.out.println("Before-dir: " + firstHorizontalCutDirection);
                    return new Note(pattern[i]._time, 2, 1, 1, 4);
                }

                //first: top right swing
                if (firstHorizontalCutDirection == 5 || (firstHorizontalCutDirection == 0 && secondHorizontalCutDirection == 2)) {
//                    if (debug) System.out.println("Before-dir: " + firstHorizontalCutDirection);
                    return new Note(pattern[i]._time, 3, 2, 1, 5);
                }

                //first: bottom left swing
                if (firstHorizontalCutDirection == 6 || (firstHorizontalCutDirection == 1 && secondHorizontalCutDirection == 3)) {
//                    if (debug) System.out.println("Before-dir: " + firstHorizontalCutDirection);
                    if (random <= 50) return new Note(pattern[i]._time, 2, 0, 1, 6);
                    else return new Note(pattern[i]._time, 1, 0, 1, 6);
                }

                //first: bottom right swing
                if (firstHorizontalCutDirection == 7 || (firstHorizontalCutDirection == 1 && secondHorizontalCutDirection == 2)) {
//                    if (debug) System.out.println("Before-dir: " + firstHorizontalCutDirection);
                    if (random <= 50) return new Note(pattern[i]._time, 3, 0, 1, 7);
                    else return new Note(pattern[i]._time, 3, 1, 1, 7);
                }
            }
        }


        System.err.println("Check parity at: " + pattern[i]._time);
        return null;
    }

    /**
     * If there was an error, a timing note is being placed.
     * This function tries to see which note came before the error and places a note accordingly, which does not break parity.
     *
     * @param pattern pattern [] is the array, where the previous notes are saved.
     * @param time    time specifies on which bpm the note should be placed.
     * @param i       i specifies at which element the last note has been placed.
     * @param j       j... If the pattern is one handed: j = 1. If two handed: j = 2.
     * @return BeatSaberObjects.Objects.Note
     */
    public static Note nextNoteAfterTimingNote(Note[] pattern, float time, int i, int j) {
        Note toReturn = firstNotePlacement(time);

        //When second last note was an up swing:
        if (pattern[i - 2 * j]._cutDirection == 6 || pattern[i - 2 * j]._cutDirection == 1 || pattern[i - 2 * j]._cutDirection == 7)
            toReturn._cutDirection = 1;

        //When second last note was a down swing:
        if (pattern[i - 2 * j]._cutDirection == 4 || pattern[i - 2 * j]._cutDirection == 0 || pattern[i - 2 * j]._cutDirection == 5)
            toReturn._cutDirection = 0;


        //When second last note was a horizontal swing:
        if (pattern[i - j]._cutDirection == 2 || pattern[i - j]._cutDirection == 3)
            toReturn._cutDirection = 1;

        return toReturn;
    }

    /**
     * This function predicts the next note based the probabilities of the pattern
     *
     * @param pattern this is probability of the patterns from a map saved as the PatternProbability class
     * @param time    time specifies on which bpm the note should be placed.
     * @return BeatSaberObjects.Objects.Note
     */
    public static Note predictNextNote(PatternProbability pattern, float time) {
        if (pattern == null || pattern.notes == null) return null;

        float currentProbability = 0;
        double placement = RANDOM.nextDouble() * 100;

        for (int i = 0; i < pattern.probabilities.length; i++) {
            if (pattern.notes[i] == null || currentProbability > 99) return null;
            currentProbability += pattern.probabilities[i];
            if (placement <= currentProbability) {
                Note n = pattern.notes[i];
                return new Note(time, n._lineIndex, n._lineLayer, n._type, n._cutDirection);
            }

        }

        return null;
    }

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

    /**
     * This function creates a note based on the previous note that doesn't break parity.
     * It only creates really linear patterns
     *
     * @param previousNote the note that came before.
     * @param time         time specifies on which bpm the note should be placed.
     * @return BeatSaberObjects.Objects.Note
     */
    public static Note nextLinearNote(Note previousNote, float time) {
        Note p = previousNote; //p is much cleaner than having a thousand times previousNote

        if (p == null) {
            System.err.println("Something went wrong. A note is null :thinking: Please have a look at beat: " + time);
            p = new Note(time, 0, 0, 1, 1);
        }

        double placement = RANDOM.nextDouble() * 100;


        //blue bottom-middle-right lane, down swing
        //2,0,1
        if (p._lineIndex == 2 && p._lineLayer == 0 && p._type != 2 && p._cutDirection == 1) {
//                if (placement < 10) return new BeatSaberObjects.Objects.Note(time,3,1,1,3);
            if (placement < 20) return new Note(time, 3, 1, 1, 5);
            else if (placement < 30) return new Note(time, 3, 2, 1, 0);
            else if (placement < 50) return new Note(time, 3, 2, 1, 5);
            else if (placement < 59) return new Note(time, 3, 0, 1, 0);
            else if (placement < 68) return new Note(time, 1, 0, 1, 0);
            else return new Note(time, 2, 2, 1, 0);
        }

        //blue middle-right lane, right swing
        //3,1,3
        else if ((p._lineIndex == 3 && p._lineLayer == 1 && p._type != 2 && p._cutDirection == 3)) {
            if (placement < 70) return new Note(time, 1, 0, 1, 6);
            else return new Note(time, 2, 0, 1, 1);
        }

        //blue upper-right lane, top-right swing
        //3,2,5
        else if (p._lineIndex == 3 && p._lineLayer == 2 && p._type != 2 && p._cutDirection == 5) {
            if (placement < 5) return new Note(time, 0, 0, 1, 6);
            else if (placement < 40) return new Note(time, 1, 0, 1, 6);
            else if (placement < 90) return new Note(time, 2, 0, 1, 1);
            else return new Note(time, 3, 0, 1, 1);
        }

        //blue upper-right lane, top swing
        //3,2,0
        else if (p._lineIndex == 3 && p._lineLayer == 2 && p._type != 2 && p._cutDirection == 0) {
            if (placement <= 50) return new Note(time, 2, 0, 1, 1);
            else return new Note(time, 3, 0, 1, 1);
        }

        //blue upper-middle-right lane, top swing
        //2,2,0
        else if (p._lineIndex == 2 && p._lineLayer == 2 && p._type != 2 && p._cutDirection == 0) {
            if (placement < 5) return new Note(time, 3, 0, 1, 7);
            else if (placement < 20) return new Note(time, 3, 0, 1, 1);
            else if (placement < 55) return new Note(time, 1, 0, 1, 6);
            else return new Note(time, 2, 0, 1, 1);
        }

        //blue bottom-middle-right lane, bottom-left swing
        //3,1,6
        else if (p._lineIndex == 3 && p._lineLayer == 1 && p._type != 2 && p._cutDirection == 6) {
            if (placement < 40) return new Note(time, 1, 0, 1, 6);
            else if (placement < 80) return new Note(time, 2, 0, 1, 6);
            else return new Note(time, 2, 0, 1, 1);
        }

        //blue bottom-middle-left lane, bottom-left swing
        //1,0,6
        else if (p._lineIndex == 1 && p._lineLayer == 0 && p._type != 2 && p._cutDirection == 6) {
            if (placement < 38) return new Note(time, 3, 2, 1, 5);
            if (placement < 43) return new Note(time, 2, 0, 1, 5);
            else if (placement < 81) return new Note(time, 3, 1, 1, 5);
            else if (placement < 85) return new Note(time, 2, 2, 1, 0);
            else return new Note(time, 2, 2, 1, 5);
        }

        //blue bottom-left lane, bottom-left swing
        //0,0,6
        else if (p._lineIndex == 0 && p._lineLayer == 0 && p._type != 2 && p._cutDirection == 6) {
            if (placement < 30) return new Note(time, 2, 2, 1, 5);
            else if (placement < 80) return new Note(time, 3, 2, 1, 5);
            else if (placement < 83) return new Note(time, 3, 1, 1, 5);
            else return new Note(time, 3, 1, 1, 3);
        }

        //blue bottom-middle-right lane, bottom-left swing
        //2,0,6
        else if (p._lineIndex == 2 && p._lineLayer == 0 && p._type != 2 && p._cutDirection == 6) {
            if (placement <= 40) return new Note(time, 3, 1, 1, 5);
            if (placement <= 60) return new Note(time, 3, 2, 1, 0);
            else return new Note(time, 3, 2, 1, 5);
        }

        //blue top-middle-right lane, top-right swing
        //2,2,5
        else if (p._lineIndex == 2 && p._lineLayer == 2 && p._type != 2 && p._cutDirection == 5) {
            if (placement <= 20) return new Note(time, 0, 0, 1, 6);
            else return new Note(time, 1, 0, 1, 6);
        }

        //blue bottom-right lane, bottom swing
        //3,0,1
        else if (p._lineIndex == 3 && p._lineLayer == 0 && p._type != 2 && p._cutDirection == 1) {
            if (placement <= 40) return new Note(time, 3, 1, 1, 0);
            if (placement <= 50) return new Note(time, 3, 0, 1, 0);
            if (placement <= 60) return new Note(time, 2, 0, 1, 0);
            else return new Note(time, 3, 2, 1, 0);
        }

        //blue middle-right lane, top swing
        //3,1,0
        else if (p._lineIndex == 3 && p._lineLayer == 1 && p._type != 2 && p._cutDirection == 0) {
            if (placement <= 5) return new Note(time, 3, 1, 1, 1);
            if (placement <= 55) return new Note(time, 3, 0, 1, 1);
            else return new Note(time, 2, 0, 1, 1);
        }

        //blue top-left-middle lane, top-left swing
        //1,2,4
        else if (p._lineIndex == 1 && p._lineLayer == 2 && p._type != 2 && p._cutDirection == 4) {
            if (placement <= 50) return new Note(time, 3, 0, 1, 7);
            else return new Note(time, 2, 0, 1, 1);
        }

        //blue middle-right lane, top-right swing
        //3,1,5
        else if (p._lineIndex == 3 && p._lineLayer == 1 && p._type != 2 && p._cutDirection == 5) {
            if (placement <= 60) return new Note(time, 2, 0, 1, 6);
            else return new Note(time, 1, 0, 1, 6);
        }

        //blue bottom-right lane, bottom-right swing
        //3,0,7
        else if (p._lineIndex == 3 && p._lineLayer == 0 && p._type != 2 && p._cutDirection == 7) {
            if (placement <= 10) return new Note(time, 3, 2, 1, 0);
            else return new Note(time, 2, 2, 1, 0);
        }

        //blue top-right-middle lane, bottom-right swing
        //3,1,7
        else if (p._lineIndex == 3 && p._lineLayer == 1 && p._type != 2 && p._cutDirection == 7) {
            return new Note(time, 2, 2, 1, 4);
        }

        //blue middle-right lane, bottom-right swing
        //3,0,1
        else if (p._lineIndex == 3 && p._lineLayer == 1 && p._type != 2 && p._cutDirection == 1) {
            if (placement <= 30) return new Note(time, 3, 1, 1, 0);
            else return new Note(time, 3, 2, 1, 0);
        }

        //blue right-bottom lane, bottom swing
        //3,0,0
        else if (p._lineIndex == 3 && p._lineLayer == 0 && p._type != 2 && p._cutDirection == 0) {
            if (placement <= 35) return new Note(time, 3, 0, 1, 1);
            else if (placement <= 80) return new Note(time, 2, 0, 1, 1);
            else if (placement <= 83) return new Note(time, 1, 0, 1, 6);
            else return new Note(time, 2, 0, 1, 6);
        }

        //blue bottom-right-middle lane, bottom swing
        //3,0,0
        else if (p._lineIndex == 2 && p._lineLayer == 0 && p._type != 2 && p._cutDirection == 0) {
            if (placement <= 35) return new Note(time, 3, 0, 1, 1);
            else if (placement <= 40) return new Note(time, 3, 0, 1, 7);
            else if (placement <= 80) return new Note(time, 2, 0, 1, 1);
            else if (placement <= 83) return new Note(time, 1, 0, 1, 6);
            else return new Note(time, 2, 0, 1, 6);
        }

        //blue bottom-right-middle lane,  top-right swing
        //2,0,5
        else if (p._lineIndex == 2 && p._lineLayer == 0 && p._type != 2 && p._cutDirection == 5) {
            return new Note(time, 3, 0, 1, 1);
        }

        //blue bottom-left-middle lane, top swing
        //1,0,0
        else if (p._lineIndex == 1 && p._lineLayer == 0 && p._type != 2 && p._cutDirection == 0) {
            if (placement <= 33) return new Note(time, 2, 0, 1, 1);
            else if (placement <= 66) return new Note(time, 3, 0, 1, 1);
            else return new Note(time, 3, 0, 1, 7);
        }

        //blue bottom-left-middle lane, bottom swing
        //1,0,0
        else if (p._lineIndex == 1 && p._lineLayer == 0 && p._type != 2 && p._cutDirection == 1) {
            if (placement <= 33) return new Note(time, 2, 0, 1, 1);
            else if (placement <= 55) return new Note(time, 2, 2, 1, 0);
            else if (placement <= 70) return new Note(time, 2, 2, 1, 5);
            else return new Note(time, 3, 2, 1, 5);
        }

        //blue top-right lane, right swing
        //3,2,3
        else if (p._lineIndex == 3 && p._lineLayer == 2 && p._type != 2 && p._cutDirection == 3) {
            if (placement <= 20) return new Note(time, 2, 2, 1, 2);
            else if (placement <= 45) return new Note(time, 1, 2, 1, 2);
            else if (placement <= 75) return new Note(time, 1, 0, 1, 6);
            else return new Note(time, 2, 0, 1, 6);
        }

        //blue top-right-middle lane, right swing
        //2,2,3
        else if (p._lineIndex == 2 && p._lineLayer == 2 && p._type != 2 && p._cutDirection == 3) {
            if (placement <= 5) return new Note(time, 2, 2, 1, 2);
            else if (placement <= 55) return new Note(time, 1, 2, 1, 2);
            else if (placement <= 64) return new Note(time, 0, 2, 1, 2);
            else return new Note(time, 1, 0, 1, 6);
        }

        //blue top-right-middle lane,  left swing
        //2,2,2
        else if (p._lineIndex == 2 && p._lineLayer == 2 && p._type != 2 && p._cutDirection == 2) {
            if (placement <= 40) return new Note(time, 3, 2, 1, 3);
            else return new Note(time, 3, 2, 1, 5);
        }

        //blue top-left-middle lane, left swing
        //1,2,2
        else if (p._lineIndex == 1 && p._lineLayer == 2 && p._type != 2 && p._cutDirection == 2) {
            if (placement <= 40) return new Note(time, 3, 2, 1, 3);
            if (placement <= 75) return new Note(time, 2, 2, 1, 3);
            else return new Note(time, 3, 2, 1, 5);
        }

        //blue top-right lane,  bottom-right swing
        //3,2,7
        else if (p._lineIndex == 3 && p._lineLayer == 2 && p._type != 2 && p._cutDirection == 7) {
            if (placement <= 20) return new Note(time, 3, 2, 1, 4);
            if (placement <= 60) return new Note(time, 2, 2, 1, 4);
            else return new Note(time, 1, 2, 1, 4);
        }


        //error catching:
        //If I forgot to add a note,it will be displayed here:
        else {
//            System.out.println(p.toString().replaceAll("\n",""));
//            System.err.println("THERE WAS AN UNDETECTED NOTE!");
            if (p._type != 2 && (p._cutDirection == 1 || p._cutDirection == 6 || p._cutDirection == 2))
                return new Note(time, 3, 2, 1, 5);
            else if (p._type != 2 && (p._cutDirection == 7 || p._cutDirection == 3))
                return new Note(time, 2, 2, 1, 4);
            else if (p._type != 4 && (p._cutDirection == 0 || p._cutDirection == 5 || p._cutDirection == 4))
                return new Note(time, 0, 2, 1, 1);

            throw new IllegalArgumentException("There is an undetected note!");
        }
    }

    /**
     * Removes notes that are too close in timing to a previous note of the same type.
     * This method collects all notes that should be removed first and then removes them in a separate operation.
     *
     * @param notes A list of Note objects, each representing a musical note with a type and timing.
     */
    private static List<Note> removeStacks(List<Note> notes) {
        List<Note> toRemove = new ArrayList<>(); // List to hold notes that need to be removed
        float lastBlueTime = 0;  // Tracks the last time a blue note was placed
        float lastRedTime = 0;   // Tracks the last time a red note was placed

        // Iterate through the list of notes
        for (Note note : notes) {
            // Initialize the last time for the first blue note
            if (lastBlueTime == 0 && note._type == 0) {
                lastBlueTime = note._time;
                continue;
            }
            // Initialize the last time for the first red note
            if (lastRedTime == 0 && note._type == 1) {
                lastRedTime = note._time;
                continue;
            }

            // Check if the current blue note is too close to the last
            if (note._type == 0) {
                if (note._time - lastBlueTime <= (float) 1 / 8) {
                    toRemove.add(note); // Add to removal list if too close
                } else {
                    lastBlueTime = note._time; // Update last blue time
                }
            } else {
                // Check if the current red note is too close to the last
                if (note._time - lastRedTime <= (float) 1 / 8) {
                    toRemove.add(note); // Add to removal list if too close
                } else {
                    lastRedTime = note._time; // Update last red time
                }
            }
        }

        notes.removeAll(toRemove); // Remove all collected notes at once
        return toRemove;
    }

    /**
     * This method processes a list of notes to determine the number of stacked notes for each note.
     * A note is considered stacked if another note of the same type occurs within 1/8 of a time unit.
     *
     * @param notes           The list of notes to be processed for stacking.
     * @param stackPlacements The list of notes used to determine stack placements.
     * @return The list of notes with updated stack counts.
     */
    private static List<Note> placeStacks(List<Note> notes, List<Note> stackPlacements) {
        // Iterate through each note in the list of notes
        for (Note n : notes) {
            // For each note, compare it with each note in the stackPlacements list
            for (Note stackPlacement : stackPlacements) {
                // Calculate the time difference between the current note and the stack placement note
                float timeDiff = stackPlacement._time - n._time;
                // Check if the time difference is within the range of 0 to 1/8 (inclusive)
                // and if the notes are of the same type
                if (timeDiff >= 0 && timeDiff <= (float) 1 / 8 && n._type == stackPlacement._type) {
                    // Increment the stack count for the current note
                    n.amountOfStackedNotes++;
                }
            }
        }
        // Call the createStacks method to finalize and return the list of notes with stacks
        return createStacks(notes);
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
     * @param notes     notes [] is the array, where the previous notes are saved.
     * @param i         i specifies at which element the last note has been placed.
     * @param oneHanded is the map a one handed map.
     * @return boolean
     */
    public static boolean invalidPlacement(Note[] notes, int i, boolean oneHanded) {
        if (notes.length <= 2) return false;
        if (i < 4) return false;
        if (notes[i - 1] == null || notes[i] == null) return true;

        int j = 2;
        if (oneHanded) j = 1;

        if (notes[i]._lineIndex < 0 || notes[i]._lineIndex >= 4 || notes[i]._lineLayer < 0 || notes[i]._lineLayer >= 3)
            return false;


        //DD:
        if (notes[i - j]._cutDirection == notes[i]._cutDirection
                || (notes[i - j]._cutDirection == 6 || notes[i - j]._cutDirection == 1 || notes[i - j]._cutDirection == 7) && (notes[i]._cutDirection == 6 || notes[i]._cutDirection == 1 || notes[i]._cutDirection == 7)
                || (notes[i - j]._cutDirection == 7 || notes[i - j]._cutDirection == 3 || notes[i - j]._cutDirection == 5) && (notes[i]._cutDirection == 7 || notes[i]._cutDirection == 3 || notes[i]._cutDirection == 5)
                || (notes[i - j]._cutDirection == 4 || notes[i - j]._cutDirection == 0 || notes[i - j]._cutDirection == 5) && (notes[i]._cutDirection == 4 || notes[i]._cutDirection == 0 || notes[i]._cutDirection == 5)
                || (notes[i - j]._cutDirection == 4 || notes[i - j]._cutDirection == 2 || notes[i - j]._cutDirection == 6) && (notes[i]._cutDirection == 4 || notes[i]._cutDirection == 2 || notes[i]._cutDirection == 6)
        ) return true;

        //weird top row notes
        if (notes[i - j]._cutDirection == 0 && notes[i]._cutDirection == 6 && notes[i - j]._lineLayer == 2 && notes[i]._lineLayer >= 1 && notes[i - j]._lineIndex <= 2 && notes[i]._lineLayer >= 2)
            return true;

        //Vision block
        if (notes[i]._lineIndex == 2 && notes[i]._lineLayer == 1) return true;

        //Only place the note, if the previous note is not placed directly in front of it
        //If one-handed it true, then we can just skip this step.
        if (oneHanded) return false;
        return notes[i - 1]._lineIndex == notes[i].getInverted()._lineIndex && notes[i - 1]._lineLayer == notes[i]._lineLayer;
    }
}