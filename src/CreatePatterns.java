import com.google.gson.Gson;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class CreatePatterns {
    public static void main(String[] args) {
        String filename = "ISeeFireXP.txt";
        String outPath = "";
        String input = CreateTimings.readFile(filename).get(0);

        Gson gson = new Gson();
        BeatSaberMap map = gson.fromJson(input, BeatSaberMap.class);
        map._events = new Events[0];


        map.toBlueLeftBottomRowDotTimings();
        Note[] n = linearSlowPattern(map._notes);
        CreateTimings.overwriteFile(outPath + "TestOutput.txt", new BeatSaberMap(n).exportAsMap());
    }

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

        //The first 2 notes have to placed manually to ensure they are not on some random position
        double placement = Math.random() * 100;
        for (int i = 0; i < 2; i++) {
            if (placement < 20) pattern[i] = new Note(timings[i]._time, 1, 0, 1, 1);
            if (placement <= 65) pattern[i] = new Note(timings[i]._time, 2, 0, 1, 1);
            else if (placement > 65) pattern[i] = new Note(timings[i]._time, 3, 0, 1, 1);
        }

        for (int i = 2; i < timings.length; i++) {
            pattern[i] = nextLinearNote(pattern[i - 2], timings[i]._time);

            if (!validPlacement(pattern, i) && i > 4) {
                pattern[i] = null;
                i--;
            }
        }
        for (int i = 1; i < pattern.length; i += 2) {
            pattern[i].invertNote();
        }

        return pattern;
    }


    //TODO: Stacked notes
    //TODO: Not linear Patterns

    //p is the current note, that is being processed
    //time is the placement position of the next note
    public static Note nextLinearNote(Note previousNote, float time) {
        Note p = previousNote; //p is much cleaner than having a thousand times previousNote
        double placement = Math.random() * 100;


        //blue bottom-middle-right lane, down swing
        //2, 0, 1, 1
        if (p._lineIndex == 2 && p._lineLayer == 0 && p._type == 1 && p._cutDirection == 1) {
//                if (placement < 10) return new Note(time, 3, 1, 1, 3);
            if (placement < 20) return new Note(time, 3, 1, 1, 5);
            else if (placement < 30) return new Note(time, 3, 2, 1, 0);
            else if (placement < 65) return new Note(time, 3, 2, 1, 5);
            else return new Note(time, 2, 2, 1, 0);
        }

        //blue middle-right lane, right swing
        //3, 1, 1, 3
        else if ((p._lineIndex == 3 && p._lineLayer == 1 && p._type == 1 && p._cutDirection == 3)) {
            if (placement < 70) return new Note(time, 1, 0, 1, 6);
            else return new Note(time, 2, 0, 1, 1);
        }

        //blue upper-right lane, top-right swing
        //3, 2, 1, 5
        else if (p._lineIndex == 3 && p._lineLayer == 2 && p._type == 1 && p._cutDirection == 5) {
            if (placement < 5) return new Note(time, 0, 0, 1, 6);
            else if (placement < 40) return new Note(time, 1, 0, 1, 6);
            else if (placement < 90) return new Note(time, 2, 0, 1, 1);
            else return new Note(time, 3, 0, 1, 1);
        }

        //blue upper-right lane, top swing
        //3, 2, 1, 0
        else if (p._lineIndex == 3 && p._lineLayer == 2 && p._type == 1 && p._cutDirection == 0) {
            if (placement <= 50) return new Note(time, 2, 0, 1, 1);
            else return new Note(time, 3, 0, 1, 1);
        }

        //blue upper-middle-right lane, top swing
        //2, 2, 1, 0
        else if (p._lineIndex == 2 && p._lineLayer == 2 && p._type == 1 && p._cutDirection == 0) {
            if (placement < 5) return new Note(time, 3, 0, 1, 7);
            else if (placement < 20) return new Note(time, 3, 0, 1, 1);
            else if (placement < 55) return new Note(time, 1, 0, 1, 6);
            else return new Note(time, 2, 0, 1, 1);
        }

        //blue bottom-middle-right lane, bottom-left swing
        //3, 1, 1, 6
        else if (p._lineIndex == 3 && p._lineLayer == 1 && p._type == 1 && p._cutDirection == 6) {
            if (placement < 40) return new Note(time, 1, 0, 1, 6);
            else if (placement < 80) return new Note(time, 2, 0, 1, 6);
            else return new Note(time, 2, 0, 1, 1);
        }

        //blue bottom-middle-left lane, bottom-left swing
        //1, 0, 1, 6
        else if (p._lineIndex == 1 && p._lineLayer == 0 && p._type == 1 && p._cutDirection == 6) {
            if (placement < 38) return new Note(time, 3, 2, 1, 5);
            else if (placement < 81) return new Note(time, 3, 1, 1, 5);
            else if (placement < 85) return new Note(time, 2, 2, 1, 0);
            else return new Note(time, 2, 2, 1, 5);
        }

        //blue bottom-left lane, bottom-left swing
        //0, 0, 1, 6
        else if (p._lineIndex == 0 && p._lineLayer == 0 && p._type == 1 && p._cutDirection == 6) {
            if (placement < 30) return new Note(time, 2, 2, 1, 5);
            else if (placement < 80) return new Note(time, 3, 2, 1, 5);
            else if (placement < 83) return new Note(time, 3, 1, 1, 5);
            else return new Note(time, 3, 1, 1, 3);
        }

        //blue bottom-middle-right lane, bottom-left swing
        //2, 0, 1, 6
        else if (p._lineIndex == 2 && p._lineLayer == 0 && p._type == 1 && p._cutDirection == 6) {
            if (placement <= 40) return new Note(time, 3, 1, 1, 5);
            if (placement <= 60) return new Note(time, 3, 2, 1, 0);
            else return new Note(time, 3, 2, 1, 5);
        }

        //blue top-middle-right lane, top-right swing
        //2, 2, 1, 5
        else if (p._lineIndex == 2 && p._lineLayer == 2 && p._type == 1 && p._cutDirection == 5) {
            if (placement <= 20) return new Note(time, 0, 0, 1, 6);
            else return new Note(time, 1, 0, 1, 6);
        }

        //blue bottom-right lane, bottom swing
        //3, 0, 1, 1
        else if (p._lineIndex == 3 && p._lineLayer == 0 && p._type == 1 && p._cutDirection == 1) {
            if (placement <= 50) return new Note(time, 3, 1, 1, 0);
            else return new Note(time, 3, 2, 1, 0);
        }

        //blue middle-right lane, top swing
        //3, 1, 1, 0
        else if (p._lineIndex == 3 && p._lineLayer == 1 && p._type == 1 && p._cutDirection == 0) {
            if (placement <= 5) return new Note(time, 3, 1, 1, 1);
            if (placement <= 55) return new Note(time, 3, 0, 1, 1);
            else return new Note(time, 2, 0, 1, 1);
        }

        //blue top-left-middle lane, top-left swing
        //1, 2, 1, 4
        else if (p._lineIndex == 1 && p._lineLayer == 2 && p._type == 1 && p._cutDirection == 4) {
            if (placement <= 50) return new Note(time, 3, 0, 1, 7);
            else return new Note(time, 2, 0, 1, 1);
        }

        //blue middle-right lane, top-right swing
        //3, 1, 1, 5
        else if (p._lineIndex == 3 && p._lineLayer == 1 && p._type == 1 && p._cutDirection == 5) {
            if (placement <= 60) return new Note(time, 2, 0, 1, 6);
            else return new Note(time, 1, 0, 1, 6);
        }

        //blue bottom-right lane, bottom-right swing
        //3, 0, 1, 7
        else if (p._lineIndex == 3 && p._lineLayer == 0 && p._type == 1 && p._cutDirection == 7) {
            if (placement <= 10) return new Note(time, 3, 2, 1, 0);
            else return new Note(time, 2, 2, 1, 0);
        }

        //blue middle-right lane, bottom-right swing
        //3, 0, 1, 7
        else if (p._lineIndex == 3 && p._lineLayer == 1 && p._type == 1 && p._cutDirection == 1) {
            if (placement <= 30) return new Note(time, 3, 1, 1, 0);
            else return new Note(time, 3, 2, 1, 0);
        }


        //error catching:
        //If I forgot to add a note, it will be displayed here:
        else {
            System.out.println(p.toString().replaceAll("\n", ""));
            throw new IllegalArgumentException("There is an undetected note!");
        }
    }


    //This function checks if the note on position i has a valid placement there
    //read further for more information
    public static boolean validPlacement(Note[] notes, int i) {
        if (notes.length <= 2) return true;
        if (i < 4) return true;

        //Avoiding vision blocks.
        //Only place the note, if the previous note is not placed directly in front of it
        return notes[i - 1]._lineIndex != notes[i].getInverted()._lineIndex || notes[i - 1]._lineLayer != notes[i]._lineLayer;
    }
}
