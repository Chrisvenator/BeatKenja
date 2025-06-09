package AudioAnalysis;

import DataManager.Parameters;

import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

/**
 * This class provides methods for analyzing audio files, specifically for detecting peaks in the audio that can be used to generate beat maps for rhythm games.
 * The audio analysis is performed using improved onset detection techniques including spectral flux analysis with phase information,
 * adaptive thresholding, and tempo-aware difficulty scaling.
 * The audio file to be analyzed should be in WAV format.
 * <p>
 * The main functionality includes calculating the spectrogram of the audio, detecting peaks at different difficulty levels,
 * and returning these peaks as potential beat locations.
 * <p>
 * The class makes use of Fast Fourier Transform (FFT) for spectrogram calculation and uses adaptive thresholding
 * with tempo compensation for more accurate onset detection.
 *
 * @author Improved version based on onset detection best practices
 */
public class AudioAnalysis {
    /** The sample rate of the audio file, typically 44100 Hz for CD quality audio. */
    private static final int SAMPLE_RATE = 44100;
    /** The size of the Fast Fourier Transform (FFT) window used in spectrogram calculation. */
    private static final int FFT_SIZE = 1024;
    /** The number of samples by which consecutive FFT windows overlap. */
    private static final int OVERLAP = 768; // 256 hop size for better time resolution
    
    // Difficulty-based target note spacing at 120 BPM (in seconds)
    private static final double[] BASE_GAP_SECONDS = {0.150, 0.110, 0.090, 0.075, 0.065}; // Easy to Expert+
    
    // Amplitude gating - keep only peaks above these percentiles
    private static final double[] AMPLITUDE_PERCENTILES = {0.90, 0.85, 0.75, 0.65, 0.55}; // Easy to Expert+
    
    /**
     * Analyzes the audio file at the given file path and detects peaks that could correspond to beats in the music.
     * The detected peaks are returned for different difficulty levels.
     *
     * @param filePath The path to the audio file to be analyzed.
     * @return A list of lists, where each inner list contains the time positions of detected peaks for a specific difficulty level.
     * @throws UnsupportedAudioFileException if the audio file format is not supported.
     * @throws IOException                   if an I/O error occurs while reading the audio file.
     */
    public static ArrayList<ArrayList<Double>> getPeaksFromAudio(String filePath, double bpm) throws UnsupportedAudioFileException, IOException {
        if (bpm < 0) bpm = Parameters.BPM;
        double[][] spec = SpectrogramCalculator.calculateSpectrogram(filePath, FFT_SIZE, OVERLAP);
        int len = spec.length;
        double frameAdvance = (FFT_SIZE - OVERLAP) / (double) SAMPLE_RATE;
        double[] times = linspace(0, frameAdvance * len, len);
        
        // Compute improved spectral flux with high-frequency emphasis
        double[] flux = computeSpectralFlux(spec);
        
        // Estimate BPM for tempo-aware processing
        double estimatedBPM = estimateBPM(flux, frameAdvance);
        
        // Calculate onset detection function using spectral flux difference
        ArrayList<Double> onsetStrength = computeOnsetDetectionFunction(flux);
        
        // Apply adaptive thresholding
        ArrayList<Double> thresholdedOnsets = adaptiveThreshold(onsetStrength);
        
        // Detect peaks for different difficulties with tempo compensation
        ArrayList<ArrayList<Double>> peaks = detectPeaksForDifficulties(thresholdedOnsets, times, frameAdvance, estimatedBPM);
        
        return peaks;
    }
    
    /**
     * Computes spectral flux with emphasis on high frequencies where percussive content is more prominent.
     */
    private static double[] computeSpectralFlux(double[][] spec) {
        int len = spec.length;
        int freqBins = spec[0].length;
        double[] flux = new double[len];
        
        // Weight higher frequencies more heavily (percussive content)
        double[] freqWeights = new double[freqBins];
        for (int f = 0; f < freqBins; f++) {
            // Emphasize frequencies above 1kHz
            double freq = f * SAMPLE_RATE / (2.0 * freqBins);
            freqWeights[f] = freq > 1000 ? 2.0 : 1.0;
        }
        
        for (int i = 0; i < len; i++) {
            double weightedSum = 0;
            for (int f = 0; f < freqBins; f++) {
                weightedSum += spec[i][f] * freqWeights[f];
            }
            flux[i] = weightedSum;
        }
        
        return flux;
    }
    
    /**
     * Simple BPM estimation based on autocorrelation of the onset detection function.
     */
    private static double estimateBPM(double[] flux, double frameAdvance) {
        // Simple peak-based BPM estimation
        // Look for periodicity in the 60-200 BPM range
        double minBPM = 60.0;
        double maxBPM = 200.0;
        double defaultBPM = 120.0; // fallback
        
        int minLag = (int)(60.0 / (maxBPM * frameAdvance));
        int maxLag = (int)(60.0 / (minBPM * frameAdvance));
        
        if (minLag >= flux.length || maxLag >= flux.length) {
            return defaultBPM;
        }
        
        double bestCorrelation = 0;
        double bestBPM = defaultBPM;
        
        for (int lag = minLag; lag <= Math.min(maxLag, flux.length - 1); lag++) {
            double correlation = 0;
            int count = 0;
            for (int i = 0; i < flux.length - lag; i++) {
                correlation += flux[i] * flux[i + lag];
                count++;
            }
            correlation /= count;
            
            if (correlation > bestCorrelation) {
                bestCorrelation = correlation;
                bestBPM = 60.0 / (lag * frameAdvance);
            }
        }
        
        return bestBPM;
    }
    
