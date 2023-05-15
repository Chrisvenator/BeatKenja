import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class CreateTimings {

    //makes a BeatSaber Map in the json format from a simple timings file.
    public static String makeMap(float bpm, String filename, double plPr) {
        List<String> timings = readFile(filename);
        StringBuilder jsonResult = new StringBuilder("{\"_version\":\"2.2.0\",\"_notes\":[");

        System.out.println("number of notes: " + timings.size());
        for (String s : timings) {
            float t = Float.parseFloat(s);
            double beat = Math.round(t * bpm / 60 / plPr) * plPr; //rounding, so that SS doesn't flag it as AI made
            if (beat % 0.015625 != 0) System.err.println("NOTE NOT PLACED CORRECTLY!");
            jsonResult.append("{\"_time\":").append(beat).append(",\"_lineIndex\":0,\"_lineLayer\":0,\"_type\":1,\"_cutDirection\":8},");
        }
        jsonResult = new StringBuilder(jsonResult.substring(0, jsonResult.length() - 1));
        jsonResult.append("],\"_obstacles\":[],\"_events\":[],\"_waypoints\":[]}");


        return jsonResult.toString();
    }


    //Reads the file and returns a String-List
    public static List<String> readFile(String filename) {
        File file = new File(filename);
        List<String> timings = new ArrayList<>();
        try {
            Scanner scanner = new Scanner(file);
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                timings.add(line);
            }
            scanner.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return timings;
    }

    //Overwrites a file with the String data
    public static void overwriteFile(String filePath, String data) {
        File file = new File(filePath);

        try {
            FileOutputStream fos = new FileOutputStream(file, false);
            fos.write(data.getBytes());
            fos.close();

            System.out.println("File overwritten successfully!");
        } catch (IOException e) {
            System.out.println("An error occurred while overwriting the file: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
