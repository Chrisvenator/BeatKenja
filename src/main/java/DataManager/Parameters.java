package DataManager;

import BeatSaberObjects.Objects.Parity.Enums.ParityErrorEnum;
import DataManager.Database.DatabaseOperations.DifficultyEntityOperations;
import DataManager.Database.DatabaseOperations.GenreEntityOperations;
import DataManager.Database.DatabaseOperations.TagEntityOperations;
import DataManager.Records.Configuration;
import DataManager.Records.PatMetadata;
import UserInterface.Elements.Buttons.ButtonTypes.GlobalButtons.Buttons.Common.DifficultyFileNameExtensionFilter;
import javafx.util.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.persistence.EntityManager;
import javax.persistence.Persistence;
import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.logging.Level;

import static BeatSaberObjects.Objects.Parity.Enums.ParityErrorEnum.*;

@SuppressWarnings("unused")
public class Parameters {
    public static final Logger logger = LogManager.getLogger();

    private static final Configuration config = new ConfigLoader("./config.json").getConfig();
    private static final Configuration.Colors COLORS = config.colors;
    private static final Configuration.DefaultPaths DEFAULT_PATHS = config.defaultPath;
    private static final Configuration.Database DATABASE = config.database;
    private static final Configuration.GeneratedDefaultPaths GENERATED_DEFAULT_PATHS = config.generatedDefaultPaths;
    private static final Configuration.Development DEVELOPMENT = config.development;
    private static final Configuration.Global GLOBAL = config.global;
    private static final Configuration.MapGenerator MAP_GENERATOR = config.mapGenerator;

    //----------------------------------- config start ----------------------------------------

    // Global
    public static boolean verbose = GLOBAL.verbose; //For debugging purposes. It prints EVERYTHING
    public static boolean DARK_MODE = GLOBAL.darkMode;
    public static boolean saveNewMapsToDefaultPath = GLOBAL.saveMapsToWipFolderAfterMp3Conversion;
    public static boolean ignoreDDs = GLOBAL.ignoreDds;
    public static final boolean SAVE_PARITY_ERRORS_AS_BOOKMARKS = GLOBAL.saveParityErrorsAsBookmarks;
    public static final boolean SAVE_PARITY_ERRORS_AS_BOOKMARKS_WILL_OVERWRITE_BOOKMARKS = GLOBAL.saveParityErrorsAsBookmarksWillOverwriteBookmarks;
    public static final boolean SAVE_DID_NOT_PLACE_STACK_AS_BOOKMARK = GLOBAL.saveDidNotPlaceStackAsBookmark;
    public static final String mapViewerURL = GLOBAL.defaultMapPreviewer; //https://skystudioapps.com/bs-viewer/  or  https://allpoland.github.io/ArcViewer/

    // Default paths
    public static String DEFAULT_PATH = DEFAULT_PATHS.wipFolder;
    public static final String CONFIG_FILE_LOCATION = DEFAULT_PATHS.config;
    public static final String README_FILE_LOCATION = DEFAULT_PATHS.readme;
    public static final String DEFAULT_PATTERN_PATH = DEFAULT_PATHS.defaultPattern;
    //    public static final String DEFAULT_PATTERN_PATH = "C:\\Program Files (x86)\\Steam\\steamapps\\common\\Beat Saber\\_SongsToTimings\\src\\main\\resources\\MapTemplates\\AllGroupedV1; 98; 4;[StandardExpert];NULL;NULL.pat";

    // Program-Paths
    public static final String DEFAULT_ONSET_GENERATION_FOLDER = GENERATED_DEFAULT_PATHS.onsetGenerationFolder;
    public static final String ONSET_GENERATION_FOLDER_PATH_INPUT = GENERATED_DEFAULT_PATHS.onsetGenerationFolderMp3Input;
    public static final String ONSET_GENERATION_FOLDER_PATH_OUTPUT = saveNewMapsToDefaultPath ? DEFAULT_PATH : GENERATED_DEFAULT_PATHS.onsetGenerationOutputFolder;
    public static final String DEFAULT_BEATSAVER_MAP_INFO_PATH = GENERATED_DEFAULT_PATHS.beatsaverMapInfoPath;
    public static final String DEFAULT_BEATSAVER_MAPS_PATH = GENERATED_DEFAULT_PATHS.beatsaverMapsPath;
    public static final String DEFAULT_EXPORT_PATH = GENERATED_DEFAULT_PATHS.extractFromJarPath;

