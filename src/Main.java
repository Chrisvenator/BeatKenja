import com.google.gson.Gson;

public class Main {
    public static void main(String[] args) {
        //Settings:
        String inputPath = "Input.txt";
        String outputPath = "./output/";
        float bpm = 120;
        double placementPrecision = (double) 1 / 32;

        //creating a BeatSaberMap Object from the input String
        BeatSaberMap map = new Gson().fromJson(CreateTimings.readFile(inputPath).get(0), BeatSaberMap.class);

        map.convertAllFlashLightsToOnLights();
        CreateTimings.overwriteFile(outputPath + "NoFlashingLights.txt", map.exportAsMap());


        //Not executing things that are not needed at the moment
        if (1 == 2) {

            //creating a Map out of the timings file:
            //Needed are: bpm, timings filename, placementPrecision
            String timingsFromSong = CreateTimings.makeMap(bpm, "timings.txt", placementPrecision);
            CreateTimings.overwriteFile(outputPath + "timingsFromSong.txt", timingsFromSong);


            //fixing timings of a map, so that is not flagged by ScoreSaber as "unsure"
            //Needed is only the placementPrecision
            map.fixPlacements(placementPrecision);
            CreateTimings.overwriteFile(outputPath + "fixedTimings.txt", map.exportAsMap());


            //Converting an already existing map into a map with timings.
            //You would most likely want to remove all lighting and Maybe all obstacles:
            if (1 == 1) map._events = new Events[0];
            if (1 == 2) map._obstacles = new Obstacle[0];

            //If you ONLY want timings and no stacks, etc. , then use "toLinearTimings".
            Note[] linearTimingsNotesFromMap = CreatePatterns.toLinearTimings(map._notes);
            CreateTimings.overwriteFile(outputPath + "linearTimings.txt", new BeatSaberMap(linearTimingsNotesFromMap, map.originalJSON).exportAsMap());
        }


        if (1 == 2) {
            //If you want timings and stacks, etc. , then use "mapToTimingNotesArray" or "mapToTimingNotesList".
            //The only difference is the format which is returned.
            Note[] timingsFromMap = CreatePatterns.mapToTimingNotesArray(map._notes);
            CreateTimings.overwriteFile(outputPath + "allTimings.txt", new BeatSaberMap(timingsFromMap, map.originalJSON).exportAsMap());

            //Creating a linear map from timings:
            Note[] linearPattern = CreatePatterns.linearSlowPattern(map._notes);
            CreateTimings.overwriteFile(outputPath + "linearMap.txt", new BeatSaberMap(linearPattern, map.originalJSON).exportAsMap());
        }
    }

}
