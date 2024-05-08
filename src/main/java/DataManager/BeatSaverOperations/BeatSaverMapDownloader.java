package DataManager.BeatSaverOperations;

import DataManager.Exceptions.WrongFileExtensionException;
import DataManager.FileManager;
import DataManager.Parameters;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

/**
 * Downloads the map into its own folder in DOWNLOAD_DIRECTORY
 */
public class BeatSaverMapDownloader {
    public static void main(String[] args) throws WrongFileExtensionException, JSONException, IOException {
//        BeatSaverMapDownloader downloader = new BeatSaverMapDownloader();
//        downloader.downloadMap("1a8", true);
//        downloader.downloadBPLIST(new File("C:\\Users\\chris\\Documents\\_Uni\\a (1).bplist"), true);

        BeatSaverMapDownloader downloader = new BeatSaverMapDownloader("C:\\Program Files (x86)\\Steam\\steamapps\\common\\Beat Saber\\BeatSaberMaps\\_toAdd\\");
        Map<String, Predicate<Object>> filter = Map.of(
                "score", value -> value instanceof Number && ((Number) value).doubleValue() >= 0.5,
                "upvotes", value -> value instanceof Number && ((Number) value).doubleValue() >= 6,
//                "tags", "anime"::equals,
                "automapper", value -> value instanceof Boolean && !((Boolean) value)
        );

        downloader.downloadFilteredMaps(filter, true);

        //How to use the filter:
//        JSONObject jsonObject = new JSONObject();
//        jsonObject.put("numericKey", 0.4); // Example numeric value
//        jsonObject.put("tags", new JSONArray(java.util.List.of("anime", "dance")));
//
//        Map<String, Predicate<Object>> filter = Map.of(
//                "numericKey", value -> value instanceof Number && ((Number) value).doubleValue() < 0.5,
//                "tags", value -> "anime".equals(value) // Check if 'tags' array contains "anime"
//        );
//
//        boolean allFiltersTrue = checkFilters(jsonObject, filter);
//        System.out.println("Are all filters true? " + allFiltersTrue);
    }

    private final String MAP_INFO_DIRECTORY;
    private final String DOWNLOAD_DIRECTORY;

    public BeatSaverMapDownloader() {
        this.DOWNLOAD_DIRECTORY = Parameters.DEFAULT_BEATSAVER_MAPS_PATH;
        this.MAP_INFO_DIRECTORY = Parameters.DEFAULT_BEATSAVER_MAP_INFO_PATH;
    }

    public BeatSaverMapDownloader(String downloadDirectory) {
        this.DOWNLOAD_DIRECTORY = downloadDirectory;
        this.MAP_INFO_DIRECTORY = Parameters.DEFAULT_BEATSAVER_MAP_INFO_PATH;
    }


