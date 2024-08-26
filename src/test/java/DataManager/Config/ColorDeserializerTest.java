package DataManager.Config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.awt.*;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class ColorDeserializerTest {

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        ColorDeserializer colorDeserializer = new ColorDeserializer();
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new com.fasterxml.jackson.databind.module.SimpleModule()
                .addDeserializer(Color.class, colorDeserializer));  // Register the custom deserializer
    }

    @Test
    void testDeserializeInvalidHexColor() {
        // Arrange
        String json = "\"#ZZZZZZ\"";  // Invalid hex string

        // Act & Assert
        assertThrows(NumberFormatException.class, () -> objectMapper.readValue(json, Color.class),
                "Invalid hex color should throw a NumberFormatException");
    }

    @Test
    void testDeserializeNullColor() throws IOException {
        // Arrange
        String json = "null";  // JSON null value

        // Act
        Color result = objectMapper.readValue(json, Color.class);

        // Assert
        assertNull(result, "Deserializing a null value should return null");
    }

    @Test
    void testDeserializeEmptyString() {
        // Arrange
        String json = "\"\"";  // Empty string

        // Act & Assert
        assertThrows(NumberFormatException.class, () -> objectMapper.readValue(json, Color.class),
                "Empty string should throw a NumberFormatException");
    }

    @Test
    void testDeserializeWithoutHashSymbol() throws IOException {
        // Arrange
        String json = "\"FFAABB\"";

        // Act
        Color result = objectMapper.readValue(json, Color.class);

        // Assert
        assertNotNull(result, "The result should not be null");
        assertEquals(new Color(0xFFAABB), result, "The color should be correctly deserialized from the hex string");
    }

    @Test
    void testDeserializeMalformedJson() {
        // Arrange
        String json = "\"#FFAABBG\"";  // Incorrect length and invalid characters

        // Act & Assert
        assertThrows(NumberFormatException.class, () -> objectMapper.readValue(json, Color.class),
                "Malformed JSON input should throw a JsonMappingException");
    }

    @Test
    void testDeserializeValidHexColor() throws IOException {
        // Arrange
        String json = "\"#FFAABB\"";

        // Act
        Color result = objectMapper.readValue(json, Color.class);

        // Assert
        assertNotNull(result, "The result should not be null");
        assertEquals(new Color(0xFFAABB), result, "The color should be correctly deserialized from the hex string");
    }

    @Test
    void testDeserializeBlackColor() throws IOException {
        // Arrange
        String json = "\"#000000\"";

        // Act
        Color result = objectMapper.readValue(json, Color.class);

        // Assert
        assertNotNull(result, "The result should not be null");
        assertEquals(Color.BLACK, result, "The color should be correctly deserialized as black");
    }

    @Test
    void testDeserializeWhiteColor() throws IOException {
        // Arrange
        String json = "\"#FFFFFF\"";

        // Act
        Color result = objectMapper.readValue(json, Color.class);

        // Assert
        assertNotNull(result, "The result should not be null");
        assertEquals(Color.WHITE, result, "The color should be correctly deserialized as white");
    }

    @Test
    void testDeserializeGrayColor() throws IOException {
        // Arrange
        String json = "\"#808080\"";

        // Act
        Color result = objectMapper.readValue(json, Color.class);

        // Assert
        assertNotNull(result, "The result should not be null");
        assertEquals(new Color(0x808080), result, "The color should be correctly deserialized as gray");
    }

    @Test
    void testDeserializeRedColor() throws IOException {
        // Arrange
        String json = "\"#FF0000\"";

        // Act
        Color result = objectMapper.readValue(json, Color.class);

        // Assert
        assertNotNull(result, "The result should not be null");
        assertEquals(Color.RED, result, "The color should be correctly deserialized as red");
    }

    /*
     * RECOMMENDATION:
     * Consider handling cases where the input JSON is not a valid hex color string
     * or lacks the expected '#' symbol. The current implementation implicitly assumes
     * that the input is always valid, which might not be the case in real-world scenarios.
     * You could add validation or more explicit error handling to improve robustness.
     */
}
