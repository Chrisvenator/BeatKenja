package AudioAnalysis;

import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * This class provides methods for analyzing audio files.
 * The main method demonstrates how to detect peaks in the audio.
 * The peaks can be used to generate beat maps for rhythm games.
 * The audio file should be in WAV format.
 *
 * @author Java port of: <a href="https://github.com/lucienmaloney/BeatSaber.jl">BeatSaberAutomapper</a>
 */
public class AudioAnalysis {
    private static final int SAMPLE_RATE = 44100; // Typical sample rate for CD quality audio
    private static final int FFT_SIZE = 1024; // FFT size, should be a power of 2
    private static final int OVERLAP = 512; // Overlap in samples for consecutive FFTs

    public static ArrayList<ArrayList<Double>> getPeaksFromAudio(String filePath) throws UnsupportedAudioFileException, IOException {
        double[][] spec = SpectrogramCalculator.calculateSpectrogram(filePath, FFT_SIZE, OVERLAP);
        int len = spec.length;
        double frameAdvance = (FFT_SIZE - OVERLAP) / (double) SAMPLE_RATE;
        double[] times = linspace(0, frameAdvance * len, len);


        // Compute spectral flux
        double[] flux = new double[len];
        for (int i = 0; i < len; i++) {
            flux[i] = Arrays.stream(spec[i]).sum();
        }

        // Calculate the difference using a rolling average
        ArrayList<Double> difference = getDoubles(len, flux);

        // Detect peaks for different difficulties
        ArrayList<ArrayList<Double>> peaks = new ArrayList<>();
        int[] ranges = {10, 7, 5, 3, 2};
        for (int j : ranges) {
            ArrayList<Double> difficultyPeaks = new ArrayList<>();
            for (int i = j; i < len - j; i++) {
                double localMax = maxInRange(difference, i - j, i + j);
                if (difference.get(i) == localMax && localMax > 0) {
                    difficultyPeaks.add(times[i]); // Here a delay can be added if the song should be delayed
                }
            }
            peaks.add(difficultyPeaks);
        }

        return peaks;
    }

    private static ArrayList<Double> getDoubles(int len, double[] flux) {
        ArrayList<Double> difference = new ArrayList<>();
        double rollingSum = 0;
        int window = 20;
        for (int i = 0; i < len; i++) {
            if (i > window) {
                rollingSum -= flux[i - window - 1];
            }
            if (i + window < len) {
                rollingSum += flux[i + window];
            }
            if (i > window) {
                double avg = rollingSum / (2 * window + 1) + 0.5;
                difference.add(flux[i] - avg);
            } else {
                difference.add(0.0); // No sufficient data for rolling average yet
            }
        }
        return difference;
    }

    private static double[] linspace(double start, double end, int num) {
        double[] result = new double[num];
        double step = (end - start) / (num - 1);
        for (int i = 0; i < num; i++) {
            result[i] = start + i * step;
        }
        return result;
    }

    private static double maxInRange(ArrayList<Double> data, int start, int end) {
        double max = data.get(start);
        for (int i = start + 1; i <= end; i++) {
            max = Math.max(max, data.get(i));
        }
        return max;
    }
}
