import com.google.gson.Gson;

import java.util.*;

public class CreatePatterns {
    public static void main(String[] args) {
        String filename = "Input.txt";
        String patternFilename = "PatternTemplates/Template--ISeeFire.txt";
        String outPath = "";
        String input = CreateTimings.readFile(filename).get(0);
        String patternInput = CreateTimings.readFile(patternFilename).get(0);

        Gson gson = new Gson();
        BeatSaberMap timings = gson.fromJson(input, BeatSaberMap.class);
        BeatSaberMap patterns = gson.fromJson(patternInput, BeatSaberMap.class);
        timings._events = new Events[0];

        //create pattern from the map:
        Pattern p = new Pattern(patterns._notes, 1);
        timings.toBlueLeftBottomRowDotTimings();

//        System.out.println(new BeatSaberMap(complexPatternFromTemplate(timings._notes, p, false, null, null)).exportAsMap());
//        System.out.println(new BeatSaberMap(linearSlowPattern(timings._notes)).exportAsMap());

        System.out.println(new BeatSaberMap(twoRightOneLeft(timings._notes, p, null, null, true)).exportAsMap());
        //new Note((float) 7.5, 1, 0, 1, 1)
    }

    //TODO: Stacked notes. Theoretically they SHOULD work...

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
     * creates a really linear two handed mid-speed pattern
     *
     * @param timings where the notes should be placed
     * @return Note []
     */
    public static Note[] linearSlowPattern(Note[] timings) {
        Note[] pattern = new Note[timings.length];

        //The first 2 notes have to placed manually to ensure that they are not on some random position
        for (int i = 0; i < 2; i++) {
            pattern[i] = firstNotePlacement(pattern[i]._time);
        }

        for (int i = 2; i < timings.length; i++) {
            pattern[i] = nextLinearNote(pattern[i - 2], timings[i]._time);

            if (!validPlacement(pattern, i, false) && i > 4) {
                pattern[i] = null;
                i--;
            }
        }
        for (int i = 1; i < pattern.length; i += 2) {
            pattern[i].invertNote();
        }

        return pattern;
    }


    /**
     * This methode creates a pattern on basis of the original Pattern.
     *
     * @param timings   where the notes should be placed
     * @param p         p are the probabilities that which note follows which. It must be in the "Pattern"-Format
     * @param oneHanded is the map one handed?
     * @param prevBlue  What the previous blue note was
     * @param prevRed   What the previous red note was
     * @return A List of all notes that have been generated
     */
    public static Note[] complexPatternFromTemplate(Note[] timings, Pattern p, boolean oneHanded, Note prevBlue, Note prevRed) {
        Note[] pattern = new Note[timings.length];
        int j = oneHanded ? 1 : 2;

        //Placing the first notes manually:
        pattern[0] = prevBlue != null ? nextLinearNote(prevBlue, timings[0]._time) : firstNotePlacement(timings[0]._time);
        if (!oneHanded) pattern[1] = prevRed != null ? nextLinearNote(prevRed, timings[1]._time) : firstNotePlacement(timings[1]._time);

        int blueHorizontalsInARow = 0; //prevent parity breaks for red notes
        int redHorizontalsInARow = 0; //prevent parity breaks for red notes
        int invalidPlacesInARow = 0; //prevent infinite loops
        for (int i = j; i < timings.length; i++) {
            boolean inValidPlacement = false;

            //manual error handling:
            //When there exists an infinite loop:
            //Then create a new next note
            if (i >= 8 && invalidPlacesInARow >= 500) {
                System.err.println("ERROR at beat: " + timings[i]._time);
                pattern[i] = new TimingNote(timings[i]._time);
                invalidPlacesInARow = 0;
                continue;
            } else if (invalidPlacesInARow >= 500)
                throw new IllegalArgumentException("Infinite Loop while creating map! Please try again.");
            if (i >= 8 && pattern[i - j]._cutDirection == 8) {
                pattern[i] = nextNoteAfterTimingNote(pattern, timings[i]._time, i, j);
                continue;
            } //<-- next note after the error


            Note previous = pattern[i - j];
            PatternProbability probabilities = p.getProbabilityOf(previous);

            //Generate a note according to the template
            //If there is an infinite loop,then try to place a linear note
            if (probabilities == null || invalidPlacesInARow >= 100) pattern[i] = nextLinearNote(previous, timings[i]._time);
            else pattern[i] = predictNextNote(probabilities, timings[i]._time);


            //check, if the placement is valid (example: dd)
            if (!validPlacement(pattern, i, oneHanded)) inValidPlacement = true;
            if (inValidPlacement && i > 4) {
                pattern[i] = null;
                i--;
                invalidPlacesInARow++;
                continue;
            } else invalidPlacesInARow = 0;


            //check if the horizontal placement is correct or if there is a parity break.
            //For further info have a look at: endHorizontalPlacements()
            if ((redHorizontalsInARow >= 2 || blueHorizontalsInARow >= 2) && (pattern[i]._cutDirection != 2 && pattern[i]._cutDirection != 3)) {
                Note noteAfterHorizontal = endHorizontalPlacements(pattern, i, j);
                pattern[i] = noteAfterHorizontal != null ? noteAfterHorizontal : pattern[i];
                if (i % 2 == 0) blueHorizontalsInARow = 0;
                if (i % 2 == 1) redHorizontalsInARow = 0;
            }
            if (i % 2 == 0 && (pattern[i]._cutDirection == 2 || pattern[i]._cutDirection == 3)) blueHorizontalsInARow++;
            if (i % 2 == 1 && (pattern[i]._cutDirection == 2 || pattern[i]._cutDirection == 3) && !oneHanded) redHorizontalsInARow++;

            //creating the flag, so that a stack may be done later;
            pattern[i].amountOfStackedNotes = timings[i].amountOfStackedNotes;
        }

        //make every second note red:
        if (!oneHanded) for (int i = 1; i < pattern.length; i += 2) pattern[i].invertNote();

        //TODO: This may break things:
        //Check, if one note is inside another note
        List<Note> l = new ArrayList(List.of(pattern));
        pattern = checkForNoteInNote(l);

        return pattern;
    }

