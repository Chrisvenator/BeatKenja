import DataManager.CreateAllNecessaryDIRsAndFiles;
import UserInterface.UserInterface;

import java.io.File;
import java.util.Random;
import java.util.logging.Level;

import static DataManager.Parameters.*;

/**
 * Default class of this Project. The main of this class is used to start and initialize every component.
 */
public class Start {

        /*
    Red: 0
    Blue: 1

    Index - Layer:          Cut direction:
    |---|---|---|---|       |---|---|---|
    |   |   |   |3-2|       | 4 | 0 | 5 |
    |---|---|---|---|       |---|---|---|
    |   |   |   |3-1|       | 2 | 8 | 3 |
    |---|---|---|---|       |---|---|---|
    |0-0|1-0|2-0|3-0|       | 6 | 1 | 7 |
    |---|---|---|---|       |---|---|---|
     */

    /*
     *********** Planned Features ***********
     * bei schnellen Sektionen gleiche Abstände <-- Parity Breaks & Note inside Note Ursachen finden
     * bei ganz schnellen sektionen dann predictable patterns <-- Testen & implementieren
     * bei > 10 nps keine inlines mehr
     * lehnen
     * alle List<Note> zu custom NoteList<Note> Konvertieren?
     * Schreiben eines Tutorials
     *****************************************
     */

    /**
     * This variable represents the User Interface.
     * It is used to display every button and feature
     */
    public static UserInterface ui;

    /**
     * Default method of the whole project. main is used to start and initialize everything.
     */
    public static void main(String[] args) {
        java.util.logging.Logger.getLogger("org.hibernate").setLevel(Level.WARNING);

        SEED = (long) (new Random().nextDouble() * 1000000000);
        RANDOM = new Random(SEED);

        logger.info("Starting up BeatKenja...");
        logger.info("Seed: {}", SEED);
        logger.info("Setting Hibernate Logger to warning");

        // Initialize necessary directories and files
        if (executedByJar) {
            logger.info("Found that the program is executed by a jar file.");
            CreateAllNecessaryDIRsAndFiles.createAllNecessaryDIRsAndFiles();
        } else if (!new File("./config.json").exists()) {
            CreateAllNecessaryDIRsAndFiles.createConfig();
        }

        // Check if CLI arguments are provided
        if (args.length > 0) {
            Start_CLI.CLIConfig config = Start_CLI.parseArgs(args);

            if (config.showHelp) {
                Start_CLI.showHelp();
                return;
            }

            if (!Start_CLI.validateCLIConfig(config)) {
                System.err.println("Use --help for usage information");
                System.exit(1);
            }

            // Execute CLI mode
            Start_CLI.executeCLI(config);

            ui = new UserInterface();
            ui.setVisible(false);


        } else {
            // No CLI arguments provided, start GUI mode
            logger.info("Starting GUI mode...");

            ui = new UserInterface();
            ui.setVisible(true);

        }
    }
}