    // Dev
    public static final boolean EXPORT_DATABASE = DEVELOPMENT.exportDatabase;
    public static final boolean AUTOLOAD_DEFAULT_MAP_for_testing = DEVELOPMENT.autoloadDefaultMapForTesting;
    public static final boolean useDatabase = DEVELOPMENT.exportDatabase;
    public static final String DEFAULT_PATH_FOR_AUTOLOAD_MAP = DEVELOPMENT.defaultPathForAutoloadMap;

    // Colors
    public static final Color lightModeBackgroundColor = COLORS.lightModeBackgroundColor;
    public static final Color lightModeForegroundColor = COLORS.lightModeForegroundColor;
    public static final Color darkModeBackgroundColor =  COLORS.darkModeBackgroundColor;
    public static final Color darkModeForegroundColor =  COLORS.darkModeForegroundColor;

    //Note Generator settings:
    public static final double BPM = MAP_GENERATOR.defaultBpm;
    public static final double PLACEMENT_PRECISION = MAP_GENERATOR.defaultPlacementPrecision; //Placement Precision
    public static final boolean FIX_PLACEMENTS = MAP_GENERATOR.fixPlacements; //should the timings be fixed so that BeatSaver doesn't flag it as AI made?
    public static final boolean SHOW_SPECTOGRAM_WHEN_GENERATING_ONSETS = MAP_GENERATOR.showSpectogramWhenGeneratingOnsets;

    // Database
    public static final PatMetadata DEFAULT_PATTERN_METADATA = new PatMetadata(DATABASE.defaultPatMetadata.name, DATABASE.defaultPatMetadata.bpm, DATABASE.defaultPatMetadata.nps, DATABASE.defaultPatMetadata.difficulties, DATABASE.defaultPatMetadata.tags, DATABASE.defaultPatMetadata.genres);
    public static final Map<String, String> DATABASE_SETTINGS = Map.of(
            "connection.driver_class",       DATABASE.settings.connection.driverClass,
            "dialect",                       DATABASE.settings.dialect,
            "hibernate.connection.url",      config.database.settings.hibernate.connection.url,
            "hibernate.connection.username", config.database.settings.hibernate.connection.username,
            "hibernate.connection.password", config.database.settings.hibernate.connection.password
    );


    //------------------------------------------ Config end --------------------------------------------------------------

    //Variables:
    public static long SEED = 133742069;
    public static Random RANDOM = new Random(SEED);
    public static String filePath;

    // Common
    public static final EntityManager entityManager = useDatabase ? Persistence.createEntityManagerFactory("default").createEntityManager() : null;
    public static final JFileChooser FILE_CHOOSER = new JFileChooser(DEFAULT_PATH.trim());
    public static final List<String> MAP_TAGS = TagEntityOperations.getAllTags();
    public static final List<String> MUSIC_GENRES = GenreEntityOperations.getAllGenres();
    public static final List<String> DIFFICULTIES = DifficultyEntityOperations.getAllDifficulties();
    public static final DifficultyFileNameExtensionFilter MAP_FILE_FORMAT = new DifficultyFileNameExtensionFilter("BeatSaber Maps (*.dat) or Pattern files (*.pat)", new String[]{"dat", "pat"}, new String[]{"info.dat", "BPMInfo.dat"});
    public static final List<Pair<Float, ParityErrorEnum>> PARITY_ERRORS_LIST = new ArrayList<>();

    public static final Map<ParityErrorEnum, Color> PARITY_ERRORS_COLORS_MAP = Map.of(
            PARITY_BREAK,             Color.RED,
            SHARP_ANGLE,              Color.GREEN,
            NOTE_OUTSIDE_OF_GRID,     Color.BLUE,
            NOTE_INSIDE_ANOTHER_NOTE, Color.CYAN,
            DID_NOT_PLACE_NOTE,       Color.WHITE,
            DID_NOT_PLACE_STACK,      Color.BLACK
    );

    static {
        java.util.logging.Logger.getLogger("org.hibernate").setLevel(Level.WARNING);
        FILE_CHOOSER.setFileFilter(MAP_FILE_FORMAT);
        if (DARK_MODE) FILE_CHOOSER.setForeground(Color.white);
    }




    @Deprecated
    public static final double MADMOM_ONSET_GENERATION_ONSET_CERTAINTY = 7.5;   //For madmom onset detection only! This is an arbitrary value. The lower the value, the more onsets will be detected
    @Deprecated
    public static final double MADMOM_ONSET_GENERATION_MINIMUM_PROXIMITY = 0.1; //For madmom onset detection only! Minimum proximity between onsets in seconds




}





































