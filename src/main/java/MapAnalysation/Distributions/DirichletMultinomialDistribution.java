package MapAnalysation.Distributions;

import MapGeneration.GenerationElements.Exceptions.NoteNotValidException;
import MapGeneration.GenerationElements.Pattern;

import java.util.Random;

public class DirichletMultinomialDistribution {
    // Methode zur Erzeugung von Stichproben aus einer Dirichlet-Verteilung
    public static double[] sampleDirichlet(double[] alpha) {
        double[] sample = new double[alpha.length];
        double sum = 0.0;
        Random rand = new Random();

        for (int i = 0; i < alpha.length; i++) {
            sample[i] = gammaSample(alpha[i], 1.0, rand);
            sum += sample[i];
        }

        for (int i = 0; i < sample.length; i++) {
            sample[i] /= sum;
        }

        return sample;
    }

    // Methode zur Erzeugung von Stichproben aus einer Gamma-Verteilung
    public static double gammaSample(double shape, double scale, Random rand) {
        if (shape < 1.0) {
            // Weibull-basierte Methode
            double u = rand.nextDouble();
            return gammaSample(1.0 + shape, scale, rand) * Math.pow(u, 1.0 / shape);
        }

        // Marsaglia und Tsang Methode
        double d = shape - 1.0 / 3.0;
        double c = 1.0 / Math.sqrt(9.0 * d);
        while (true) {
            double x = rand.nextGaussian();
            double v = 1.0 + c * x;
            if (v > 0) {
                v = v * v * v;
                double u = rand.nextDouble();
                if (u < 1.0 - 0.0331 * (x * x) * (x * x) || Math.log(u) < 0.5 * x * x + d * (1.0 - v + Math.log(v))) {
                    return d * v * scale;
                }
            }
        }
    }

    // Methode zur Erzeugung von Stichproben aus einer Multinomial-Verteilung
    public static int[] sampleMultinomial(int N, double[] p) {
        int[] counts = new int[p.length];
        Random rand = new Random();

        for (int i = 0; i < N; i++) {
            double r = rand.nextDouble();
            double cumulative = 0.0;
            for (int j = 1; j < p.length; j++) {
                cumulative += p[j];
                if (r < cumulative) {
                    counts[j]++;
                    break;
                }
            }
        }

        return counts;
    }

    // andere Methoden, einschließlich computeProbabilities()...

    public static void main(String[] args) throws NoteNotValidException {
        Pattern pattern = new Pattern("C:\\Users\\SCCO\\IdeaProjects\\BeatKenja\\src\\main\\resources\\MapTemplates\\Template--ISeeFire.txt");
        // Setze die Parameter für die Dirichlet-Verteilung
        double[] alpha = {2.0, 2.0, 2.0}; // Parameter der Dirichlet-Verteilung
        int N = 10; // Anzahl der Ziehungen

        pattern.applyDirichletMultinomial(alpha, N);

        // Ausgabe der Resultate
//        for (int i = 0; i < pattern.count.length; i++) {
        for (int i = 0; i < 50; i++) {
            for (int j = 0; j < 14; j++) {
                System.out.print(pattern.count[i][j] + " ");
            }
            System.out.println();
        }
    }
}
