package MapGeneration.StyleSpace;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Loads the baked {@link StyleArchetype} list from the jar's resources at startup.
 * If the resource is absent (not yet trained), returns an empty list so generation
 * silently falls back to the 1st-order baseline.
 */
public class StyleSpaceLoader {

    private static final Logger logger = LogManager.getLogger(StyleSpaceLoader.class);
    private static final String RESOURCE_PATH = "/style_archetypes.ser";

    @SuppressWarnings("unchecked")
    public static StyleSpace load() {
        try (InputStream is = StyleSpaceLoader.class.getResourceAsStream(RESOURCE_PATH)) {
            if (is == null) {
                logger.info("style_archetypes.ser not found in resources — style space disabled");
                return new StyleSpace(new ArrayList<>());
            }
            try (ObjectInputStream ois = new ObjectInputStream(is)) {
                List<StyleArchetype> archetypes = (List<StyleArchetype>) ois.readObject();
                logger.info("Loaded {} style archetypes", archetypes.size());
                return new StyleSpace(archetypes);
            }
        } catch (IOException | ClassNotFoundException e) {
            logger.warn("Failed to load style archetypes: {} — style space disabled", e.getMessage());
            return new StyleSpace(new ArrayList<>());
        }
    }
}
