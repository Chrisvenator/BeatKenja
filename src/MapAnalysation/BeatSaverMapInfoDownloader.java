package MapAnalysation;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.TimeUnit;

import DataManager.Parameters;
import org.json.JSONException;
import org.json.JSONObject;


public class BeatSaverMapInfoDownloader {
    private static final HttpClient httpClient = HttpClient.newBuilder().build();
    private final String DOWNLOAD_DIRECTORY;

    public static void main(String[] args) throws InterruptedException {
        BeatSaverMapInfoDownloader downloader = new BeatSaverMapInfoDownloader();
        downloader.downloadAllMaps();
    }

    public BeatSaverMapInfoDownloader() {
        this.DOWNLOAD_DIRECTORY = Parameters.DEFAULT_BEATSAVER_MAPS_PATH;
    }

    public BeatSaverMapInfoDownloader(String outputDirectory) {
        if (!outputDirectory.endsWith("/")) outputDirectory += "/";
        this.DOWNLOAD_DIRECTORY = outputDirectory;
    }

    public void downloadAllMaps() throws InterruptedException {
        int i = 228691;
        for (; i < 230000; i++) {
            downloadMap(Integer.toHexString(i));

            TimeUnit.MILLISECONDS.sleep(100);
            if (i % 999 == 0) {
                System.out.println("Waiting 60 seconds...");
                TimeUnit.SECONDS.sleep(60);
            }
        }

        System.out.println("Finished downloading all maps. Last Map: " + i);
    }

    public void downloadMap(String mapID) {
        try {
            String BEATSAVER_API_URL = "https://api.beatsaver.com/maps/id/";
            String url = BEATSAVER_API_URL + mapID;
            HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).GET().build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.body().charAt(0) != '{') {
                System.out.println(response.body());
                throw new RuntimeException("Response is not a JSON object");
            }

            JSONObject jsonResponse = new JSONObject(response.body());
            jsonResponse.put("description", "");

            if (!jsonResponse.has("error")) saveToFile(jsonResponse.toString(4), (DOWNLOAD_DIRECTORY + mapID + ".json").toLowerCase());
            else System.out.println("Map " + mapID + " not found");
        } catch (IOException | InterruptedException | JSONException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    private static void saveToFile(String jsonContent, String filePath) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(filePath))) {
            bw.write(jsonContent);
            bw.flush();
            bw.close();
            System.out.println("Saved to " + filePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
