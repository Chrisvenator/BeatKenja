package MapGeneration.GenerationElements.Types;

import MapGeneration.GenerationElements.Pattern;

@Deprecated
public record ProbabilityPattern(String category, String genre, float nps_min, float nps_max, Pattern probability) {
}
