import DataManager.CreateAllNecessaryDIRsAndFiles;
import DataManager.Database.DatabaseExport;
import DataManager.Parameters;
import MapGeneration.GenerationElements.Exceptions.NoteNotValidException;
import UserInterface.UserInterface;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.util.Objects;
import java.util.Random;
import java.util.logging.Level;

import static DataManager.Parameters.*;

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

    /**
     *********** Planned Features ***********
     * Bei schnellen Sektionen abwechselndes Pattern machen mit gleichen Abständen
     * lehnen
     *****************************************
    **/

    public static UserInterface ui;


    public static void main(String[] args) throws NoteNotValidException {
        java.util.logging.Logger.getLogger("org.hibernate").setLevel(Level.WARNING);

        SEED = (long) (new Random().nextDouble() * 1000000000);
        RANDOM = new Random(SEED);

        logger.info("Starting Start...");
        logger.info("Seed: {}", SEED);
        logger.info("Setting Hibernate Logger to warning");

        if (executedByJar && !new File("./congi.json").exists()) {
            logger.info("Found that the program is executed by a jar file.");
            CreateAllNecessaryDIRsAndFiles.createAllNecessaryDIRsAndFiles();
        }

        ui = new UserInterface();
        ui.setVisible(true);

        if (useDatabase) ui.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                logger.trace("Closing Window");
                if (EXPORT_DATABASE) DatabaseExport.exportDatabase("./database"); // Export the database if the user wants to. Currently disabled because of bugs.
                entityManager.close();
            }
        });
    }
}