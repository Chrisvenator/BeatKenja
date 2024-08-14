package MapAnalysation.PatternVisualisation.NpsPlotters;

/**
 * Record for storing info about the nps
 * @param nps nps
 * @param fromTime when the nps begin
 * @param toTime Time when the nps stop
 */
public record NpsInfo(float nps, float fromTime, float toTime) {}
