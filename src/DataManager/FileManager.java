package DataManager;

import UserInterface.Elements.Buttons.ButtonTypes.GlobalButtons.Exceptions.ZipCreationException;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import static DataManager.Parameters.verbose;

@SuppressWarnings("ResultOfMethodCallIgnored")
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

    public static void downloadFile(String URL, String filePath) throws IOException, URISyntaxException {
        BufferedInputStream in = new BufferedInputStream(new URI(URL).toURL().openStream());
        FileOutputStream fileOutputStream = new FileOutputStream(filePath);
        byte[] dataBuffer = new byte[1024];
        int bytesRead;
        while ((bytesRead = in.read(dataBuffer, 0, 1024)) != -1) {
            fileOutputStream.write(dataBuffer, 0, bytesRead);
        }
        fileOutputStream.close();
        in.close();

    }

    /**
     * Creates a zip file from a directory. The zip file will not include subdirectories.
     *
     * @param directoryPath The path to the directory.
     * @param filename      The name of the zip file.
     * @throws IOException If an I/O error has occurred.
     */
    public static void createZipFileFromDirectory(String directoryPath, String filename) throws IOException {
        String sourceDir = new File(directoryPath).getAbsolutePath();
        System.out.println(sourceDir);

        FileOutputStream fos = new FileOutputStream(filename);
        ZipOutputStream zipOut = new ZipOutputStream(fos);

        byte[] buffer = new byte[1024];

        // Get a list of files in the source directory
        File dir = new File(sourceDir);
        File[] files = dir.listFiles();

        if (files == null || files.length <= 3) throw new ZipCreationException("There was an error while creating the zip file!");


        for (File file : files) {
            if (file.getName().contains(".zip") || file.isDirectory()) continue;
            ZipEntry zipEntry = new ZipEntry(file.getName());
            zipOut.putNextEntry(zipEntry);

            FileInputStream fis = new FileInputStream(file);
            int length;
            while ((length = fis.read(buffer)) > 0) {
                zipOut.write(buffer, 0, length);
            }
            fis.close();
        }

        zipOut.close();

    }


    /**
     * Extracts a zip file into a directory. If the directory does not exist, it will be created. The zip file will not create a subdirectory.
     *
     * @param zipFilePath      The path to the zip file.
     * @param outputFolderPath The path to the output folder.
     * @throws IOException If an I/O error has occurred.
     */
    public static void extractZipFilesIntoDirectory(String zipFilePath, String outputFolderPath) throws IOException {
        File outputFolder = new File(outputFolderPath);
        if (!outputFolder.exists()) {
            outputFolder.mkdirs();
        }

        FileInputStream fileInputStream = new FileInputStream(zipFilePath);
        ZipInputStream zipInputStream = new ZipInputStream(fileInputStream);

        ZipEntry zipEntry;
        while ((zipEntry = zipInputStream.getNextEntry()) != null) {
            String entryName = zipEntry.getName();
            String entryPath = outputFolderPath + File.separator + entryName;

            // Create subdirectories if they don't exist
            File entryFile = new File(entryPath);
            if (zipEntry.isDirectory()) entryFile.mkdirs();
            else {
                // Create parent directories if they don't exist
                File parent = entryFile.getParentFile();
                if (!parent.exists()) parent.mkdirs();


                // Extract the entry's contents
                FileOutputStream fileOutputStream = new FileOutputStream(entryFile);
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = zipInputStream.read(buffer)) != -1) {
                    fileOutputStream.write(buffer, 0, bytesRead);
                }
                fileOutputStream.close();
            }

            zipInputStream.closeEntry();
        }

        zipInputStream.close();
        fileInputStream.close();

        if (verbose) System.out.println("Zip file extracted successfully to: " + outputFolderPath);
    }

    /**
     * Removes all files in a directory that do not have the specified extensions.
     *
     * @param directoryPath      The path to the directory.
     * @param possibleExtensions The extensions of the files that should be kept.
     */
    public static void removeUnnecessaryFiles(String directoryPath, String... possibleExtensions) {
        File dir = new File(directoryPath);
        if (!dir.isDirectory()) {
            System.err.println(directoryPath + " is not a directory.");
            return;
        }

        File[] files = dir.listFiles();

        if (files == null) return;

        Arrays.stream(files).forEach(file -> {
            if (!file.isFile()) return;
            String fileName = file.getName();
            String extension = fileName.substring(fileName.lastIndexOf(".") + 1);

            // Keep all extensions in "possibleExtensions", delete all others
            if (!String.join("", possibleExtensions).contains(extension))
                if (!file.delete() && verbose) System.err.println("Failed to delete: " + file.getName());

        });

    }


}
