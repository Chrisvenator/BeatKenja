import com.google.gson.Gson;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Pattern {

    //In this variable all the possible notes
    protected Note[][] patterns;

    //how often a certain block follows another block. Here are the values of "patterns" saved
    protected int[][] count;
    protected float[][] probabilities;

    public static void main(String[] args) {
        String inputPath = "Input.txt";
        String outputPath = "./output/";
        BeatSaberMap map = new Gson().fromJson(CreateTimings.readFile(inputPath).get(0), BeatSaberMap.class);


        Pattern p = new Pattern(map._notes, 1);
//        System.out.println(p.toString());
        p.removeXTimes(8);
        System.out.println(p);

        System.out.println(p.getProbabilityOf(new Note(0, 2, 0, 1, 1)));
    }

    public Pattern(Note[] notes, int type) {
        if (type != 0 && type != 1) return;
        count = new int[108][108];
        patterns = new Note[108][108];
        probabilities = new float[108][108];

        analyzePattern(notes, type);
        computeProbabilities();
    }

    public void analyzePattern(Note[] map, int type) {
        Note[] notes = removeAllOtherTypes(map, type);

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
    }

    public String toString() {

        String s = "";
        int ct = 0;
        for (Note[] nottes : this.patterns) {
            if (nottes[0] != null) {
                s += nottes[0].toString().replaceAll("\n", "") + ": [\n";

                for (int i = 1; i < nottes.length; i++) {
                    if (nottes[i] != null) {
                        s += "  " + nottes[i].toString().replaceAll("\n", "") + ": " + this.count[ct][i] + " times = " + (this.probabilities[ct][i]) + "% , \n";
                    }
                }
                s += "] \n";
            }
            ct++;
        }

        return s;
    }

    public String removeXTimes(int threshold) {
        String s = this.toString();
        String result = "";
        String[] strings = s.split("\n");
        List<String> split = new ArrayList<>();

        //I WAS DUMB SO I MADE IT WITH STRINGS. I KNOW! DON'T SHOUT AT ME ):

        for (String ss : strings) {
            boolean contains = false;
            for (int i = 0; i <= threshold; i++) {
                if (ss.contains(" " + i + " time")) {
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
                    i--;
                }

                if (split.get(i).contains("]") && split.get(i + 1).contains("]")) {
                    split.remove(i + 1);
                    i--;
                }
            }
        } catch (IndexOutOfBoundsException e) {
        }

        this.patterns = new Note[this.patterns.length][this.patterns[0].length];
        this.count = new int[this.patterns.length][this.patterns[0].length];
        this.probabilities = new float[this.patterns.length][this.patterns[0].length];

        int positionY = 0;
        int positionX = 0;


        //building the new Array without everything under the threshold
        for (int i = 0; i < split.size() && this.patterns[positionY] != null; i++) {
            String note = split.get(i).split("}:")[0] + "}";

            //if it is a note:
            if (split.get(i).contains("[")) {
                positionX = 0;
                this.patterns[positionY][positionX] = new Gson().fromJson(note, Note.class);
                positionX++;

                //pattern over:
            } else if (split.get(i).contains("]")) {
                positionY++;

                //extracting the information out of the string lol:
            } else {
                //getting the percent (=69%) of (example): {"_time":197.531,"_lineIndex":3,"_lineLayer":2,"_type":1,"_cutDirection":8}: 18 times = 69.230774%
                float prob = Float.parseFloat(split.get(i).split("}:")[1].split(" = ")[1].split("%")[0].replaceAll(" ", ""));

                //getting the count (=18) of (example): {"_time":197.531,"_lineIndex":3,"_lineLayer":2,"_type":1,"_cutDirection":8}: 18 times = 69.230774%
                int count = Integer.parseInt(split.get(i).split("}:")[1].split(" times")[0].replaceAll(" ", ""));
//                System.out.println(note + " " + count + " " + prob);


                this.patterns[positionY][positionX] = new Gson().fromJson(note, Note.class);
                this.probabilities[positionY][positionX] = prob;
                this.count[positionY][positionX] = count;


                positionX++;
            }
        }

        //Is it still the same?
        //Just try and uncomment this line:
//        System.out.println(split.toString().replaceAll(", ", "").replaceAll(" ,", "").contains(("[" + this.toString() + "]").replaceAll(", ", "").replaceAll(" ,", "")));

        //when everything is removed, the probabilities have to be calculated anew
        computeProbabilities();
        return split.toString();
    }

    //This methode is used to calculate the probabilities of each following-note
    public void computeProbabilities() {
        for (int i = 0; i < this.patterns.length; i++) {

            //counting, how many notes are in the arrays
            int ct = 0;
            for (int j = 0; j < this.patterns[i].length; j++) {
                this.probabilities[i][j] = 0;
                if (this.patterns[i][j] != null) ct += this.count[i][j];
            }

            //calculate the probability for every note
            for (int j = 0; j < this.patterns[i].length; j++) {
                if (this.patterns[i][j] != null) this.probabilities[i][j] = (float) this.count[i][j] / ct * 100;
            }
        }
    }

    private static Note[] removeAllOtherTypes(Note[] notes, int type) {
        List<Note> noteList = new ArrayList<>();
        for (Note n : notes) {
            if (n._type == type) noteList.add(n);
        }

        return noteList.toArray(Note[]::new);
    }

    public PatternProbability getProbabilityOf(Note n) {
        for (int i = 0; i < this.patterns.length && this.patterns[i] != null; i++) {
            if (this.patterns[i][0].equalPlacement(n))
                return new PatternProbability(this.patterns[i], this.probabilities[i]);
        }
        return null;
    }

}

class PatternProbability {
    protected Note[] notes;
    protected float[] probabilities;

    public PatternProbability(Note[] notes, float[] probabilities) {
        this.notes = notes;
        this.probabilities = probabilities;

        removeNulls();
    }

    public void removeNulls() {
        int ct = 0;
        for (Note note : notes) {
            if (note == null) break;
            ct++;
        }
        Note[] notesNew = new Note[ct];
        float[] probNew = new float[ct];
        for (int i = 0; i < ct; i++) {
            notesNew[i] = notes[i];
            probNew[i] = probabilities[i];
        }

        this.notes = notesNew;
        this.probabilities = probNew;
    }

    @Override
    public String toString() {
        String s = "";
        for (int i = 0; i < notes.length; i++) {
            s += (probabilities[i] == 0.0 ? "" : "  ") + notes[i].toString().replaceAll("\n", "") + ": " + (probabilities[i] == 0.0 ? "[\n" : (probabilities[i] + "%") + "\n");
        }

        return s + "]";
    }
}