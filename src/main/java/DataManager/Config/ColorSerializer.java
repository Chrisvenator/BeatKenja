package DataManager.Config;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.awt.*;
import java.io.IOException;

public class ColorSerializer extends StdSerializer<Color>{

    // Füge einen Standardkonstruktor hinzu, der den Typ an den Superkonstruktor übergibt
    public ColorSerializer() {
        super(Color.class);
    }

    @Override
    public void serialize(Color value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        // Konvertiere die Color-Instanz in einen Hex-String
        String hex = String.format("#%02X%02X%02X", value.getRed(), value.getGreen(), value.getBlue());
        gen.writeString(hex);
    }
}
