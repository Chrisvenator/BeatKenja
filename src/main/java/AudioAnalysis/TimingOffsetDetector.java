package AudioAnalysis;

import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * This class provides methods for detecting timing offset (phase shift) in audio files.
 * It calculates how many milliseconds the song's beats are shifted from the expected
 * beat grid positions based on the detected or provided BPM.
 * <p>
 * The timing offset detection process involves:
 * 1. Detecting or using provided BPM to establish expected beat positions
 * 2. Finding actual onset positions in the audio
 * 3. Calculating the phase shift between expected and actual beat positions
 * 4. Using cross-correlation and statistical analysis for accurate offset detection
 * <p>
 * This is crucial for rhythm games like Beat Saber where precise timing alignment
 * is essential for gameplay synchronization.
 *
 * @author Timing Offset Detection System
 */
public class TimingOffsetDetector {
    /** The sample rate of the audio file, typically 44100 Hz for CD quality audio. */
    private static final int SAMPLE_RATE = 44100;
    /** The size of the Fast Fourier Transform (FFT) window used in spectrogram calculation. */
    private static final int FFT_SIZE = 1024;
    /** The number of samples by which consecutive FFT windows overlap. */
    private static final int OVERLAP = 768; // 256 hop size for better time resolution
    
    /** Maximum offset to consider in milliseconds (±500ms seems reasonable) */
    private static final double MAX_OFFSET_MS = 500.0;
    /** Resolution for offset detection in milliseconds */
    private static final double OFFSET_RESOLUTION_MS = 1.0;
    /** Minimum analysis duration in seconds to get reliable results */
    private static final double MIN_ANALYSIS_DURATION = 10.0;
    
    /**
     * Detects the timing offset of an audio file using auto-detected BPM.
     *
     * @param filePath The path to the audio file to analyze
     * @return The timing offset in milliseconds (positive = song is ahead, negative = song is behind)
     * @throws UnsupportedAudioFileException if the audio file format is not supported
     * @throws IOException if an I/O error occurs while reading the audio file
     */
    public static double detectTimingOffset(String filePath) throws UnsupportedAudioFileException, IOException {
        double detectedBPM = BPMDetector.detectBPM(filePath);
        return detectTimingOffset(filePath, detectedBPM);
    }
    
    /**
     * Detects the timing offset of an audio file using a provided BPM.
     *
     * @param filePath The path to the audio file to analyze
     * @param bpm The BPM of the song (beats per minute)
     * @return The timing offset in milliseconds (positive = song is ahead, negative = song is behind)
     * @throws UnsupportedAudioFileException if the audio file format is not supported
     * @throws IOException if an I/O error occurs while reading the audio file
     */
    public static Double detectTimingOffset(String filePath, double bpm) throws UnsupportedAudioFileException, IOException {
        if (bpm <= 0) {
            return null;
        }
        
        // Calculate spectrogram
        double[][] spectrogram = SpectrogramCalculator.calculateSpectrogram(filePath, FFT_SIZE, OVERLAP);
        double frameAdvance = (FFT_SIZE - OVERLAP) / (double) SAMPLE_RATE;
        
        // Compute onset detection function
        double[] onsetStrength = computeOnsetDetectionFunction(spectrogram);
        
        // Find strong onset peaks
        ArrayList<Double> onsetTimes = findOnsetTimes(onsetStrength, frameAdvance);
        
        // Calculate expected beat positions based on BPM
        double beatInterval = 60.0 / bpm; // seconds per beat
        double analysisLength = onsetStrength.length * frameAdvance;
        
        // Use multiple analysis methods and combine results
        double offsetCrossCorr = detectOffsetCrossCorrelation(onsetTimes, beatInterval, analysisLength);
        double offsetPhaseAnalysis = detectOffsetPhaseAnalysis(onsetTimes, beatInterval);
        double offsetGridFitting = detectOffsetGridFitting(onsetTimes, beatInterval);
        
        // Combine estimates using weighted average
        double finalOffset = combineOffsetEstimates(offsetCrossCorr, offsetPhaseAnalysis, offsetGridFitting);
        
        finalOffset = finalOffset % bpm; //Return the remainder. Like offset : 110.0000001, bpm 100 -> return 10
        
        return Math.round(finalOffset*0.001) + 0.0; // Round to nearest millisecond
    }
    
