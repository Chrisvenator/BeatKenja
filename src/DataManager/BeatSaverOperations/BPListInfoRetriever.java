package DataManager.BeatSaverOperations;

import DataManager.Exceptions.WrongFileExtensionException;
import DataManager.FileManager;
import DataManager.Parameters;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

@SuppressWarnings("ResultOfMethodCallIgnored")
public class BPListInfoRetriever {
    public static void main(String[] args) throws WrongFileExtensionException, IOException, JSONException {
        BPListInfoRetriever retriever = new BPListInfoRetriever();
        retriever.retrieveBPLIST(new File("C:\\Users\\chris\\Documents\\_Uni\\a (1).bplist"));
    }

    private final String MAPS_INFO_FOLDER;
    private final String MAPS_OUTPUT_FOLDER;


    public BPListInfoRetriever() {
        this.MAPS_INFO_FOLDER = Parameters.DEFAULT_BEATSAVER_MAP_INFO_PATH;
        this.MAPS_OUTPUT_FOLDER = Parameters.DEFAULT_BEATSAVER_MAPS_PATH;
    }

    /**
     * Retrieves all maps from a bplist file.
     *
     * @param file The bplist file to retrieve the maps from.
     * @throws WrongFileExtensionException If the file is not a bplist file.
     * @throws IOException                 If the file does not exist.
     * @throws JSONException               If the file is not a valid JSON file.
     */
    public void retrieveBPLIST(File file) throws WrongFileExtensionException, IOException, JSONException {
        if (!file.exists()) throw new IOException("File " + file.getName() + " does not exist.");

        if (!file.isDirectory() && file.getName().endsWith(".bplist")) {
            System.out.println("Found bplist file: " + file.getName());
            String content = String.join("", FileManager.readFile(file.getAbsolutePath()));

            JSONArray songs = new JSONObject(content).getJSONArray("songs");

            for (int i = 0; i < songs.length(); i++) retrieve(new JSONObject(songs.get(i).toString()).getString("key"));
        } else {
            System.err.println("File " + file.getName() + " is not a bplist file.");
            throw new WrongFileExtensionException(file, ".bplist");
        }
    }


    /**
     * "retrieve()" copies the map info into the output folder and then downloads the map into its own folder.<br>
     * Important: only the necessary files are kept in the output folder. The rest will be deleted (like mp3 and images).<br>
     * Important: the map will be downloaded into its own folder, so the output folder will contain a folder with the map ID as its name.<br>
     *
     * @param mapID The ID of the map to retrieve in hexadecimal.
     * @return Whether the map was successfully retrieved or not.
     * @warning This method can create up to 1000 requests before it has to wait 60 seconds. THE 60-SECOND WAIT IS NOT IMPLEMENTED HERE!
     */
    public boolean retrieve(String mapID) {
        Path mapInfoInputPath = Path.of(MAPS_INFO_FOLDER + mapID + ".json");
        Path mapInfoOutputFolderPath = Path.of(MAPS_OUTPUT_FOLDER + mapID);

        Path mapOutputPath = Path.of(mapInfoOutputFolderPath + "/" + mapID + ".json");

        try {
            File folder = new File(mapInfoOutputFolderPath.toString());
            if (!folder.exists()) folder.mkdir();

            Files.copy(mapInfoInputPath, mapOutputPath, java.nio.file.StandardCopyOption.REPLACE_EXISTING);

            BeatSaverMapDownloader mapDownloader = new BeatSaverMapDownloader();

            mapDownloader.downloadMap(mapID, true);
            TimeUnit.MILLISECONDS.sleep(100);
        } catch (IOException e) {
            System.out.println("Failed to copy file " + mapID + ".json: " + e.getMessage());
            e.printStackTrace();
            return false;
        } catch (InterruptedException ignored) {
        }

        return true;
    }

}
