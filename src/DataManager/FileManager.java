package DataManager;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Scanner;

public class FileManager {

    //Deprecated feature. It may work or may not. No Idea
    //makes a BeatSaber Map in the json format from a simple timings file.
    //Only kept for Archive purposes
    private static String makeMap(float bpm, String filename, double plPr) {
        List<String> timings = readFile(filename);
        StringBuilder jsonResult = new StringBuilder("{\"_version\":\"2.2.0\",\"_notes\":[");

        System.out.println("number of notes: " + timings.size());
        for (String s : timings) {
            try {
                float t = Float.parseFloat(s);
                double beat = Math.round(t * bpm / 60 / plPr) * plPr; //rounding, so that SS doesn't flag it as AI made
                if (beat % 0.015625 != 0) System.err.println("NOTE NOT PLACED CORRECTLY!");
                jsonResult.append("{\"_time\":").append(beat).append(",\"_lineIndex\":0,\"_lineLayer\":0,\"_type\":1,\"_cutDirection\":8},");
            } catch (NumberFormatException e) {
                System.err.println("line in timings file is not a float!");
            }
        }
        jsonResult = new StringBuilder(jsonResult.substring(0, jsonResult.length() - 1));
        jsonResult.append("],\"_obstacles\":[],\"_events\":[],\"_waypoints\":[]}");


        return jsonResult.toString();
    }


    /**
     * Reads the file and returns a String-List
     *
     * @param filename Filename
     * @return Every line of the File in List form
     */
    public static List<String> readFile(String filename, boolean... print) {
        File file = new File(filename);
        if (!file.exists()) {
            //The following if is for testing, so that it doesn't span the console
            if (print == null || print.length == 0) System.err.println("File not found!");
            throw new NoSuchElementException("File not found: " + filename + "!");
        }

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
            return timings;
        }
        return timings;
    }

    /**
     * Overwrites a file with the String data
     *
     * @param filePath Path to the file
     * @param data     the string data with which the File should be overwritten
     */
    public static void overwriteFile(String filePath, String data, boolean... print) {
        File file = new File(filePath);

        try {
            FileOutputStream fos = new FileOutputStream(file, false);
            fos.write(data.getBytes());
            fos.close();

            //The following if is for testing, so that it doesn't span the console
            if (print == null || print.length == 0) System.out.println("File overwritten successfully!");
        } catch (IOException e) {
            System.out.println("An error occurred while overwriting the file: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