    /**
     * Computes onset detection function optimized for timing detection.
     */
    private static double[] computeOnsetDetectionFunction(double[][] spectrogram) {
        int timeFrames = spectrogram.length;
        int freqBins = spectrogram[0].length;
        double[] onsetStrength = new double[timeFrames];
        
        // Frequency weighting - emphasize percussion frequencies for timing
        double[] freqWeights = new double[freqBins];
        for (int f = 0; f < freqBins; f++) {
            double freq = f * SAMPLE_RATE / (2.0 * freqBins);
            if (freq < 60) {
                freqWeights[f] = 0.3; // Sub-bass
            } else if (freq < 250) {
                freqWeights[f] = 1.5; // Bass drum fundamentals
            } else if (freq < 2000) {
                freqWeights[f] = 1.2; // Mid frequencies
            } else if (freq < 8000) {
                freqWeights[f] = 2.0; // Snare and hi-hat attack
            } else {
                freqWeights[f] = 1.0; // High frequencies
            }
        }
        
        // Compute weighted spectral flux
        for (int t = 1; t < timeFrames; t++) {
            double flux = 0;
            for (int f = 0; f < freqBins; f++) {
                double diff = spectrogram[t][f] - spectrogram[t-1][f];
                // Half-wave rectification with emphasis on strong attacks
                if (diff > 0) {
                    flux += Math.pow(diff, 1.2) * freqWeights[f];
                }
            }
            onsetStrength[t] = flux;
        }
        
        // Apply adaptive smoothing
        return adaptiveSmooth(onsetStrength);
    }
    
    /**
     * Finds onset times from the onset strength function.
     */
    private static ArrayList<Double> findOnsetTimes(double[] onsetStrength, double frameAdvance) {
        ArrayList<Double> onsetTimes = new ArrayList<>();
        
        // Dynamic threshold based on signal statistics
        double[] sortedValues = onsetStrength.clone();
        Arrays.sort(sortedValues);
        double medianValue = sortedValues[sortedValues.length / 2];
        double threshold = medianValue * 2.0; // Threshold at 2x median
        
        // Minimum time between onsets (prevent false positives)
        double minOnsetInterval = 0.05; // 50ms minimum
        double lastOnsetTime = -1;
        
        // Find peaks above threshold
        for (int i = 2; i < onsetStrength.length - 2; i++) {
            double currentTime = i * frameAdvance;
            
            // Check if above threshold and is local maximum
            if (onsetStrength[i] > threshold &&
                    onsetStrength[i] > onsetStrength[i-1] &&
                    onsetStrength[i] > onsetStrength[i+1] &&
                    onsetStrength[i] > onsetStrength[i-2] &&
                    onsetStrength[i] > onsetStrength[i+2]) {
                
                // Check minimum interval
                if (currentTime - lastOnsetTime >= minOnsetInterval) {
                    onsetTimes.add(currentTime);
                    lastOnsetTime = currentTime;
                }
            }
        }
        
        return onsetTimes;
    }
    
