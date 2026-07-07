package AppLogic;

import AudioAnalysis.BPMDetector;
import AudioAnalysis.FooteSectionDetector;
import AudioAnalysis.Mp3ToWavConverter;
import AudioAnalysis.SpectrogramCalculator;
import AudioAnalysis.SuperFluxOnsetDetector;
import BeatSaberObjects.Objects.Bookmark;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static DataManager.Parameters.logger;

/**
 * UI-independent service that runs the full section analysis of one audio file:
 * SuperFlux filterbank spectrogram → Foote novelty boundaries → intensity tiers,
 * plus detected onsets and an estimated BPM for display.
 * <p>
 * Accepts .wav directly and .mp3 via the bundled converter. .ogg/.egg is decoded through
 * ffmpeg when it is installed (developer convenience) — end users should analyze the
 * .wav/.mp3 they built the map from.
 * <p>
 * {@link #toBookmarks} converts a result into bookmarks named after the pattern flags the
 * SECTIONED generator understands (see {@code MapGeneration.CreateMap}), so applying them
 * makes section-aware generation work end to end.
 */
public final class SectionAnalysisService {

    private static final int SAMPLE_RATE = 44100;
    /** Mirrors the Expert+ tier of {@link AudioAnalysis.AudioAnalysis} (FFT 2048 / hop 256, corpus-tuned). */
    private static final int FFT_SIZE = 2048;
    private static final int HOP_SIZE = 256;
    private static final int MU = 2;
    private static final double EXPERT_PLUS_DELTA_RELATIVE = 0.6;
    private static final double EXPERT_PLUS_BASE_GAP = 0.065;

    /**
     * Bookmark name per intensity tier (0 = calm … 4 = peak) — pattern flags consumed by the
     * SECTIONED generator. Heuristic first cut; tune by playtesting.
     */
    public static final String[] TIER_FLAGS = {"linear", "1-2", "complex", "complex", "normal_jumps"};

    /** Bookmark/visualization color per tier (RGB 0..1): blue → teal → green → orange → red. */
    public static final float[][] TIER_COLORS = {
            {0.25f, 0.45f, 0.85f},
            {0.20f, 0.70f, 0.70f},
            {0.30f, 0.75f, 0.30f},
            {0.90f, 0.60f, 0.20f},
            {0.85f, 0.25f, 0.25f}};

    /**
     * @param boundaries          section-boundary times in seconds, ascending
     * @param tiers               intensity tier (0..4) per section; length = boundaries + 1
     * @param noveltyTimesSeconds time axis of the novelty curve
     * @param novelty             Foote novelty curve (for the Song Map drawing)
     * @param onsetTimesSeconds   detected onsets, Expert+ parameters
     * @param durationSeconds     analyzed audio duration
     * @param estimatedBpm        tempo estimate of the audio
     * @param wavFile             the wav that was analyzed (source file or temp conversion) —
     *                            reusable e.g. for audio preview playback
     */
    public record SectionAnalysis(ArrayList<Double> boundaries, int[] tiers,
                                  double[] noveltyTimesSeconds, double[] novelty,
                                  double[] onsetTimesSeconds, double durationSeconds,
                                  double estimatedBpm, File wavFile) {}

    private SectionAnalysisService() {}

