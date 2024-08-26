package DataManager;

import DataManager.Config.Configuration;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ConfigLoaderTest {

    private ObjectMapper mockMapper;
    private Configuration mockConfiguration;
    private static final String DUMMY_CONFIG_PATH = "dummyConfig.json";

    @BeforeEach
    void setUp() {
        mockMapper = mock(ObjectMapper.class);
        mockConfiguration = mock(Configuration.class);
    }

    @Test
    void testConfigLoaderLoadsValidConfig() throws IOException {
        // Arrange
        when(mockMapper.readValue(new File(DUMMY_CONFIG_PATH), Configuration.class)).thenReturn(mockConfiguration);

        // Act
        ConfigLoader configLoader = new ConfigLoader(DUMMY_CONFIG_PATH);
        Configuration config = configLoader.getConfig();

        // Assert
        assertNotNull(config, "The configuration should be loaded successfully.");
    }

    @Test
    void testConfigLoaderFallsBackToDefaultOnInvalidFile() throws IOException {
        // Arrange
        when(mockMapper.readValue(new File(DUMMY_CONFIG_PATH), Configuration.class)).thenThrow(new IOException("File not found"));

        // Act
        ConfigLoader configLoader = new ConfigLoader(DUMMY_CONFIG_PATH);
        Configuration config = configLoader.getConfig();

        // Assert
        assertNotNull(config, "The configuration should fall back to default.");
        assertTrue(config instanceof Configuration, "The configuration should be an instance of the default Configuration class.");
    }


    /*
     * RECOMMENDATION:
     * Consider adding validation logic in the ConfigLoader to ensure that
     * the configuration being loaded is complete and does not contain invalid values.
     * Additionally, you may want to provide more informative logging or error messages
     * when falling back to default configurations.
     */
}
