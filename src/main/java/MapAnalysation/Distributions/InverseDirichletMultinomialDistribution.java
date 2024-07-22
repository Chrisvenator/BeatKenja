package MapAnalysation.Distributions;
import org.apache.commons.math3.special.Gamma;

import java.util.Arrays;

public class InverseDirichletMultinomialDistribution {
    public static double[] estimateDirichletParameters(int[] sample, int N) {
        int n = sample.length;
        double[] logSample = new double[n];
        double sumLogSample = 0.0;
        for (int i = 0; i < n; i++) {
            logSample[i] = Math.log(sample[i]);
            sumLogSample += logSample[i];
        }
        double averageLogSample = sumLogSample / n;

        // Newton-Raphson method to estimate the parameters
        double[] alpha = new double[N];
        Arrays.fill(alpha, 1.0);  // Initialize with ones
        boolean converged = false;
        double tolerance = 1e-5;
        int maxIterations = 1000;
        int iteration = 0;

        while (!converged && iteration < maxIterations) {
            double sumAlpha = 0.0;
            double sumPsiAlpha = 0.0;
            double sumPsiAlphaDerivative = 0.0;
            for (int i = 0; i < N; i++) {
                sumAlpha += alpha[i];
                sumPsiAlpha += Gamma.digamma(alpha[i]);
                sumPsiAlphaDerivative += Gamma.trigamma(alpha[i]);
            }
            double commonTerm = Gamma.digamma(sumAlpha) - averageLogSample;

            converged = true;
            for (int i = 0; i < N; i++) {
                double delta = commonTerm + (sumPsiAlpha / N) - Gamma.digamma(alpha[i]);
                double step = delta / ((sumPsiAlphaDerivative / N) - Gamma.trigamma(alpha[i]));
                alpha[i] -= step;
                if (Math.abs(step) > tolerance) {
                    converged = false;
                }
            }
            iteration++;
        }

        if (iteration == maxIterations) {
            System.out.println("Warning: Maximum iterations reached without convergence");
        }

        return alpha;
    }

    private static double digamma(double x) {
        double result = 0;
        while (x < 7) {
            result -= 1 / x;
            x += 1;
        }
        x -= 1 / 2;
        double xx = 1 / x;
        double xx2 = xx * xx;
        double xx4 = xx2 * xx2;
        result += Math.log(x) + (1 / 24.0) * xx2 - (7 / 960.0) * xx4 + (31 / 8064.0) * xx4 * xx2;
        return result;
    }

    private static double trigamma(double x) {
        double result = 0;
        while (x < 7) {
            result += 1 / (x * x);
            x += 1;
        }
        x -= (double) 1 / 2;
        double xx = 1 / x;
        double xx2 = xx * xx;
        double xx4 = xx2 * xx2;
        result += 1 / x + (1 / 24.0) * xx2 - (1 / 40.0) * xx4;
        return result;
    }

    public static int[] estimateMultinomialProbabilities(int N, double[] counts) {
        int[] probabilities = new int[counts.length];
        for (int i = 0; i < counts.length; i++) {
            probabilities[i] = (int) Math.round(counts[i] / N);
        }
        return probabilities;
    }


}
