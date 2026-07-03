package AudioAnalysis;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.File;
import java.io.IOException;

/**
 * A utility class for calculating the spectrogram of an audio file.
 * The spectrogram is a visual representation of the spectrum of frequencies in a sound signal as it varies with time.
 * This class uses Fast Fourier Transform (FFT) to compute the frequency components of the audio signal.
 * <p>
 * The audio file is decoded to mono PCM samples, resampled to 44100 Hz if necessary (all analyzers
 * assume that rate), split into Hann-windowed frames of {@code fftSize} samples advancing by
 * {@code fftSize - overlap} samples, and transformed with an in-place radix-2 FFT.
 * Each row of the result holds the magnitudes of the first {@code fftSize / 2} frequency bins.
 */
public class SpectrogramCalculator {

    /** All analyzers (BPMDetector, AudioAnalysis, TimingOffsetDetector) assume this sample rate. */
    private static final float TARGET_SAMPLE_RATE = 44100f;

    /**
     * Calculates the spectrogram for the provided audio file.
     *
     * @param filePath the path to the audio file.
     * @param fftSize  the size of the FFT window and buffer (must be a power of 2, e.g., 1024).
     * @param overlap  the number of samples that consecutive FFT windows overlap.
     * @return a 2D array where each row corresponds to a time frame and each column to a frequency bin magnitude.
     * @throws UnsupportedAudioFileException if the audio file format is not supported.
     * @throws IOException                   if an I/O error occurs while reading the audio file.
     */
    public static double[][] calculateSpectrogram(String filePath, int fftSize, int overlap) throws UnsupportedAudioFileException, IOException {
        if (fftSize <= 0 || Integer.bitCount(fftSize) != 1) throw new IllegalArgumentException("fftSize must be a power of 2, got: " + fftSize);
        int hop = fftSize - overlap;
        if (hop <= 0) throw new IllegalArgumentException("overlap must be smaller than fftSize");

        double[] samples = readMonoSamples(filePath);
        double[] window = hannWindow(fftSize);

        int frameCount = samples.length <= fftSize ? 1 : (samples.length - fftSize) / hop + 1;
        double[][] spectrogram = new double[frameCount][];

        double[] re = new double[fftSize];
        double[] im = new double[fftSize];
        for (int frame = 0; frame < frameCount; frame++) {
            int offset = frame * hop;
            for (int i = 0; i < fftSize; i++) {
                int s = offset + i;
                re[i] = s < samples.length ? samples[s] * window[i] : 0.0;
                im[i] = 0.0;
            }
            fft(re, im);

            double[] magnitudes = new double[fftSize / 2];
            for (int k = 0; k < magnitudes.length; k++) {
                magnitudes[k] = Math.sqrt(re[k] * re[k] + im[k] * im[k]);
            }
            spectrogram[frame] = magnitudes;
        }
        return spectrogram;
    }

    /**
     * Decodes the audio file to normalized mono samples in [-1, 1] at 44100 Hz.
     *
     * Non-PCM formats are converted to signed 16-bit PCM first, channels are averaged
     * into one, and other sample rates are linearly resampled.
     */
    private static double[] readMonoSamples(String filePath) throws UnsupportedAudioFileException, IOException {
        try (AudioInputStream in = AudioSystem.getAudioInputStream(new File(filePath))) {
            AudioFormat src = in.getFormat();
            AudioFormat pcm = new AudioFormat(
                    AudioFormat.Encoding.PCM_SIGNED,
                    src.getSampleRate(),
                    16,
                    src.getChannels(),
                    src.getChannels() * 2,
                    src.getSampleRate(),
                    false);
            try (AudioInputStream pcmIn = AudioSystem.getAudioInputStream(pcm, in)) {
                byte[] bytes = pcmIn.readAllBytes();
                int channels = pcm.getChannels();
                int frames = bytes.length / (2 * channels);

                double[] mono = new double[frames];
                for (int i = 0; i < frames; i++) {
                    double sum = 0;
                    for (int c = 0; c < channels; c++) {
                        int idx = (i * channels + c) * 2;
                        sum += (short) ((bytes[idx + 1] << 8) | (bytes[idx] & 0xFF));
                    }
                    mono[i] = sum / (channels * 32768.0);
                }
                return resampleTo44100(mono, src.getSampleRate());
            }
        }
    }

    /** Linearly resamples the signal to 44100 Hz; good enough for onset/tempo analysis. */
    private static double[] resampleTo44100(double[] samples, float fromRate) {
        if (Math.abs(fromRate - TARGET_SAMPLE_RATE) < 1 || samples.length == 0) return samples;

        int outLength = (int) ((long) samples.length * TARGET_SAMPLE_RATE / fromRate);
        double[] out = new double[outLength];
        double ratio = fromRate / TARGET_SAMPLE_RATE;
        for (int i = 0; i < outLength; i++) {
            double pos = i * ratio;
            int i0 = (int) pos;
            int i1 = Math.min(i0 + 1, samples.length - 1);
            double frac = pos - i0;
            out[i] = samples[i0] * (1 - frac) + samples[i1] * frac;
        }
        return out;
    }

    private static double[] hannWindow(int size) {
        double[] window = new double[size];
        for (int i = 0; i < size; i++) {
            window[i] = 0.5 * (1 - Math.cos(2 * Math.PI * i / (size - 1)));
        }
        return window;
    }

    /**
     * In-place iterative radix-2 Cooley-Tukey FFT.
     * Array length must be a power of 2 (validated by the caller).
     */
    private static void fft(double[] re, double[] im) {
        int n = re.length;

        // Bit-reversal permutation
        for (int i = 1, j = 0; i < n; i++) {
            int bit = n >> 1;
            for (; (j & bit) != 0; bit >>= 1) j ^= bit;
            j ^= bit;
            if (i < j) {
                double tmp = re[i]; re[i] = re[j]; re[j] = tmp;
                tmp = im[i]; im[i] = im[j]; im[j] = tmp;
            }
        }

        // Butterfly stages
        for (int len = 2; len <= n; len <<= 1) {
            double angle = -2 * Math.PI / len;
            double wRe = Math.cos(angle);
            double wIm = Math.sin(angle);
            for (int i = 0; i < n; i += len) {
                double curRe = 1, curIm = 0;
                for (int k = 0; k < len / 2; k++) {
                    int a = i + k;
                    int b = i + k + len / 2;
                    double tRe = re[b] * curRe - im[b] * curIm;
                    double tIm = re[b] * curIm + im[b] * curRe;
                    re[b] = re[a] - tRe;
                    im[b] = im[a] - tIm;
                    re[a] += tRe;
                    im[a] += tIm;
                    double nextRe = curRe * wRe - curIm * wIm;
                    curIm = curRe * wIm + curIm * wRe;
                    curRe = nextRe;
                }
            }
        }
    }
}