    /**
     * Detects offset using cross-correlation between onset function and beat grid.
     */
    private static double detectOffsetCrossCorrelation(ArrayList<Double> onsetTimes, double beatInterval, double analysisLength) {
        if (onsetTimes.isEmpty()) return 0.0;
        
        // Create onset impulse train
        int numSamples = (int) Math.ceil(analysisLength / 0.001); // 1ms resolution
        double[] onsetTrain = new double[numSamples];
        
        for (double onsetTime : onsetTimes) {
            int index = (int) Math.round(onsetTime / 0.001);
            if (index >= 0 && index < numSamples) {
                onsetTrain[index] = 1.0;
            }
        }
        
        // Test different offsets
        int maxOffsetSamples = (int) (MAX_OFFSET_MS);
        double bestOffset = 0.0;
        double maxCorrelation = -1.0;
        
        for (int offsetMs = -maxOffsetSamples; offsetMs <= maxOffsetSamples; offsetMs++) {
            double offset = offsetMs * 0.001; // Convert to seconds
            double correlation = calculateBeatGridCorrelation(onsetTrain, beatInterval, offset, analysisLength);
            
            if (correlation > maxCorrelation) {
                maxCorrelation = correlation;
                bestOffset = offset;
            }
        }
        
        return bestOffset * 1000.0; // Convert to milliseconds
    }
    
    /**
     * Detects offset using phase analysis of onset positions relative to beat grid.
     */
    private static double detectOffsetPhaseAnalysis(ArrayList<Double> onsetTimes, double beatInterval) {
        if (onsetTimes.size() < 4) return 0.0;
        
        // Calculate phase of each onset relative to beat grid
        ArrayList<Double> phases = new ArrayList<>();
        
        for (double onsetTime : onsetTimes) {
            // Skip very early onsets (might be intro/pickup)
            if (onsetTime < beatInterval) continue;
            
            double phase = (onsetTime % beatInterval) / beatInterval;
            // Normalize phase to [-0.5, 0.5] range
            if (phase > 0.5) phase -= 1.0;
            phases.add(phase);
        }
        
        if (phases.isEmpty()) return 0.0;
        
        // Find the most common phase (circular statistics)
        return findCircularMean(phases) * beatInterval * 1000.0; // Convert to milliseconds
    }
    
    /**
     * Detects offset by fitting onset times to a beat grid.
     */
    private static double detectOffsetGridFitting(ArrayList<Double> onsetTimes, double beatInterval) {
        if (onsetTimes.size() < 3) return 0.0;
        
        double bestOffset = 0.0;
        double minError = Double.MAX_VALUE;
        
        // Test different starting offsets
        int numOffsetTests = (int) (MAX_OFFSET_MS / OFFSET_RESOLUTION_MS);
        
        for (int i = -numOffsetTests; i <= numOffsetTests; i++) {
            double testOffset = i * OFFSET_RESOLUTION_MS * 0.001; // Convert to seconds
            double totalError = 0.0;
            int validOnsets = 0;
            
            for (double onsetTime : onsetTimes) {
                // Find nearest beat position with this offset
                double adjustedTime = onsetTime - testOffset;
                double beatPosition = Math.round(adjustedTime / beatInterval) * beatInterval;
                double error = Math.abs(adjustedTime - beatPosition);
                
                // Only consider onsets that are reasonably close to a beat
                if (error < beatInterval * 0.25) { // Within 25% of beat interval
                    totalError += error * error; // Squared error
                    validOnsets++;
                }
            }
            
            if (validOnsets > 0) {
                double avgError = totalError / validOnsets;
                if (avgError < minError) {
                    minError = avgError;
                    bestOffset = testOffset;
                }
            }
        }
        
        return bestOffset * 1000.0; // Convert to milliseconds
    }
    
    /**
     * Combines multiple offset estimates using weighted averaging.
     */
    private static double combineOffsetEstimates(double offset1, double offset2, double offset3) {
        // Weights: Cross-correlation gets highest weight, then grid fitting, then phase analysis
        double[] offsets = {offset1, offset2, offset3};
        double[] weights = {0.5, 0.3, 0.2};
        
        // Check for consistency between estimates
        double maxDiff = 0;
        for (int i = 0; i < offsets.length; i++) {
            for (int j = i + 1; j < offsets.length; j++) {
                maxDiff = Math.max(maxDiff, Math.abs(offsets[i] - offsets[j]));
            }
        }
        
        // If estimates are very different, rely more on cross-correlation
        if (maxDiff > 50.0) { // More than 50ms difference
            weights[0] = 0.8;
            weights[1] = 0.15;
            weights[2] = 0.05;
        }
        
        // Weighted average
        double weightedSum = 0.0;
        double totalWeight = 0.0;
        
        for (int i = 0; i < offsets.length; i++) {
            if (Math.abs(offsets[i]) <= MAX_OFFSET_MS) { // Validate range
                weightedSum += offsets[i] * weights[i];
                totalWeight += weights[i];
            }
        }
        
        return totalWeight > 0 ? weightedSum / totalWeight : 0.0;
    }
    
