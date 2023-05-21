import com.google.gson.Gson;

public class Main {

        /*
    Red: 0
    Blue: 1

    Layer - Index:          Cut direction:
    |---|---|---|---|       |---|---|---|
    |   |   |   |3-2|       | 4 | 0 | 5 |
    |---|---|---|---|       |---|---|---|
    |   |   |   |3-1|       | 2 | 8 | 3 |
    |---|---|---|---|       |---|---|---|
    |0-0|1-0|2-0|3-0|       | 6 | 1 | 7 |
    |---|---|---|---|       |---|---|---|
     */

    public static void main(String[] args) {
        //Settings:
        String inputPath = "Input.txt";
        String outputPath = "./output/";
        float bpm = 165;
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
            map.toBlueLeftBottomRowDotTimings();
            CreateTimings.overwriteFile(outputPath + "linearTimings.txt", map.exportAsMap());


            //If you want timings and stacks, etc. , then use "mapToTimingNotesArray" or "mapToTimingNotesList".
            //The only difference is the format which is returned.
            map.toTimingNotes();
            CreateTimings.overwriteFile(outputPath + "TestOutput.txt", map.exportAsMap());

            //Creating a linear map from timings:
            Note[] linearPattern = CreatePatterns.linearSlowPattern(map._notes, null, null);
            CreateTimings.overwriteFile(outputPath + "linearMap.txt", new BeatSaberMap(linearPattern, map.originalJSON).exportAsMap());


            //removing all notes with the red color:
            map.makeOneHanded(0);
            map.makeOneHanded(2); //same with bombs

            //converting flash-lights to on-lights. This may be useful for converting an auto-lighted map
            map.convertAllFlashLightsToOnLights();
        }
    }
}