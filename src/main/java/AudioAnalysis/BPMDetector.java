package AudioAnalysis;

import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

/**
 * This class provides methods for automatic BPM (Beats Per Minute) detection from audio files.
 * It uses multiple analysis techniques including autocorrelation, spectral flux analysis,
 * and peak detection to accurately determine the tempo of music tracks.
 * The audio file to be analyzed should be in WAV format.
 * <p>
 * The detection process involves:
 * 1. Computing spectral flux to identify onset events
 * 2. Using autocorrelation to find periodic patterns
 * 3. Analyzing beat intervals for tempo estimation
 * 4. Applying multiple validation techniques for accuracy
 *
 * @author BPM Detection System
 */
public class BPMDetector {
    /** The sample rate of the audio file, typically 44100 Hz for CD quality audio. */
    private static final int SAMPLE_RATE = 44100;
    /** The size of the Fast Fourier Transform (FFT) window used in spectrogram calculation. */
    private static final int FFT_SIZE = 1024;
    /** The number of samples by which consecutive FFT windows overlap. */
    private static final int OVERLAP = 768; // 256 hop size for better time resolution
    
    /** Minimum BPM to consider valid */
    private static final double MIN_BPM = 60.0;
    /** Maximum BPM to consider valid */
    private static final double MAX_BPM = 200.0;
    /** Default BPM fallback value */
    private static final double DEFAULT_BPM = 120.0;
    
    /** Common BPM values for validation */
    private static final double[] COMMON_BPMS = {
            60, 70, 80, 90, 100, 110, 120, 128, 130, 140, 150, 160, 170, 180, 200, 300, 400, 500, 600, 650, 666, 700, 800, 900, 1000, 2020, 2021, 2022, 2023, 2024, 2025, 2026, 2027
    };
    
    /**
     * Detects the BPM of an audio file using multiple analysis techniques.
     *
     * @param filePath The path to the audio file to analyze
     * @return The detected BPM as a double value
     * @throws UnsupportedAudioFileException if the audio file format is not supported
     * @throws IOException                   if an I/O error occurs while reading the audio file
     */
    public static double detectBPM(String filePath) throws UnsupportedAudioFileException, IOException {
        // Calculate spectrogram
        double[][] spectrogram = SpectrogramCalculator.calculateSpectrogram(filePath, FFT_SIZE, OVERLAP);
        double frameAdvance = (FFT_SIZE - OVERLAP) / (double) SAMPLE_RATE;
        
        // Compute onset detection function
        double[] onsetStrength = computeOnsetDetectionFunction(spectrogram);
        
        // Apply multiple BPM detection methods and combine results
        double bpmAutocorr = detectBPMAutocorrelation(onsetStrength, frameAdvance);
        double bpmIntervals = detectBPMIntervalAnalysis(onsetStrength, frameAdvance);
        double bpmSpectral = detectBPMSpectralAnalysis(spectrogram, frameAdvance);
        
        // Combine and validate results
        double finalBPM = combineBPMEstimates(bpmAutocorr, bpmIntervals, bpmSpectral);
        
        return validateAndRefineBPM(finalBPM);
    }
    
    /**
     * Computes the onset detection function from the spectrogram using spectral flux.
     */
    private static double[] computeOnsetDetectionFunction(double[][] spectrogram) {
        int timeFrames = spectrogram.length;
        int freqBins = spectrogram[0].length;
        double[] onsetStrength = new double[timeFrames];
        
        // Weight frequencies - emphasize mid and high frequencies where beats are prominent
        double[] freqWeights = new double[freqBins];
        for (int f = 0; f < freqBins; f++) {
            double freq = f * SAMPLE_RATE / (2.0 * freqBins);
            if (freq < 200) {
                freqWeights[f] = 0.5; // De-emphasize very low frequencies
            } else if (freq < 2000) {
                freqWeights[f] = 1.0; // Normal weight for mid frequencies
            } else if (freq < 8000) {
                freqWeights[f] = 1.5; // Emphasize higher frequencies
            } else {
                freqWeights[f] = 0.8; // Slightly reduce very high frequencies
            }
        }
        
        // Compute spectral flux with frequency weighting
        for (int t = 1; t < timeFrames; t++) {
            double flux = 0;
            for (int f = 0; f < freqBins; f++) {
                double diff = spectrogram[t][f] - spectrogram[t - 1][f];
                // Half-wave rectification - only consider increases in energy
                if (diff > 0) {
                    flux += diff * freqWeights[f];
                }
            }
            onsetStrength[t] = flux;
        }
        
        // Smooth the onset detection function
        return smoothSignal(onsetStrength, 3);
    }
    