    /**
     * Downloads all maps that match the filter. <br>
     *
     * @param filter                 The filter.
     * @param deleteUnnecessaryFiles Whether to delete unnecessary files after downloading.
     * @example: JSONObject jsonObject = new JSONObject();<br>
     * jsonObject.put("numericKey", 0.4); // Example numeric value<br>
     * jsonObject.put("tags", new JSONArray(java.util.List.of("anime", "dance")));<br>
     * <br>
     * Map<String, Predicate<Object>> filter = Map.of(<br>
     * "score", value -> value instanceof Number && ((Number) value).doubleValue() < 0.5,<br>
     * "tags", "dance"::equals, <br>
     * );<br>
     * <br>
     * boolean allFiltersTrue = checkFilters(jsonObject, filter);<br>
     * System.out.println("Are all filters true? " + allFiltersTrue);<br>
     */
    public void downloadFilteredMaps(Map<String, Predicate<Object>> filter, boolean deleteUnnecessaryFiles) {
        File mapInfoDirectory = new File(MAP_INFO_DIRECTORY);
        File[] mapInfoFiles = mapInfoDirectory.listFiles();

        if (mapInfoFiles == null) {
            System.err.println("No map info files found!");
            return;
        }

        for (File mapInfoFile : mapInfoFiles) {
            try {
                JSONObject mapInfoJson = new JSONObject(String.join("", FileManager.readFile(mapInfoFile.getAbsolutePath())));
                boolean matchesFilter = checkFilters(mapInfoJson, filter);

                System.out.println("Checking map: " + mapInfoFile.getName() + ": filter " + (matchesFilter ? "matches" : "doesn't match"));

                if (matchesFilter) {
                    Thread thread = new Thread(() -> downloadMap(mapInfoFile.getName().replace(".json", ""), deleteUnnecessaryFiles));
//                    downloadMap(mapInfoFile.getName().replace(".json", ""), deleteUnnecessaryFiles);
//                    throw new RuntimeException("Not implemented yet!");
                    thread.start();
                    TimeUnit.SECONDS.sleep(4);
                }
            } catch (JSONException e) {
                System.err.println(mapInfoFile.getName() + " was in the wrong format!");
//                throw new RuntimeException(e);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * Checks if the map matches the filter. The filter is a map of keys and conditions. The map is checked recursively.
     *
     * @param jsonObject The map info file.
     * @param filter     The filter.
     * @return True if the map matches the filter, false otherwise.
     */
    private static boolean checkFilters(JSONObject jsonObject, Map<String, Predicate<Object>> filter) {
        Iterator<String> keys = jsonObject.keys();

        try {
            while (keys.hasNext()) {
                String key = keys.next();
                Object value = jsonObject.get(key);
                 if (filter.containsKey(key)) {
                    Predicate<Object> condition = filter.get(key);

                    if (value instanceof JSONArray array) {
                        // If the value is a JSONArray, check if any element satisfies the condition
                        boolean matchFound = false;
                        for (int i = 0; i < array.length(); i++) {
                            if (condition.test(array.get(i))) {
                                matchFound = true;
                                break;
                            }
                        }
                        if (!matchFound) {
                            // No element in the array satisfies the condition
                            return false;
                        }
                    } else if (!condition.test(value)) {
                        // Condition isn't met for this key
                        return false;
                    }
                }

                if (value instanceof JSONObject) {
                    // Recurse into nested JSONObject
                    if (!checkFilters((JSONObject) value, filter)) {
                        return false;
                    }
                }
            }
        } catch (JSONException e) {
            System.err.println("JSON Exception");
            return false;
        }

        return true; // All conditions met
    }

    /**
     * Downloads the map with the given ID.
     *
     * @param mapID                  The ID of the map to download.
     * @param deleteUnnecessaryFiles Whether to delete unnecessary files after downloading.
     */
    public void downloadMap(String mapID, boolean deleteUnnecessaryFiles) {
        File mapInfo = new File(MAP_INFO_DIRECTORY + mapID + ".json");
        if (!mapInfo.exists()) return;

        String downloadURL = "NULL";
        File downloadDir = new File(this.DOWNLOAD_DIRECTORY + mapID);
//        noinspection ResultOfMethodCallIgnored
        downloadDir.mkdirs();


        try {
            JSONObject mapInfoJson = new JSONObject(String.join("", FileManager.readFile(mapInfo.getAbsolutePath())));
            downloadURL = new JSONObject(mapInfoJson.getJSONArray("versions").get(0).toString()).getString("downloadURL");

            //checking the url, so that no malicious entity can download something unwanted.
            if (!downloadURL.contains("https://r2cdn.beatsaver.com/") && !downloadURL.contains("https://cdn.beatsaver.com/")) throw new MalformedURLException("wrong URL");

            String path = downloadDir + "/" + mapID + ".zip";
            System.out.println("Started downloading " + mapID + ": " + downloadURL);

            //Retrieving the map information
            Files.copy(Path.of(MAP_INFO_DIRECTORY + mapID + ".json"), Path.of(DOWNLOAD_DIRECTORY + mapID + "/" + mapID + ".json"), java.nio.file.StandardCopyOption.REPLACE_EXISTING);

            //Downloading and extracting the map
            FileManager.downloadFile(downloadURL, path);
            FileManager.extractZipFilesIntoDirectory(path, downloadDir.getAbsolutePath());

            if (deleteUnnecessaryFiles) FileManager.removeUnnecessaryFiles(downloadDir.getAbsolutePath(), "dat", "json");

            TimeUnit.MILLISECONDS.sleep(100);
        } catch (JSONException e) {
            System.err.println(mapID + ".json was in the wrong format! " + mapInfo.getName());
        } catch (MalformedURLException | URISyntaxException | FileNotFoundException e) {
            System.err.println("URL was not found. Does this map still exist? skipping " + mapID + ". URL: " + downloadURL);
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Failed to download the file.");
        } catch (InterruptedException ignored) {
        }
    }

    /**
     * Retrieves all maps from a bplist file.
     *
     * @param file The bplist file to retrieve the maps from.
     * @throws WrongFileExtensionException If the file is not a bplist file.
     * @throws IOException                 If the file does not exist.
     * @throws JSONException               If the file is not a valid JSON file.
     */
    public void downloadBPLIST(File file, boolean deleteUnnecessaryFiles) throws WrongFileExtensionException, IOException, JSONException {
        if (!file.exists()) throw new IOException("File " + file.getName() + " does not exist.");

        if (!file.isDirectory() && file.getName().endsWith(".bplist")) {
            System.out.println("Found bplist file: " + file.getName());
            String content = String.join("", FileManager.readFile(file.getAbsolutePath()));

            JSONArray songs = new JSONObject(content).getJSONArray("songs");
            BeatSaverMapDownloader downloader = new BeatSaverMapDownloader();

            for (int i = 0; i < songs.length(); i++) downloader.downloadMap(new JSONObject(songs.get(i).toString()).getString("key"), deleteUnnecessaryFiles);
        } else {
            System.err.println("File " + file.getName() + " is not a bplist file.");
            throw new WrongFileExtensionException(file, ".bplist");
        }
    }
}
