package DataManager.Config;

import DataManager.Parameters;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.awt.*;
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
    @JsonProperty("map-generator")
    public MapGenerator mapGenerator = new MapGenerator();
    @JsonProperty("development")
    public Development development = new Development();
    @JsonProperty("default-pat-metadata")
    public DefaultPatMetadata defaultPatMetadata = new DefaultPatMetadata();

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
        @JsonProperty("autoload-default-pattern")
        public boolean autoloadDefaultPattern = true;
    }

    @lombok.Getter @lombok.Setter @lombok.ToString
    public static class DefaultPaths
    {
        @JsonProperty("wip-folder")
//        public String wipFolder = "C:/Program Files (x86)/Steam/steamapps/common/Beat Saber/Beat Saber_Data/CustomWIPLevels/";
        public String wipFolder = "./CustomWIPLevels";
        @JsonProperty("config")
        public String config = "./config.json";
        @JsonProperty("readme")
        public String readme = "./README.md";
        @JsonProperty("pattern-folder")
        public String patternFolder = (Parameters.executedByJar ? "./src/main/resources/" : "")  + "Patterns/";
        @JsonProperty("default-pattern")
        public String defaultPattern = (Parameters.executedByJar ? "./src/main/resources/" : "") + "Patterns/Normal_Pattern_ISeeFire.txt";
        @JsonProperty("easy-default-pattern")
        public String defaultEasyPattern = (Parameters.executedByJar ? "./src/main/resources/" : "") + "Patterns/Easy_Pattern_AllGroupedV1.pat";
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
    public static class DefaultPatMetadata
    {
        public String name = "ISeeFire";
        public int bpm = 170;
        public double nps = 5.91;
        public List<String> difficulties = List.of("Easy", "Normal", "Hard", "Expert", "ExpertPlus", "EasyStandard", "StandardEasy", "NormalStandard", "StandardNormal", "HardStandard", "StandardHard", "ExpertStandard", "StandardExpert", "ExpertPlusStandard", "StandardExpertPlus");
        public List<String> tags = List.of("Accuracy", "Balanced", "Challenge", "Dance", "Fitness", "Speed", "Tech", "NULL");
        public List<String> genres = List.of("Alternative", "Ambient", "Anime", "Classical & Orchestral", "Comedy & Meme", "Dance", "Drum and Bass", "Dubstep", "Electronic", "Folk & Acoustic", "Funk & Disco", "Hardcore", "Hip Hop & Rap", "Holiday", "House", "Indie", "Instrumental", "J-Pop", "J-Rock", "Jazz", "K-Pop", "Kids & Family", "Metal", "Nightcore", "Pop", "Punk", "R&B", "Rock", "Soul", "Speedcore", "Swing", "TV & Film", "Techno", "Trance", "Video Game", "Vocaloid", "NULL");
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
        @JsonProperty("delete-wav-after-conversion")
        public boolean deleteWavAfterConversion = true;
    }

    @lombok.Getter @lombok.Setter @lombok.ToString
    public static class Development
    {
        @Deprecated
        @JsonProperty("export-database")
        public boolean exportDatabase = false;
        @JsonProperty("autoload-default-map-for-testing")
        public boolean autoloadDefaultMapForTesting = false;
        @JsonProperty("default-path-for-autoload-map")
        public String defaultPathForAutoloadMap = (Parameters.executedByJar ? "src/main/resources/" : "") + "Beat Saber_Data/CustomWIPLevels/3df62/ExpertPlusStandard.dat";
    }
}