    /**
     * Calculates correlation between onset train and beat grid at given offset.
     */
    private static double calculateBeatGridCorrelation(double[] onsetTrain, double beatInterval, double offset, double duration) {
        double correlation = 0.0;
        int beatCount = 0;
        
        // Generate beat positions with offset
        for (double beatTime = offset; beatTime < duration; beatTime += beatInterval) {
            if (beatTime < 0) continue;
            
            int index = (int) Math.round(beatTime / 0.001);
            if (index >= 0 && index < onsetTrain.length) {
                // Check correlation in a small window around each beat
                int windowSize = 10; // ±10ms window
                double windowSum = 0.0;
                int windowCount = 0;
                
                for (int w = -windowSize; w <= windowSize; w++) {
                    int checkIndex = index + w;
                    if (checkIndex >= 0 && checkIndex < onsetTrain.length) {
                        windowSum += onsetTrain[checkIndex];
                        windowCount++;
                    }
                }
                
                if (windowCount > 0) {
                    correlation += windowSum / windowCount;
                    beatCount++;
                }
            }
        }
        
        return beatCount > 0 ? correlation / beatCount : 0.0;
    }
    
    /**
     * Finds circular mean for phase analysis (handles wraparound at ±0.5).
     */
    private static double findCircularMean(ArrayList<Double> phases) {
        if (phases.isEmpty()) return 0.0;
        
        // Convert to unit circle coordinates
        double sumSin = 0.0;
        double sumCos = 0.0;
        
        for (double phase : phases) {
            double angle = phase * 2 * Math.PI; // Convert to radians
            sumSin += Math.sin(angle);
            sumCos += Math.cos(angle);
        }
        
        // Calculate mean angle and convert back to phase
        double meanAngle = Math.atan2(sumSin / phases.size(), sumCos / phases.size());
        return meanAngle / (2 * Math.PI);
    }
    
    /**
     * Applies adaptive smoothing to reduce noise while preserving sharp onsets.
     */
    private static double[] adaptiveSmooth(double[] signal) {
        double[] smoothed = new double[signal.length];
        
        for (int i = 0; i < signal.length; i++) {
            // Determine smoothing window based on local variation
            int windowSize = 3; // Base window size
            
            // Calculate local variance to adapt smoothing
            if (i > 2 && i < signal.length - 2) {
                double localVar = 0.0;
                for (int j = i - 2; j <= i + 2; j++) {
                    localVar += Math.pow(signal[j] - signal[i], 2);
                }
                localVar /= 5;
                
                // Reduce smoothing for high-variance regions (sharp onsets)
                if (localVar > 0.1) windowSize = 1;
            }
            
            // Apply smoothing
            int halfWindow = windowSize / 2;
            int start = Math.max(0, i - halfWindow);
            int end = Math.min(signal.length - 1, i + halfWindow);
            
            double sum = 0.0;
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
     * Result class containing offset and confidence information.
     */
    public static class OffsetResult {
        public final double offsetMs;
        public final double confidence;
        
        public OffsetResult(double offsetMs, double confidence) {
            this.offsetMs = offsetMs;
            this.confidence = confidence;
        }
        
        @Override
        public String toString() {
            return String.format("Offset: %.1fms (confidence: %.2f)", offsetMs, confidence);
        }
    }
}