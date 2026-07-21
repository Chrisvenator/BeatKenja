package AudioAnalysis;

import MapGeneration.BatchWavToMaps;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests the pure-Java spectrogram implementation (the previous TarsosDSP-based one was
 * disabled and made the whole MP3 → timing maps pipeline throw at runtime).
 *
 * Uses synthesized wav files: a sine tone must show its energy in the matching
 * frequency bin, and a click track must let the BPM detector return a sane tempo.
 */
class SpectrogramCalculatorTest {

    private static final int SAMPLE_RATE = 44100;
    private static final int FFT_SIZE = 1024;
    private static final int OVERLAP = 768;

    @TempDir
    Path tempDir;

    @Test
    void sineToneShowsUpInTheMatchingFrequencyBin() throws Exception {
        double frequency = 440.0;
        File wav = writeSineWav("sine440.wav", frequency, SAMPLE_RATE, 1.0);

        double[][] spec = SpectrogramCalculator.calculateSpectrogram(wav.getAbsolutePath(), FFT_SIZE, OVERLAP);

        int expectedFrames = (SAMPLE_RATE - FFT_SIZE) / (FFT_SIZE - OVERLAP) + 1;
        assertThat(spec.length).isEqualTo(expectedFrames);
        assertThat(spec[0].length).isEqualTo(FFT_SIZE / 2);

        int expectedBin = (int) Math.round(frequency * FFT_SIZE / SAMPLE_RATE);
        assertThat(maxBin(spec[spec.length / 2])).isBetween(expectedBin - 1, expectedBin + 1);
    }

    @Test
    void nonStandardSampleRateIsResampledSoBinsStayComparable() throws Exception {
        double frequency = 440.0;
        File wav = writeSineWav("sine440_22k.wav", frequency, 22050, 1.0);

        double[][] spec = SpectrogramCalculator.calculateSpectrogram(wav.getAbsolutePath(), FFT_SIZE, OVERLAP);

        // After resampling to 44100 Hz the tone must land in the same bin as a 44100 Hz file
        int expectedBin = (int) Math.round(frequency * FFT_SIZE / SAMPLE_RATE);
        assertThat(maxBin(spec[spec.length / 2])).isBetween(expectedBin - 1, expectedBin + 1);
    }

    @Test
    void bpmDetectorReturnsASaneTempoInsteadOfThrowing() throws Exception {
        File wav = writeClickTrackWav("clicks120.wav", 120.0, 10.0);

        double bpm = BPMDetector.detectBPM(wav.getAbsolutePath());

        assertThat(bpm).isBetween(60.0, 200.0);
    }

    @Test
    void extractBpmReadsTagOrReturnsNull() {
        assertThat(BatchWavToMaps.extractBpm("mySong128bpm")).isEqualTo(128);
        assertThat(BatchWavToMaps.extractBpm("Faun - Tanz mit mir")).isNull();
    }

    /** Highest-magnitude frequency bin of one spectrogram frame. */
    private static int maxBin(double[] frame) {
        int best = 0;
        for (int i = 1; i < frame.length; i++) {
            if (frame[i] > frame[best]) best = i;
        }
        return best;
    }

    private File writeSineWav(String name, double frequency, int sampleRate, double seconds) throws IOException {
        int frames = (int) (sampleRate * seconds);
        double[] samples = new double[frames];
        for (int i = 0; i < frames; i++) {
            samples[i] = 0.8 * Math.sin(2 * Math.PI * frequency * i / sampleRate);
        }
        return writeWav(name, samples, sampleRate);
    }

    /** Short noise bursts on every beat of the given tempo, silence in between. */
    private File writeClickTrackWav(String name, double bpm, double seconds) throws IOException {
        int frames = (int) (SAMPLE_RATE * seconds);
        double[] samples = new double[frames];
        double beatInterval = 60.0 / bpm;
        int clickLength = SAMPLE_RATE / 100; // 10ms clicks
        for (double t = 0; t < seconds; t += beatInterval) {
            int start = (int) (t * SAMPLE_RATE);
            for (int i = start; i < Math.min(start + clickLength, frames); i++) {
                samples[i] = 0.9 * Math.sin(2 * Math.PI * 1500 * (i - start) / (double) SAMPLE_RATE);
            }
        }
        return writeWav(name, samples, SAMPLE_RATE);
    }

    private File writeWav(String name, double[] samples, int sampleRate) throws IOException {
        byte[] pcm = new byte[samples.length * 2];
        for (int i = 0; i < samples.length; i++) {
            short value = (short) (samples[i] * Short.MAX_VALUE);
            pcm[2 * i] = (byte) (value & 0xFF);
            pcm[2 * i + 1] = (byte) ((value >> 8) & 0xFF);
        }
        AudioFormat format = new AudioFormat(sampleRate, 16, 1, true, false);
        File file = tempDir.resolve(name).toFile();
        try (AudioInputStream stream = new AudioInputStream(new ByteArrayInputStream(pcm), format, samples.length)) {
            AudioSystem.write(stream, AudioFileFormat.Type.WAVE, file);
        }
        return file;
    }
}
