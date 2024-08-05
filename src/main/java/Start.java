import DataManager.CreateAllNecessaryDIRsAndFiles;
import DataManager.Database.DatabaseExport;
import MapGeneration.GenerationElements.Exceptions.NoteNotValidException;
import UserInterface.UserInterface;

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
     * public static final boolean PARITY_ERRORS_AS_BOOKMARKS = true; <-- TESTEN
     * ^ DIDNT_PLACE_STACK in "parityErrorsAsBookmarks"
     * export as .jar
     * Random V2 "File not found crash"
     * Sleep interrupted exception bei linear, one-handed complex
     * Verifizieren, dass ToBLueOnlyTimingNotes funktioniert
     * Möglichkeit .webp als map zu laden XD (╯°□°)╯︵ ┻━┻
     * BatchWavToMaps Testen, ob output noch immer geht
     * lehnen
     * ganzen Ordner zu einer Complex-Map machen
     * Bei schnellen Sektionen abwechselndes Pattern machen mit gleichen Abständen
     *****************************************
    **/


    public static void main(String[] args) throws NoteNotValidException {
        java.util.logging.Logger.getLogger("org.hibernate").setLevel(Level.WARNING);

        SEED = (long) (new Random().nextDouble() * 1000000000);
        RANDOM = new Random(SEED);

        logger.info("Starting Start...");
        logger.info("Seed: {}", SEED);
        logger.info("Setting Hibernate Logger to warning");

        CreateAllNecessaryDIRsAndFiles.createAllNecessaryDIRsAndFiles();

        UserInterface ui = new UserInterface();
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