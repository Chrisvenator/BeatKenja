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

@Getter
public class ConfigLoader {
    private Configuration config = getDefaultConfiguration();

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

    private Configuration getDefaultConfiguration() {
        // Initialize the default configuration here
        return new Configuration();
    }

    public String exportConfig() throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(this.config);
    }
}
