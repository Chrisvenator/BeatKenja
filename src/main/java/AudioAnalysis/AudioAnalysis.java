package AudioAnalysis;

import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * This class provides methods for analyzing audio files, specifically for detecting peaks in the audio that can be used to generate beat maps for rhythm games.
 * The audio analysis is performed using techniques like spectrogram calculation, spectral flux analysis, and peak detection.
 * The audio file to be analyzed should be in WAV format.
 * <p>
 * The main functionality includes calculating the spectrogram of the audio, detecting peaks at different difficulty levels,
 * and returning these peaks as potential beat locations.
 * <p>
 * The class makes use of Fast Fourier Transform (FFT) for spectrogram calculation and uses a rolling average to compute spectral flux.
 *
 * @author Java port of:
 * <a href="https://github.com/lucienmaloney/BeatSaber.jl">BeatSaberAutomapper</a>
 */
public class AudioAnalysis {
    /** The sample rate of the audio file, typically 44100 Hz for CD quality audio. This value is used to determine the time resolution of the spectrogram.*/
    private static final int SAMPLE_RATE = 44100;
    /** The size of the Fast Fourier Transform (FFT) window used in spectrogram calculation. This should be a power of 2, and it determines the frequency resolution of the spectrogram.*/
    private static final int FFT_SIZE = 1024;
    /** The number of samples by which consecutive FFT windows overlap. This value affects the time resolution of the spectrogram and is typically set to half of the FFT size.*/
    private static final int OVERLAP = 512;


    /**
     * Analyzes the audio file at the given file path and detects peaks that could correspond to beats in the music.
     * The detected peaks are returned for different difficulty levels.
     *
     * @param filePath The path to the audio file to be analyzed.
     * @return A list of lists, where each inner list contains the time positions of detected peaks for a specific difficulty level.
     * @throws UnsupportedAudioFileException if the audio file format is not supported.
     * @throws IOException                   if an I/O error occurs while reading the audio file.
     */
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

    /**
     * Computes the spectral flux difference using a rolling average over the given flux array.
     * This method is used to identify changes in the audio signal that may correspond to beats.
     *
     * @param len  The length of the flux array.
     * @param flux The array containing the spectral flux values.
     * @return An ArrayList of differences representing the spectral flux.
     */
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

    /**
     * Generates a linearly spaced array of doubles between the start and end values.
     * This method is used to create a time axis for the spectrogram.
     *
     * @param start The starting value of the range.
     * @param end   The ending value of the range.
     * @param num   The number of values to generate.
     * @return A double array containing the linearly spaced values.
     */
    private static double[] linspace(double start, double end, int num) {
        double[] result = new double[num];
        double step = (end - start) / (num - 1);
        for (int i = 0; i < num; i++) {
            result[i] = start + i * step;
        }
        return result;
    }

    /**
     * Finds the maximum value within a specified range of an ArrayList of Doubles.
     * This method is used during peak detection to find local maxima in the spectral flux.
     *
     * @param data  The ArrayList of Double values to search within.
     * @param start The starting index of the range.
     * @param end   The ending index of the range.
     * @return The maximum value found within the specified range.
     */
    private static double maxInRange(ArrayList<Double> data, int start, int end) {
        double max = data.get(start);
        for (int i = start + 1; i <= end; i++) {
            max = Math.max(max, data.get(i));
        }
        return max;
    }
}
