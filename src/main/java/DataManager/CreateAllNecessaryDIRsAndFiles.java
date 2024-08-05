package DataManager;

import static DataManager.Parameters.*;


import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;

public class CreateAllNecessaryDIRsAndFiles {
    public static final String[] preMadePatternsFilesToCopy = {
            DEFAULT_ONSET_GENERATION_FOLDER + "SongToOnsets.py",      //Default value: "OnsetGeneration/SongToOnsets.py",
            DEFAULT_ONSET_GENERATION_FOLDER + "ConvertSong.py",       //Default value: "OnsetGeneration/ConvertSong.py",
            README_FILE_LOCATION                                      //Default value: "README.md"
    };

    public static final String[] directories = {
            DEFAULT_ONSET_GENERATION_FOLDER,
            ONSET_GENERATION_FOLDER_PATH_INPUT,
            ONSET_GENERATION_FOLDER_PATH_OUTPUT
    };

    public static final String config = """
            defaultPath:C:/Program Files (x86)/Steam/steamapps/common/Beat Saber/Beat Saber_Data/CustomWIPLevels //This should link to your WIP folder
            verbose:false //It is not recommended to change this except for debugging purposes.
            dark-mode:false
            save_new_maps_to_default_path:true //If true, new maps will be saved to the default path. If false, new maps will be saved to ./OnsetGeneration/out/""";

    public static void createAllNecessaryDIRsAndFiles() {
        //Checking dependencies:
        if (isPipInstalled()) {
            logger.info("Pip is installed.");
            System.out.println("Pip is installed.");
        }
        else installPip();


        logger.info("Checking if directories exist");
        //Checking if the directories exist.
        //If yes, then don't create them again
        File f3 = new File(CONFIG_FILE_LOCATION);             //Default Value: "./config.txt" //TODO: Ändern
        if (f3.exists() && f3.isFile()) {
            logger.info("config file exists");
            return;
        }
        logger.info("Creating all necessary directories and files.");
        System.out.println("Creating all necessary directories and files.");


        createConfigFile();
        createDirectories(CreateAllNecessaryDIRsAndFiles.directories);

        extractFilesFromJar(CreateAllNecessaryDIRsAndFiles.preMadePatternsFilesToCopy);
    }

    /**
     * Creates a config file with default values.
     */
    private static void createConfigFile() {
        logger.info("Creating config file.");
        FileManager.overwriteFile(CONFIG_FILE_LOCATION, config);
    }

    /**
     * Creates all the directories that are needed for the program to work.
     */
    public static void createDirectories(String[] dirs) {
        try {
            logger.info("Creating directories.");
            for (String dir : dirs) {
                logger.info("Creating directory: {}", dir);
                Files.createDirectories(Paths.get(dir));
            }
        } catch (IOException e) {
            logger.fatal("There has been an Exception while creating the directories: {}", e.getMessage());
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
        logger.info("Extracting files from jar.");

        try {
            for (String filePathToCopy : filesToCopy) {
                logger.info("Extracting file: {}", filePathToCopy);
                filePathToCopy = filePathToCopy.replaceAll("\\./", "");
                InputStream inputStream = classLoader.getResourceAsStream(filePathToCopy);
                File f = new File(filePathToCopy);
                logger.info("Path: {}", f.getAbsolutePath());
                System.out.println(f.getAbsolutePath());

                if (inputStream != null) {

                    File destinationFile = new File(DEFAULT_EXPORT_PATH + filePathToCopy);
                    if (!destinationFile.getParentFile().mkdirs()) {
                        System.err.println("[FATAL]: Something went wrong while trying to create folder!");
                        logger.fatal("Something went wrong while trying to create folder: {}", destinationFile.getParentFile().getAbsolutePath());
                    }

                    OutputStream outputStream = new FileOutputStream(destinationFile);
                    byte[] buffer = new byte[1024];
                    int bytesRead;
                    while ((bytesRead = inputStream.read(buffer)) != -1) {
                        outputStream.write(buffer, 0, bytesRead);
                    }

                    outputStream.close();
                    inputStream.close();

                    logger.info("File copied: {}", filePathToCopy);
                    System.out.println("File copied: " + filePathToCopy);
                } else {
                    logger.error("File not found in the JAR: {}", filePathToCopy);
                    System.out.println("File not found in the JAR: " + filePathToCopy);
                    return;
                }
            }
        } catch (IOException e) {
            logger.fatal("There has been an Exception while creating the files: {}", e.toString());
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
            logger.info("Checking if FFMpeg is installed.");
            ProcessBuilder processBuilder = new ProcessBuilder("ffmpeg", "-version");
            Process process = processBuilder.start();
            int exitCode = process.waitFor();

            logger.info("FFmpeg installation exited with code {}", exitCode);
            return exitCode == 0;
        } catch (IOException | InterruptedException e) {
            logger.error("Something went wrong with the installation of FFMPEG: {}", e.toString());
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
            logger.info("Checking if Python is installed.");
            ProcessBuilder processBuilder = new ProcessBuilder("python", "--version");
            Process process = processBuilder.start();
            int exitCode = process.waitFor();

            logger.info("Python installation exited with code {}", exitCode);
            return exitCode == 0;
        } catch (IOException | InterruptedException e) {
            logger.error("Something went wrong with the installation of Python: {}", e.getMessage());
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
            logger.info("Checking if Pip is installed.");
            ProcessBuilder processBuilder = new ProcessBuilder("pip", "--version");
            Process process = processBuilder.start();
            int exitCode = process.waitFor();

            logger.info("Pip installation exited with code {}", exitCode);
            return exitCode == 0;
        } catch (IOException | InterruptedException e) {
            logger.error("Something went wrong with the installation of Pip: {}", e.getMessage());
            return false; // An exception occurred or pip is not found
        }
    }

    /**
     * Installs pip.
     */
    public static void installPip() {
        try {
            logger.info("Installing Pip...");
            ProcessBuilder processBuilder = new ProcessBuilder("python", "-m", "ensurepip");
            processBuilder.start();

            logger.info("Pip has been installed.");
            System.out.println("Pip has been installed.");
        } catch (IOException e) {
            logger.error("Failed to install Pip.");
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
            logger.info("Installing dependencies...");
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

            logger.info("Dependency pydub installation exited with code {}", exitCode);
            logger.info("Dependency librosa installation exited with code {}", exitCode2);
            logger.info("Dependency numpy installation exited with code {}", exitCode3);
            logger.info("Dependency upgrade installation exited with code {}", exitCode4);

            if (exitCode2 == 0 && exitCode == 0 && exitCode3 == 0 && exitCode4 == 0) {
                logger.info("Dependencies have been installed.");
                System.out.println("Dependencies have been installed.");
                return true;
            } else {
                logger.warn("Failed to install dependencies. Are they already installed?");
                System.err.println("Failed to install dependencies. Are they already installed?");
                return false;
            }
        } catch (IOException | InterruptedException e) {
            logger.error("Failed to install dependencies.");
            System.out.println("Failed to install dependencies.");
            return false;
        }
    }

}
