import BeatSaberObjects.Objects.BeatSaberMap;
import BeatSaberObjects.Objects.Note;
import DataManager.FileManager;
import DataManager.Parameters;
import MapGeneration.ComplexPattern;
import MapGeneration.GenerationElements.Pattern;
import MapGeneration.PatternGeneration.LinearSlowPattern;
import UserInterface.UserInterface;

import java.io.File;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;

import static DataManager.Parameters.logger;

public class Start_CLI {
    
    /**
     * CLI configuration class to hold command line arguments
     */
    public static class CLIConfig {
        public String inputFile;
        public String outputFile;
        public String mode;
        public String patternFile;
        public boolean showHelp;
        public boolean isValid;
        
        public CLIConfig() {
            this.isValid = true;
        }
    }
    
    /**
     * Parse command line arguments
     */
    static CLIConfig parseArgs(String[] args) {
        CLIConfig config = new CLIConfig();
        
        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "--input":
                case "-i":
                    if (i + 1 < args.length) {
                        config.inputFile = args[++i];
                    } else {
                        System.err.println("Error: --input requires a file path");
                        config.isValid = false;
                    }
                    break;
                
                case "--output":
                case "-o":
                    if (i + 1 < args.length) {
                        config.outputFile = args[++i];
                    } else {
                        System.err.println("Error: --output requires a file path");
                        config.isValid = false;
                    }
                    break;
                
                case "--mode":
                case "-m":
                    if (i + 1 < args.length) {
                        config.mode = args[++i];
                    } else {
                        System.err.println("Error: --mode requires a mode value");
                        config.isValid = false;
                    }
                    break;
                
                case "--pattern":
                case "-p":
                    if (i + 1 < args.length) {
                        config.patternFile = args[++i];
                    } else {
                        System.err.println("Error: --pattern requires a file path");
                        config.isValid = false;
                    }
                    break;
                
                case "--help":
                case "-h":
                    config.showHelp = true;
                    break;
                
                default:
                    System.err.println("Unknown argument: " + args[i]);
                    config.isValid = false;
                    break;
            }
        }
        
        return config;
    }
    
    /**
     * Display help information
     */
    static void showHelp() {
        System.out.println("BeatKenja - Beat Mapping Tool");
        System.out.println("Usage:");
        System.out.println("  GUI Mode:  java -jar BeatKenja.jar");
        System.out.println("  CLI Mode:  java -jar BeatKenja.jar --input <file> --output <file> --mode <mode> [--pattern <file>]");
        System.out.println();
        System.out.println("Options:");
        System.out.println("  -i, --input <file>    Input file path (.dat file)");
        System.out.println("  -o, --output <file>   Output file path (.dat file)");
        System.out.println("  -m, --mode <mode>     Processing mode (linear, complex)");
        System.out.println("  -p, --pattern <file>  Optional pattern file (.pat file or .dat file)");
        System.out.println("  -h, --help            Show this help message");
        System.out.println();
        System.out.println("Examples:");
        System.out.println("  java -jar BeatKenja.jar --input normal_a.dat --output normal_b.dat --mode complex");
        System.out.println("  java -jar BeatKenja.jar -i input.dat -o output.dat -m simple -p custom.pat");
        System.out.println("  java -jar BeatKenja.jar --input timings.dat --output expert.dat --mode complex --pattern ExpertStandard.dat");
    }
    
    /**
     * Validate CLI configuration
     */
    static boolean validateCLIConfig(CLIConfig config) {
        if (!config.isValid) {
            return false;
        }
        
        if (config.inputFile == null || config.outputFile == null || config.mode == null) {
            System.err.println("Error: --input, --output, and --mode are required for CLI mode");
            return false;
        }
        
        // Check if input file exists
        File inputFile = new File(config.inputFile);
        if (!inputFile.exists()) {
            System.err.println("Error: Input file does not exist: " + config.inputFile);
            return false;
        }
        
        // Check if pattern file exists (if provided)
        if (config.patternFile != null) {
            File patternFile = new File(config.patternFile);
            if (!patternFile.exists()) {
                System.err.println("Error: Pattern file does not exist: " + config.patternFile);
                return false;
            }
            
            // Validate pattern file extension
            String patternFileName = patternFile.getName().toLowerCase();
            if (!patternFileName.endsWith(".pat") && !patternFileName.endsWith(".dat")) {
                System.err.println("Error: Pattern file must be a .pat or .dat file: " + config.patternFile);
                return false;
            }
        }
        
        // Validate mode
        String[] validModes = {"linear", "complex"};
        boolean validMode = false;
        for (String mode : validModes) {
            if (mode.equalsIgnoreCase(config.mode)) {
                validMode = true;
                break;
            }
        }
        
        if (!validMode) {
            System.err.println("Error: Invalid mode. Valid modes are: simple, normal, complex");
            return false;
        }
        
        return true;
    }
    
    /**
     * Execute CLI processing
     */
    static void executeCLI(CLIConfig config) {
        try {
            logger.info("Starting CLI processing...");
            logger.info("Input: {}", config.inputFile);
            logger.info("Output: {}", config.outputFile);
            logger.info("Mode: {}", config.mode);
            if (config.patternFile != null) {
                logger.info("Pattern: {}", config.patternFile);
            }
            
            final File input = new File(config.inputFile);
            final File output = new File(config.outputFile);
            final Pattern p = config.patternFile != null ? new Pattern(config.patternFile) : new Pattern(Parameters.DEFAULT_PATTERN_PATH);
            final String mode = config.mode;
            
            if (!input.exists() || !input.isFile()) {
                System.err.println("Error: Input file does not exist: " + config.inputFile);
                logger.info("Input file does not exist: {}. Aborting...", config.inputFile);
                System.exit(1);
            }
            
            BeatSaberMap map = BeatSaberMap.newMapFromJSON(input.getAbsolutePath());
            List<Note> notes = Arrays.stream(map._notes).toList();
            
            switch (mode) {
                case "linear" -> map._notes = LinearSlowPattern.linearSlowPattern(notes, false, null, null).toArray(new Note[0]);
                case "complex" -> map._notes = ComplexPattern.complexPattern(notes, p, p, false, false, false, false, null, null).toArray(new Note[0]);
                default -> throw new IllegalArgumentException("Invalid mode: " + config.mode);
            }
            
            FileManager.overwriteFile(config.outputFile, map.exportAsMap());
            
            System.out.println("Processing completed successfully!");
            System.out.println("Output saved to: " + config.outputFile);
            if (config.patternFile != null) {
                System.out.println("Used custom patterns from: " + config.patternFile);
            }
            
            System.exit(0);
        } catch (Exception e) {
            System.err.println("Error during processing: " + e.getMessage());
            logger.error("CLI processing failed", e);
            System.exit(1);
        }
        
        System.exit(1);
    }
}
