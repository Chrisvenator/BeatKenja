package DataManager;

import BeatSaberObjects.Objects.Enums.ParityErrorEnum;
import DataManager.Config.Configuration;
import DataManager.Records.PatMetadata;
import UserInterface.Elements.Buttons.ButtonTypes.GlobalButtons.Buttons.Common.DifficultyFileNameExtensionFilter;
import javafx.util.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.logging.Level;

import static BeatSaberObjects.Objects.Enums.ParityErrorEnum.*;

@SuppressWarnings("unused")
public class Parameters {
    public static final Logger logger = LogManager.getLogger(Parameters.class);

    public static final ConfigLoader configLoader = new ConfigLoader("./config.json");
    private static final Configuration config = configLoader.getConfig();
    private static final Configuration.Colors COLORS = config.colors;
    private static final Configuration.DefaultPaths DEFAULT_PATHS = config.defaultPath;
    private static final Configuration.DefaultPatMetadata DEFAULT_PAT_METADATA = config.defaultPatMetadata;
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
    public static final boolean AUTOLOAD_DEFAULT_PATTERNS = GLOBAL.autoloadDefaultPattern;
    public static final boolean SAVE_PARITY_ERRORS_AS_BOOKMARKS = GLOBAL.saveParityErrorsAsBookmarks;
    public static final boolean SAVE_PARITY_ERRORS_AS_BOOKMARKS_WILL_OVERWRITE_BOOKMARKS = GLOBAL.saveParityErrorsAsBookmarksWillOverwriteBookmarks;
    public static final boolean SAVE_DID_NOT_PLACE_STACK_AS_BOOKMARK = GLOBAL.saveDidNotPlaceStackAsBookmark;
    public static final String mapViewerURL = GLOBAL.defaultMapPreviewer; //https://skystudioapps.com/bs-viewer/  or  https://allpoland.github.io/ArcViewer/

    // Default paths
    public static String DEFAULT_PATH = DEFAULT_PATHS.wipFolder;
    public static final String CONFIG_FILE_LOCATION = DEFAULT_PATHS.config;
    public static final String README_FILE_LOCATION = DEFAULT_PATHS.readme;
    public static final String DEFAULT_PATTERN_PATH = DEFAULT_PATHS.defaultPattern;
    public static final String DEFAULT_EASY_PATTERN_PATH = DEFAULT_PATHS.defaultEasyPattern;
    //    public static final String DEFAULT_PATTERN_PATH = "C:\\Program Files (x86)\\Steam\\steamapps\\common\\Beat Saber\\_SongsToTimings\\src\\main\\resources\\MapTemplates\\Easy_Pattern_AllGroupedV1.pat";
    public static final String DEFAULT_PATTERN_FOLDER_PATH = DEFAULT_PATHS.patternFolder;

    // Program-Paths
    public static final String DEFAULT_ONSET_GENERATION_FOLDER = GENERATED_DEFAULT_PATHS.onsetGenerationFolder;
    public static final String ONSET_GENERATION_FOLDER_PATH_INPUT = GENERATED_DEFAULT_PATHS.onsetGenerationFolderMp3Input;
    public static final String ONSET_GENERATION_FOLDER_PATH_OUTPUT = saveNewMapsToDefaultPath ? DEFAULT_PATH : GENERATED_DEFAULT_PATHS.onsetGenerationOutputFolder;
    public static final String DEFAULT_BEATSAVER_MAP_INFO_PATH = GENERATED_DEFAULT_PATHS.beatsaverMapInfoPath;
    public static final String DEFAULT_BEATSAVER_MAPS_PATH = GENERATED_DEFAULT_PATHS.beatsaverMapsPath;
    public static final String DEFAULT_EXPORT_PATH = GENERATED_DEFAULT_PATHS.extractFromJarPath;

    // Dev
    public static final boolean AUTOLOAD_DEFAULT_MAP_for_testing = DEVELOPMENT.autoloadDefaultMapForTesting;
    public static final String DEFAULT_PATH_FOR_AUTOLOAD_MAP = DEVELOPMENT.defaultPathForAutoloadMap;

    // Colors
    public static final Color lightModeBackgroundColor = COLORS.lightModeBackgroundColor;
    public static final Color lightModeForegroundColor = COLORS.lightModeForegroundColor;
    public static final Color darkModeBackgroundColor =  COLORS.darkModeBackgroundColor;
    public static final Color darkModeForegroundColor =  COLORS.darkModeForegroundColor;

