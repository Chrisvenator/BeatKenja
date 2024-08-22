package MapAnalysation.PatternVisualisation.NpsPlotters;

/**
 * A record that stores information about the Notes Per Second (NPS) for a specific time interval.
 * This record is used to encapsulate the NPS value along with the start and end times of the interval it represents.
 *
 * @param nps      The Notes Per Second (NPS) value for the specified time interval.
 * @param fromTime The start time (in seconds) of the interval for which the NPS is calculated.
 * @param toTime   The end time (in seconds) of the interval for which the NPS is calculated.
 */
public record NpsInfo(float nps, float fromTime, float toTime) {}
