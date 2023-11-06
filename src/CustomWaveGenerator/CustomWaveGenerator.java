package CustomWaveGenerator;

import BeatSaberObjects.Note;
import BeatSaberObjects.TimingNote;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class CustomWaveGenerator {

    private final List<Double> peaks;
    private final Random random;
    private double lastPeakX = -1;

    public CustomWaveGenerator(long SEED) {
        this.peaks = new ArrayList<>();
        this.random = new Random(SEED);
        addNewPeak();
        addNewPeak();
    }

    public CustomWaveGenerator(long SEED, int points) {
        this.peaks = new ArrayList<>();
        this.random = new Random(SEED);
        addNewPeak();
        addNewPeak();

        for (int i = 0; i < points; i++) getY(i);
    }


    private void addNewPeak() {
        double newY = random.nextDouble() * 3;
        double newX = lastPeakX + random.nextDouble() / 2; // Ensuring a minimum distance of 10 units and up to 100 units.
        peaks.add(newX);
        peaks.add(newY);
        lastPeakX = newX;
    }

    public double getY(double x) {
        // If x is beyond the last peak, generate new peaks
        while (lastPeakX < x) {
            addNewPeak();
        }

        // Find the two peaks that x is between
        for (int i = 0; i < peaks.size() - 2; i += 2) {
            double x1 = peaks.get(i);
            double y1 = peaks.get(i + 1);
            double x2 = peaks.get(i + 2);
            double y2 = peaks.get(i + 3);

            if (x >= x1 && x <= x2) {
                // Interpolate between the two peaks
                double t = (x - x1) / (x2 - x1);
                return y1 + t * (y2 - y1);
            }
        }

        // Default return if something goes wrong
        return -1;
    }

    public List<Coordinate> getCoordinates(List<Note> notes) {
        List<Coordinate> coordinates = new ArrayList<>();
        for (int i = 0; i < notes.size() - 1; i++) {
            float time = notes.get(i)._time;
            double y = getY(time) * 1.5 - 1;
            if (y <= 0) y = 0.001;
            if (y > 3) y = 3;
            coordinates.add(new Coordinate(time, y));
        }
        return coordinates;
    }
}