    /**
     * Detects BPM using autocorrelation analysis.
     */
    private static double detectBPMAutocorrelation(double[] onsetStrength, double frameAdvance) {
        int minLag = (int) Math.ceil(60.0 / (MAX_BPM * frameAdvance));
        int maxLag = (int) Math.floor(60.0 / (MIN_BPM * frameAdvance));
        
        if (maxLag >= onsetStrength.length) {
            maxLag = onsetStrength.length - 1;
        }
        if (minLag >= maxLag) {
            return DEFAULT_BPM;
        }
        
        double[] autocorr = new double[maxLag - minLag + 1];
        
        // Compute autocorrelation
        for (int lag = minLag; lag <= maxLag; lag++) {
            double correlation = 0;
            int count = 0;
            for (int i = 0; i < onsetStrength.length - lag; i++) {
                correlation += onsetStrength[i] * onsetStrength[i + lag];
                count++;
            }
            if (count > 0) {
                autocorr[lag - minLag] = correlation / count;
            }
        }
        
        // Find the lag with maximum correlation
        int bestLagIndex = 0;
        double maxCorrelation = autocorr[0];
        for (int i = 1; i < autocorr.length; i++) {
            if (autocorr[i] > maxCorrelation) {
                maxCorrelation = autocorr[i];
                bestLagIndex = i;
            }
        }
        
        int bestLag = bestLagIndex + minLag;
        return 60.0 / (bestLag * frameAdvance);
    }
    
    /**
     * Detects BPM by analyzing intervals between onset peaks.
     */
    private static double detectBPMIntervalAnalysis(double[] onsetStrength, double frameAdvance) {
        // Find peaks in onset strength
        ArrayList<Integer> peakIndices = findPeaks(onsetStrength, 0.3); // Threshold at 30% of max
        
        if (peakIndices.size() < 2) {
            return DEFAULT_BPM;
        }
        
        // Calculate intervals between consecutive peaks
        ArrayList<Double> intervals = new ArrayList<>();
        for (int i = 1; i < peakIndices.size(); i++) {
            double interval = (peakIndices.get(i) - peakIndices.get(i - 1)) * frameAdvance;
            intervals.add(interval);
        }
        
        // Find the most common interval (mode)
        Collections.sort(intervals);
        double mostCommonInterval = findMostCommonInterval(intervals);
        
        return 60.0 / mostCommonInterval;
    }
    
    /**
     * Detects BPM using spectral analysis in the tempo domain.
     */
    private static double detectBPMSpectralAnalysis(double[][] spectrogram, double frameAdvance) {
        // Focus on lower frequency bands where bass and kick drums are prominent
        int lowFreqBins = spectrogram[0].length / 4; // Roughly up to 5.5kHz
        double[] bassOnsets = new double[spectrogram.length];
        
        for (int t = 1; t < spectrogram.length; t++) {
            double flux = 0;
            for (int f = 0; f < lowFreqBins; f++) {
                double diff = spectrogram[t][f] - spectrogram[t - 1][f];
                if (diff > 0) {
                    flux += diff;
                }
            }
            bassOnsets[t] = flux;
        }
        
        // Apply autocorrelation to bass-focused onset function
        return detectBPMAutocorrelation(bassOnsets, frameAdvance);
    }
    
