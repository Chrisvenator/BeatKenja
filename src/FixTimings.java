import java.util.Arrays;

import com.google.gson.Gson;

public class FixTimings {

    //This function fixes the note placements.
    //Reason: ScoreSaber flags a map as unsure, when the placement is not divisible by 64.
    //Note that this function is outdated...
    public static String fixTimings(double placementPrecision, BeatSaberMap map) {
        map.fixPlacements(placementPrecision);
        return map.exportAsMap();
    }
}