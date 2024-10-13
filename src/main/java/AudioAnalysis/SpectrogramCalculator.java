package AudioAnalysis;

import be.tarsos.dsp.AudioDispatcher;
import be.tarsos.dsp.AudioEvent;
import be.tarsos.dsp.AudioProcessor;
import be.tarsos.dsp.io.jvm.AudioDispatcherFactory;
import be.tarsos.dsp.util.fft.FFT;
import lombok.SneakyThrows;

import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * A utility class for calculating the spectrogram of an audio file.
 * The spectrogram is a visual representation of the spectrum of frequencies in a sound signal as it varies with time.
 * This class uses Fast Fourier Transform (FFT) to compute the frequency components of the audio signal.
 * <p>
 * The calculated spectrogram can be used for various audio analysis tasks, such as beat detection, pitch tracking, and sound visualization.
 * <p>
 * This implementation is a Java port inspired by the BeatSaberAutomapper project.
 *
 * @author Java port of: <a href="https://github.com/lucienmaloney/BeatSaber.jl">BeatSaberAutomapper</a>
 */
public class SpectrogramCalculator {
    
    /**
     * /**
     * Calculates the spectrogram for the provided audio file.
     * This method reads the audio file, applies FFT on overlapping windows of audio samples,
     * and returns a 2D array representing the spectrogram (amplitude squared).
     *
     * @param filePath the path to the audio file.
     * @param fftSize  the size of the FFT window and buffer (should be a power of 2, e.g., 1024).
     * @param overlap  the number of samples that consecutive FFT windows overlap.
     * @return a 2D array representing the spectrogram, where each row corresponds to a time frame and each column to a frequency bin.
     * @throws UnsupportedAudioFileException if the audio file format is not supported.
     * @throws IOException                   if an I/O error occurs while reading the audio file.
     */
    @SneakyThrows
    public static double[][] calculateSpectrogram(String filePath, int fftSize, int overlap) {
        AudioDispatcher dispatcher = AudioDispatcherFactory.fromFile(new File(filePath), fftSize, overlap);
        FFT fft = new FFT(fftSize);
        List<double[]> spectrogram = new ArrayList<>();
        
        dispatcher.addAudioProcessor(new AudioProcessor() {
            @Override
            public boolean process(AudioEvent audioEvent) {
                float[] audioBuffer = audioEvent.getFloatBuffer().clone();
                float[] fftBuffer = new float[fftSize * 2];
                System.arraycopy(audioBuffer, 0, fftBuffer, 0, audioBuffer.length);
                fft.forwardTransform(fftBuffer);
                double[] amplitudes = new double[fftSize / 2];
                
                for (int i = 0; i < amplitudes.length; i++) {
                    amplitudes[i] = Math.pow(fftBuffer[2 * i] * fftBuffer[2 * i] + fftBuffer[2 * i + 1] * fftBuffer[2 * i + 1], 0.5);
                }
                spectrogram.add(amplitudes);
                return true;
            }
            
            @Override
            public void processingFinished() {
                System.out.println("Finished processing!");
            }
        });
        
        dispatcher.run();
        return listToArray(spectrogram);
    }
    
    /**
     * Converts a list of double arrays into a 2D double array.
     * This utility method is used to transform the dynamically growing list of spectrogram frames into a fixed-size 2D array.
     *
     * @param list the list of double arrays to convert.
     * @return a 2D array representing the same data as the list.
     */
    private static double[][] listToArray(List<double[]> list) {
        double[][] array = new double[list.size()][];
        for (int i = 0; i < list.size(); i++) {
            array[i] = list.get(i);
        }
        return array;
    }
}