    /**
     * This methode creates a pattern, where there is one right-hand swing followed by a both-hand swing followed by a right-hand swing.
     * This repeats until the end of timings[] is reached
     *
     * @param timings  where the notes should be placed
     * @param p        p are the probabilities that which note follows which. It must be in the "Pattern"-Format
     * @param prevBlue What the previous blue note was
     * @param prevRed  What the previous red note was
     * @param stacks   should stacks be generated?
     * @return A List of all notes that have been generated
     */
    public static List<Note> twoRightOneLeft(Note[] timings, Pattern p, Note prevBlue, Note prevRed, boolean stacks) {
        List<Note> redNotes = new ArrayList<>();


        prevRed = prevRed == null ? firstNotePlacement(timings[0]._time) : nextLinearNote(prevRed, timings[0]._time);
        redNotes.add(prevRed);

        Note[] complexPattern = complexPatternFromTemplate(timings, p, true, prevBlue, null);

        int invalidPlacementsInARow = 0;
        for (int i = 2; i < complexPattern.length; i += 2) {
            boolean validPlacement = true;

            // ERROR handling:
            // Try 100 times to place a normal note. If this doesn't work, then place a Timing-Note.
            // If this still doesn't work, then throw an exception
            if (i >= 8 && invalidPlacementsInARow >= 100) {
                System.err.println("ERROR at beat: " + timings[i]._time);
                redNotes.add(new TimingNote(timings[i]._time));
                invalidPlacementsInARow = 0;
                continue;
            } else if (invalidPlacementsInARow >= 500)
                throw new IllegalArgumentException("Infinite Loop while creating map! Please try again.");

            //Place a Note that doesn't break parity after the error:
            if (i >= 8 && redNotes.get(redNotes.size() - 1)._cutDirection == 8) {
                redNotes.add(nextNoteAfterTimingNote(redNotes.toArray(redNotes.toArray(new Note[0])), timings[i]._time, redNotes.size(), 1));
                continue;
            }


            Note n = nextLinearNote(redNotes.get(redNotes.size() - 1), complexPattern[i]._time);

            Note[] stackedNotes = complexPattern[i].createStackedNote();
//            for (Note s : stackedNotes) if (s.invertNote().equalNotePlacement(n)) validPlacement = false;


            //If the Notes are placed inside each other or too close to one another, then try again
            if (i >= 2 && (complexPattern[i]._lineIndex == n.getInverted()._lineIndex && complexPattern[i]._lineLayer == n._lineLayer || complexPattern[i - 1]._lineIndex == n.getInverted()._lineIndex && complexPattern[i - 1]._lineLayer == n._lineLayer) || !validPlacement) {
                i -= 2;
                invalidPlacementsInARow++;
                continue;
            }

            n.amountOfStackedNotes = timings[i].amountOfStackedNotes;
            redNotes.add(n);
        }


        //Inverting all red Notes so that they are actually red notes LUL
        for (Note n : redNotes) n.invertNote();

        //Creating a list of all notes that should be returned
        List<Note> allNotes = new ArrayList<>();

        if (stacks) {
            for (Note n : redNotes) {
                allNotes.addAll(List.of(n.createStackedNote()));
            }
            for (Note n : complexPattern) {
                allNotes.addAll(List.of(n.createStackedNote()));
            }
        } else {
            allNotes.addAll(redNotes);
            allNotes.addAll(List.of(complexPattern));
        }

        Collections.sort(allNotes);
        checkForNoteInNote(allNotes);

        return allNotes;
    }

