package DataManager;

import static DataManager.Parameters.*;


import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;

public class CreateAllNecessaryDIRsAndFiles {
    public static String[] preMadePatternsFilesToCopy = {
            DEFAULT_ONSET_GENERATION_FOLDER + "SongToOnsets.py",      //Default value: "OnsetGeneration/SongToOnsets.py",
            DEFAULT_ONSET_GENERATION_FOLDER + "ConvertSong.py",       //Default value: "OnsetGeneration/ConvertSong.py",
            README_FILE_LOCATION                                      //Default value: "README.md"
    };

    public static String[] directories = {
            DEFAULT_ONSET_GENERATION_FOLDER,
            ONSET_GENERATION_FOLDER_PATH_INPUT,
            ONSET_GENERATION_FOLDER_PATH_OUTPUT
    };

    public static String config = """
            defaultPath:C:/Program Files (x86)/Steam/steamapps/common/Beat Saber/Beat Saber_Data/CustomWIPLevels //This should link to your WIP folder
            verbose:false //It is not recommended to change this except for debugging purposes.
            dark-mode:false
            save_new_maps_to_default_path:true //If true, new maps will be saved to the default path. If false, new maps will be saved to ./OnsetGeneration/out/""";

    public static void createAllNecessaryDIRsAndFiles() {
        //Checking dependencies:
        if (isPipInstalled()) System.out.println("Pip is installed.");
        else installPip();


        //Checking if the directories exist.
        //If yes, then don't create them again
        File f3 = new File(CONFIG_FILE_LOCATION);             //Default Value: "./config.txt"
        if (f3.exists() && f3.isFile()) {
            return;
        }
        System.out.println("Creating all necessary directories and files.");


        createConfigFile();
        createDirectories(CreateAllNecessaryDIRsAndFiles.directories);

        extractFilesFromJar(CreateAllNecessaryDIRsAndFiles.preMadePatternsFilesToCopy);
    }

    /**
     * Creates a config file with default values.
     */
    private static void createConfigFile() {

        FileManager.overwriteFile(CONFIG_FILE_LOCATION, config);
    }

    /**
     * Creates all the directories that are needed for the program to work.
     */
    public static void createDirectories(String[] dirs) {
        try {
            for (String dir : dirs) {
                Files.createDirectories(Paths.get(dir));
            }
        } catch (IOException e) {
            System.err.println("There has been an Exception while creating the files:\n");
            e.printStackTrace();
        }
    }


    /**
     * Extracts files from the JAR and copies them to the destination directory.
     *
     * @param filesToCopy String array of the relative file paths of the files that should be moved out of the JAR.
     */
    private static void extractFilesFromJar(String[] filesToCopy) {
        // Get the current ClassLoader
        ClassLoader classLoader = CreateAllNecessaryDIRsAndFiles.class.getClassLoader();

        try {
            for (String filePathToCopy : filesToCopy) {
                filePathToCopy = filePathToCopy.replaceAll("\\./", "");
                InputStream inputStream = classLoader.getResourceAsStream(filePathToCopy);
                File f = new File(filePathToCopy);
                System.out.println(f.getAbsolutePath());

                if (inputStream != null) {

                    File destinationFile = new File(DEFAULT_EXPORT_PATH + filePathToCopy);
                    destinationFile.getParentFile().mkdirs();

                    OutputStream outputStream = new FileOutputStream(destinationFile);
                    byte[] buffer = new byte[1024];
                    int bytesRead;
                    while ((bytesRead = inputStream.read(buffer)) != -1) {
                        outputStream.write(buffer, 0, bytesRead);
                    }

                    outputStream.close();
                    inputStream.close();

                    System.out.println("File copied: " + filePathToCopy);
                } else {
                    System.out.println("File not found in the JAR: " + filePathToCopy);
                    return;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Checks if ffmpeg is installed.
     *
     * @return true if ffmpeg is installed, false if not.
     */
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

    /**
     * Checks if python is installed.
     *
     * @return true if python is installed, false if not.
     */
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

    /**
     * Checks if pip is installed.
     *
     * @return true if pip is installed, false if not.
     */
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

    /**
     * Installs pip.
     */
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

    /**
     * Installs the dependencies.
     *
     * @return true if the dependencies have been installed, false if not.
     */
    public static boolean installDependencies() {
        try {
            ProcessBuilder processBuilder = new ProcessBuilder("pip", "install", "pydub");
            ProcessBuilder processBuilder2 = new ProcessBuilder("pip", "install", "librosa");
            ProcessBuilder processBuilder3 = new ProcessBuilder("pip", "install", "numpy");
            ProcessBuilder processBuilder4 = new ProcessBuilder("pip", "install", "--upgrade", "git+https://github.com/CPJKU/madmom.git", "--user");
            //pip install --upgrade --no-deps --force-reinstall 'git+https://github.com/CPJKU/madmom.git' --user
            Process p = processBuilder.start();
            Process p2 = processBuilder2.start();
            Process p3 = processBuilder3.start();
            Process p4 = processBuilder4.start();

            int exitCode = p.waitFor();
            int exitCode2 = p2.waitFor();
            int exitCode3 = p3.waitFor();
            int exitCode4 = p4.waitFor();


            if (exitCode2 == 0 && exitCode == 0 && exitCode3 == 0 && exitCode4 == 0) {
                System.out.println("Dependencies has been installed.");
                return true;
            } else {
                System.err.println("Failed to install dependencies. Are they already installed?");
                return false;
            }
        } catch (IOException | InterruptedException e) {
            System.out.println("Failed to install dependencies.");
            e.printStackTrace();
            return false;
        }
    }

}
