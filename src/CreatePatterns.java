import com.google.gson.Gson;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class CreatePatterns {
    public static void main(String[] args) {
        String filename = "Input.txt";
        String outPath = "";
        String input = CreateTimings.readFile(filename).get(0);

        Gson gson = new Gson();
        BeatSaberMap map = gson.fromJson(input, BeatSaberMap.class);
        map._events = new Events[0];


        Note[] timingsFromMap = mapToTimingNotesArray(map._notes);
        CreateTimings.overwriteFile(outPath + "TestOutput.txt", new BeatSaberMap(timingsFromMap).exportAsMap());
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

//    public static Note[] linearSlowPattern(Note[] timings) {
//        Note[] pattern = new Note[timings.length];
//
//        Note[] rightHand = new Note[timings.length / 2];
//        Note[] leftHand = new Note[timings.length / 2];
//
//
//        for (int i = 1; i < pattern.length; i += 2) {
//            pattern[i].invertNote();
//        }
//
//        int[] array1 = {1, 2, 3};
//        int[] array2 = {4, 5, 6, 7};
//        int[] mergedArray = new int[array1.length + array2.length];
//        int index = 0;
//        for (int j : array1) {
//            mergedArray[index] = j;
//            index++;
//        }
//        for (int j : array2) {
//            mergedArray[index] = j;
//            index++;
//        }
//
//        return pattern;
//    }

    public static Note[] linearSlowPattern(Note[] timings) {
        Note[] pattern = new Note[timings.length];

        double placement = Math.random() * 100;
        for (int i = 0; i < 2; i++) {
            if (placement < 20) pattern[i] = new Note(timings[i]._time, 1, 0, 1, 1);
            if (placement <= 65) pattern[i] = new Note(timings[i]._time, 2, 0, 1, 1);
            else if (placement > 65) pattern[i] = new Note(timings[i]._time, 3, 0, 1, 1);
        }

        for (int i = 2; i < timings.length; i++) {
            Note p = pattern[i - 2];
            placement = Math.random() * 100;

            //blue bottom-middle-right lane, down swing
            //2, 0, 1, 1
            if (p._lineIndex == 2 && p._lineLayer == 0 && p._type == 1 && p._cutDirection == 1) {
//                if (placement < 10) pattern[i] = new Note(timings[i]._time, 3, 1, 1, 3);
                if (placement < 20) pattern[i] = new Note(timings[i]._time, 3, 1, 1, 5);
                else if (placement < 30) pattern[i] = new Note(timings[i]._time, 3, 2, 1, 0);
                else if (placement < 65) pattern[i] = new Note(timings[i]._time, 3, 2, 1, 5);
                else pattern[i] = new Note(timings[i]._time, 2, 2, 1, 0);
            }

            //blue middle-right lane, right swing
            //3, 1, 1, 3
            else if ((p._lineIndex == 3 && p._lineLayer == 1 && p._type == 1 && p._cutDirection == 3)) {
                if (placement < 70) pattern[i] = new Note(timings[i]._time, 1, 0, 1, 6);
                else pattern[i] = new Note(timings[i]._time, 2, 0, 1, 1);
            }

            //blue upper-right lane, top-right swing
            //3, 2, 1, 5
            else if (p._lineIndex == 3 && p._lineLayer == 2 && p._type == 1 && p._cutDirection == 5) {
                if (placement < 5) pattern[i] = new Note(timings[i]._time, 0, 0, 1, 6);
                else if (placement < 40) pattern[i] = new Note(timings[i]._time, 1, 0, 1, 6);
                else if (placement < 90) pattern[i] = new Note(timings[i]._time, 2, 0, 1, 1);
                else pattern[i] = new Note(timings[i]._time, 3, 0, 1, 1);
            }

            //blue upper-right lane, top swing
            //3, 2, 1, 0
            else if (p._lineIndex == 3 && p._lineLayer == 2 && p._type == 1 && p._cutDirection == 0) {
                if (placement <= 50) pattern[i] = new Note(timings[i]._time, 2, 0, 1, 1);
                else pattern[i] = new Note(timings[i]._time, 3, 0, 1, 1);
            }

            //blue upper-middle-right lane, top swing
            //2, 2, 1, 0
            else if (p._lineIndex == 2 && p._lineLayer == 2 && p._type == 1 && p._cutDirection == 0) {
                if (placement < 5) pattern[i] = new Note(timings[i]._time, 3, 0, 1, 7);
                else if (placement < 20) pattern[i] = new Note(timings[i]._time, 3, 0, 1, 1);
                else if (placement < 55) pattern[i] = new Note(timings[i]._time, 1, 0, 1, 6);
                else pattern[i] = new Note(timings[i]._time, 2, 0, 1, 1);
            }

            //blue bottom-middle-right lane, bottom-left swing
            //3, 1, 1, 6
            else if (p._lineIndex == 3 && p._lineLayer == 1 && p._type == 1 && p._cutDirection == 6) {
                if (placement < 40) pattern[i] = new Note(timings[i]._time, 1, 0, 1, 6);
                else if (placement < 80) pattern[i] = new Note(timings[i]._time, 2, 0, 1, 6);
                else pattern[i] = new Note(timings[i]._time, 2, 0, 1, 1);
            }

            //blue bottom-middle-left lane, bottom-left swing
            //1, 0, 1, 6
            else if (p._lineIndex == 1 && p._lineLayer == 0 && p._type == 1 && p._cutDirection == 6) {
                if (placement < 38) pattern[i] = new Note(timings[i]._time, 3, 2, 1, 5);
                else if (placement < 81) pattern[i] = new Note(timings[i]._time, 3, 1, 1, 5);
                else if (placement < 85) pattern[i] = new Note(timings[i]._time, 2, 2, 1, 0);
                else pattern[i] = new Note(timings[i]._time, 2, 2, 1, 5);
            }

            //blue bottom-left lane, bottom-left swing
            //0, 0, 1, 6
            else if (p._lineIndex == 0 && p._lineLayer == 0 && p._type == 1 && p._cutDirection == 6) {
                if (placement < 30) pattern[i] = new Note(timings[i]._time, 2, 2, 1, 5);
                else if (placement < 80) pattern[i] = new Note(timings[i]._time, 3, 2, 1, 5);
                else if (placement < 83) pattern[i] = new Note(timings[i]._time, 3, 1, 1, 5);
                else pattern[i] = new Note(timings[i]._time, 3, 1, 1, 3);
            }

            //blue bottom-middle-right lane, bottom-left swing
            //2, 0, 1, 6
            else if (p._lineIndex == 2 && p._lineLayer == 0 && p._type == 1 && p._cutDirection == 6) {
                if (placement <= 40) pattern[i] = new Note(timings[i]._time, 3, 1, 1, 5);
                if (placement <= 60) pattern[i] = new Note(timings[i]._time, 3, 2, 1, 0);
                else pattern[i] = new Note(timings[i]._time, 3, 2, 1, 5);
            }

            //blue top-middle-right lane, top-right swing
            //2, 2, 1, 5
            else if (p._lineIndex == 2 && p._lineLayer == 2 && p._type == 1 && p._cutDirection == 5) {
                if (placement <= 20) pattern[i] = new Note(timings[i]._time, 0, 0, 1, 6);
                else pattern[i] = new Note(timings[i]._time, 1, 0, 1, 6);
            }

            //blue bottom-right lane, bottom swing
            //3, 0, 1, 1
            else if (p._lineIndex == 3 && p._lineLayer == 0 && p._type == 1 && p._cutDirection == 1) {
                if (placement <= 50) pattern[i] = new Note(timings[i]._time, 3, 1, 1, 0);
                else pattern[i] = new Note(timings[i]._time, 3, 2, 1, 0);
            }

            //blue middle-right lane, top swing
            //3, 1, 1, 0
            else if (p._lineIndex == 3 && p._lineLayer == 1 && p._type == 1 && p._cutDirection == 0) {
                if (placement <= 5) pattern[i] = new Note(timings[i]._time, 3, 1, 1, 1);
                if (placement <= 55) pattern[i] = new Note(timings[i]._time, 3, 0, 1, 1);
                else pattern[i] = new Note(timings[i]._time, 2, 0, 1, 1);
            }

            //blue top-left-middle lane, top-left swing
            //1, 2, 1, 4
            else if (p._lineIndex == 1 && p._lineLayer == 2 && p._type == 1 && p._cutDirection == 4) {
                if (placement <= 50) pattern[i] = new Note(timings[i]._time, 3, 0, 1, 7);
                else pattern[i] = new Note(timings[i]._time, 2, 0, 1, 1);
            }

            //blue middle-right lane, top-right swing
            //3, 1, 1, 5
            else if (p._lineIndex == 3 && p._lineLayer == 1 && p._type == 1 && p._cutDirection == 5) {
                if (placement <= 60) pattern[i] = new Note(timings[i]._time, 2, 0, 1, 6);
                else pattern[i] = new Note(timings[i]._time, 1, 0, 1, 6);
            }

            //blue bottom-right lane, bottom-right swing
            //3, 0, 1, 7
            else if (p._lineIndex == 3 && p._lineLayer == 0 && p._type == 1 && p._cutDirection == 7) {
                if (placement <= 10) pattern[i] = new Note(timings[i]._time, 3, 2, 1, 0);
                else pattern[i] = new Note(timings[i]._time, 2, 2, 1, 0);
            }


            //error catching
            else {
                pattern[i] = new Note(timings[i]._time, 2, 0, 1, 8);
//                System.out.println(pattern[i - 4].toString().replaceAll("\n", ""));
//                System.out.println(pattern[i - 3].toString().replaceAll("\n", ""));
//                System.out.println(pattern[i - 2].toString().replaceAll("\n", ""));
//                System.out.println(pattern[i].toString().replaceAll("\n", ""));
                System.out.println(pattern[i - 1].toString().replaceAll("\n", ""));
                throw new IllegalArgumentException("There is an undetected note!");
            }


            if (!validPlacement(pattern, i) && i > 4) {
//                System.out.println(pattern[i - 1].toString());
//                System.out.println(pattern[i].toString() + "\n");
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

    //This function checks if the note on position i has a valid placement there
    //read further for more information
    public static boolean validPlacement(Note[] notes, int i) {
        if (notes.length <= 2) return true;
        if (i < 4) return true;

        //Avoiding vision blocks.
        //Only place the note, if the previous note is not placed directly in front of it
        if (notes[i - 1]._lineIndex == notes[i].getInverted()._lineIndex && notes[i - 1]._lineLayer == notes[i]._lineLayer)
            return false;


        return true;
    }

    //This function makes timings from a map.
    //Every note is converted into a blue dot block on the leftmost lane
    //WARNING: If there is more than 1 note in the same beat, then all but one are erased (for example stacks)
    //If you want to keep them, then have a look at "mapToTimingNotesArray" or "mapToTimingNotesList"
    public static Note[] toLinearTimings(Note[] notes) {
        Note[] timings = new Note[notes.length];
        int numberOfNulls = 0;

        //traversing every note
        for (int i = 0; i < notes.length; i++) {

            //when the note exists, then DON'T place another one on top of it
            if (i >= 2 && notes[i - 1]._time == notes[i]._time) {
                numberOfNulls++;
                continue;
            }

            //else:
            timings[i] = new Note(notes[i]._time);
        }

        //Since there may be null values, we need to remove them
        Note[] toReturn = new Note[notes.length - numberOfNulls];
        int ct = 0;
        for (Note n : timings) {
            if (n != null) {
                toReturn[ct] = n;
                ct++;
            }
        }

        return toReturn;
    }

    //This function is only here to make the List in a List into a one dimensional array, so that it is compatible with
    //the other functions
    public static Note[] mapToTimingNotesArray(Note[] notes) {
        List<List<Note>> note = mapToTimingNotesList(notes);
        List<Note> list = new ArrayList<>();

        //traversing every note
        for (List<Note> l : note) {
            list.addAll(l);
        }

        //returning the List as an Array
        return list.toArray(new Note[0]);
    }

    //This function takes all the notes of a map and converts it to a List inside a List in which all the notes are saved
    //as a dot on the leftmost lane.
    //If there are more notes on the same beat, then the notes are being converted into stacks
    //Red Notes are only created if there is a blue and a red note on the same beat. They are saved on the second lane
    //Note that there can only be a maximum of 6 Notes in one Beat or else the script will not create a 7th note;
    public static List<List<Note>> mapToTimingNotesList(Note[] notes) {
        //Here is a List, where all grids are being saved.
        List<List<Note>> timings = new ArrayList<>(List.of(new ArrayList<>(List.of(new Note(notes[0]._time, notes[0]._type == 0 ? 1 : 0, 0, notes[0]._type, 8)))));

        for (Note n : notes) {
            //the first note must be set manually
            if (notes[0] == n) continue;

            //retrieving the grid
            List<Note> grid = timings.get(timings.size() - 1);

            //if grid exists
            if (grid.get(0)._time == n._time) {
                int ctBlue = 0;
                int ctRed = 0;
                for (Note note : grid) {
                    if (note._type == 0) ctRed++;
                    if (note._type == 1) ctBlue++;
                }

                //It will only create a note, when there are up to 6 notes already saved.
                //It will create it in the desired lane: Red lane 1; Blue lane 0
                Note newNote = new Note(n._time, n._type == 0 ? 1 : 0, n._type == 0 ? ctRed : ctBlue, n._type, 8);
                if (ctBlue < 3 && ctRed < 3) grid.add(newNote);
            } else {
                //creating a new grid
                Note newNote = new Note(n._time, n._type == 0 ? 1 : 0, 0, n._type, 8);
                timings.add(new ArrayList<>(List.of(newNote)));
            }
        }

        //when there is only one red note, the we will be converting this red note into a blue one.
        // (It makes copying someone else's map way harder)
        for (List<Note> l : timings) {
            if (l.size() == 1) {
                l.get(0)._lineIndex = 0;
                l.get(0)._type = 1;
            }
//            for (Note n : l) { //for debugging purposes
//                System.out.println("Note:" + n.toString().replaceAll("\n", ""));
//            }
//            System.out.println("");
        }
        return timings;
    }
}
