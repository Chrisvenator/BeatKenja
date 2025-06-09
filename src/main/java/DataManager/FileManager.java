package DataManager;

import UserInterface.Elements.Buttons.ButtonTypes.GlobalButtons.Exceptions.ZipCreationException;
import lombok.Cleanup;

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

/**
 * A utility class that provides various file management operations, such as reading, writing, downloading, compressing, and extracting files.
 * The class also includes methods for filtering files based on their extensions and removing unnecessary files from directories.
 */
@SuppressWarnings("ResultOfMethodCallIgnored")
public class FileManager {

    /**
     * Reads the contents of a file and returns them as a list of strings, where each string represents a line in the file.
     *
     * @param filename The name or path of the file to read.
     * @return A list of strings, each representing a line in the file. If the file cannot be read, an empty list is returned.
     * @throws NoSuchElementException If the file does not exist.
     */
    public static List<String> readFile(String filename) {
        Path filePath = Paths.get(filename);
        if (!Files.exists(filePath)) {
            logger.error("File not found: {}", filename);
            System.err.println("File not found: " + filename);
            throw new NoSuchElementException("File not found: " + filename + "!");
        }

        try {
            return Files.readAllLines(filePath);
        } catch (IOException e) {
            logger.error("Error reading the file: {}", e.getMessage());
            System.err.println("Error reading the file: " + e.getMessage());
            return Collections.emptyList(); // Return an empty list on failure
        }
    }


    /**
     * Overwrites the specified file with the provided string data.
     *
     * @param filePath The path to the file to be overwritten.
     * @param data     The string data to write to the file.
     * @param print    An optional boolean parameter. If true, a success message is printed; otherwise, it is not.
     */
    public static void overwriteFile(String filePath, String data, boolean... print) {
        File file = new File(filePath);

        try {
            FileOutputStream fos = new FileOutputStream(file, false);
            fos.write(data.getBytes());
            fos.flush();
            fos.close();

            //The following if is for testing, so that it doesn't span the console
            if (print == null || print.length == 0) {
                logger.info("File overwritten successfully!");
                System.out.println("File overwritten successfully!");
            }
        } catch (IOException e) {
            logger.error("An error occurred while overwriting the file: {}", e.getMessage());
            System.out.println("An error occurred while overwriting the file: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Downloads a file from the specified URL and saves it to the given file path.
     *
     * @param URL      The URL from which to download the file.
     * @param filePath The path where the downloaded file should be saved.
     * @throws IOException        If an I/O error occurs during the download.
     * @throws URISyntaxException If the URL is not properly formatted.
     */
    public static void downloadFile(String URL, String filePath) throws IOException, URISyntaxException {
        @Cleanup BufferedInputStream in = new BufferedInputStream(new URI(URL).toURL().openStream());
        @Cleanup FileOutputStream fileOutputStream = new FileOutputStream(filePath);
        byte[] dataBuffer = new byte[1024];
        int bytesRead;
        while ((bytesRead = in.read(dataBuffer, 0, 1024)) != -1) {
            fileOutputStream.write(dataBuffer, 0, bytesRead);
        }
//        fileOutputStream.close();
//        in.close();

    }

    /**
     * Creates a zip file from the contents of a directory. Subdirectories are not included in the zip file.
     *
     * @param directoryPath The path to the directory whose contents should be compressed.
     * @param filename      The name of the resulting zip file.
     * @throws IOException           If an I/O error occurs during the creation of the zip file.
     * @throws ZipCreationException  If the directory is empty or contains fewer than three files.
     */
    public static void createZipFileFromDirectory(String directoryPath, String filename) throws IOException {
        String sourceDir = new File(directoryPath).getAbsolutePath();
        logger.debug("Creater Zip: SourceDir: {}", sourceDir);
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

            @Cleanup FileInputStream fis = new FileInputStream(file);
            int length;
            while ((length = fis.read(buffer)) > 0) {
                zipOut.write(buffer, 0, length);
            }
//            fis.close();
        }

        zipOut.close();

    }


    /**
     * Extracts the contents of a zip file into the specified directory. Creates the directory if it does not exist.
     * The extracted files will be placed directly in the specified directory without creating a subdirectory.
     *
     * @param zipFilePath      The path to the zip file to be extracted.
     * @param outputFolderPath The path to the directory where the contents should be extracted.
     * @throws IOException If an I/O error occurs during extraction.
     */
    public static void extractZipFilesIntoDirectory(String zipFilePath, String outputFolderPath) throws IOException {
        File outputFolder = new File(outputFolderPath);
        if (!outputFolder.exists()) {
            outputFolder.mkdirs();
        }

        @Cleanup FileInputStream fileInputStream = new FileInputStream(zipFilePath);
        @Cleanup ZipInputStream zipInputStream = new ZipInputStream(fileInputStream);

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
                @Cleanup FileOutputStream fileOutputStream = new FileOutputStream(entryFile);
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = zipInputStream.read(buffer)) != -1) {
                    fileOutputStream.write(buffer, 0, bytesRead);
                }
//                fileOutputStream.close();
            }

//            zipInputStream.closeEntry();
        }

