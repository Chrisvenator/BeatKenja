package DataManager.Objects;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TripleTest {

    @Test
    void testTripleCreation() {
        // Arrange
        String left = "Left";
        Integer middle = 123;
        Double right = 456.78;

        // Act
        Triple<String, Integer, Double> triple = new Triple<>(left, middle, right);

        // Assert
        assertEquals(left, triple.getLeft(), "The left element should be 'Left'");
        assertEquals(middle, triple.getMiddle(), "The middle element should be 123");
        assertEquals(right, triple.getRight(), "The right element should be 456.78");
    }

    @Test
    void testTripleWithNullValues() {
        // Act
        Triple<String, Integer, Double> triple = new Triple<>(null, null, null);

        // Assert
        assertNull(triple.getLeft(), "The left element should be null");
        assertNull(triple.getMiddle(), "The middle element should be null");
        assertNull(triple.getRight(), "The right element should be null");
    }

    @Test
    void testTripleEquality() {
        // Arrange
        Triple<String, Integer, Double> triple1 = new Triple<>("Left", 123, 456.78);
        Triple<String, Integer, Double> triple2 = new Triple<>("Left", 123, 456.78);
        Triple<String, Integer, Double> triple3 = new Triple<>("Right", 456, 789.01);

        // Act & Assert
        assertEquals(triple1, triple2, "Two triples with the same values should be equal");
        assertNotEquals(triple1, triple3, "Two triples with different values should not be equal");
    }

    @Test
    void testTripleHashCode() {
        // Arrange
        Triple<String, Integer, Double> triple1 = new Triple<>("Left", 123, 456.78);
        Triple<String, Integer, Double> triple2 = new Triple<>("Left", 123, 456.78);
        Triple<String, Integer, Double> triple3 = new Triple<>("Right", 456, 789.01);

        // Act & Assert
        assertEquals(triple1.hashCode(), triple2.hashCode(), "Two equal triples should have the same hash code");
        assertNotEquals(triple1.hashCode(), triple3.hashCode(), "Two different triples should have different hash codes");
    }

    @Test
    void testTripleToString() {
        // Arrange
        Triple<String, Integer, Double> triple = new Triple<>("Left", 123, 456.78);

        // Act
        String result = triple.toString();

        // Assert
        assertEquals("Triple(left=Left, middle=123, right=456.78)", result, "The toString method should return the correct string representation");
    }

    @Test
    void testTripleWithDifferentTypes() {
        // Arrange
        Triple<Integer, String, Boolean> triple = new Triple<>(1, "Middle", true);

        // Act & Assert
        assertEquals(1, triple.getLeft(), "The left element should be 1");
        assertEquals("Middle", triple.getMiddle(), "The middle element should be 'Middle'");
        assertEquals(true, triple.getRight(), "The right element should be true");
    }
}
