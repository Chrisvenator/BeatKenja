package DataManager.BeatSaverOperations;

import DataManager.FileManager;
import DataManager.Parameters;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URISyntaxException;

/**
 * Downloads the map into its own folder in DOWNLOAD_DIRECTORY
 */
public class BeatSaverMapDownloader {
    private final String MAP_INFO_DIRECTORY;
    private final String DOWNLOAD_DIRECTORY;

    public BeatSaverMapDownloader() {
        this.DOWNLOAD_DIRECTORY = Parameters.DEFAULT_BEATSAVER_MAPS_PATH;
        this.MAP_INFO_DIRECTORY = Parameters.DEFAULT_BEATSAVER_MAP_INFO_PATH;
    }

    public void downloadMap(String mapID, boolean deleteUnnecessaryFiles) {
        File mapInfo = new File(MAP_INFO_DIRECTORY + mapID + ".json");
        if (!mapInfo.exists()) return;

        String downloadURL = "NULL";
        File downloadDir = new File(this.DOWNLOAD_DIRECTORY + mapID);
        //noinspection ResultOfMethodCallIgnored
        downloadDir.mkdir();


        try {
            JSONObject mapInfoJson = new JSONObject(String.join("", FileManager.readFile(mapInfo.getAbsolutePath())));
            downloadURL = new JSONObject(mapInfoJson.getJSONArray("versions").get(0).toString()).getString("downloadURL");
            String path = downloadDir + "/" + mapID + ".zip";
            System.out.println("Started downloading " + mapID + ": " + downloadURL);

            FileManager.downloadFile(downloadURL, path);
            FileManager.extractZipFilesIntoDirectory(path, downloadDir.getAbsolutePath());

            if (deleteUnnecessaryFiles) FileManager.removeUnnecessaryFiles(downloadDir.getAbsolutePath(), "dat", "json");

        } catch (JSONException e) {
            System.err.println(mapID + ".json was in the wrong format! " + mapInfo.getName());
        } catch (MalformedURLException | URISyntaxException e) {
            System.err.println("URL was not found. Does this map still exist? skipping " + mapID + ". URL: " + downloadURL);
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Failed to download the file.");
        }
    }

}
