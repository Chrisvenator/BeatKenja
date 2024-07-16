package AudioAnalysis;

//import be.tarsos.dsp.AudioDispatcher;
//import be.tarsos.dsp.AudioEvent;
//import be.tarsos.dsp.AudioProcessor;
//import be.tarsos.dsp.io.jvm.AudioDispatcherFactory;
//import be.tarsos.dsp.util.fft.FFT;

import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Calculates the spectrogram for the provided audio file.
 *
 * @author Java port of: <a href="https://github.com/lucienmaloney/BeatSaber.jl">BeatSaberAutomapper</a>
 */
public class SpectrogramCalculator {

    /**
     * Calculates the spectrogram for the provided audio file.
     *
     * @param filePath   the path to the audio file.
     * @param fftSize    the size of the FFT and the buffer (power of 2, e.g., 1024).
     * @param overlap    the number of samples to overlap between consecutive FFTs.
     * @return a 2D array representing the spectrogram (amplitude squared).
     */
    public static double[][] calculateSpectrogram(String filePath, int fftSize, int overlap) throws UnsupportedAudioFileException, IOException {
//        AudioDispatcher dispatcher = AudioDispatcherFactory.fromFile(new File(filePath), fftSize, overlap);
//        FFT fft = new FFT(fftSize);
        List<double[]> spectrogram = new ArrayList<>();
//
//        dispatcher.addAudioProcessor(new AudioProcessor() {
//            @Override
//            public boolean process(AudioEvent audioEvent) {
//                float[] audioBuffer = audioEvent.getFloatBuffer().clone();
//                float[] fftBuffer = new float[fftSize * 2];
//                System.arraycopy(audioBuffer, 0, fftBuffer, 0, audioBuffer.length);
//                fft.forwardTransform(fftBuffer);
//                double[] amplitudes = new double[fftSize / 2];
//
//                for (int i = 0; i < amplitudes.length; i++) {
//                    amplitudes[i] = Math.pow(fftBuffer[2 * i] * fftBuffer[2 * i] + fftBuffer[2 * i + 1] * fftBuffer[2 * i + 1], 0.5);
//                }
//                spectrogram.add(amplitudes);
//                return true;
//            }
//
//            @Override
//            public void processingFinished() {
//                System.out.println("Finished processing!");
//            }
//        });
//
//        dispatcher.run();
        return listToArray(spectrogram);
    }

    /**
     * Converts a list of double arrays into a 2D double array.
     *
     * @param list the list to convert.
     * @return the converted 2D array.
     */
    private static double[][] listToArray(List<double[]> list) {
        double[][] array = new double[list.size()][];
        for (int i = 0; i < list.size(); i++) {
            array[i] = list.get(i);
        }
        return array;
    }
}
