package MapGeneration.PatternGeneration.CommonMethods;

import BeatSaberObjects.Objects.Note;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import BeatSaberObjects.Objects.RedTimingNote;
import BeatSaberObjects.Objects.TimingNote;
import DataManager.Parameters;
import MapAnalysation.PatternVisualisation.CombinedPlotter;
import MapAnalysation.PatternVisualisation.NpsPlotters.AverageNpsPlotter;
import MapAnalysation.PatternVisualisation.NpsPlotters.DynamicNpsPlotter;
import MapAnalysation.PatternVisualisation.NpsPlotters.NpsCutOffPlotter;
import UserInterface.UserInterface;

import static DataManager.Parameters.BPM;
import static DataManager.Parameters.FIX_INCONSISTENT_TIMINGS_FASTER_THAN_NPS_THRESHOLD;

public class FixSwingTimings {
    public static List<Note> fixSwingAlternating(List<Note> notesImmutable, UserInterface ui) {
        List<Note> notes = new ArrayList<>(notesImmutable);


        if (BPM == -1) return notes;
        if (notes.size() <= 2) return notes;
        NpsBpmConverter.convertBeatsToSeconds(notes);
        final double minDistanceBetweenNotes = 1 / Parameters.FIX_INCONSISTENT_TIMINGS_FASTER_THAN_NPS_THRESHOLD;

        List<Map<Integer, Note>> toFix = new ArrayList<>();


        Note previousNote = notes.get(0);
        int index = 0;
        for (int i = 1; i < notes.size(); i++) {
            Note note = notes.get(i);

            if (note._time - previousNote._time <= minDistanceBetweenNotes) {
                if (toFix.isEmpty() || index != toFix.size() - 1 || toFix.get(index) == null) toFix.add(new HashMap<>());

                if (note._time == previousNote._time && note._type != previousNote._type) {toFix.get(index).put(i, new RedTimingNote(note._time));}
                if (note._time == previousNote._time) { previousNote.amountOfStackedNotes++; continue; }

                toFix.get(index).put(i, new TimingNote(note._time));
            } else {
                try {
                    toFix.get(index);
                    index++;
                } catch (IndexOutOfBoundsException ignored) {
//                    index++;
                }
            }
            previousNote = note;
        }

//        List<NpsInfo> info = DynamicNpsPlotter.computeNps(notes, 1,4);

        int amountForFixingThreshold = 4;

        for (Map<Integer, Note> fix : toFix) {
            if (fix.size() <= amountForFixingThreshold) continue;

            int modOffset = 0;
            List<Integer> keys = new ArrayList<>(fix.keySet());
            for (int i = 0; i < keys.size(); i++) {
                if (notes.get(keys.get(i)) instanceof RedTimingNote) modOffset++;
                notes.get(keys.get(i))._type = i % 2 + modOffset == 0 ? 1 : 0;
            }
        }


        //TODO: Hier weitermachen


        // List<note> notes (currently in seconds) must be converted back to beats!
        NpsBpmConverter.convertSecondsToBeats(notes);
        return notes;
    }

    private static Note getPrev(List<Note> notes, int color, int index){
        if (notes == null || notes.isEmpty()) return null;

        for (int i = index - 1; i >= 0; i--)
            if (notes.get(i)._type == color) return notes.get(i);


        return null;
    }

    private static List<Note> fix(List<Note> notes) {
        //TBD
        return notes;
    }




    public static void plotAsGraphs(String name, List<Note> notes){

        CombinedPlotter combinedPlotter = new CombinedPlotter("NPS of: " + name,
//              new NoteDistancePlotter(name + ": NoteDistancePlotter", notes).getSeries(),
//                new DynamicNpsPlotter(name + ": NpsPlotter 0.25", notes, 0.25f, 4).getSeries(),
                new DynamicNpsPlotter(": 1 Second Intervals | ", notes, 1f, 4).getSeries(),
                new DynamicNpsPlotter(": 2 Seconds Intervals | ", notes, 2f, 4).getSeries(),
                new AverageNpsPlotter(": Average", notes).getSeries(),
                new NpsCutOffPlotter(": Cut Off", FIX_INCONSISTENT_TIMINGS_FASTER_THAN_NPS_THRESHOLD, 0, notes.get(notes.size() - 1)._time).getSeries()
//                new DynamicNpsPlotter(name + ": NpsPlotter 3", notes, 3f, 4).getSeries(),
//                 new StaticNpsPlotter(name + ": StaticNpsPlotter", notes, 2).getSeries()
                );

        combinedPlotter.visualize();
    }
}