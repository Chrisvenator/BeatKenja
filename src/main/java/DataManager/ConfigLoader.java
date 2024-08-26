package DataManager;

import DataManager.Config.Configuration;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;

import java.io.File;
import java.io.IOException;

import static DataManager.Parameters.logger;

/**
 * A utility class responsible for loading and managing the application's configuration settings.
 * The configuration is loaded from a JSON file, with fallback to default settings if the file is not found or cannot be read.
 * This class also provides functionality to export the current configuration back to a JSON string.
 */
@Getter
public class ConfigLoader {
    /** The configuration object that holds the application's settings. This is either loaded from a specified JSON file or initialized with default settings.*/
    private Configuration config = getDefaultConfiguration();

    /**
     * Constructs a `ConfigLoader` that attempts to load the configuration from the specified file path.
     * If the file is not found or cannot be read, the configuration falls back to default settings.
     *
     * @param configFilePath The path to the JSON configuration file.
     */
    public ConfigLoader(String configFilePath) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            mapper.configure(DeserializationFeature.FAIL_ON_TRAILING_TOKENS, false);
            mapper.configure(JsonGenerator.Feature.AUTO_CLOSE_TARGET, false);
            this.config = mapper.readValue(new File(configFilePath), Configuration.class);
            logger.info("Config: {}", mapper.writeValueAsString(this.config));
        } catch (IOException e) {
            logger.warn("Could not find config. Falling back to default...");
        }
    }

    /**
     * Initializes and returns the default configuration settings.
     * This method is used to provide a fallback configuration in case the specified config file cannot be loaded.
     *
     * @return A `Configuration` object with default settings.
     */
    private Configuration getDefaultConfiguration() {
        // Initialize the default configuration here
        return new Configuration();
    }

    /**
     * Exports the current configuration to a JSON string.
     * The configuration is serialized with pretty-print formatting.
     *
     * @return A JSON string representing the current configuration.
     * @throws JsonProcessingException If there is an error during serialization.
     */
    public String exportConfig() throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(this.config);
    }

    public void setConfig(Configuration mockConfiguration) {
        this.config = mockConfiguration;
    }
}
