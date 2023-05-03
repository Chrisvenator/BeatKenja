import com.google.gson.Gson;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AnalyzePatterns {
    private static int[][] count;

    public static void main(String[] args) {
        String inputPath = "Input.txt";
        String outputPath = "./output/";
        BeatSaberMap map = new Gson().fromJson(CreateTimings.readFile(inputPath).get(0), BeatSaberMap.class);

        //Example for removing all notes of color red:
//        map._notes = removeAllOtherTypes(map._notes, 1);
//        CreateTimings.overwriteFile(outputPath + "removedAllTypeX.txt", map.exportAsMap());

        //Example on how to analyze patterns:
//        Note[][] result = analyzePatterns(map, 1);
//        System.out.println(analyzePatternsToString(result));
//        System.out.println(removeXTimes(result, 4));
    }

    public static Note[][] analyzePatterns(BeatSaberMap map, int type) {
        if (type != 0 && type != 1) return null;
        Note[][] patterns = new Note[108][108];
        count = new int[108][108]; //how often a certain block follows another block. Here are the values of "patterns" saved
        Note[] notes = removeAllOtherTypes(map._notes, type);
//        patterns[0][0] = notes[0];


        //checking every Note in the map
        for (int k = 1; k < notes.length; k++) {
            Note n = notes[k];
            Note prev = notes[k - 1];

            //iterating over the 2-dimensional array:
            twoDimArr:
            for (int i = 0; i < patterns.length; i++) {
                //If the previous Note was not found (in the pattern list):
                if ((patterns[i][0] == null)) {
                    patterns[i][0] = prev;
                    patterns[i][1] = n;
                    count[i][1] = 1;
                    break;

                    //If it was found, then look if n is already saved (in the pattern list):
                } else if (patterns[i][0].equalPlacement(prev)) {
                    for (int j = 1; j < patterns[i].length; j++) {
                        if (patterns[i][j] == null) {
                            patterns[i][j] = n;
                            count[i][j] = 1;
                            break twoDimArr;
                        } else if (patterns[i][j].equalPlacement(n)) {
                            count[i][j]++;
                            break twoDimArr;
                        }
                    }
                }
            }
        }

        return patterns;
    }

    public static String analyzePatternsToString(Note[][] patterns) {

        String s = "";
        int ct = 0;
        for (Note[] nottes : patterns) {
            if (nottes[0] != null) {
                s += nottes[0].toString().replaceAll("\n", "") + ": [\n";

                for (int i = 1; i < nottes.length; i++) {
                    if (nottes[i] != null) {
                        s += "  " + nottes[i].toString().replaceAll("\n", "") + ": " + count[ct][i] + " times, \n";
                    }
                }
                s += "] \n";
            }
            ct++;
        }

//        System.out.println(s);
        return s;
    }

    public static String removeXTimes(Note[][] patterns, int threshold) {
        String s = analyzePatternsToString(patterns);
        String result = "";
        String[] strings = s.split("\n");
        List<String> split = new ArrayList<>();

        for (String ss : strings) {
            boolean contains = false;
            for (int i = 0; i <= threshold; i++) {
                if (ss.contains(i + " time")) {
                    contains = true;
                    break;
                }
            }
            if (!contains) {
                split.add(ss + "\n");
            }
        }

        try {

            for (int i = 0; i < split.size(); i++) {
                if (split.get(i).contains("[")
                        && split.get(i + 1).contains("]")
                        && !split.get(i + 1).contains("time")) {
                    split.remove(i);
//                    split.remove(i + 1);
                    i--;
                }

                if (split.get(i).contains("]") && split.get(i + 1).contains("]")) {
                    split.remove(i + 1);
                    i--;
                }
            }
        } catch (IndexOutOfBoundsException e) {
        }


        return split.toString();
    }

    public static Note[] removeAllOtherTypes(Note[] notes, int type) {
        List<Note> noteList = new ArrayList<>();
        for (Note n : notes) {
            if (n._type == type) noteList.add(n);
        }

        return noteList.toArray(Note[]::new);
    }
}
