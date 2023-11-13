package UserInterface.Elements.Buttons.ButtonTypes.GlobalButtons;

import UserInterface.Elements.Buttons.MyButton;
import UserInterface.Elements.ElementTypes;
import UserInterface.UserInterface;

import java.awt.*;
import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static DataManager.Parameters.*;

public class GlobalOpenMapInBrowser extends MyButton {
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
                }
            } else {
                System.out.println("Map preview viewing is not supported on this platform.");
                ui.statusCheck.setText(ui.statusCheck.getText() + "\n[ERROR]: Map preview viewing is not supported on this platform.");
            }
        } catch (IOException | URISyntaxException ex) {
            ex.printStackTrace();
            ui.statusCheck.setText(ui.statusCheck.getText() + "\n[ERROR]: Map preview viewing encountered an error! This feature is currently not available :/");
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

            if (files == null || files.length <= 3) {
                ui.statusCheck.setText(ui.statusCheck.getText() + "\n[ERROR]: Something went wrong...");
                return;
            }

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
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

}
