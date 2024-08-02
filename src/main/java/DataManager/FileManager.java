package DataManager;

import UserInterface.Elements.Buttons.ButtonTypes.GlobalButtons.Exceptions.ZipCreationException;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

import static DataManager.Parameters.logger;
import static DataManager.Parameters.verbose;

@SuppressWarnings("ResultOfMethodCallIgnored")
public class FileManager {

    /**
     * Reads the file and returns a String-List
     *
     * @param filename Filename
     * @return Every line of the File in List form
     */
    public static List<String> readFile(String filename, boolean... print) {
        Path filePath = Paths.get(filename);
        if (!Files.exists(filePath)) {
            if (print == null || print.length == 0) {
                logger.error("File not found!");
            }
            throw new NoSuchElementException("File not found: " + filename + "!");
        }

        try {
            return Files.readAllLines(filePath);
        } catch (IOException e) {
            logger.error("Error reading the file: {}", e.getMessage());
            return Collections.emptyList(); // Return an empty list on failure
        }
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
            fos.flush();
            fos.close();

            //The following if is for testing, so that it doesn't span the console
            if (print == null || print.length == 0) logger.info("File overwritten successfully!");
        } catch (IOException e) {
            logger.error("An error occurred while overwriting the file: {}", e.getMessage());
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
        logger.debug("Creater Zip: SourceDir: {}", sourceDir);

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

        logger.info("Zip file extracted successfully to: {}", outputFolderPath);
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
            logger.error("{} is not a directory.", directoryPath);
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
                if (!file.delete() && verbose) logger.error("Failed to delete: {}", file.getName());

        });

    }


}
