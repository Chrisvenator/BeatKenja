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
import org.jetbrains.annotations.NotNull;

import static DataManager.Parameters.BPM;
import static DataManager.Parameters.FIX_INCONSISTENT_TIMINGS_FASTER_THAN_NPS_THRESHOLD;

public class FixSwingTimings {
    /**
     * The function fixSwingAlternating takes a List of notes (2-colored).
     * If a certain section passes a threshold set in Parameters.java, it makes the color alternating.
     * For example, if there is a 13 nps section in a map, then this function will take this section and change the color of the notes in the section to be alternating Red and Blue.
     * Doubles and stacks will not be taken into account when calculating the nps.
     * They will still be kept and NOT removed.
     *
     * @require 2-colored Notes
     * @param notesImmutable A list of notes that should be processed.
     * @return Returns a List of Notes that have been processed
     */
    public static List<Note> fixSwingAlternating(@NotNull List<Note> notesImmutable) {
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
                // Create entry, if it does not exist.
                if (toFix.isEmpty() || index != toFix.size() - 1 || toFix.get(index) == null) toFix.add(new HashMap<>());

                if (note._time == previousNote._time && note._type != previousNote._type) {toFix.get(index).put(i, new RedTimingNote(note._time));}
                if (note._time == previousNote._time) { previousNote.amountOfStackedNotes++; continue; }

                toFix.get(index).put(i, new TimingNote(note._time));
            } else {
                try {
                    // This line checks, if the entry at index exists. If not, an IndexOutOfBoundsException is thrown and the entry will be created a few lines above this one.
                    // If this line does not throw an IndexOutOfBoundsException, then we know that we have to increment the index.
                    // The index will only be incremented, when the last hashmap has been filled
                    toFix.get(index);
                    index++;
                } catch (IndexOutOfBoundsException ignored) {
                }
            }
            previousNote = note;
        }

        for (Map<Integer, Note> fix : toFix) {
            if (fix.size() <= Parameters.FIX_INCONSISTENT_TIMINGS_FASTER_THAN_NPS_AMOUNT_OF_NOTES_THRESHOLD) continue;

            int modOffset = 0;
            List<Integer> keys = new ArrayList<>(fix.keySet());
            for (int i = 0; i < keys.size(); i++) {
                if (notes.get(keys.get(i)) instanceof RedTimingNote) modOffset++;
                notes.get(keys.get(i))._type = i % 2 + modOffset == 0 ? 1 : 0;
            }
        }

        // List<note> notes (currently in seconds) must be converted back to beats!
        NpsBpmConverter.convertSecondsToBeats(notes);
        return notes;
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