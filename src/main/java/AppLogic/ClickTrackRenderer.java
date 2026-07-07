package AppLogic;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.nio.file.Files;

/**
 * Renders a copy of a wav with a short click mixed on top of each predicted onset —
 * the standard MIR way to verify onset detection by ear (librosa's {@code clicks}).
 * Used by the Song Map card's "Click on onsets" checkbox together with
 * {@link AudioPreviewPlayer#loadClickTrack}.
 */
public final class ClickTrackRenderer {

    private static final double CLICK_FREQUENCY_HZ = 1500;
    private static final double CLICK_DURATION_SECONDS = 0.03;
    private static final double CLICK_DECAY_SECONDS = 0.008;
    private static final double CLICK_AMPLITUDE = 0.85;

    private ClickTrackRenderer() {}

    /**
     * Mixes a decaying sine click at each onset into a temp-file copy of the wav.
     *
     * @param sourceWav         the analyzed wav (any PCM format; converted to 16-bit if needed)
     * @param onsetTimesSeconds click positions in seconds, on the same timeline as the wav
     * @return a temp wav (deleted on JVM exit) with the clicks mixed in, same duration as the source
     * @throws Exception if the wav cannot be read or written
     */
    public static File render(File sourceWav, double[] onsetTimesSeconds) throws Exception {
        AudioInputStream raw = AudioSystem.getAudioInputStream(sourceWav);
        AudioFormat format = raw.getFormat();
        AudioInputStream pcm = raw;
        if (format.getEncoding() != AudioFormat.Encoding.PCM_SIGNED || format.getSampleSizeInBits() > 16) {
            format = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED,
                    format.getSampleRate(), 16, format.getChannels(),
                    format.getChannels() * 2, format.getSampleRate(), false);
            pcm = AudioSystem.getAudioInputStream(format, raw);
        }
        byte[] data = pcm.readAllBytes();
        pcm.close();

        int channels = format.getChannels();
        float sampleRate = format.getSampleRate();
        boolean bigEndian = format.isBigEndian();
        int frameCount = data.length / (channels * 2);

        double[] click = buildClick(sampleRate);
        for (double onset : onsetTimesSeconds) {
            int startFrame = (int) Math.round(onset * sampleRate);
            if (startFrame < 0) continue;
            for (int i = 0; i < click.length && startFrame + i < frameCount; i++) {
                for (int ch = 0; ch < channels; ch++) {
                    int byteIndex = ((startFrame + i) * channels + ch) * 2;
                    int mixed = readSample(data, byteIndex, bigEndian) + (int) (click[i] * 32767);
                    writeSample(data, byteIndex, clamp(mixed), bigEndian);
                }
            }
        }

        File out = Files.createTempFile("beatkenja-clicks-", ".wav").toFile();
        out.deleteOnExit();
        try (AudioInputStream mixedStream = new AudioInputStream(
                new ByteArrayInputStream(data), format, frameCount)) {
            AudioSystem.write(mixedStream, AudioFileFormat.Type.WAVE, out);
        }
        return out;
    }

    /** A short sine burst with exponential decay — audible over music without masking it. */
    private static double[] buildClick(float sampleRate) {
        int length = (int) (CLICK_DURATION_SECONDS * sampleRate);
        double[] click = new double[length];
        for (int i = 0; i < length; i++) {
            double t = i / (double) sampleRate;
            click[i] = CLICK_AMPLITUDE * Math.sin(2 * Math.PI * CLICK_FREQUENCY_HZ * t)
                    * Math.exp(-t / CLICK_DECAY_SECONDS);
        }
        return click;
    }

    private static int readSample(byte[] data, int byteIndex, boolean bigEndian) {
        return bigEndian
                ? (short) ((data[byteIndex] << 8) | (data[byteIndex + 1] & 0xFF))
                : (short) ((data[byteIndex + 1] << 8) | (data[byteIndex] & 0xFF));
    }

    private static void writeSample(byte[] data, int byteIndex, int sample, boolean bigEndian) {
        if (bigEndian) {
            data[byteIndex] = (byte) (sample >> 8);
            data[byteIndex + 1] = (byte) sample;
        } else {
            data[byteIndex] = (byte) sample;
            data[byteIndex + 1] = (byte) (sample >> 8);
        }
    }

    private static int clamp(int sample) {
        return Math.max(Short.MIN_VALUE, Math.min(Short.MAX_VALUE, sample));
    }
}
