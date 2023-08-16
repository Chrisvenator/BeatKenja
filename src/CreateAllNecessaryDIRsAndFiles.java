import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;

public class CreateAllNecessaryDIRsAndFiles {
    public static void createAllNecessaryDIRsAndFiles() {
        //Checking dependencies:
        if (isPipInstalled()) System.out.println("Pip is installed.");
        else installPip();


        //Checking if the directories exist.
        //If yes, then don't create them again
        File f1 = new File("./PatternTemplates");
        File f2 = new File("./Patterns");
        File f3 = new File("./config.txt");
        if (f1.exists() && f1.isDirectory() && f2.exists() && f2.isDirectory() && f3.exists() && f3.isFile()) {
            return;
        }

        String[] templateFilesToCopy = {
                "PatternTemplates/Template--ISeeFire.txt"
        };
        String[] preMadePatternsFilesToCopy = {
                "Patterns/jumps.txt",
                "Patterns/test.txt",
                "Patterns/umapyoi-test.txt",
                "OnsetGeneration/SongToOnsets.py",
                "OnsetGeneration/ConvertSong.py",
                "README.md"
        };


        createConfigFile();
        createDirectories();

        extractFilesFromJar(templateFilesToCopy);
        extractFilesFromJar(preMadePatternsFilesToCopy);
    }

    private static void createConfigFile() {
        String config = """
                defaultPath:C:/Program Files/Steam/steamapps/common/Beat Saber/Beat Saber_Data/CustomWIPLevels
                verbose:false //It is not recommended to change this except for debugging purposes.
                dark-mode:false""";
        FileManager.overwriteFile("./config.txt", config);
    }

    public static void createDirectories() {
        try {
            Files.createDirectories(Paths.get("./PatternTemplates"));
            Files.createDirectories(Paths.get("./Patterns"));
            Files.createDirectories(Paths.get("./OnsetGeneration"));
            Files.createDirectories(Paths.get("./OnsetGeneration/mp3Files"));
            Files.createDirectories(Paths.get("./OnsetGeneration/output"));
        } catch (IOException e) {
            System.err.println("There has been an Exception while creating the files:\n");
            e.printStackTrace();
        }

    }


    private static void extractFilesFromJar(String[] filesToCopy) {
        // Get the current ClassLoader
        ClassLoader classLoader = CreateAllNecessaryDIRsAndFiles.class.getClassLoader();

        try {
            // Loop through each file path and copy it to the destination directory
            for (String fileToCopy : filesToCopy) {
                // Read the resource from the JAR
                InputStream inputStream = classLoader.getResourceAsStream(fileToCopy);

                if (inputStream != null) {
                    // Create the destination file path
                    String destinationPath = "./" + fileToCopy;

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
                    return;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static boolean isFFMpegInstalled() {
        try {
            ProcessBuilder processBuilder = new ProcessBuilder("ffmpeg", "-version");
            Process process = processBuilder.start();
            int exitCode = process.waitFor();

            return exitCode == 0;
        } catch (IOException | InterruptedException e) {
            return false; // An exception occurred or ffmpeg is not found
        }
    }

    public static boolean isPythonInstalled() {
        try {
            ProcessBuilder processBuilder = new ProcessBuilder("python", "--version");
            Process process = processBuilder.start();
            int exitCode = process.waitFor();

            return exitCode == 0;
        } catch (IOException | InterruptedException e) {
            return false; // An exception occurred or python is not found
        }
    }

    public static boolean isPipInstalled() {
        try {
            ProcessBuilder processBuilder = new ProcessBuilder("pip", "--version");
            Process process = processBuilder.start();
            int exitCode = process.waitFor();

            return exitCode == 0;
        } catch (IOException | InterruptedException e) {
            return false; // An exception occurred or pip is not found
        }
    }

    public static void installPip() {
        try {
            ProcessBuilder processBuilder = new ProcessBuilder("python", "-m", "ensurepip");
            processBuilder.start();

            System.out.println("Pip has been installed.");
        } catch (IOException e) {
            System.out.println("Failed to install Pip.");
            e.printStackTrace();
        }
    }

    public static boolean installDependencies() {
        try {
            ProcessBuilder processBuilder = new ProcessBuilder("pip", "install", "pydub");
            ProcessBuilder processBuilder2 = new ProcessBuilder("pip", "install", "librosa");
            Process p = processBuilder.start();
            Process p2 = processBuilder2.start();

            int exitCode = p.waitFor();
            int exitCode2 = p2.waitFor();


            System.out.println("dependencies has been installed.");
            return exitCode == exitCode2 && exitCode == 0;
        } catch (IOException | InterruptedException e) {
            System.out.println("Failed to install dependencies.");
            e.printStackTrace();
            return false;
        }
    }
}
