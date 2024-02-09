package DataManager;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class JsonFileMerger {
    public static void main(String[] args) {
        JsonFileMerger merger = new JsonFileMerger();
        merger.mergeAll("src/DataManager/merged.json", 200000, 201000);
    }


    private final String DOWNLOAD_DIRECTORY;

    public JsonFileMerger() {
        this.DOWNLOAD_DIRECTORY = Parameters.DEFAULT_BEATSAVER_MAP_INFO_PATH;
    }

    /**
     * Merges all files in the download directory from start to end into one json file.
     *
     * @param outputFile path of the output file
     * @param start      beginning of the range (inclusive) in decimal (will be converted to hexadecimal)
     * @param end        end of the range (exclusive) in decimal (will be converted to hexadecimal)
     */
    public void mergeAll(String outputFile, int start, int end) {
        assert start - end >= 1;
        StringBuilder output = new StringBuilder("{\n");

        for (int i = start; i < end; i++) {
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
            System.err.println("Could not write to file " + outputFile + "!. So it will be outputted here: ");
            System.out.println(output);
        }
    }

    /**
     * Merges file2 into file 1.
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
