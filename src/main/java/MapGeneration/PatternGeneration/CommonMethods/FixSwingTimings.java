package MapGeneration.PatternGeneration.CommonMethods;

import BeatSaberObjects.Objects.Note;

import java.util.ArrayList;
import java.util.List;

import MapAnalysation.PatternVisualisation.CombinedPlotter;
import MapAnalysation.PatternVisualisation.NpsPlotters.AverageNpsPlotter;
import MapAnalysation.PatternVisualisation.NpsPlotters.DynamicNpsPlotter;

import static DataManager.Parameters.BPM;

public class FixSwingTimings {
    public static List<Note> fixSwingTiming(List<Note> notes) {
        if (BPM == -1) return notes;
        NpsBpmConverter.convertBeatsToSeconds(notes);
        List<Note> newNotes = new ArrayList<>();


        //TODO: Hier weitermachen


        // List<note> notes (currently in seconds) must be converted back to beats!
        NpsBpmConverter.convertSecondsToBeats(notes);
        return newNotes;
    }




    public static void plotAsGraphs(String name, List<Note> notes){
        CombinedPlotter combinedPlotter = new CombinedPlotter("Combined plotter",
//              new NoteDistancePlotter(name + ": NoteDistancePlotter", notes).getSeries(),
//                new DynamicNpsPlotter(name + ": NpsPlotter 0.25", notes, 0.25f, 4).getSeries(),
                new DynamicNpsPlotter(name + ": NpsPlotter 1", notes, 1f, 4).getSeries(),
                new DynamicNpsPlotter(name + ": NpsPlotter 2", notes, 2f, 4).getSeries(),
                new AverageNpsPlotter(name + ": AverageNpsPlotter 1", notes).getSeries()
//                new DynamicNpsPlotter(name + ": NpsPlotter 3", notes, 3f, 4).getSeries(),
//                 new StaticNpsPlotter(name + ": StaticNpsPlotter", notes, 2).getSeries()
                );

        combinedPlotter.visualize();
    }
}