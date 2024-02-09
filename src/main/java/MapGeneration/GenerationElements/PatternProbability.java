package MapGeneration.GenerationElements;

import BeatSaberObjects.Objects.Note;

public class PatternProbability {
    public Note[] notes; // Array of notes
    public float[] probabilities; // Array of probabilities

    /**
     * Constructs a new MapGeneration.GenerationElements.PatternProbability object with the given notes and probabilities.
     *
     * @param notes         The array of notes.
     * @param probabilities The array of probabilities.
     */
    public PatternProbability(Note[] notes, float[] probabilities) {
        this.notes = notes;
        this.probabilities = probabilities;

        removeNulls(); // Remove any null elements from the notes and probabilities arrays
    }

    /**
     * Removes any null elements from the notes and probabilities arrays.
     * <p>
     * The MapGeneration.GenerationElements.PatternProbability class represents a pattern probability consisting of an array of notes and corresponding probabilities.
     * It has two instance variables: notes, an array of notes, and probabilities, an array of probabilities.
     * The constructor initializes the notes and probabilities arrays with the given values and then calls the removeNulls method to remove any null elements from the arrays.
     * The removeNulls method iterates over the notes array and counts the number of non-null elements. It then creates new arrays with only the non-null elements and updates the notes and probabilities arrays accordingly.
     * The toString method returns a string representation of the MapGeneration.GenerationElements.PatternProbability object. It iterates over the notes array, appends each note's string representation along with its probability to the string, and handles formatting based on the probability value. The resulting string is returned with the closing bracket appended.
     */
    public void removeNulls() {
        int ct = 0; // Counter for non-null elements
        for (Note note : notes) {
            if (note == null) break; // Exit loop if a null note is encountered
            ct++; // Increment the counter for non-null elements
        }

        // Create new arrays with non-null elements
        Note[] notesNew = new Note[ct];
        float[] probNew = new float[ct];
        for (int i = 0; i < ct; i++) {
            notesNew[i] = notes[i];
            probNew[i] = probabilities[i];
        }

        // Update the notes and probabilities arrays with non-null elements
        this.notes = notesNew;
        this.probabilities = probNew;
    }

    /**
     * Returns a string representation of the MapGeneration.GenerationElements.PatternProbability object.
     *
     * @return A string representation of the object.
     */
    @Override
    public String toString() {
        StringBuilder s = new StringBuilder();
        for (int i = 0; i < notes.length; i++) {
            // Append the note and its probability to the string
            s.append(probabilities[i] == 0.0 ? "" : "  ").append(notes[i].toString().replaceAll("\n", "")).append(": ").append(probabilities[i] == 0.0 ? "[\n" : (probabilities[i] + "%") + "\n");
        }

        return s + "]"; // Append the closing bracket to the string
    }
}