import com.google.gson.Gson;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

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

//        System.out.println(new BeatSaberMap(mapFromPatterns(timings._notes, p, false)).exportAsMap());
//        System.out.println(new BeatSaberMap(linearSlowPattern(timings._notes)).exportAsMap());

        timings.originalJSON = input;
        System.out.println(timings.calculateBookmarks());
    }

    //TODO: Stacked notes. Theoretically they should work...

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


    public static Note[] mapFromPatterns(Note[] timings, Pattern p, boolean oneHanded) {
        Note[] pattern = new Note[timings.length];
        List<Note> patterns = new ArrayList<>();
        int j = oneHanded ? 1 : 2;

        for (int i = 0; i < j; i++) {
            pattern[i] = firstNotePlacement(timings[i]._time);
            patterns.add(pattern[i]);
        }

        Note previousBlue = pattern[0];
        Note previousRed = pattern[1];

        //invalidPlacesInARow is there to prevent an infinite loop.
        int invalidPlacesInARow = 0;
        int horizontalsInARow = 0;
        int firstHorizontalCutDirection = -1;
        for (int i = 2; i < timings.length; i++) {
            boolean inValidPlacement = false;

            //manual error handling:
            //When there exists an infinite loop:
            //Then create a new next note
            if (i >= 8 && invalidPlacesInARow >= 500) {
                System.err.println("ERROR at beat: " + timings[i]._time);
                pattern[i] = new TimingNote(timings[i]._time);
                invalidPlacesInARow = 0;
                continue;
            } else if (invalidPlacesInARow >= 500) {
                throw new IllegalArgumentException("Infinite Loop while creating map! Please try again.");
            }
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
            } else invalidPlacesInARow = 0;


            //Set previous notes:
            if (pattern[i]._type == 1) previousBlue = pattern[i];
            if (pattern[i]._type == 0) previousRed = pattern[i];
        }

        //make every second note red:
        if (!oneHanded) for (int i = 1; i < pattern.length; i += 2) pattern[i].invertNote();


        return pattern;
    }

    public static Note endHorizontalPlacements(Note pattern, int firstHorizontalCutDirection, int horizontalsInARow) {
        float random = (float) Math.random() * 100;

        //starting with left swing and number is uneven
        if (firstHorizontalCutDirection == 2 && horizontalsInARow % 2 == 1 && horizontalsInARow > 0) {
            if (random <= 50) return new Note(pattern._time, 3, 1, 1, 7);
            else return new Note(pattern._time, 3, 0, 1, 7);
        }

        //starting with left swing and number is even
        if (firstHorizontalCutDirection == 2 && horizontalsInARow % 2 == 0 && horizontalsInARow > 0)
            return new Note(pattern._time, 2, 2, 1, 4);


        //starting with right swing and number is uneven
        if (firstHorizontalCutDirection == 3 && horizontalsInARow % 2 == 1 && horizontalsInARow >= 2)
            return new Note(pattern._time, 2, 0, 1, 6);
        //starting with left swing and number is even
        if (firstHorizontalCutDirection == 3 && horizontalsInARow % 2 == 0 && horizontalsInARow > 0)
            return new Note(pattern._time, 3, 2, 1, 5);
        return null;
    }

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

    public static Note firstNotePlacement(float _time) {
        Note n;
        double placement = Math.random() * 100;

        if (placement < 20) n = new Note(_time, 1, 0, 1, 1);
        else if (placement <= 65) n = new Note(_time, 2, 0, 1, 1);
        else n = new Note(_time, 3, 0, 1, 1);

        return n;
    }


    //p is the current note, that is being processed
    //time is the placement position of the next note
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
            if (placement <= 65) return new Note(time, 3, 2, 1, 3);
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
            else if (p._type != 4 && (p._cutDirection == 0 || p._cutDirection == 5))
                return new Note(time, 0, 2, 1, 1);

            throw new IllegalArgumentException("There is an undetected note!");
        }
    }


    //This function checks if the note on position i has a valid placement there
    //read further for more information
    public static boolean validPlacement(Note[] notes, int i, boolean oneHanded) {
        if (notes.length <= 2) return true;
        if (i < 4) return true;
        if (notes[i - 1] == null || notes[i] == null) return false;

        //avoid DDs
        //for One-Handed only. For two handed replace i-1 with i-2

        int j = 2;
        if (oneHanded) j = 1;

        if (notes[i - j]._cutDirection == notes[i]._cutDirection
                || (notes[i - j]._cutDirection == 6 || notes[i - j]._cutDirection == 1 || notes[i - j]._cutDirection == 7) && (notes[i]._cutDirection == 6 || notes[i]._cutDirection == 1 || notes[i]._cutDirection == 7)
                || (notes[i - j]._cutDirection == 7 || notes[i - j]._cutDirection == 3 || notes[i - j]._cutDirection == 5) && (notes[i]._cutDirection == 7 || notes[i]._cutDirection == 3 || notes[i]._cutDirection == 5)
                || (notes[i - j]._cutDirection == 4 || notes[i - j]._cutDirection == 0 || notes[i - j]._cutDirection == 5) && (notes[i]._cutDirection == 4 || notes[i]._cutDirection == 0 || notes[i]._cutDirection == 5)
                || (notes[i - j]._cutDirection == 4 || notes[i - j]._cutDirection == 2 || notes[i - j]._cutDirection == 6) && (notes[i]._cutDirection == 4 || notes[i]._cutDirection == 2 || notes[i]._cutDirection == 6)
        ) return false;
        if (notes[i - j]._cutDirection == 0 && notes[i]._cutDirection == 6 && notes[i - j]._lineLayer == 2 && notes[i]._lineLayer >= 1 && notes[i - j]._lineIndex <= 2 && notes[i]._lineLayer >= 2)
            return false;
        if (notes[i]._lineIndex == 2 && notes[i]._lineLayer == 1) return false;

        //Avoiding vision blocks.
        //Only place the note, if the previous note is not placed directly in front of it
        //If one-handed it true, then we can just skip this step.
        if (oneHanded) return true;
        return notes[i - 1]._lineIndex != notes[i].getInverted()._lineIndex || notes[i - 1]._lineLayer != notes[i]._lineLayer;
    }
}
