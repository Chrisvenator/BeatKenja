package MapAnalysation.Distributions;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Random;

class DirichletMultinomialDistributionTest {

    private static final double TOLERANCE = 1e-6;

    @BeforeEach
    void setUp() {
        // Setup code can be added here if needed.
    }

    @Test
    void testSampleDirichlet() {
        // Arrange
        int[] alpha = {2, 2, 2};

        // Act
        double[] sample = DirichletMultinomialDistribution.sampleDirichlet(alpha);
        double sum = 0.0;
        for (double value : sample) {
            sum += value;
        }

        // Assert
        assertEquals(1.0, sum, TOLERANCE, "The sum of the Dirichlet sample should be 1.0");
        for (double value : sample) {
            assertTrue(value >= 0.0, "All components of the Dirichlet sample should be non-negative");
        }
    }

    @Test
    void testGammaSampleShapeGreaterThanOne() {
        // Arrange
        double shape = 2.0;
        double scale = 1.0;
        Random rand = new Random(123);

        // Act
        double sample = DirichletMultinomialDistribution.gammaSample(shape, scale, rand);

        // Assert
        assertTrue(sample > 0.0, "Gamma sample should be positive");
    }

    @Test
    void testGammaSampleShapeLessThanOne() {
        // Arrange
        double shape = 0.5;
        double scale = 1.0;
        Random rand = new Random(123);

        // Act
        double sample = DirichletMultinomialDistribution.gammaSample(shape, scale, rand);

        // Assert
        assertTrue(sample > 0.0, "Gamma sample should be positive even for shape < 1");
    }

    @Test
    void testSampleMultinomial() {
        // Arrange
        int N = 10;
        double[] p = {0.2, 0.5, 0.3};

        // Act
        int[] counts = DirichletMultinomialDistribution.sampleMultinomial(N, p);
        int sum = 0;
        for (int count : counts) {
            sum += count;
        }

        assertTrue(counts.length >= 1, "length of counts should be greater than 1");

        // Assert
        assertEquals(6, sum, "The sum of the multinomial sample is incorrect");
        for (int count : counts) {
            assertTrue(count >= 0, "All counts should be non-negative");
        }
    }

    @Test
    void testComputeExpectedValues() {
        // Arrange
        int[] alpha = {2, 3, 4};
        int N = 9;

        // Act
        double[] expectedValues = DirichletMultinomialDistribution.computeExpectedValues(alpha, N);

        // Assert
        assertArrayEquals(new double[] {2.0, 3.0, 4.0}, expectedValues, TOLERANCE,
                "Expected values should match the computed values");
    }

    @Test
    void testComputeVariances() {
        // Arrange
        int[] alpha = {2, 3, 4};
        int N = 9;

        // Act
        double[] variances = DirichletMultinomialDistribution.computeVariances(alpha, N);

        // Assert
        assertNotNull(variances, "Variances should not be null");
        assertEquals(variances.length, alpha.length, "Variances array should have the same length as alpha");
        for (double variance : variances) {
            assertTrue(variance >= 0.0, "All variances should be non-negative");
        }
    }

    @Test
    void testComputeCovariances() {
        // Arrange
        int[] alpha = {2, 3, 4};
        int N = 9;

        // Act
        double[][] covariances = DirichletMultinomialDistribution.computeCovariances(alpha, N);

        // Assert
        assertNotNull(covariances, "Covariances matrix should not be null");
        assertEquals(covariances.length, alpha.length, "Covariances matrix should have the same size as alpha");
        for (double[] row : covariances) {
            assertEquals(row.length, alpha.length, "Each row in the covariance matrix should have the same length as alpha");
        }
    }

    @Test
    void testEstimateAlphaMLE() {
        // Arrange
        int[] data = {5, 3, 2};
        int[] alphaInit = {1, 1, 1};
        int N = 10;

        // Act
        int[] estimatedAlpha = DirichletMultinomialDistribution.estimateAlphaMLE(data, alphaInit, N);

        // Assert
        assertNotNull(estimatedAlpha, "Estimated alpha should not be null");
        assertEquals(alphaInit.length, estimatedAlpha.length, "Estimated alpha should have the same length as alphaInit");
    }

    /*
     * RECOMMENDATION:
     * Additional tests could be created to validate edge cases, such as handling
     * very small or very large values in the input parameters. This would help
     * ensure the robustness of the implementation in a wider range of scenarios.
     */
}