    /**
     * Analyzes an audio file; blocking, call from a background thread.
     *
     * @param audioFile .wav, .mp3, or (with ffmpeg installed) .ogg/.egg
     * @return sections, novelty curve, onsets and BPM estimate
     * @throws Exception if the file cannot be decoded or analyzed
     */
    public static SectionAnalysis analyze(File audioFile) throws Exception {
        File wav = ensureWav(audioFile);
        double frameAdvance = HOP_SIZE / (double) SAMPLE_RATE;

        double[][] spectrogram = SpectrogramCalculator.calculateSpectrogram(
                wav.getAbsolutePath(), FFT_SIZE, FFT_SIZE - HOP_SIZE);
        double[][] filtered = SuperFluxOnsetDetector.filteredLogSpectrogram(spectrogram, SAMPLE_RATE);
        double[] odf = SuperFluxOnsetDetector.computeODF(spectrogram, SAMPLE_RATE, MU);

        double bpm = BPMDetector.estimateTempo(odf, frameAdvance);
        double delta = EXPERT_PLUS_DELTA_RELATIVE * SuperFluxOnsetDetector.positiveMean(odf);
        double minGap = EXPERT_PLUS_BASE_GAP * 120.0 / bpm;
        double timeShift = FFT_SIZE / 2.0 / SAMPLE_RATE;
        ArrayList<Double> onsets = SuperFluxOnsetDetector.pickPeaks(odf, frameAdvance, timeShift, delta, minGap);
        double[] onsetTimes = onsets.stream().mapToDouble(Double::doubleValue).toArray();

        FooteSectionDetector.Result sections = FooteSectionDetector.detect(filtered, frameAdvance);
        double duration = spectrogram.length * frameAdvance;
        int[] tiers = FooteSectionDetector.rateIntensity(
                sections.boundaries(), duration, filtered, frameAdvance, onsetTimes);

        logger.info("Section analysis of {}: {} sections, BPM estimate {}",
                audioFile.getName(), sections.boundaries().size() + 1, bpm);
        return new SectionAnalysis(sections.boundaries(), tiers,
                sections.noveltyTimesSeconds(), sections.novelty(), onsetTimes, duration, bpm, wav);
    }

    /**
     * Converts the analysis into SECTIONED-generator bookmarks: one at beat 0 for the first
     * section, one per boundary, named by the section's tier flag and colored by tier.
     *
     * @param analysis a result of {@link #analyze}
     * @param bpm      the map's BPM (bookmark times are in beats)
     * @return bookmarks ready to put on a {@code BeatSaberMap}
     */
    public static List<Bookmark> toBookmarks(SectionAnalysis analysis, double bpm) {
        List<Bookmark> bookmarks = new ArrayList<>();
        double beatsPerSecond = bpm / 60.0;
        for (int s = 0; s < analysis.tiers().length; s++) {
            double startSeconds = s == 0 ? 0 : analysis.boundaries().get(s - 1);
            int tier = analysis.tiers()[s];
            bookmarks.add(new Bookmark((float) (startSeconds * beatsPerSecond),
                    TIER_FLAGS[tier], TIER_COLORS[tier].clone()));
        }
        return bookmarks;
    }

    /** Returns a readable wav for the given audio file, converting into a temp file if needed. */
    private static File ensureWav(File audio) throws IOException {
        String name = audio.getName().toLowerCase(Locale.ROOT);
        if (name.endsWith(".wav")) return audio;

        File wav = Files.createTempFile("beatkenja-sections-", ".wav").toFile();
        wav.deleteOnExit();
        if (name.endsWith(".mp3")) {
            Mp3ToWavConverter.convert(audio.getAbsolutePath(), wav.getAbsolutePath());
            return wav;
        }
        if (name.endsWith(".ogg") || name.endsWith(".egg")) {
            return decodeWithFfmpeg(audio, wav);
        }
        throw new IOException("Unsupported audio format: " + audio.getName() + " (use .wav or .mp3)");
    }

    private static File decodeWithFfmpeg(File audio, File wav) throws IOException {
        try {
            Process process = new ProcessBuilder(
                    "ffmpeg", "-y", "-v", "error",
                    "-i", audio.getAbsolutePath(),
                    "-ac", "1", "-ar", String.valueOf(SAMPLE_RATE), "-sample_fmt", "s16",
                    wav.getAbsolutePath())
                    .redirectErrorStream(true)
                    .start();
            String output = new String(process.getInputStream().readAllBytes());
            if (process.waitFor() != 0) throw new IOException("ffmpeg failed: " + output);
            return wav;
        } catch (IOException | InterruptedException e) {
            if (e instanceof InterruptedException) Thread.currentThread().interrupt();
            throw new IOException(".ogg/.egg needs ffmpeg on the PATH — or analyze the .wav/.mp3 "
                    + "the map was built from (" + e.getMessage() + ")");
        }
    }
}