    public static Note[] checkForNoteInNote(List<Note> allNotes) {
        Collections.sort(allNotes);
        for (int i = 0; i < allNotes.size() - 1; i++) {
            if (allNotes.get(i)._time == allNotes.get(i + 1)._time && allNotes.get(i).equalNotePlacement(allNotes.get(i + 1))) {
                if (allNotes.get(i)._type == 0) allNotes.get(i)._lineIndex--;
                System.err.println("Warning at beat: " + allNotes.get(i)._time + ": Note in another Note. Might be already fixed");
            }
        }
        return allNotes.toArray(new Note[0]);
    }


    /**
     * This function tries to avoid parity breaks when a horizontal segment is coming to an end.
     *
     * @param pattern pattern [] is the array, where the previous notes are saved.
     * @param i       i specifies at which element the last note has been placed.
     * @param j       j... If the pattern is one handed: j = 1. If two handed: j = 2.
     * @return Note
     */
    public static Note endHorizontalPlacements(Note[] pattern, int i, int j) {
        float random = (float) Math.random() * 100;
        boolean debug = false;
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
        if (debug) System.out.println("In a row:   " + horizontalsInARow);
        if (debug) System.out.println("Direction:  " + firstHorizontalCutDirection);
        if (debug) System.out.println("Note (i):   " + i);
        if (debug) System.out.println();

        if (horizontalsInARow == 0) return null;

        switch (horizontalsInARow % 2) {
            case 0 -> {
                //first: top left swing
                if (firstHorizontalCutDirection == 4 || (firstHorizontalCutDirection == 0 && secondHorizontalCutDirection == 3)) {
                    if (debug) System.out.println("Before-dir: " + firstHorizontalCutDirection);
                    if (random <= 50) return new Note(pattern[i]._time, 3, 0, 1, 7);
                    else return new Note(pattern[i]._time, 3, 1, 1, 7);
                }

                //first: top right swing
                if (firstHorizontalCutDirection == 5 || (firstHorizontalCutDirection == 0 && secondHorizontalCutDirection == 2)) {
                    if (debug) System.out.println("Before-dir: " + firstHorizontalCutDirection);
                    if (random <= 50) return new Note(pattern[i]._time, 2, 0, 1, 6);
                    else return new Note(pattern[i]._time, 1, 0, 1, 6);
                }

                //first: bottom left swing
                if (firstHorizontalCutDirection == 6 || (firstHorizontalCutDirection == 1 && secondHorizontalCutDirection == 3)) {
                    if (debug) System.out.println("Before-dir: " + firstHorizontalCutDirection);
                    return new Note(pattern[i]._time, 3, 2, 1, 5);
                }

                //first: bottom right swing
                if (firstHorizontalCutDirection == 7 || (firstHorizontalCutDirection == 1 && secondHorizontalCutDirection == 2)) {
                    if (debug) System.out.println("Before-dir: " + firstHorizontalCutDirection);
                    return new Note(pattern[i]._time, 2, 1, 1, 4);
                }
            }
            case 1 -> {
                //first: top left swing
                if (firstHorizontalCutDirection == 4 || (firstHorizontalCutDirection == 0 && secondHorizontalCutDirection == 3)) {
                    if (debug) System.out.println("Before-dir: " + firstHorizontalCutDirection);
                    return new Note(pattern[i]._time, 2, 1, 1, 4);
                }

                //first: top right swing
                if (firstHorizontalCutDirection == 5 || (firstHorizontalCutDirection == 0 && secondHorizontalCutDirection == 2)) {
                    if (debug) System.out.println("Before-dir: " + firstHorizontalCutDirection);
                    return new Note(pattern[i]._time, 3, 2, 1, 5);
                }

                //first: bottom left swing
                if (firstHorizontalCutDirection == 6 || (firstHorizontalCutDirection == 1 && secondHorizontalCutDirection == 3)) {
                    if (debug) System.out.println("Before-dir: " + firstHorizontalCutDirection);
                    if (random <= 50) return new Note(pattern[i]._time, 2, 0, 1, 6);
                    else return new Note(pattern[i]._time, 1, 0, 1, 6);
                }

                //first: bottom right swing
                if (firstHorizontalCutDirection == 7 || (firstHorizontalCutDirection == 1 && secondHorizontalCutDirection == 2)) {
                    if (debug) System.out.println("Before-dir: " + firstHorizontalCutDirection);
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
     * @return Note
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
     * @return Note
     */
    public static Note predictNextNote(PatternProbability pattern, float time) {
        if (pattern == null || pattern.notes == null) return null;

        float currentProbability = 0;
        double placement = Math.random() * 100;

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
     * @return Note
     */
    public static Note firstNotePlacement(float _time) {
        Note n;
        double placement = Math.random() * 100;

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
     * @return Note
     */
    public static Note nextLinearNote(Note previousNote, float time) {
        Note p = previousNote; //p is much cleaner than having a thousand times previousNote
        double placement = Math.random() * 100;


        //blue bottom-middle-right lane, down swing
        //2,0,1
        if (p._lineIndex == 2 && p._lineLayer == 0 && p._type != 2 && p._cutDirection == 1) {
//                if (placement < 10) return new Note(time,3,1,1,3);
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
     * This function checks if the note on position i has a valid placement there
     * currently supports:
     * - Double Directional
     * - Vision blocks
     * - placeing note, if the previous note is not placed directly in front of it
     * <p>
     * read further for more information
     *
     * @param notes     notes [] is the array, where the previous notes are saved.
     * @param i         i specifies at which element the last note has been placed.
     * @param oneHanded is the map a one handed map.
     * @return boolean
     */
    public static boolean validPlacement(Note[] notes, int i, boolean oneHanded) {
        if (notes.length <= 2) return true;
        if (i < 4) return true;
        if (notes[i - 1] == null || notes[i] == null) return false;

        int j = 2;
        if (oneHanded) j = 1;


        //DD:
        if (notes[i - j]._cutDirection == notes[i]._cutDirection
                || (notes[i - j]._cutDirection == 6 || notes[i - j]._cutDirection == 1 || notes[i - j]._cutDirection == 7) && (notes[i]._cutDirection == 6 || notes[i]._cutDirection == 1 || notes[i]._cutDirection == 7)
                || (notes[i - j]._cutDirection == 7 || notes[i - j]._cutDirection == 3 || notes[i - j]._cutDirection == 5) && (notes[i]._cutDirection == 7 || notes[i]._cutDirection == 3 || notes[i]._cutDirection == 5)
                || (notes[i - j]._cutDirection == 4 || notes[i - j]._cutDirection == 0 || notes[i - j]._cutDirection == 5) && (notes[i]._cutDirection == 4 || notes[i]._cutDirection == 0 || notes[i]._cutDirection == 5)
                || (notes[i - j]._cutDirection == 4 || notes[i - j]._cutDirection == 2 || notes[i - j]._cutDirection == 6) && (notes[i]._cutDirection == 4 || notes[i]._cutDirection == 2 || notes[i]._cutDirection == 6)
        ) return false;

        //weird top row notes
        if (notes[i - j]._cutDirection == 0 && notes[i]._cutDirection == 6 && notes[i - j]._lineLayer == 2 && notes[i]._lineLayer >= 1 && notes[i - j]._lineIndex <= 2 && notes[i]._lineLayer >= 2)
            return false;

        //Vision block
        if (notes[i]._lineIndex == 2 && notes[i]._lineLayer == 1) return false;

        //Only place the note, if the previous note is not placed directly in front of it
        //If one-handed it true, then we can just skip this step.
        if (oneHanded) return true;
        return notes[i - 1]._lineIndex != notes[i].getInverted()._lineIndex || notes[i - 1]._lineLayer != notes[i]._lineLayer;
    }

    /**
     * Removes every null element in the array notes[]
     *
     * @param notes notes[] is the array, where the notes are saved.
     * @return Note [] without nulls
     */
    public static Note[] removeAllNulls(Note[] notes) {
        List<Note> list = new ArrayList<>();
        Collections.addAll(list, notes);

        while (list.remove(null)) ;

        return list.toArray(new Note[0]);
    }
}