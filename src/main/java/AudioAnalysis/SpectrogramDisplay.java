package AudioAnalysis;

import DataManager.Parameters;
import javazoom.jl.decoder.JavaLayerException;

import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import static AudioAnalysis.SpectrogramCalculator.calculateSpectrogram;

public class SpectrogramDisplay extends JFrame {
    private final BufferedImage image;
    private final ArrayList<Double> peakTimes; // Times where peaks are detected
    private final double duration; // Total duration of the audio
    private final String difficultyName;

    public SpectrogramDisplay(double[][] spectrogramData, ArrayList<Double> peakTimes, double duration, String difficultyName ,boolean markPeaks) {
        this.peakTimes = peakTimes;
        this.duration = duration;
        this.image = createImageFromSpectrogram(spectrogramData);
        this.difficultyName = difficultyName;
        if (markPeaks) markPeaks();
        initUI();
    }

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

    public static void main(String[] args) throws UnsupportedAudioFileException, IOException, LineUnavailableException, JavaLayerException {
        String filePath = Parameters.ONSET_GENERATION_FOLDER_PATH_INPUT + "/old/song.wav";
        File f = new File(filePath);
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
