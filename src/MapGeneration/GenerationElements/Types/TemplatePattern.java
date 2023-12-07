package MapGeneration.GenerationElements.Types;

import MapGeneration.GenerationElements.Sequence;

import java.util.List;

public record TemplatePattern(String category, String genre, float nps_min, float nps_max, List<Sequence> sequences) {
}
