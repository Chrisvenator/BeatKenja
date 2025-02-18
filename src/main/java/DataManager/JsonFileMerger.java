package DataManager;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static DataManager.Parameters.logger;

/**
 * A utility class for merging JSON files. This class provides methods to merge multiple JSON files into a single JSON file
 * or to merge the contents of one JSON file into another.
 * The files are expected to be stored in a specified download directory.
 */
public class JsonFileMerger {
    public static void main(String[] args) {
        JsonFileMerger merger = new JsonFileMerger();
        merger.mergeAll("src/DataManager/merged.json", 200000, 201000);
    }

    /** The directory where the JSON files to be merged are located.*/
    private final String DOWNLOAD_DIRECTORY;

    /** Constructs a `JsonFileMerger` with the default download directory specified in the application parameters.*/
    public JsonFileMerger() {
        this.DOWNLOAD_DIRECTORY = Parameters.DEFAULT_BEATSAVER_MAP_INFO_PATH;
    }

    /**
     * Merges all JSON files within the specified range of names (converted from decimal to hexadecimal) into a single JSON file.
     * The merged content is stored in the specified output file.
     *
     * @param outputFile The path of the output file where the merged JSON content will be saved.
     * @param start      The beginning of the range (inclusive) in decimal, which will be converted to hexadecimal.
     * @param end        The end of the range (exclusive) in decimal, which will be converted to hexadecimal.
     */
    public void mergeAll(String outputFile, int start, int end) {
        assert start - end >= 1;
        StringBuilder output = new StringBuilder("{\n");

        for (int i = start; i < end; i++) {
            if (i % 1000 == 0) logger.debug("Merging map {}...", i);
            if (i % 1000 == 0) System.out.println("Merging map " + i + "...");
            File file = new File(DOWNLOAD_DIRECTORY + Integer.toHexString(i) + ".json");
            if (!file.exists() || !file.isFile()) continue;

            try {
                output.append("\"").append(file.getName().replace(".json", "")).append("\": ").append(new String(Files.readAllBytes(Paths.get(file.getAbsolutePath())))).append(",\n");
            } catch (IOException ignored) {
            }

        }

        output = new StringBuilder(output.substring(0, output.lastIndexOf(",")) + "\n}");

        BufferedWriter writer;
        try {
            writer = new BufferedWriter(new FileWriter(outputFile));
            writer.write(output.toString());
            writer.close();
        } catch (IOException e) {
            logger.error("Could not write to file {}!. So it will be outputted here: ", outputFile);
            logger.info(output);
            System.err.println("Could not write to file " + outputFile + "!. So it will be outputted here: ");
            System.out.println(output);
        }
    }

    /**
     * Merges file2 into file 1.
     * The merged content replaces the content of the first file.
     *
     * @param f1 file to merge into
     * @param f2 file to merge
     */
    public void merge(File f1, File f2) {
        String f1Name = f1.getName().replace(".json", "");
        String f2Name = f2.getName().replace(".json", "");
        try {
            String content1 = new String(Files.readAllBytes(Paths.get(f1.getAbsolutePath())));
            String content2 = new String(Files.readAllBytes(Paths.get(f2.getAbsolutePath())));

            String merged = "";
            if (content1.contains(f1Name))
                merged = content1.substring(0, content1.lastIndexOf("}")) + "," + f2Name + ":[" + content2.substring(1) + "]}";


            assert !merged.isEmpty();

            BufferedWriter writer = new BufferedWriter(new FileWriter(f1));
            writer.write(merged);
            writer.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