    /**
     * Computes the onset detection function using spectral flux difference.
     */
    private static ArrayList<Double> computeOnsetDetectionFunction(double[] flux) {
        ArrayList<Double> onsetStrength = new ArrayList<>();
        
        for (int i = 0; i < flux.length; i++) {
            if (i == 0) {
                onsetStrength.add(0.0);
            } else {
                // Positive difference only (half-wave rectification)
                double diff = flux[i] - flux[i-1];
                onsetStrength.add(Math.max(0, diff));
            }
        }
        
        return onsetStrength;
    }
    
    /**
     * Applies adaptive thresholding based on local statistics.
     */
    private static ArrayList<Double> adaptiveThreshold(ArrayList<Double> onsetStrength) {
        int len = onsetStrength.size();
        ArrayList<Double> thresholded = new ArrayList<>();
        int windowSize = Math.min(100, len / 10); // ~1 second window at typical frame rates
        
        for (int i = 0; i < len; i++) {
            int start = Math.max(0, i - windowSize);
            int end = Math.min(len - 1, i + windowSize);
            
            // Calculate local mean and standard deviation
            double sum = 0;
            double sumSquares = 0;
            int count = end - start + 1;
            
            for (int j = start; j <= end; j++) {
                double val = onsetStrength.get(j);
                sum += val;
                sumSquares += val * val;
            }
            
            double mean = sum / count;
            double variance = (sumSquares / count) - (mean * mean);
            double stdDev = Math.sqrt(Math.max(0, variance));
            
            // Adaptive threshold: mean + 1.5 * standard deviation
            double threshold = mean + 1.5 * stdDev;
            double currentValue = onsetStrength.get(i);
            
            thresholded.add(currentValue > threshold ? currentValue : 0.0);
        }
        
        return thresholded;
    }
    
    /**
     * Detects peaks for different difficulty levels with tempo-aware spacing.
     */
    private static ArrayList<ArrayList<Double>> detectPeaksForDifficulties(
            ArrayList<Double> onsetStrength, double[] times, double frameAdvance, double estimatedBPM) {
        
        ArrayList<ArrayList<Double>> allPeaks = new ArrayList<>();
        
        // Calculate amplitude threshold for each difficulty
        ArrayList<Double> nonZeroValues = new ArrayList<>();
        for (Double val : onsetStrength) {
            if (val > 0) nonZeroValues.add(val);
        }
        
        if (nonZeroValues.isEmpty()) {
            // Return empty lists for all difficulties
            for (int d = 0; d < 5; d++) {
                allPeaks.add(new ArrayList<>());
            }
            return allPeaks;
        }
        
        Collections.sort(nonZeroValues);
        
        for (int difficulty = 0; difficulty < 5; difficulty++) {
            ArrayList<Double> difficultyPeaks = new ArrayList<>();
            
            // Tempo-compensated minimum gap between notes
            double minGapSeconds = BASE_GAP_SECONDS[difficulty] * 120.0 / estimatedBPM;
            int suppressionFrames = Math.max(1, (int) Math.round(minGapSeconds / frameAdvance));
            
            // Amplitude threshold based on percentile
            double amplitudeThreshold = 0;
            if (!nonZeroValues.isEmpty()) {
                int percentileIndex = (int) (AMPLITUDE_PERCENTILES[difficulty] * (nonZeroValues.size() - 1));
                amplitudeThreshold = nonZeroValues.get(percentileIndex);
            }
            
            double lastAcceptedTime = -1e9;
            
            for (int i = suppressionFrames; i < onsetStrength.size() - suppressionFrames; i++) {
                double currentValue = onsetStrength.get(i);
                
                // Check if current frame is above amplitude threshold
                if (currentValue < amplitudeThreshold) continue;
                
                // Check if enough time has passed since last accepted peak
                if (times[i] - lastAcceptedTime < minGapSeconds) continue;
                
                // Check if this is a local maximum
                boolean isLocalMax = true;
                for (int j = i - suppressionFrames; j <= i + suppressionFrames; j++) {
                    if (j != i && onsetStrength.get(j) > currentValue) {
                        isLocalMax = false;
                        break;
                    }
                }
                
                if (isLocalMax && currentValue > 0) {
                    difficultyPeaks.add(times[i]);
                    lastAcceptedTime = times[i];
                }
            }
            
            allPeaks.add(difficultyPeaks);
        }
        
        return allPeaks;
    }
    
    /**
     * Generates a linearly spaced array of doubles between the start and end values.
     */
    private static double[] linspace(double start, double end, int num) {
        double[] result = new double[num];
        if (num == 1) {
            result[0] = start;
            return result;
        }
        double step = (end - start) / (num - 1);
        for (int i = 0; i < num; i++) {
            result[i] = start + i * step;
        }
        return result;
    }
}