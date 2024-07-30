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

    //TODO: Config loader dependency weiter machen
    //TODO: Besserer Logger
    //TODO: public static final boolean PARITY_ERRORS_AS_BOOKMARKS = true;

    public static void main(String[] args) throws NoteNotValidException {
        java.util.logging.Logger.getLogger("org.hibernate").setLevel(Level.WARNING);

        SEED = (long) (new Random().nextDouble() * 1000000000);
        RANDOM = new Random(SEED);
        System.out.println("Current seed is: " + SEED);


        CreateAllNecessaryDIRsAndFiles.createAllNecessaryDIRsAndFiles();

        UserInterface ui = new UserInterface();
        ui.setVisible(true);

        if (useDatabase) ui.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                if (EXPORT_DATABASE) DatabaseExport.exportDatabase("./database"); // Export the database if the user wants to. Currently disabled because of bugs.
                entityManager.close();
            }
        });
    }
}