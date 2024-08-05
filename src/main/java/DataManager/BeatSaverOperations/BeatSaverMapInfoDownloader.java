package DataManager.BeatSaverOperations;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import DataManager.FileManager;
import DataManager.Parameters;
import org.json.JSONException;
import org.json.JSONObject;

import static DataManager.Parameters.logger;

public class BeatSaverMapInfoDownloader {
    private static final HttpClient httpClient = HttpClient.newBuilder().build();
    private final String DOWNLOAD_DIRECTORY;

    public static void main(String[] args) throws InterruptedException {
        BeatSaverMapInfoDownloader downloader = new BeatSaverMapInfoDownloader();
        downloader.downloadAllMaps();
    }

    public BeatSaverMapInfoDownloader() {
        this.DOWNLOAD_DIRECTORY = Parameters.DEFAULT_BEATSAVER_MAP_INFO_PATH;
    }

    public void downloadAllMaps() throws InterruptedException {
        int i = 228691;
        for (; i < 230000; i++) {
            downloadMap(Integer.toHexString(i));

            TimeUnit.MILLISECONDS.sleep(100);
            if (i % 999 == 0) {
                logger.info("Waiting 60 seconds...");
                System.out.println("Waiting 60 seconds...");
                TimeUnit.SECONDS.sleep(60);
            }
        }

        logger.info("Finished downloading all maps. Last Map: " + i);
        System.out.println("Finished downloading all maps. Last Map: " + i);
    }

    /**
     * Downloads map information from the BeatSaver API.
     *
     * @param mapID The ID of the map to download.
     */
    public void downloadMap(String mapID) {
        try {
            String BEATSAVER_API_URL = "https://api.beatsaver.com/maps/id/";
            String url = BEATSAVER_API_URL + mapID;
            HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).GET().build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.body().charAt(0) != '{') {
                logger.info(response.body());
                System.out.println(response.body());
                throw new RuntimeException("Response is not a JSON object");
            }

            JSONObject jsonResponse = new JSONObject(response.body());
            jsonResponse.put("description", "");

            if (!jsonResponse.has("error")) FileManager.overwriteFile((DOWNLOAD_DIRECTORY + mapID + ".json").toLowerCase(), jsonResponse.toString(4));
            else {
                logger.info("Map {} not found", mapID);
                System.out.println("Map " + mapID + " not found");
            }
        } catch (IOException | InterruptedException | JSONException e) {
            logger.fatal("WHY WAS THE THREAD INTERRUPTED??? {}", e.getMessage());
            logger.fatal(Arrays.toString(e.getStackTrace()));
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}
