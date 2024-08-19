package DataManager.Config;

import DataManager.Database.DatabaseOperations.DifficultyEntityOperations;
import DataManager.Database.DatabaseOperations.GenreEntityOperations;
import DataManager.Database.DatabaseOperations.TagEntityOperations;
import DataManager.Parameters;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.awt.*;
import java.io.File;
import java.util.List;

@lombok.Getter @lombok.Setter @lombok.ToString
public class Configuration
{
    @JsonProperty("global")
    public Global global = new Global();
    @JsonProperty("default-paths")
    public DefaultPaths defaultPath = new DefaultPaths();
    @JsonProperty("colors")
    public Colors colors = new Colors();
    @JsonProperty("generated-default-paths")
    public GeneratedDefaultPaths generatedDefaultPaths = new GeneratedDefaultPaths();
    @JsonProperty("database")
    public Database database = new Database();
    @JsonProperty("map-generator")
    public MapGenerator mapGenerator = new MapGenerator();
    @JsonProperty("development")
    public Development development = new Development();

    @lombok.Getter @lombok.Setter @lombok.ToString
    public static class Global
    {
        public boolean verbose = false;
        @JsonProperty("dark-mode")
        public boolean darkMode = false;
        @JsonProperty("save-maps-to-wip-folder-after-mp3-conversion")
        public boolean saveMapsToWipFolderAfterMp3Conversion = true;
        @JsonProperty("ignore-dds")
        public boolean ignoreDds = false;
        @JsonProperty("save-parity-errors-as-bookmarks")
        public boolean saveParityErrorsAsBookmarks = true;
        @JsonProperty("save-parity-errors-as-bookmarks-will-overwrite-bookmarks")
        public boolean saveParityErrorsAsBookmarksWillOverwriteBookmarks = true;
        @JsonProperty("save-did-not-place-stack-as-bookmarks")
        public boolean saveDidNotPlaceStackAsBookmark = true;
        @JsonProperty("default-map-previewer")
        public String defaultMapPreviewer = "https://skystudioapps.com/bs-viewer/";
        @JsonProperty("secondary-map-previewer")
        public String secondaryMapPreviewer = "https://allpoland.github.io/ArcViewer/";
    }

    @lombok.Getter @lombok.Setter @lombok.ToString
    public static class DefaultPaths
    {
        @JsonProperty("wip-folder")
//        public String wipFolder = "C:/Program Files (x86)/Steam/steamapps/common/Beat Saber/Beat Saber_Data/CustomWIPLevels/";
        public String wipFolder = new File("CustomWIPLevels").getAbsolutePath();
        @JsonProperty("config")
        public String config = "./config.json";
        @JsonProperty("readme")
        public String readme = "./README.md";
        @JsonProperty("pattern-folder")
        public String patternFolder = (Parameters.executedByJar ? "src/main/resources/" : "")  + "Patterns/";
        @JsonProperty("default-pattern")
        public String defaultPattern = (Parameters.executedByJar ? "src/main/resources/" : "") + "Patterns/AllGroupedV1; 98; 4;[StandardExpert];NULL;NULL.pat";
    }

    @lombok.Getter @lombok.Setter @lombok.ToString
    public static class Colors
    {
        @JsonDeserialize(using = ColorDeserializer.class)
        @JsonSerialize(using = ColorSerializer.class)
        @JsonProperty("light-mode-background-color")
        public Color lightModeBackgroundColor = Color.WHITE;

        @JsonDeserialize(using = ColorDeserializer.class)
        @JsonSerialize(using = ColorSerializer.class)
        @JsonProperty("light-mode-foreground-color")
        public Color lightModeForegroundColor = Color.BLACK;

        @JsonDeserialize(using = ColorDeserializer.class)
        @JsonSerialize(using = ColorSerializer.class)
        @JsonProperty("dark-mode-background-color")
        public Color darkModeBackgroundColor = Color.DARK_GRAY;

        @JsonDeserialize(using = ColorDeserializer.class)
        @JsonSerialize(using = ColorSerializer.class)
        @JsonProperty("dark-mode-foreground-color")
        public Color darkModeForegroundColor = Color.WHITE;
    }

