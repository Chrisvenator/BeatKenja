package UserInterface.Elements.Buttons.ButtonTypes.GlobalButtons.Buttons;

import DataManager.FileManager;
import UserInterface.Elements.Buttons.ButtonTypes.GlobalButtons.GlobalButton;
import UserInterface.Elements.ElementTypes;
import UserInterface.Elements.Buttons.ButtonTypes.GlobalButtons.Exceptions.ActionNotSupportedException;
import UserInterface.UserInterface;

import java.awt.*;
import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;

import static DataManager.Parameters.*;

public class GlobalOpenMapInBrowser extends GlobalButton {

    public GlobalOpenMapInBrowser(UserInterface ui) {
        super(ElementTypes.GLOBAL_OPEN_MAP_IN_BROWSER, ui);
        setBackground(Color.gray);
        logger.debug("GlobalOpenMapInBrowser button initialized.");
    }

    @Override
    public void onClick() {
        // url: "https://allpoland.github.io/ArcViewer/";
        // url: "https://skystudioapps.com/bs-viewer/";

        try {
            URI uri = new URI(mapViewerURL);
            logger.debug("URI created: {}", mapViewerURL);

            if (Desktop.isDesktopSupported()) {
                Desktop desktop = Desktop.getDesktop();

                if (filePath != null && !filePath.isEmpty()) {
                    desktop.browse(uri);
                    logger.info("Browsing URL: {}", uri);
                    desktop.open(new File(filePath));
                    logger.info("Opening file path: {}", filePath);

                    try {
                        String zipFileName = filePath + "/output.zip";
                        FileManager.createZipFileFromDirectory(filePath, zipFileName);
                        logger.info("The files have been successfully added to {}", zipFileName);
                        System.out.println("The files have been successfully added to " + zipFileName);
                    } catch (IOException ex) {
                        logger.error("Error while creating zip file from directory: {}", ex.getMessage());
                        printException(ex);
                    }

                } else throw new FileNotFoundException("File path is null or empty!");
            } else throw new ActionNotSupportedException("Map preview viewing is not supported on this platform!");
        } catch (IOException | URISyntaxException | ActionNotSupportedException ex) {
            printException(ex);
        }
    }
}
