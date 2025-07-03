package DataManager.Config;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.awt.*;
import java.io.IOException;
import java.io.StringWriter;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ColorSerializerTest {

    private ColorSerializer colorSerializer;
    private JsonGenerator jsonGenerator;
    private SerializerProvider serializerProvider;
    private StringWriter writer;

    @BeforeEach
    void setUp() {
        colorSerializer = new ColorSerializer();
        jsonGenerator = mock(JsonGenerator.class);
        serializerProvider = mock(SerializerProvider.class);
        writer = new StringWriter();
    }

    @Test
    void testSerializeNullColor() {
        // Arrange
        Color color = null;

        // Act & Assert
        assertThrows(NullPointerException.class, () -> colorSerializer.serialize(color, jsonGenerator, serializerProvider),
                "Serializing a null Color should throw a NullPointerException");
    }

    @Test
    void testSerializeTransparentColor() throws IOException {
        // Arrange
        Color color = new Color(0, 0, 0, 0);  // Fully transparent black
        when(jsonGenerator.getOutputTarget()).thenReturn(new StringWriter());

        // Act
        colorSerializer.serialize(color, jsonGenerator, serializerProvider);

        // Assert
        verify(jsonGenerator).writeString("#000000");
    }

    @Test
    void testSerializeOutOfBoundsColor() throws IOException {
        // Arrange
        Color color = new Color(255, 255, 255, 255);  // Fully opaque white with alpha channel
        when(jsonGenerator.getOutputTarget()).thenReturn(new StringWriter());

        // Act
        colorSerializer.serialize(color, jsonGenerator, serializerProvider);

        // Assert
        verify(jsonGenerator).writeString("#FFFFFF");
    }

    @Test
    void testSerializeAlphaColorIgnored() throws IOException {
        // Arrange
        Color color = new Color(50, 100, 150, 200);  // Color with alpha, which should be ignored
        when(jsonGenerator.getOutputTarget()).thenReturn(new StringWriter());

        // Act
        colorSerializer.serialize(color, jsonGenerator, serializerProvider);

        // Assert
        verify(jsonGenerator).writeString("#326496");
    }

    @Test
    void testSerializeBlackColor() throws IOException {
        // Arrange
        Color color = Color.BLACK;
        when(jsonGenerator.getOutputTarget()).thenReturn(writer);

        // Act
        colorSerializer.serialize(color, jsonGenerator, serializerProvider);

        // Assert
        verify(jsonGenerator).writeString("#000000");
    }

    @Test
    void testSerializeWhiteColor() throws IOException {
        // Arrange
        Color color = Color.WHITE;
        when(jsonGenerator.getOutputTarget()).thenReturn(writer);

        // Act
        colorSerializer.serialize(color, jsonGenerator, serializerProvider);

        // Assert
        verify(jsonGenerator).writeString("#FFFFFF");
    }

    @Test
    void testSerializeRedColor() throws IOException {
        // Arrange
        Color color = Color.RED;
        when(jsonGenerator.getOutputTarget()).thenReturn(writer);

        // Act
        colorSerializer.serialize(color, jsonGenerator, serializerProvider);

        // Assert
        verify(jsonGenerator).writeString("#FF0000");
    }

    @Test
    void testSerializeGreenColor() throws IOException {
        // Arrange
        Color color = Color.GREEN;
        when(jsonGenerator.getOutputTarget()).thenReturn(writer);

        // Act
        colorSerializer.serialize(color, jsonGenerator, serializerProvider);

        // Assert
        verify(jsonGenerator).writeString("#00FF00");
    }

    @Test
    void testSerializeBlueColor() throws IOException {
        // Arrange
        Color color = Color.BLUE;
        when(jsonGenerator.getOutputTarget()).thenReturn(writer);

        // Act
        colorSerializer.serialize(color, jsonGenerator, serializerProvider);

        // Assert
        verify(jsonGenerator).writeString("#0000FF");
    }

    /*
     * RECOMMENDATION:
     * Consider extending the serializer to handle edge cases like null values more gracefully.
     * Also, consider caching the hex string if the Color object remains unchanged
     * to improve serialization performance.
     */
}