    /**
     * Combines multiple BPM estimates using weighted averaging and validation.
     */
    private static double combineBPMEstimates(double bpm1, double bpm2, double bpm3) {
        double[] bpms = {bpm1, bpm2, bpm3};
        double[] weights = {0.4, 0.35, 0.25}; // Autocorr gets highest weight
        
        // Check for harmonic relationships and adjust
        for (int i = 0; i < bpms.length; i++) {
            for (int j = i + 1; j < bpms.length; j++) {
                double ratio = bpms[i] / bpms[j];
                // Check if one is half or double the other
                if (Math.abs(ratio - 2.0) < 0.1) {
                    bpms[j] = bpms[i]; // Use the consistent value
                } else if (Math.abs(ratio - 0.5) < 0.1) {
                    bpms[i] = bpms[j]; // Use the consistent value
                }
            }
        }
        
        // Weighted average
        double weightedSum = 0;
        double totalWeight = 0;
        for (int i = 0; i < bpms.length; i++) {
            if (bpms[i] >= MIN_BPM && bpms[i] <= MAX_BPM) {
                weightedSum += bpms[i] * weights[i];
                totalWeight += weights[i];
            }
        }
        
        return totalWeight > 0 ? weightedSum / totalWeight : DEFAULT_BPM;
    }
    
    /**
     * Validates and refines the BPM estimate by checking against common values.
     */
    private static double validateAndRefineBPM(double estimatedBPM) {
        if (estimatedBPM < MIN_BPM || estimatedBPM > MAX_BPM) {
            return DEFAULT_BPM;
        }
        
        // Check if the estimate is close to common BPM values
        double closestCommon = COMMON_BPMS[0];
        double minDistance = Math.abs(estimatedBPM - closestCommon);
        
        for (double commonBPM : COMMON_BPMS) {
            double distance = Math.abs(estimatedBPM - commonBPM);
            if (distance < minDistance) {
                minDistance = distance;
                closestCommon = commonBPM;
            }
        }
        
        // If very close to a common BPM (within 3 BPM), snap to it
        if (minDistance <= 3.0) {
            return closestCommon;
        }
        
        return Math.round(estimatedBPM * 10.0) / 10.0; // Round to 1 decimal place
    }
    
    /**
     * Finds peaks in a signal above a relative threshold.
     */
    private static ArrayList<Integer> findPeaks(double[] signal, double relativeThreshold) {
        ArrayList<Integer> peaks = new ArrayList<>();
        
        // Find maximum value for threshold calculation
        double maxValue = Arrays.stream(signal).max().orElse(1.0);
        double threshold = maxValue * relativeThreshold;
        
        // Find local maxima above threshold
        for (int i = 1; i < signal.length - 1; i++) {
            if (signal[i] > threshold &&
                    signal[i] > signal[i - 1] &&
                    signal[i] > signal[i + 1]) {
                peaks.add(i);
            }
        }
        
        return peaks;
    }
    
    /**
     * Finds the most common interval in a list of intervals.
     */
    private static double findMostCommonInterval(ArrayList<Double> intervals) {
        if (intervals.isEmpty()) {
            return 0.5; // Default to 120 BPM equivalent interval
        }
        
        // Group intervals into bins
        final double binSize = 0.02; // 20ms bins
        java.util.Map<Integer, Integer> histogram = new java.util.HashMap<>();
        
        for (double interval : intervals) {
            int bin = (int) Math.round(interval / binSize);
            histogram.put(bin, histogram.getOrDefault(bin, 0) + 1);
        }
        
        // Find the bin with the most occurrences
        int mostCommonBin = Collections.max(histogram.entrySet(),
                java.util.Map.Entry.comparingByValue()).getKey();
        
        return mostCommonBin * binSize;
    }
    
    /**
     * Smooths a signal using a simple moving average filter.
     */
    private static double[] smoothSignal(double[] signal, int windowSize) {
        double[] smoothed = new double[signal.length];
        int halfWindow = windowSize / 2;
        
        for (int i = 0; i < signal.length; i++) {
            int start = Math.max(0, i - halfWindow);
            int end = Math.min(signal.length - 1, i + halfWindow);
            
            double sum = 0;
            int count = 0;
            for (int j = start; j <= end; j++) {
                sum += signal[j];
                count++;
            }
            
            smoothed[i] = sum / count;
        }
        
        return smoothed;
    }
    
    /**
     * Convenience method that returns BPM as integer for cases where precision isn't critical.
     */
    public static int detectBPMInteger(String filePath) throws UnsupportedAudioFileException, IOException {
        return (int) Math.round(detectBPM(filePath));
    }
}