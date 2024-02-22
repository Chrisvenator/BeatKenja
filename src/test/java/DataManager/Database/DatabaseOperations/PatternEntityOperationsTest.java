package DataManager.Database.DatabaseOperations;

import DataManager.Database.DatabaseEntities.PatternDescriptionEntity;
import DataManager.Parameters;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PatternEntityOperationsTest {

    @Test
    void getPatternByDescription() {
        PatternDescriptionEntity desc = PatternDescriptionEntityOperations.getPatternDescription(Parameters.DEFAULT_PATTERN_METADATA);
        assertNotNull(PatternEntityOperations.getPatternByDescription(desc));
    }

    @Test
    void getPatternById() {
        PatternDescriptionEntity desc = PatternDescriptionEntityOperations.getPatternDescription(Parameters.DEFAULT_PATTERN_METADATA);
        assertNotNull(PatternEntityOperations.getPatternById(1));
    }
}