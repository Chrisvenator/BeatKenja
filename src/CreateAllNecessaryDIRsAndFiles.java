import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;

public class CreateAllNecessaryDIRsAndFiles {
    public static boolean main() {
        //Checking if the directories exist.
        //If yes, then don't create them again
        File f1 = new File("./PatternTemplates");
        File f2 = new File("./PreMadePatterns");
        if (f1.exists() && f1.isDirectory() && f2.exists() && f2.isDirectory()) {
            return true;
        }

        String[] templateFilesToCopy = {
                "PatternTemplates/Template--ISeeFire.txt"
        };
        String[] preMadePatternsFilesToCopy = {
                "PreMadePatterns/jumps.txt",
                "PreMadePatterns/test.txt",
                "PreMadePatterns/umapyoi-test.txt",
                "OnsetGeneration/SongToOnsets.py"
        };

        String config = "defaultPath:C:/Program Files/Steam/steamapps/common/Beat Saber/Beat Saber_Data/CustomWIPLevels\nverbose:false //It is not recommended to change this except for debugging purposes.";
        FileManager.overwriteFile("./config.txt", config);

        return createDirectories() && extractFilesFromJar(templateFilesToCopy, "./") && extractFilesFromJar(preMadePatternsFilesToCopy, "./");
    }

    public static boolean createDirectories() {
        try {
            Files.createDirectories(Paths.get("./PatternTemplates"));
            Files.createDirectories(Paths.get("./PreMadePatterns"));
            Files.createDirectories(Paths.get("./OnsetGeneration"));
            Files.createDirectories(Paths.get("./OnsetGeneration/mp3Files"));
            Files.createDirectories(Paths.get("./OnsetGeneration/output"));
        } catch (IOException e) {
            return false;
        }

        return true;
    }


    private static boolean extractFilesFromJar(String[] filesToCopy, String destinationDir) {
        // Get the current ClassLoader
        ClassLoader classLoader = CreateAllNecessaryDIRsAndFiles.class.getClassLoader();

        try {
            // Loop through each file path and copy it to the destination directory
            for (String fileToCopy : filesToCopy) {
                // Read the resource from the JAR
                InputStream inputStream = classLoader.getResourceAsStream(fileToCopy);

                if (inputStream != null) {
                    // Create the destination file path
                    String destinationPath = destinationDir + fileToCopy;

                    // Create parent directories if they don't exist
                    File destinationFile = new File(destinationPath);
                    destinationFile.getParentFile().mkdirs();

                    // Write the resource to the destination file
                    OutputStream outputStream = new FileOutputStream(destinationFile);
                    byte[] buffer = new byte[1024];
                    int bytesRead;
                    while ((bytesRead = inputStream.read(buffer)) != -1) {
                        outputStream.write(buffer, 0, bytesRead);
                    }

                    // Close streams
                    outputStream.close();
                    inputStream.close();

                    System.out.println("File copied: " + destinationPath);
                } else {
                    System.out.println("File not found in the JAR: " + fileToCopy);
                    return false;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

}
