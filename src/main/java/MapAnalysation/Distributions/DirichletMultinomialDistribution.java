package MapAnalysation.Distributions;

import DataManager.Parameters;

import java.util.Arrays;
import java.util.Random;

import static DataManager.Parameters.logger;

/**
 * The `DirichletMultinomialDistribution` class provides methods for generating samples from Dirichlet and Multinomial distributions,
 * computing statistical properties such as expected values, variances, and covariances, and estimating Dirichlet distribution parameters
 * using Maximum Likelihood Estimation (MLE).
 */
public class DirichletMultinomialDistribution {
    /** The maximum number of iterations allowed for the Maximum Likelihood Estimation (MLE) process.*/
    private static final int MAX_ITERATIONS = 10000;
    /** The tolerance level for convergence during the Maximum Likelihood Estimation (MLE) process.*/
    private static final double TOLERANCE = 1e-6;


    /**
     * Generates a sample from a Dirichlet distribution given the concentration parameters.
     * Each component of the sample is generated using a Gamma distribution and normalized.
     *
     * @param alpha The concentration parameters for the Dirichlet distribution.
     * @return A sample from the Dirichlet distribution.
     */
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

    /**
     * Generates a sample from a Gamma distribution given the shape and scale parameters.
     * This method supports both the Marsaglia-Tsang method for shape >= 1 and the Weibull-based method for shape < 1.
     *
     * @param shape The shape parameter for the Gamma distribution.
     * @param scale The scale parameter for the Gamma distribution.
     * @param rand  The random number generator.
     * @return A sample from the Gamma distribution.
     */
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

    /**
     * Generates a sample from a Multinomial distribution given the number of trials and the probability distribution.
     * The method accumulates probabilities and determines the outcome for each trial.
     *
     * @param N The number of trials.
     * @param p The probability distribution for the outcomes.
     * @return An array representing the count of occurrences for each outcome.
     */
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

    /**
     * Prints the characteristics (expected value, variance, and covariance) of a Dirichlet-Multinomial distribution
     * given the concentration parameters and the number of trials.
     *
     * @param alpha The concentration parameters for the Dirichlet distribution.
     * @param N     The number of trials for the Multinomial distribution.
     */
    public static void printCharacteristics(int[] alpha, int N) {
        logger.info("Expected Value: {}", Arrays.toString(computeExpectedValues(alpha, N)));
        logger.info("Variance: {}", Arrays.toString(computeVariances(alpha, N)));
        logger.info("Covariance: {}", Arrays.deepToString(computeCovariances(alpha, N)));
        System.out.println("Expected Value: " + Arrays.toString(computeExpectedValues(alpha, N)));
        System.out.println("Variance: " + Arrays.toString(computeVariances(alpha, N)));
        System.out.println("Covariance: " + Arrays.deepToString(computeCovariances(alpha, N)));
    }

    /**
     * Computes the expected values for each component of a Dirichlet-Multinomial distribution
     * given the concentration parameters and the number of trials.
     *
     * @param alpha The concentration parameters for the Dirichlet distribution.
     * @param N     The number of trials for the Multinomial distribution.
     * @return An array of expected values.
     */
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

    /**
     * Computes the variances for each component of a Dirichlet-Multinomial distribution
     * given the concentration parameters and the number of trials.
     *
     * @param alpha The concentration parameters for the Dirichlet distribution.
     * @param N     The number of trials for the Multinomial distribution.
     * @return An array of variances.
     */
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

    /**
     * Computes the covariances between each pair of components in a Dirichlet-Multinomial distribution
     * given the concentration parameters and the number of trials.
     *
     * @param alpha The concentration parameters for the Dirichlet distribution.
     * @param N     The number of trials for the Multinomial distribution.
     * @return A matrix of covariances.
     */
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

    /**
     * Sums the elements of an integer array.
     *
     * @param array The array to be summed.
     * @return The sum of the elements in the array.
     */
    private static int sum(int[] array) {
        int sum = 0;
        for (int v : array) {
            sum += v;
        }
        return sum;
    }

    /**
     * Estimates the parameters of a Dirichlet distribution using Maximum Likelihood Estimation (MLE)
     * given the observed data and initial estimates of the parameters.
     *
     * @param data      The observed data counts.
     * @param alphaInit The initial estimates for the Dirichlet parameters.
     * @param N         The number of trials.
     * @return The estimated Dirichlet parameters.
     */
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
