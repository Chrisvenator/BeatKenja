package DataManager.Database.DatabaseOperations;

import DataManager.Parameters;
import org.junit.jupiter.api.Test;

class PatternDescriptionEntityOperationsTest {

    @Test
    void getPatternDescriptionMetadata() {
        PatternDescriptionEntityOperations.getPatternDescription(Parameters.DEFAULT_PATTERN_METADATA);
    }

    @Test
    void getPatternDescriptionValues() {
        PatternDescriptionEntityOperations.getPatternDescription(Parameters.DEFAULT_PATTERN_METADATA.name(), Parameters.DEFAULT_PATTERN_METADATA.bpm(), Parameters.DEFAULT_PATTERN_METADATA.nps(), Parameters.DEFAULT_PATTERN_METADATA.difficulty(), Parameters.DEFAULT_PATTERN_METADATA.genre(), Parameters.DEFAULT_PATTERN_METADATA.tags());
    }
}