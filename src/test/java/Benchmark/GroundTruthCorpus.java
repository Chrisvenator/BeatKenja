package Benchmark;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

/**
 * Loads the local benchmark ground-truth corpus (ranked/curated Beat Saber maps with audio)
 * from {@code data/ground_truth} (override with {@code -Dbk.corpus=<path>}).
 * <p>
 * Each map folder must contain an Info.dat (v2), difficulty .dat files and a song.egg/.ogg.
 * Audio is decoded to mono 44.1 kHz WAV via ffmpeg and cached in {@code .wav_cache} inside
 * the corpus directory (the whole corpus dir is gitignored).
 * <p>
 * Maps with real mid-song BPM changes (custom-data _BPMChanges differing from the base BPM)
 * are flagged: their note times cannot be converted to seconds with a constant factor, so
 * they are excluded from onset F-measure and only used for BPM-detection evaluation.
 */
public final class GroundTruthCorpus {

    public static final String DEFAULT_LOCATION = "data/ground_truth";

    /**
     * @param bookmarkTimesSeconds hand-placed mapper bookmarks of the hardest Standard difficulty,
     *                             converted with the base BPM (empty if none; unreliable for
     *                             variable-BPM maps). Semantics vary per mapper: section markers,
     *                             lyrics, choreography cues — curate by spacing before using as
     *                             section ground truth.
     */
    public record CorpusMap(String id, String songName, File folder, File audio,
                            double bpm, boolean variableBpm, double[] noteTimesSeconds,
                            double[] bookmarkTimesSeconds) {}

    private GroundTruthCorpus() {}

    public static File corpusDirectory() {
        return new File(System.getProperty("bk.corpus", DEFAULT_LOCATION));
    }

    /** Loads all map folders; note times converted to seconds via the base BPM. */
    public static List<CorpusMap> load() throws IOException {
        File root = corpusDirectory();
        File[] folders = root.listFiles(File::isDirectory);
        if (folders == null) return List.of();

        List<CorpusMap> maps = new ArrayList<>();
        for (File folder : folders) {
            if (folder.getName().startsWith(".")) continue;
            CorpusMap map = loadSingle(folder);
            if (map != null) maps.add(map);
        }
        maps.sort((a, b) -> a.id().compareTo(b.id()));
        return maps;
    }

    private static CorpusMap loadSingle(File folder) throws IOException {
        File infoFile = new File(folder, "Info.dat");
        if (!infoFile.exists()) infoFile = new File(folder, "info.dat");
        if (!infoFile.exists()) return null;

        JsonObject info = JsonParser.parseString(readUtf8(infoFile)).getAsJsonObject();
        double bpm = info.get("_beatsPerMinute").getAsDouble();
        String songName = info.has("_songName") ? info.get("_songName").getAsString() : folder.getName();

        File audio = findAudio(folder);
        File diffFile = findHardestStandardDiff(folder, info);
        if (audio == null || diffFile == null) return null;

        JsonObject diff = JsonParser.parseString(readUtf8(diffFile)).getAsJsonObject();
        boolean variableBpm = hasRealBpmChanges(diff, bpm);

        List<Double> noteTimes = new ArrayList<>();
        double secondsPerBeat = 60.0 / bpm;
        JsonArray notes = diff.getAsJsonArray("_notes"); // v2 format
        if (notes != null) {
            for (JsonElement e : notes) {
                JsonObject n = e.getAsJsonObject();
                int type = n.has("_type") ? n.get("_type").getAsInt() : 0;
                if (type != 0 && type != 1) continue; // skip bombs (type 3)
                noteTimes.add(n.get("_time").getAsDouble() * secondsPerBeat);
            }
        } else {
            JsonArray colorNotes = diff.getAsJsonArray("colorNotes"); // v3 format (bombs are separate)
            if (colorNotes != null) {
                for (JsonElement e : colorNotes) {
                    noteTimes.add(e.getAsJsonObject().get("b").getAsDouble() * secondsPerBeat);
                }
            }
        }
        double[] times = noteTimes.stream().mapToDouble(Double::doubleValue).toArray();

        String id = folder.getName().split(" ")[0];
        return new CorpusMap(id, songName, folder, audio, bpm, variableBpm, times,
                parseBookmarks(diff, secondsPerBeat));
    }