    //Note Generator settings:
    public static double BPM = MAP_GENERATOR.defaultBpm;
    public static final double PLACEMENT_PRECISION = MAP_GENERATOR.defaultPlacementPrecision; //Placement Precision
    public static final boolean FIX_PLACEMENTS = MAP_GENERATOR.fixPlacements; //should the timings be fixed so that BeatSaver doesn't flag it as AI made?
    public static final boolean SHOW_SPECTOGRAM_WHEN_GENERATING_ONSETS = MAP_GENERATOR.showSpectogramWhenGeneratingOnsets;
    public static final boolean FIX_INCONSISTENT_TIMINGS = MAP_GENERATOR.fixInconsistentTimings;
    public static final double FIX_INCONSISTENT_TIMINGS_FASTER_THAN_NPS_THRESHOLD = MAP_GENERATOR.fixInconsistentTimingsFasterThan;
    public static final double FIX_INCONSISTENT_TIMINGS_FASTER_THAN_NPS_AMOUNT_OF_NOTES_THRESHOLD = MAP_GENERATOR.fixInconsistentTimingsNoteAmountThreshold;
    public static final boolean PLOT_NPS_DISTRIBUTION = MAP_GENERATOR.plotNpsDistribution;
    public static final boolean DELETE_WAV_AFTER_CONVERSION = MAP_GENERATOR.deleteWavAfterConversion;

    //------------------------------------------ Config end --------------------------------------------------------------

    //Variables:
    public static long SEED = 133742069;
    public static Random RANDOM = new Random(SEED);
    public static String filePath = DEFAULT_PATHS.getWipFolder();
    public static boolean executedByJar = false;
    public static final PatMetadata DEFAULT_PATTERN_METADATA = new PatMetadata(DEFAULT_PAT_METADATA.name, DEFAULT_PAT_METADATA.bpm, DEFAULT_PAT_METADATA.nps, DEFAULT_PAT_METADATA.difficulties, DEFAULT_PAT_METADATA.tags, DEFAULT_PAT_METADATA.genres);

    // Common
    public static final JFileChooser FILE_CHOOSER = new JFileChooser(new File(DEFAULT_PATH.trim()));
    public static final List<String> MAP_TAGS = DEFAULT_PAT_METADATA.tags;
    public static final List<String> MUSIC_GENRES = DEFAULT_PAT_METADATA.genres;
    public static final List<String> DIFFICULTIES = DEFAULT_PAT_METADATA.difficulties;
    public static final DifficultyFileNameExtensionFilter MAP_FILE_FORMAT = new DifficultyFileNameExtensionFilter("BeatSaber Maps (*.dat) or Pattern files (*.pat)", new String[]{"dat", "pat"}, new String[]{"info.dat", "BPMInfo.dat"});
    public static final Map<String, List<Pair<Float, ParityErrorEnum>>> PARITY_ERRORS_LIST = new HashMap<>();

    public static final Map<ParityErrorEnum, Color> PARITY_ERRORS_COLORS_MAP = Map.of(
            PARITY_BREAK,             Color.RED,
            SHARP_ANGLE,              Color.GREEN,
            NOTE_OUTSIDE_OF_GRID,     Color.BLUE,
            NOTE_INSIDE_ANOTHER_NOTE, Color.CYAN,
            DID_NOT_PLACE_NOTE,       Color.WHITE,
            DID_NOT_PLACE_STACK,      Color.BLACK
    );

    // NPS Computation
    public static final float   NPS_COMPUTATION__INTERVAL_SIZE = 2f;
    public static final int     NPS_COMPUTATION__RANGE_INTERVALS = 1;
    public static final boolean NPS_COMPUTATION__IGNORE_STACKS_AND_SLIDERS = true;


    static {
        if (Objects.requireNonNull(Parameters.class.getResource("Parameters.class")).toString().startsWith("jar:")) executedByJar = true;
        java.util.logging.Logger.getLogger("org.hibernate").setLevel(Level.WARNING);
        FILE_CHOOSER.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        FILE_CHOOSER.setFileFilter(MAP_FILE_FORMAT);
        if (DARK_MODE) FILE_CHOOSER.setForeground(Color.white);
    }

    // Status Text Area Styles:
    public static final Color STATUS_TEXT_FATAL_STYLE_BACKGROUND = new Color(213, 0, 0);
    public static final Color STATUS_TEXT_FATAL_STYLE_FOREGROUND = new Color(255,255,255);
    public static final Color STATUS_TEXT_ERROR_STYLE_BACKGROUND = new Color(255, 115, 0);
    public static final Color STATUS_TEXT_ERROR_STYLE_FOREGROUND = new Color(255, 255, 255);
    public static final Color STATUS_TEXT_WARN_STYLE = new Color(222, 149, 0);
    public static final Color STATUS_TEXT_NOTICE_STYLE = new Color(183, 177, 0);
    public static final Color STATUS_TEXT_INFO_STYLE = DARK_MODE ? new Color(255,255,255): new Color(0,0,0);
    public static final Color STATUS_TEXT_DEBUG_STYLE = new Color(128, 128, 128);
    public static final Color STATUS_TEXT_CHECKING_MAP_STYLE_BACKGROUND = new Color(130, 198, 255, 161);
    public static final Color STATUS_TEXT_CHECKING_MAP_STYLE_FOREGROUND = new Color(0, 0, 0);




    @Deprecated
    public static final double MADMOM_ONSET_GENERATION_ONSET_CERTAINTY = 7.5;   //For madmom onset detection only! This is an arbitrary value. The lower the value, the more onsets will be detected
    @Deprecated
    public static final double MADMOM_ONSET_GENERATION_MINIMUM_PROXIMITY = 0.1; //For madmom onset detection only! Minimum proximity between onsets in seconds




}





































