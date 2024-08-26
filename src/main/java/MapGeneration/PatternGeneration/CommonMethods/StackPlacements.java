package MapGeneration.PatternGeneration.CommonMethods;

import BeatSaberObjects.Objects.Note;

import java.util.ArrayList;
import java.util.List;

import static MapGeneration.PatternGeneration.CommonMethods.CheckParity.checkAndFixBasicMappingErrors;

public class StackPlacements extends MapGeneratorCommons {
    /**
     * This function creates stacks for every note in notes. Stacks will only be placed if the flag has been set.
     * The flag can be set with: note.amountOfStackedNotes = 2
     *
     * @param notes all the notes that should be looked at to create stacks. Doesn't guarantee that a stack will be placed!
     * @return a List of all notes including stacks
     */
    public static List<Note> createStacks(List<Note> notes) {
        List<Note> toReturn = new ArrayList<>();
        for (Note n : notes) {
            toReturn.addAll(List.of(n.createStackedNote()));
        }

        //Check if there is a note inside another note
        return checkAndFixBasicMappingErrors(toReturn, true);
    }

    /**
     * Removes notes that are too close in timing to a previous note of the same type.
     * This method collects all notes that should be removed first and then removes them in a separate operation.
     *
     * @param notes A list of Note objects, each representing a musical note with a type and timing.
     * @return A list of Note objects with the notes, that were too close, removed.
     */
    public static List<Note> removeStacks(List<Note> notes) {
        List<Note> toRemove = new ArrayList<>(); // List to hold notes that need to be removed
        float lastBlueTime = 0;  // Tracks the last time a blue note was placed
        float lastRedTime = 0;   // Tracks the last time a red note was placed

        // Iterate through the list of notes
        for (Note note : notes) {
            // Initialize the last time for the first blue note
            if (lastBlueTime == 0 && note._type == 0) {
                lastBlueTime = note._time;
                continue;
            }
            // Initialize the last time for the first red note
            if (lastRedTime == 0 && note._type == 1) {
                lastRedTime = note._time;
                continue;
            }

            // Check if the current blue note is too close to the last
            if (note._type == 0) {
                if (note._time - lastBlueTime <= (float) 1 / 8) {
                    toRemove.add(note); // Add to removal list if too close
                } else {
                    lastBlueTime = note._time; // Update last blue time
                }
            } else {
                // Check if the current red note is too close to the last
                if (note._time - lastRedTime <= (float) 1 / 8) {
                    toRemove.add(note); // Add to removal list if too close
                } else {
                    lastRedTime = note._time; // Update last red time
                }
            }
        }

        notes.removeAll(toRemove); // Remove all collected notes at once
        return toRemove;
    }

    /**
     * This method processes a list of notes to determine the number of stacked notes for each note.
     * A note is considered stacked if another note of the same type occurs within 1/8 of a time unit.
     *
     * @param notes           The list of notes to be processed for stacking.
     * @param stackPlacements The list of notes used to determine stack placements.
     * @return The list of notes with updated stack counts.
     */
    public static List<Note> placeStacks(List<Note> notes, List<Note> stackPlacements) {
        // Iterate through each note in the list of notes
        for (Note n : notes) {
            // For each note, compare it with each note in the stackPlacements list
            for (Note stackPlacement : stackPlacements) {
                // Calculate the time difference between the current note and the stack placement note
                float timeDiff = stackPlacement._time - n._time;
                // Check if the time difference is within the range of 0 to 1/8 (inclusive)
                // and if the notes are of the same type
                if (timeDiff >= 0 && timeDiff <= (float) 1 / 8 && n._type == stackPlacement._type) {
                    // Increment the stack count for the current note
                    n.amountOfStackedNotes++;
                }
            }
        }
        // Call the createStacks method to finalize and return the list of notes with stacks
        return createStacks(notes);
    }

}