    /** Bookmark beat times from v2 (_customData._bookmarks[]._time) or v3 (customData.bookmarks[].b). */
    private static double[] parseBookmarks(JsonObject diff, double secondsPerBeat) {
        JsonArray bookmarks = null;
        JsonObject customData = diff.getAsJsonObject("_customData");
        if (customData != null) bookmarks = customData.getAsJsonArray("_bookmarks");
        if (bookmarks == null) {
            customData = diff.getAsJsonObject("customData");
            if (customData != null) bookmarks = customData.getAsJsonArray("bookmarks");
        }
        if (bookmarks == null) return new double[0];

        List<Double> seconds = new ArrayList<>();
        for (JsonElement e : bookmarks) {
            JsonObject bookmark = e.getAsJsonObject();
            JsonElement beat = bookmark.has("_time") ? bookmark.get("_time") : bookmark.get("b");
            if (beat != null) seconds.add(beat.getAsDouble() * secondsPerBeat);
        }
        seconds.sort(Double::compareTo);
        return seconds.stream().mapToDouble(Double::doubleValue).toArray();
    }

    private static File findAudio(File folder) {
        File[] candidates = folder.listFiles((dir, name) ->
                name.endsWith(".egg") || name.endsWith(".ogg"));
        return candidates != null && candidates.length > 0 ? candidates[0] : null;
    }

    /** Picks the hardest Standard-characteristic difficulty listed in Info.dat (last entry). */
    private static File findHardestStandardDiff(File folder, JsonObject info) {
        JsonArray sets = info.getAsJsonArray("_difficultyBeatmapSets");
        if (sets == null) return null;
        File best = null;
        for (JsonElement setElement : sets) {
            JsonObject set = setElement.getAsJsonObject();
            if (!"Standard".equals(set.get("_beatmapCharacteristicName").getAsString())) continue;
            for (JsonElement diffElement : set.getAsJsonArray("_difficultyBeatmaps")) {
                File f = new File(folder, diffElement.getAsJsonObject().get("_beatmapFilename").getAsString());
                if (f.exists()) best = f; // Info.dat lists difficulties in ascending order
            }
        }
        return best;
    }

    private static boolean hasRealBpmChanges(JsonObject diff, double baseBpm) {
        // v3 format: top-level bpmEvents [{b: beat, m: bpm}]
        JsonArray bpmEvents = diff.getAsJsonArray("bpmEvents");
        if (bpmEvents != null) {
            for (JsonElement e : bpmEvents) {
                if (Math.abs(e.getAsJsonObject().get("m").getAsDouble() - baseBpm) > 0.5) return true;
            }
            return false;
        }
        // v2 format: _customData._BPMChanges
        JsonObject customData = diff.getAsJsonObject("_customData");
        if (customData == null) return false;
        JsonArray changes = customData.getAsJsonArray("_BPMChanges");
        if (changes == null) changes = customData.getAsJsonArray("BPMChanges");
        if (changes == null) return false;
        for (JsonElement e : changes) {
            JsonObject change = e.getAsJsonObject();
            double changedBpm = change.has("_BPM") ? change.get("_BPM").getAsDouble()
                    : change.has("m") ? change.get("m").getAsDouble() : baseBpm;
            if (Math.abs(changedBpm - baseBpm) > 0.5) return true;
        }
        return false;
    }

    /**
     * Decodes the map's ogg to mono 44.1 kHz 16-bit WAV via ffmpeg, cached inside the corpus dir.
     * The existing pipeline (SpectrogramCalculator) reads WAV through javax.sound.
     */
    public static File decodeToWav(CorpusMap map) throws IOException, InterruptedException {
        File cacheDir = new File(corpusDirectory(), ".wav_cache");
        if (!cacheDir.exists() && !cacheDir.mkdirs()) {
            throw new IOException("Cannot create wav cache dir: " + cacheDir);
        }
        File wav = new File(cacheDir, map.id() + ".wav");
        if (wav.exists() && wav.length() > 44) return wav;

        Process process = new ProcessBuilder(
                "ffmpeg", "-y", "-v", "error",
                "-i", map.audio().getAbsolutePath(),
                "-ac", "1", "-ar", "44100", "-sample_fmt", "s16",
                wav.getAbsolutePath())
                .redirectErrorStream(true)
                .start();
        String output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        int exit = process.waitFor();
        if (exit != 0) throw new IOException("ffmpeg failed for " + map.id() + ": " + output);
        return wav;
    }

    private static String readUtf8(File file) throws IOException {
        String content = Files.readString(file.toPath(), StandardCharsets.UTF_8);
        // strip UTF-8 BOM if present
        return content.startsWith("﻿") ? content.substring(1) : content;
    }
}
