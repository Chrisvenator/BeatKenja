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

/**
 * A class responsible for downloading map information from the BeatSaver API and saving it as JSON files.
 * The class supports bulk downloading of map information using a specified range of map IDs.
 */
public class BeatSaverMapInfoDownloader {
    /** An HTTP client used for sending requests to the BeatSaver API. This client is initialized once and reused for all API requests.*/
    private static final HttpClient httpClient = HttpClient.newBuilder().build();
    /** The directory where the downloaded map information JSON files will be saved.*/
    private final String DOWNLOAD_DIRECTORY;

    /**
     * The main method for initiating the download process.
     * It creates an instance of `BeatSaverMapInfoDownloader` and starts downloading all maps within the specified range.
     *
     * @param args Command-line arguments (not used).
     * @throws InterruptedException If the thread is interrupted during the sleep intervals.
     */
    public static void main(String[] args) throws InterruptedException {
        BeatSaverMapInfoDownloader downloader = new BeatSaverMapInfoDownloader();
        downloader.downloadAllMaps();
    }

    /**
     * Constructs a `BeatSaverMapInfoDownloader` with the default download directory specified in the parameters.
     */
    public BeatSaverMapInfoDownloader() {
        this.DOWNLOAD_DIRECTORY = Parameters.DEFAULT_BEATSAVER_MAP_INFO_PATH;
    }

    /**
     * Downloads map information for a range of map IDs, starting from a specified ID.
     * The method pauses for a short interval between downloads to avoid overwhelming the server.
     * Additionally, it takes a longer break every 999 maps to comply with potential rate limits.
     *
     * @throws InterruptedException If the thread is interrupted during the sleep intervals.
     */
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
     * Downloads the information for a specific map from the BeatSaver API.
     * The map information is fetched in JSON format and saved to the specified download directory.
     *
     * @param mapID The ID of the map to download, represented as a hexadecimal string.
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
