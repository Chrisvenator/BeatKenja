package UserInterface.Elements.Buttons.ButtonTypes.GlobalButtons.Buttons;

import UserInterface.Elements.Buttons.ButtonTypes.GlobalButtons.GlobalButton;
import UserInterface.Elements.ElementTypes;
import UserInterface.Elements.Buttons.ButtonTypes.GlobalButtons.Exceptions.ActionNotSupportedException;
import UserInterface.Elements.Buttons.ButtonTypes.GlobalButtons.Exceptions.ZipCreationException;
import UserInterface.UserInterface;

import java.awt.*;
import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static DataManager.Parameters.*;

public class GlobalOpenMapInBrowser extends GlobalButton {
    public GlobalOpenMapInBrowser(UserInterface ui) {
        super(ElementTypes.GLOBAL_OPEN_MAP_IN_BROWSER, ui);
        setBackground(Color.gray);
    }

    @Override
    public void onClick() {
//            String url = "https://allpoland.github.io/ArcViewer/";
//            String url = "https://skystudioapps.com/bs-viewer/";

        try {
            URI uri = new URI(mapViewerURL);

            if (Desktop.isDesktopSupported()) {
                Desktop desktop = Desktop.getDesktop();

                if (filePath != null && !filePath.equals("")) {
                    desktop.browse(uri);
                    desktop.open(new File(filePath));
                    createZipFileFromCurrentDifficulty();

                } else throw new FileNotFoundException("File path is null or empty!");
            } else throw new ActionNotSupportedException("Map preview viewing is not supported on this platform!");
        } catch (IOException | URISyntaxException | ActionNotSupportedException ex) {
            printException(ex);
        }
    }

    private void createZipFileFromCurrentDifficulty() {
        String sourceDir = new File(filePath).getAbsolutePath();
        System.out.println(sourceDir);

        String zipFileName = sourceDir + "/output.zip";

        try {
            FileOutputStream fos = new FileOutputStream(zipFileName);
            ZipOutputStream zipOut = new ZipOutputStream(fos);

            byte[] buffer = new byte[1024];

            // Get a list of files in the source directory
            File dir = new File(sourceDir);
            File[] files = dir.listFiles();

            if (files == null || files.length <= 3) throw new ZipCreationException("There was an error while creating the zip file!");


            for (File file : files) {
                if (file.getName().contains(".zip") || file.isDirectory()) continue;
                ZipEntry zipEntry = new ZipEntry(file.getName());
                zipOut.putNextEntry(zipEntry);

                FileInputStream fis = new FileInputStream(file);
                int length;
                while ((length = fis.read(buffer)) > 0) {
                    zipOut.write(buffer, 0, length);
                }
                fis.close();
            }

            zipOut.close();

            System.out.println("The files have been successfully added to " + zipFileName);
            ui.statusCheck.append("\n[INFO]: The files have been successfully added to " + zipFileName);
        } catch (IOException ex) {
            printException(ex);
        }
    }

}
