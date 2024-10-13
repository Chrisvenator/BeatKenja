package MapGeneration.PatternGeneration.CommonMethods;

import BeatSaberObjects.Objects.BeatSaberMap;
import BeatSaberObjects.Objects.Note;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import BeatSaberObjects.Objects.RedTimingNote;
import BeatSaberObjects.Objects.TimingNote;
import DataManager.Parameters;
import MapAnalysation.PatternVisualisation.CombinedPlotter;
import MapAnalysation.PatternVisualisation.NpsPlotters.AverageNpsPlotter;
import MapAnalysation.PatternVisualisation.NpsPlotters.DynamicNpsPlotter;
import MapAnalysation.PatternVisualisation.NpsPlotters.NpsCutOffPlotter;
import MapAnalysation.PatternVisualisation.NpsPlotters.NpsInfo;
import com.mysql.cj.exceptions.WrongArgumentException;
import javafx.util.Pair;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

import static DataManager.Parameters.BPM;
import static DataManager.Parameters.FIX_INCONSISTENT_TIMINGS_FASTER_THAN_NPS_THRESHOLD;
import static javax.swing.WindowConstants.DISPOSE_ON_CLOSE;
        /*
    Red: 0
    Blue: 1

    Index - Layer:          Cut direction:
    |---|---|---|---|       |---|---|---|
    |   |   |   |3-2|       | 4 | 0 | 5 |
    |---|---|---|---|       |---|---|---|
    |   |   |   |3-1|       | 2 | 8 | 3 |
    |---|---|---|---|       |---|---|---|
    |0-0|1-0|2-0|3-0|       | 6 | 1 | 7 |
    |---|---|---|---|       |---|---|---|
     */



public class FixSwingTimings extends MapGeneratorCommons {
    public static List<Note> fixFastMapTimings(List<Note> notesImmutable){
        List<Note> notes = new ArrayList<>(notesImmutable);

//        List<Note> distanceFixed = fixDistanceBetweenNotes(notes);
        List<Note> swingsFixed = fixSwingAlternating(notes);

        BeatSaberMap b = new BeatSaberMap(swingsFixed);
        b.fixPlacements(Parameters.PLACEMENT_PRECISION);

        List<Note> placementsFixed = new ArrayList<>(Arrays.stream(b._notes).toList());

        System.out.println("Notes before fix: " + notesImmutable.size());
        System.out.println("Notes after  fix: " + placementsFixed.size());

        return placementsFixed;
    }


    private static List<Note> fixDistanceBetweenNotes(List<Note> notes) {
        NpsBpmConverter.convertBeatsToSeconds(notes);

        List<Note> result = new ArrayList<>();
        List<NpsInfo> npbInfos = DynamicNpsPlotter.computeNps(notes, 2f, 1, true); //TODO: Macht das sinn so kleine Intervalle zu nehmen?
        List<Pair<NpsInfo, List<Note>>> npsNotePairs = calculateNpsNotePairs(notes, npbInfos);

        for (Pair<NpsInfo, List<Note>> npsInfoPair : npsNotePairs) {
            int nps = Math.round(npsInfoPair.getKey().nps()); //Notes Per Beat
            List<Note> values = npsInfoPair.getValue();

            if (nps < FIX_INCONSISTENT_TIMINGS_FASTER_THAN_NPS_THRESHOLD || values.size() <= 2) {
                result.addAll(values);
                continue;
            }

            float diff = npsInfoPair.getKey().toTime() - npsInfoPair.getKey().fromTime();
            int amountOfNotesToBePlaced = Math.round(diff * nps);
            float placeEveryXBeats = diff / amountOfNotesToBePlaced;

            for (float i = npsInfoPair.getKey().fromTime(); i < npsInfoPair.getKey().toTime(); i += placeEveryXBeats) {
                Note n = getNearestNote(notes, i);
                Note prev = null;
                if (!result.isEmpty()) prev = result.get(result.size() - 1);
                if (result.isEmpty() || prev != null && (prev._time != n._time || prev._type == n._type)) n._time = i;
                result.add(n.clone());
            }
        }

        NpsBpmConverter.convertSecondsToBeats(result);
        return result;
    }

    // This function Preserve Stacks. It may slightly adjust them though
    @SneakyThrows
    private static Note getNearestNote(List<Note> notes, float timing) {
        if (notes.size() <= 2) {
            throw new WrongArgumentException("A section to be fixed can not have less than 2 elements"); // Return null if the list is empty
        }

        Note closestNote = notes.get(0); // Assume the first note is the closest initially
        float minDifference = Math.abs(notes.get(0)._time - timing); // Compute the initial time difference

        for (int i = 1; i < notes.size(); i++) { // Start from the second note
            float currentDifference = Math.abs(notes.get(i)._time - timing);
            if (currentDifference < minDifference) {
                minDifference = currentDifference;
                closestNote = notes.get(i);
            }
        }

        return closestNote; // Return the closest note found
    }

    private static List<Pair<NpsInfo, List<Note>>> calculateNpsNotePairs(List<Note> notes, List<NpsInfo> npsInfos) {
        List<Pair<NpsInfo, List<Note>>> npsNotePairs = new ArrayList<>();

        npsInfos.forEach(npsInfo ->
                npsNotePairs.add(
                        new Pair<>(npsInfo, notes
                                .stream()
                                .filter(n -> n._time >= npsInfo.fromTime() && n._time <= npsInfo.toTime())
                                .collect(Collectors.toList())
                        )
                )
        );


        return npsNotePairs;
    }

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
    private static List<Note> fixSwingAlternating(@NotNull List<Note> notesImmutable) {
        List<Note> notes = new ArrayList<>(notesImmutable);


        if (BPM == -1) return notes;
        if (notes.size() <= 2) return notes;
        NpsBpmConverter.convertBeatsToSeconds(notes);
        final double minDistanceBetweenNotes = 1 / FIX_INCONSISTENT_TIMINGS_FASTER_THAN_NPS_THRESHOLD;

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


    public static void plotAsGraphs(String name, List<Note> notes){

        CombinedPlotter combinedPlotter = new CombinedPlotter("NPS of: " + name,
//              new NoteDistancePlotter(name + ": NoteDistancePlotter", notes).getSeries(),
//                new DynamicNpsPlotter(name + ": NpsPlotter 0.25", notes, 0.25f, 4).getSeries(),
                new DynamicNpsPlotter(": 1 Second Intervals | ",     notes, 1f,   4).getSeries(),
                new DynamicNpsPlotter(": 2 Seconds Intervals | ",    notes, 2f,   4).getSeries(),
                new DynamicNpsPlotter(": 0.5 Second Intervals | ",   notes, 0.5f, 1).getSeries(),
                new AverageNpsPlotter(": Average", notes).getSeries(),
                new NpsCutOffPlotter(": Cut Off", FIX_INCONSISTENT_TIMINGS_FASTER_THAN_NPS_THRESHOLD, 0, notes.get(notes.size() - 1)._time).getSeries()
//                new DynamicNpsPlotter(name + ": NpsPlotter 3", notes, 3f, 4).getSeries(),
//                 new StaticNpsPlotter(name + ": StaticNpsPlotter", notes, 2).getSeries()
                );

        combinedPlotter.visualize();
    }
}