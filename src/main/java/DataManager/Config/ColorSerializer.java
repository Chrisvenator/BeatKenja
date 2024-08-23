package DataManager.Config;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.awt.*;
import java.io.IOException;

/**
 * A custom serializer for the `Color` class, used to convert a `Color` object into a hex string representation when serializing to JSON.
 * This serializer is designed to work with Jackson, a popular JSON processing library.
 * <p>
 * The resulting JSON will contain the color in the format `#RRGGBB`, where `RR`, `GG`, and `BB` are the hexadecimal values of the red, green, and blue components of the color, respectively.
 */
public class ColorSerializer extends StdSerializer<Color>{

    /**
     * Constructs a `ColorSerializer` by passing the `Color` class type to the superclass constructor.
     * This constructor is required by the Jackson framework for custom serializers.
     */
    public ColorSerializer() {
        super(Color.class);
    }

    /**
     * Serializes a `Color` object into its hex string representation.
     * The hex string is in the format `#RRGGBB`.
     *
     * @param value    The `Color` object to serialize.
     * @param gen      The `JsonGenerator` used to write the serialized JSON content.
     * @param provider The `SerializerProvider` that can be used to get serializers for serializing the object's properties.
     * @throws IOException If an I/O error occurs during serialization.
     */
    @Override
    public void serialize(Color value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        // Konvertiere die Color-Instanz in einen Hex-String
        String hex = String.format("#%02X%02X%02X", value.getRed(), value.getGreen(), value.getBlue());
        gen.writeString(hex);
    }
}
