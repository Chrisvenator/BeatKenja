package DataManager;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.util.List;
import java.util.Random;

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
    public static final double PLACEMENT_PRECISION = (double) 1 / 8; //Placement Precision
    public static final boolean FIX_PLACEMENTS = true; //should the timings be fixed so that SS doesn't flag it as AI made?

    //Try to load the config. If it doesn't exist, then use the default values
    static {
        try {
            UserInterface.UserInterface.loadConfig();
        } catch (Exception e) {
            System.err.println("Couldn't find config.txt. Is it created yet? Using default values...");
        }
    }

    //General Config:
    public static final String CONFIG_FILE_LOCATION = "./config.txt";
    public static final String README_FILE_LOCATION = "README.md";
    public static final String DEFAULT_PATTERN_TEMPLATE_FOLDER = "./MapTemplates/";
    public static final String DEFAULT_PATTERN_TEMPLATE = "./MapTemplates/Template--ISeeFire.txt";
    public static final String DEFAULT_ONSET_GENERATION_FOLDER = "./OnsetGeneration/";
    public static final String DEFAULT_PATTERN_PROBABILITIES_FOLDER = "./Patterns/PatternProbabilities/";
    public static final String DEFAULT_SEQUENCES_FOLDER = "./Patterns/sequences/";
    public static final String ONSET_GENERATION_FOLDER_PATH_INPUT = "./OnsetGeneration/mp3Files/";
    public static final String ONSET_GENERATION_FOLDER_PATH_OUTPUT = saveNewMapsToDefaultPath ? DEFAULT_PATH : "./OnsetGeneration/output/";
    public static final String DEFAULT_SEQUENCE_TEMPLATE_FOLDER = "./Patterns/";
    public static final String DEFAULT_BEATSAVER_MAP_INFO_PATH = "./BeatSaberMaps/MapInfos/";
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
    public static final List<String> MAP_TAGS = List.of("NULL", "Accuracy", "Balanced", "Challenge", "Dance", "Fitness", "Speed", "Tech");
    public static final List<String> MUSIC_GENRE = List.of("NULL", "Alternative", "Ambient", "Anime", "Classical-Orchestral", "Comedy-Meme", "Dance", "Drum and Bass", "Dubstep", "Electronic", "Folk-Acoustic", "Funk-Disco", "Hardcore", "Hip Hop-Rap", "Holiday", "House", "Indie", "Instrumental", "J-Pop", "J-Rock", "Jazz", "K-Pop", "Kids-Family", "Metal", "Nightcore", "Pop", "Punk", "R&B", "Rock", "Soul", "Speedcore", "Swing", "TV-Film", "Techno", "Trance", "Video Game", "Vocaloid");

    static {
        FILE_CHOOSER.setFileFilter(MAP_FILE_FORMAT);
        if (darkMode) FILE_CHOOSER.setForeground(Color.white);

    }
}
