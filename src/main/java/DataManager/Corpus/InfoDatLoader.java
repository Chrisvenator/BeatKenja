package DataManager.Corpus;

import org.json.JSONArray;
import org.json.JSONObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

/**
 * Parses a Beat Saber Info.dat (V2 format, with V3-key fallbacks) for corpus ingestion.
 * Handles both capital-I and lowercase-i filename variants.
 */
public class InfoDatLoader {

    private static final Logger logger = LogManager.getLogger(InfoDatLoader.class);

    public record DifficultyEntry(
        String characteristic,
        String difficulty,
        int difficultyRank,
        String filename,
        double njs,
        double beatOffset,
        /** Tags parsed from _customData/_tags or tags field; empty list when absent. */
        List<String> tags
    ) {}

    public record InfoDat(
        double bpm,
        String songFilename,
        List<DifficultyEntry> difficulties
    ) {}

    public static InfoDat load(File mapFolder) throws IOException {
        File infoFile = findInfoDat(mapFolder);
        if (infoFile == null) throw new IOException("No Info.dat in: " + mapFolder.getAbsolutePath());

        String content = Files.readString(infoFile.toPath());
        JSONObject json = new JSONObject(content);

        double bpm = json.optDouble("_beatsPerMinute", json.optDouble("beatsPerMinute", 0.0));
        String songFile = json.optString("_songFilename", json.optString("songFilename", ""));

        List<DifficultyEntry> entries = new ArrayList<>();
        JSONArray sets = json.optJSONArray("_difficultyBeatmapSets");
        if (sets == null) sets = json.optJSONArray("difficultyBeatmapSets");

        if (sets != null) {
            for (int i = 0; i < sets.length(); i++) {
                JSONObject set = sets.getJSONObject(i);
                String characteristic = set.optString("_beatmapCharacteristicName",
                        set.optString("beatmapCharacteristicName", "Standard"));

                JSONArray diffs = set.optJSONArray("_difficultyBeatmaps");
                if (diffs == null) diffs = set.optJSONArray("difficultyBeatmaps");
                if (diffs == null) continue;

                for (int j = 0; j < diffs.length(); j++) {
                    JSONObject d = diffs.getJSONObject(j);
                    entries.add(new DifficultyEntry(
                        characteristic,
                        d.optString("_difficulty", d.optString("difficulty", "")),
                        d.optInt("_difficultyRank", d.optInt("difficultyRank", 0)),
                        d.optString("_beatmapFilename", d.optString("beatmapFilename", "")),
                        d.optDouble("_noteJumpMovementSpeed", d.optDouble("noteJumpMovementSpeed", 10.0)),
                        d.optDouble("_noteJumpStartBeatOffset", d.optDouble("noteJumpStartBeatOffset", 0.0)),
                        parseTags(d)
                    ));
                }
            }
        }

        return new InfoDat(bpm, songFile, entries);
    }

    /** Extracts tags from _customData/_tags, customData/tags, or a top-level tags array. */
    private static List<String> parseTags(JSONObject d) {
        JSONArray arr = null;
        JSONObject customData = d.optJSONObject("_customData");
        if (customData == null) customData = d.optJSONObject("customData");
        if (customData != null) {
            arr = customData.optJSONArray("_tags");
            if (arr == null) arr = customData.optJSONArray("tags");
        }
        if (arr == null) arr = d.optJSONArray("_tags");
        if (arr == null) arr = d.optJSONArray("tags");
        if (arr == null) return List.of();

        List<String> result = new ArrayList<>();
        for (int k = 0; k < arr.length(); k++) result.add(arr.optString(k, "NULL"));
        return result;
    }

    private static File findInfoDat(File folder) {
        File[] files = folder.listFiles();
        if (files == null) return null;
        for (File f : files) {
            if (f.getName().equalsIgnoreCase("info.dat")) return f;
        }
        return null;
    }
}
