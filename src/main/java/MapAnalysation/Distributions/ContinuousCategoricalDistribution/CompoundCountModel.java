package MapAnalysation.Distributions.ContinuousCategoricalDistribution;

import DataManager.Parameters;

public class CompoundCountModel {
    
    // Method to compute p(x; lambda) which is proportional to product of lambda_i^x_i
    private static double computeProbability(int[] x, double[] lambda) {
        if (x.length != lambda.length) {
            throw new IllegalArgumentException("Arrays x and lambda must have the same length.");
        }
        
        double product = 1.0;
        for (int i = 0; i < x.length; i++) {
            product *= Math.pow(lambda[i], x[i]);
        }
        return product;  // This is proportional to the probability
    }
    
    // Method to compute the normalizing constant C(eta)
    private static double computeNormalizingConstant(double[] eta, double s) {
        double product = 1.0;
        for (double eta_i : eta) {
            product *= 1.0 / (s - eta_i);
        }
        return product;
    }
    
    public static double[] generateRandomLambda(int size, double min, double max) {
        double[] lambda = new double[size];
        
        for (int i = 0; i < size; i++) {
//            lambda[i] = min + (max - min) * Parameters.RANDOM.nextDouble();
//            System.out.println(lambda[i]);
            lambda[i] = 1;
        }
        
        return lambda;
    }
    
    public static double[][] normalizeAsContinuousCategoricalDistribution(int[][] count, double[] lambda, double[] eta, double s, float amplificationFactor) {
        double[][] normalized = new double[count.length][count[0].length];
        
        for (int i = 0; i < count.length; i++) {
            int[] x = count[i];
            
            // Step 1: Compute the proportional probability
            double probability = computeProbability(x, lambda);
            
            // Step 2: Compute the normalizing constant
            double normalizingConstant = computeNormalizingConstant(eta, s);
            
            // Step 3: Normalize each element in the int[] based on probability and normalizing constant
            double[] normalizedArray = new double[x.length];
            for (int j = 0; j < x.length; j++) {
                normalizedArray[j] = x[j] * probability / normalizingConstant * amplificationFactor;
            }
            
            normalized[i] = normalizedArray;
        }
        
        return normalized;
    }
    
  
    // Example usage
    public static void main(String[] args) {
        int[] x = {2, 3, 1};  // Example x values
        double[] lambda = {0.5, 1.5, 2.0};  // Example lambda values
        double[] eta = {0.3, 0.7, 1.2};  // Example eta values
        double s = 2.0;  // Example constant s
        
        // Compute proportional probability
        double probability = computeProbability(x, lambda);
        System.out.println("Computed Probability (Proportional): " + probability);
        
        // Compute normalizing constant
        double normalizingConstant = computeNormalizingConstant(eta, s);
        System.out.println("Normalizing Constant: " + normalizingConstant);
    }
}