    @lombok.Getter @lombok.Setter @lombok.ToString
    public static class GeneratedDefaultPaths
    {
        @JsonProperty("onset-generation-folder")
        public String onsetGenerationFolder = "./OnsetGeneration/";
        @JsonProperty("onset-generation-folder-mp3-input")
        public String onsetGenerationFolderMp3Input = "./OnsetGeneration/mp3Files/";
        @JsonProperty("onset-generation-output-folder")
        public String onsetGenerationOutputFolder = "./OnsetGeneration/output/";
        @JsonProperty("beatsaver-map-info-path")
        public String beatsaverMapInfoPath = "C:\\Program Files (x86)\\Steam\\steamapps\\common\\Beat Saber\\BeatSaberMaps\\MapInfos\\";
        @JsonProperty("beatsaver-maps-path")
        public String beatsaverMapsPath = "./BeatSaberMaps/Maps/";
        @JsonProperty("extract-from-jar-path")
        public String extractFromJarPath = "./";
    }

    @lombok.Getter @lombok.Setter @lombok.ToString
    public static class Database
    {
        public Settings settings = new Settings();
        @JsonProperty("use-database")
        public boolean useDatabase = false;
        @JsonProperty("default-pat-metadata")
        public DefaultPatMetadata defaultPatMetadata = new DefaultPatMetadata();

        @lombok.Getter @lombok.Setter @lombok.ToString
        public static class Settings
        {
            public Connection connection = new Connection();
            public String dialect = "org.hibernate.dialect.MySQLDialect";
            public Hibernate hibernate = new Hibernate();

            @lombok.Getter @lombok.Setter @lombok.ToString
            public static class Connection
            {
                @JsonProperty("driver_class")
                public String driverClass = "com.mysql.cj.jdbc.Driver";
            }

            @lombok.Getter @lombok.Setter @lombok.ToString
            public static class Hibernate
            {
                @JsonProperty("db-connection")
                public DBConnection connection = new DBConnection();

                @lombok.Getter @lombok.Setter @lombok.ToString
                public static class DBConnection
                {
                    public String url = "jdbc:mysql://localhost:3306/beatKenja";
                    public String username = "root";
                    public String password = "root";
                }
            }
        }

        @lombok.Getter @lombok.Setter @lombok.ToString
        public static class DefaultPatMetadata
        {
            public String name = "ISeeFire";
            public int bpm = 170;
            public double nps = 5.91;
            public List<String> difficulties = DifficultyEntityOperations.getAllDifficulties();
            public List<String> tags = TagEntityOperations.getAllTags();
            public List<String> genres = GenreEntityOperations.getAllGenres();
        }
    }

    @lombok.Getter @lombok.Setter @lombok.ToString
    public static class MapGenerator
    {
        @JsonProperty("default-bpm")
        public int defaultBpm = 120;
        @JsonProperty("fix-placements")
        public boolean fixPlacements = true;
        @JsonProperty("default-placement-precision")
        public double defaultPlacementPrecision = (double) 1/16;
        @JsonProperty("show-spectogram-when-generating-onsets")
        public boolean showSpectogramWhenGeneratingOnsets = true;
        @JsonProperty("fix-inconsistent-timings-when-map-gets-faster")
        public boolean fixInconsistentTimings = true;
        @JsonProperty("fix-inconsistent-timings-when-map-gets-faster-than-nps")
        public double fixInconsistentTimingsFasterThan = 8;
        @JsonProperty("fix-inconsistent-timings-when-more-than-X-notes-are-in-the-section")
        public double fixInconsistentTimingsNoteAmountThreshold = 4;
        @JsonProperty("plot-nps-distribution")
        public boolean plotNpsDistribution = true;
    }

    @lombok.Getter @lombok.Setter @lombok.ToString
    public static class Development
    {
        @JsonProperty("export-database")
        public boolean exportDatabase = false;
        @JsonProperty("autoload-default-map-for-testing")
        public boolean autoloadDefaultMapForTesting = true;
        @JsonProperty("default-path-for-autoload-map")
        public String defaultPathForAutoloadMap = (Parameters.executedByJar ? "src/main/resources/" : "") + "dev/3df62/ExpertPlusStandard.dat";
    }
}
