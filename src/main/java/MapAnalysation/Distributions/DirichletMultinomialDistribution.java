package MapAnalysation.Distributions;

import DataManager.Parameters;

import java.util.Arrays;
import java.util.Random;

public class DirichletMultinomialDistribution {
    private static final int MAX_ITERATIONS = 10000;
    private static final double TOLERANCE = 1e-6;


    // Methode zur Erzeugung von Stichproben aus einer Dirichlet-Verteilung
    public static double[] sampleDirichlet(int[] alpha) {
        double[] sample = new double[alpha.length];
        double sum = 0.0;
        Random rand = new Random(Parameters.SEED);

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

    public static void printCharacteristics(int[] alpha, int N) {
        System.out.println("Expected Value: " + Arrays.toString(computeExpectedValues(alpha, N)));
        System.out.println("Variance: " + Arrays.toString(computeVariances(alpha, N)));
        System.out.println("Covariance: " + Arrays.deepToString(computeCovariances(alpha, N)));
    }

    // Methode zur Berechnung des Erwartungswerts
    public static double[] computeExpectedValues(int[] alpha, int N) {
        double sumAlpha = 0.0;
        for (double a : alpha) {
            sumAlpha += a;
        }
        double[] expectedValues = new double[alpha.length];
        for (int i = 0; i < alpha.length; i++) {
            expectedValues[i] = N * (alpha[i] / sumAlpha);
        }
        return expectedValues;
    }

    // Methode zur Berechnung der Varianz
    public static double[] computeVariances( int[] alpha, int N) {
        double[] variance = new double[alpha.length];
        int alphaSum = sum(alpha);
        double factor = (double) (N + alphaSum) / (alphaSum + 1);

        for (int i = 0; i < alpha.length; i++) {
            double p = (double) alpha[i] / alphaSum;
            variance[i] = N * p * (1 - p) * factor;
        }
        return variance;
    }

    // Methode zur Berechnung der Kovarianz
    public static double[][] computeCovariances(int[] alpha, int N) {
        double[][] covariance = new double[alpha.length][alpha.length];
        double alphaSum = sum(alpha);
        double factor = (N + alphaSum) / (alphaSum + 1);

        for (int i = 0; i < alpha.length; i++) {
            for (int j = 0; j < alpha.length; j++) {
                if (i != j) {
                    covariance[i][j] = -N * (alpha[i] * alpha[j]) / (alphaSum * alphaSum) * factor;
                } else {
                    double p = alpha[i] / alphaSum;
                    covariance[i][i] = N * p * (1 - p) * factor;
                }
            }
        }
        return covariance;
    }

    private static int sum(int[] array) {
        int sum = 0;
        for (int v : array) {
            sum += v;
        }
        return sum;
    }

    // Maximum Likelihood Estimation for Dirichlet parameters
    public static int[] estimateAlphaMLE(int[] data, int[] alphaInit, int N) {
        int K = Math.min(alphaInit.length, data.length);
        int[] alpha = Arrays.copyOf(alphaInit, K);
        double[] g = new double[K];
        double[] h = new double[K];
        int[] newAlpha = new int[K];

        for (int iter = 0; iter < MAX_ITERATIONS; iter++) {
            Arrays.fill(g, 0);
            Arrays.fill(h, 0);
            double sumAlpha = 0.0;

            for (double a : alpha) {
                sumAlpha += a;
            }

                for (int k = 0; k < K; k++) {
                    g[k] += (data[k] - N * (alpha[k] / sumAlpha));
                    h[k] += (N * (alpha[k] / sumAlpha) * (1 - (alpha[k] / sumAlpha)));
                }

            boolean converged = true;
            for (int k = 0; k < K; k++) {
                newAlpha[k] = (int) Math.round(alpha[k] + g[k] / h[k]);
                if (Math.abs(newAlpha[k] - alpha[k]) > TOLERANCE) {
                    converged = false;
                }
            }

            if (converged) break;
            alpha = Arrays.copyOf(newAlpha, K);
        }
        return alpha;
    }
}
