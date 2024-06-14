package DataManager;

import DataManager.Database.DatabaseEntities.DifficultyEntity;
import DataManager.Database.DatabaseEntities.GenreEntity;
import DataManager.Database.DatabaseEntities.TagEntity;
import DataManager.Database.DatabaseOperations.DifficultyEntityOperations;
import DataManager.Database.DatabaseOperations.GenreEntityOperations;
import DataManager.Database.DatabaseOperations.TagEntityOperations;
import DataManager.Records.PatMetadata;

import javax.persistence.EntityManager;
import javax.persistence.Persistence;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;

@SuppressWarnings("unused")
public class Parameters {


    //Variables:
    public static long SEED = 133742069;
    public static Random RANDOM = new Random(SEED);
    public static String filePath;

    //config.txt:
    public static boolean verbose = true; //For debugging purposes. It prints EVERYTHING
    public static String DEFAULT_PATH = "C:/Program Files (x86)/Steam/steamapps/common/Beat Saber/Beat Saber_Data/CustomWIPLevels";
    public static boolean darkMode = false;
    public static boolean saveNewMapsToDefaultPath = true;
    public static boolean ignoreDDs = false;

    //Note Generator settings:
    public static final double BPM = 120;
    public static final double PLACEMENT_PRECISION = (double) 1 / 16; //Placement Precision
    public static final boolean FIX_PLACEMENTS = true; //should the timings be fixed so that SS doesn't flag it as AI made?

    //Try to load the config. If it doesn't exist, then use the default values
    static {
        try {
            java.util.logging.Logger.getLogger("org.hibernate").setLevel(Level.WARNING);
            UserInterface.UserInterface.loadConfig();
        } catch (Exception e) {
            System.err.println("Couldn't find config.txt. Is it created yet? Using default values...");
        }
    }

    //Definitions:
    public static final EntityManager entityManager = Persistence.createEntityManagerFactory("default").createEntityManager();

    //General Config:
    public static final String CONFIG_FILE_LOCATION = "./config.txt";
    public static final String README_FILE_LOCATION = "README.md";
    public static final PatMetadata DEFAULT_PATTERN_METADATA = new PatMetadata("ISeeFire", 170, 5.91, Collections.singletonList("StandardExpertPlus"), Collections.singletonList("Balanced"), Collections.singletonList("Metal"));
    public static final String DEFAULT_ONSET_GENERATION_FOLDER = "./OnsetGeneration/";
    public static final String ONSET_GENERATION_FOLDER_PATH_INPUT = "./OnsetGeneration/mp3Files/";
    public static final String ONSET_GENERATION_FOLDER_PATH_OUTPUT = saveNewMapsToDefaultPath ? DEFAULT_PATH : "./OnsetGeneration/output/";
    public static final String DEFAULT_BEATSAVER_MAP_INFO_PATH = "C:\\Program Files (x86)\\Steam\\steamapps\\common\\Beat Saber\\BeatSaberMaps\\MapInfos\\";
    public static final String DEFAULT_BEATSAVER_MAPS_PATH = "./BeatSaberMaps/Maps/";
    public static final String DEFAULT_EXPORT_PATH = "./";
    public static final Color lightModeBackgroundColor = Color.white;
    public static final Color lightModeForegroundColor = Color.BLACK;
    public static final Color darkModeBackgroundColor = Color.darkGray;
    public static final Color darkModeForegroundColor = Color.white;
    public static final String mapViewerURL = "https://skystudioapps.com/bs-viewer/"; //https://skystudioapps.com/bs-viewer/  or  https://skystudioapps.com/bs-viewer/
    public static final int WAVE_NOTE_GENERATION_SAMPLING_POINTS = 1000;
    public static final JFileChooser FILE_CHOOSER = new JFileChooser(DEFAULT_PATH.trim());
    public static final FileNameExtensionFilter MAP_FILE_FORMAT = new FileNameExtensionFilter("BeatSaber Maps (*.dat) or Pattern files (*.pat)", "dat", "pat");
    public static final List<String> MAP_TAGS = TagEntityOperations.getAllTags().stream().map(TagEntity::getName).toList();
    public static final List<String> MUSIC_GENRES = GenreEntityOperations.getAllGenres().stream().map(GenreEntity::getName).toList();
    public static final List<String> DIFFICULTIES = DifficultyEntityOperations.getAllDifficulties().stream().map(DifficultyEntity::getName).toList();
    public static final java.util.Map<String, String> databaseSettings = new java.util.HashMap<>();
    public static final boolean exportDatabase = true;

    public static double MADMOM_ONSET_GENERATION_ONSET_CERTAINTY = 7.5;   //For madmom onset detection only! This is an arbitrary value. The lower the value, the more onsets will be detected
    public static double MADMOM_ONSET_GENERATION_MINIMUM_PROXIMITY = 0.1; //For madmom onset detection only! Minimum proximity between onsets in seconds


    static {
        FILE_CHOOSER.setFileFilter(MAP_FILE_FORMAT);
        if (darkMode) FILE_CHOOSER.setForeground(Color.white);

        //Database settings:
        databaseSettings.put("connection.driver_class", "com.mysql.cj.jdbc.Driver");
        databaseSettings.put("dialect", "org.hibernate.dialect.MySQLDialect");
        databaseSettings.put("hibernate.connection.url", "jdbc:mysql://localhost:3306/beatKenja");
        databaseSettings.put("hibernate.connection.username", "root");
        databaseSettings.put("hibernate.connection.password", "root");
    }

}
