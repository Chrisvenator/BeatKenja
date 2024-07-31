package DataManager;

import DataManager.Records.Configuration;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;

import java.io.File;
import java.io.IOException;

@Getter
public class ConfigLoader {
    private Configuration config;

    public ConfigLoader(String configFilePath) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            mapper.configure(JsonGenerator.Feature.AUTO_CLOSE_TARGET, false);
            this.config = mapper.readValue(new File(configFilePath), Configuration.class);
            System.out.println(mapper.writeValueAsString(this.config));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static void main(String[] args) {
        ConfigLoader loader = new ConfigLoader("./config.json");
        Configuration config = loader.getConfig();
        System.out.println("Database URL: " + config.database.settings.hibernate.connection.url);
        System.out.println("Dark-Mode: " + config.global.darkMode);
    }
}