//        zipInputStream.close();
//        fileInputStream.close();

        logger.info("Zip file extracted successfully to: {}", outputFolderPath);
    }

    /**
     * Removes all files from a directory that do not have the specified extensions.
     * This method is useful for cleaning up directories by removing unwanted file types.
     *
     * @param directoryPath      The path to the directory from which files should be removed.
     * @param possibleExtensions The extensions of the files that should be kept. All other files are deleted.
     */
    public static void removeUnnecessaryFiles(String directoryPath, String... possibleExtensions) {
        File dir = new File(directoryPath);
        if (!dir.isDirectory()) {
            logger.error("{} is not a directory.", directoryPath);
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
                if (!file.delete() && verbose) logger.error("Failed to delete: {}", file.getName());

        });

    }
    
    /**
     * Creates a ZIP file containing all generated Beat Saber map files (excluding .wav files)
     * and places it in the same destination folder.
     *
     * @param destinationFolderPath The path to the folder containing the generated map files
     * @param filename The base filename (without extension) for the ZIP file
     * @param verbose Whether to print verbose logging information
     * @throws IOException If there's an error during ZIP creation
     */
    public static void createZipPackage(String destinationFolderPath, String filename, boolean verbose) throws IOException {
        String zipFilePath = destinationFolderPath + "/" + filename + ".zip";
        File sourceFolder = new File(destinationFolderPath);
        
        try (FileOutputStream fos = new FileOutputStream(zipFilePath);
             ZipOutputStream zos = new ZipOutputStream(fos)) {
            
            File[] files = sourceFolder.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isFile() && !file.getName().endsWith(".wav") && !file.getName().endsWith(".zip")) {
                        addFileToZip(zos, file, file.getName(), verbose);
                    }
                }
            }
            
            if (verbose) {
                logger.info("Created ZIP package: {}", zipFilePath);
                System.out.println("Created ZIP package: " + zipFilePath);
            }
        }
    }
    
    /**
     * Helper method to add a single file to the ZIP archive
     *
     * @param zos The ZipOutputStream to write to
     * @param file The file to add to the ZIP
     * @param entryName The name for the entry in the ZIP file
     * @param verbose Whether to print verbose logging information
     * @throws IOException If there's an error reading the file or writing to the ZIP
     */
    private static void addFileToZip(ZipOutputStream zos, File file, String entryName, boolean verbose) throws IOException {
        try (FileInputStream fis = new FileInputStream(file)) {
            ZipEntry zipEntry = new ZipEntry(entryName);
            zos.putNextEntry(zipEntry);
            
            byte[] buffer = new byte[1024];
            int length;
            while ((length = fis.read(buffer)) > 0) {
                zos.write(buffer, 0, length);
            }
            
            zos.closeEntry();
            
            if (verbose) {
                logger.info("Added to ZIP: {}", entryName);
                System.out.println("Added to ZIP: " + entryName);
            }
        }
    }

}
