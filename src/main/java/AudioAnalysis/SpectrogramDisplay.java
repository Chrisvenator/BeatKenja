package AudioAnalysis;

import DataManager.Parameters;

import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/**
 * A class that displays a spectrogram visualization of an audio file in a graphical window.
 * This class can also mark the detected peaks (onsets) on the spectrogram, making it useful for analyzing rhythm games or music production.
 * The spectrogram is displayed using a grayscale image where the intensity of each pixel corresponds to the amplitude of a frequency component at a given time.
 * Red vertical lines can be drawn to indicate the positions of detected peaks.
 */
public class SpectrogramDisplay extends JFrame {
    /** The image representing the spectrogram, where each pixel corresponds to a frequency component's amplitude at a specific time.*/
    private final BufferedImage image;
    /** A list of time points where peaks (onsets) have been detected in the audio. These peaks can be visually marked on the spectrogram.*/
    private final ArrayList<Double> peakTimes;
    /** The total duration of the audio file in seconds. This is used to map peak times and other features to the correct positions on the spectrogram.*/
    private final double duration;
    /** The name of the difficulty level associated with the displayed spectrogram. This is used as part of the window's title.*/
    private final String difficultyName;

    /**
     * Constructs a new SpectrogramDisplay with the given spectrogram data, peak times, audio duration, and difficulty name.
     * Optionally, detected peaks can be marked on the spectrogram.
     *
     * @param spectrogramData The 2D array containing the spectrogram data (amplitude squared values).
     * @param peakTimes       A list of time points where peaks (onsets) were detected.
     * @param duration        The total duration of the audio file in seconds.
     * @param difficultyName  The name of the difficulty level to be displayed in the window title.
     * @param markPeaks       If true, peaks will be marked on the spectrogram with red lines.
     */
    public SpectrogramDisplay(double[][] spectrogramData, ArrayList<Double> peakTimes, double duration, String difficultyName ,boolean markPeaks) {
        this.peakTimes = peakTimes;
        this.duration = duration;
        this.image = createImageFromSpectrogram(spectrogramData);
        this.difficultyName = difficultyName;
        if (markPeaks) markPeaks();
        initUI();
    }

    /**
     * Initializes the user interface of the SpectrogramDisplay.
     * Sets up the window with the spectrogram image and configures basic properties like size and closing behavior.
     */
    private void initUI() {
        setTitle("Spectrogram Display with Onsets: " + difficultyName);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(new Dimension(image.getWidth(), image.getHeight()));
        setLocationRelativeTo(null);

        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.drawImage(image, 0, 0, this);
            }
        };
        add(panel);
    }

    /**
     * Creates a BufferedImage from the given spectrogram data.
     * The spectrogram data is scaled and converted into a grayscale image where each pixel's intensity represents the amplitude of a frequency component.
     *
     * @param data The 2D array of spectrogram data.
     * @return A BufferedImage representing the spectrogram.
     */
    private BufferedImage createImageFromSpectrogram(double[][] data) {
        int width = data.length;
        int height = data[0].length;
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = image.createGraphics();

        // Find min and max values for contrast adjustment
        double min = Double.MAX_VALUE;
        double max = Double.MIN_VALUE;
        for (double[] row : data) {
            for (double d : row) {
                if (d < min) min = d;
                if (d > max) max = d;
            }
        }

        // Use a logarithmic scale if dynamic range is large
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                double value = data[x][y];
                // Normalize and apply logarithmic scaling if necessary
                double scaledValue = (Math.log10(value - min + 1) / Math.log10(max - min + 1));
                int colorValue = (int) (scaledValue * 255);
                Color color = new Color(colorValue, colorValue, colorValue);
                image.setRGB(x, height - y - 1, color.getRGB());
            }
        }

        g2d.dispose();
        return image;
    }

    /**
     * Marks the detected peaks on the spectrogram by drawing vertical red lines at the corresponding time positions.
     * The positions of the peaks are determined by the `peakTimes` list.
     */
    private void markPeaks() {
        int width = image.getWidth();
        Graphics2D g2d = image.createGraphics();
        g2d.setColor(Color.RED);

        // Draw vertical lines at the detected peak times
        for (Double time : peakTimes) {
            int x = (int) (time / duration * width);
            g2d.drawLine(x, 0, x, image.getHeight());
        }
        g2d.dispose();
    }

    /**
     * Finds the maximum value in a 2D array of doubles.
     * This method is useful for normalizing spectrogram data by identifying the highest amplitude value.
     *
     * @param data The 2D array to search for the maximum value.
     * @return The maximum value found in the array.
     */
    private double findMax(double[][] data) {
        double max = Double.MIN_VALUE;
        for (double[] row : data) {
            for (double d : row) {
                if (d > max) {
                    max = d;
                }
            }
        }
        return max;
    }

    /**
     * The main method to run the SpectrogramDisplay application.
     * It calculates the spectrogram and peaks from a given audio file, then displays the spectrogram with marked peaks in a window.
     *
     * @param args Command-line arguments (not used).
     * @throws UnsupportedAudioFileException If the audio file format is not supported.
     * @throws IOException                   If an I/O error occurs while reading the audio file.
     */
    public static void main(String[] args) throws UnsupportedAudioFileException, IOException
    {
        String filePath = Parameters.ONSET_GENERATION_FOLDER_PATH_INPUT + "/old/song.wav";
        int FFT_SIZE = 1024;
        int OVERLAP = 512;

        ArrayList<ArrayList<Double>> peaks = AudioAnalysis.getPeaksFromAudio(filePath);
        double duration = peaks.get(0).get(peaks.get(0).size() - 1); // Assuming the last peak time gives approximate duration
        double[][] spectrogram = SpectrogramCalculator.calculateSpectrogram(filePath, FFT_SIZE, OVERLAP);
        SwingUtilities.invokeLater(() -> {
            SpectrogramDisplay frame = new SpectrogramDisplay(spectrogram, peaks.get(0), duration, "Easy", true); // Example uses the first difficulty level
            frame.setVisible(true);
        });
    }
}
