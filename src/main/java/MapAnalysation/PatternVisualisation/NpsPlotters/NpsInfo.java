package MapAnalysation.PatternVisualisation.NpsPlotters;

/**
 * Record for storing info about the nps
 * @param nps nps
 * @param fromTime when the section begins
 * @param toTime Time when the section stops
 */
public record NpsInfo(float nps, float fromTime, float toTime) {}
