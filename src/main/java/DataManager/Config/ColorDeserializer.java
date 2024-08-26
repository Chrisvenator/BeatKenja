package DataManager.Config;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;

import java.awt.*;
import java.io.IOException;
import java.util.Objects;

/**
 * A custom deserializer for the `Color` class, used to convert a hex string representation of a color from JSON into a `Color` object.
 * This deserializer is designed to work with Jackson, a popular JSON processing library.
 * <p>
 * The JSON input is expected to be in the format `#RRGGBB`, where `RR`, `GG`, and `BB` are the hexadecimal values of the red, green, and blue components of the color, respectively.
 */
public class ColorDeserializer extends JsonDeserializer<Color> {

    /**
     * Deserializes a hex string from JSON into a `Color` object.
     * The hex string should be in the format `#RRGGBB`.
     *
     * @param jsonParser               The `JsonParser` used to parse the JSON content.
     * @param deserializationContext   The `DeserializationContext` that can be used to access information for deserialization.
     * @return A `Color` object corresponding to the hex string in the JSON input.
     * @throws IOException If an I/O error occurs during deserialization.
     */
    @Override
    public Color deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
        String colorHex = jsonParser.getText();
        if (colorHex == null) return null;
        if (colorHex.isEmpty()) throw new NumberFormatException();
        if (colorHex.charAt(0) != '#') colorHex = "#" + colorHex;

        try {
            return Color.decode(colorHex);
        }catch (NumberFormatException e) {
            throw new NumberFormatException(e.getMessage());
        }
    }
}
