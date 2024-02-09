package MapGeneration.GenerationElements;

import BeatSaberObjects.Objects.Note;
import DataManager.FileManager;
import MapGeneration.GenerationElements.Exceptions.MalformedFileExtensionException;
import MapGeneration.GenerationElements.Exceptions.MalformedSequenceException;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.util.*;

/**
 * The Sequence class represents a sequence of notes for a Beat Saber map.
 * It allows for iterating over these notes, and provides functionality to parse
 * a sequence from a file and manage notes within the sequence.
 */
public class Sequence implements Iterable<Note> {

    // Map to store notes based on their time
    private final Map<Float, List<Note>> notes = new HashMap<>();

    // Metadata for the sequence
    public final String[] tags;
    public final String[] genres;
    public final float nps;
    public final float bpm;


    /**
     * Constructs a Sequence object with a list of notes, tag, genre, and notes per second (nps).
     *
     * @param notes The list of notes to initialize the sequence with.
     * @param tag   The tag associated with the sequence.
     * @param genre The genre associated with the sequence.
     * @param nps   The notes per second for the sequence.
     */
    public Sequence(List<Note> notes, String[] tag, String[] genre, float nps, float bpm) {
        for (Note n : notes) {
            addNote(n);
        }
        this.tags = tag;
        this.genres = genre;
        this.nps = nps;
        this.bpm = bpm;
    }

    /**
     * Constructs a Sequence object by reading data from a file.<br>
     * In the first line, the file must contain a header with the tag, genre, and nps all separated by commas.<br>
     * The path must lead to a .seq file.<br>
     *
     * @param path The path to the sequence file to be read.
     * @throws NumberFormatException           If there is an issue parsing the file contents.
     * @throws MalformedSequenceException      If the sequence file is not formatted correctly.
     * @throws MalformedFileExtensionException If the sequence file extension is correct (.seq file).
     */
    public Sequence(String path) throws NumberFormatException, MalformedSequenceException, MalformedFileExtensionException {
        if (!path.endsWith(".seq")) throw new MalformedFileExtensionException("Sequence file is not a .seq file!");

        Gson gson = new Gson();
        List<String> notes = FileManager.readFile(path);
        String[] header = notes.getFirst().replaceAll(" ", "").split(";");
        if (header.length != 4) throw new MalformedSequenceException("Header of sequence file is not valid!");

        tags = header[0].split(",");
        genres = header[1].split(",");
        nps = Float.parseFloat(header[2]);
        bpm = Float.parseFloat(header[3]);

        for (int i = 1; i < notes.size(); i++) {
            try {
                Note n = gson.fromJson(notes.get(i), Note.class);
                addNote(n);
            } catch (JsonSyntaxException e) {
                throw new MalformedSequenceException("Note in sequence file is not valid!");
            }
        }
    }

    /**
     * Adds a note to the sequence.
     * The notes will be stored in a list so that there may be multiple notes at the same beat.
     *
     * @param n The note to be added.
     */
    public void addNote(Note n) {
        float time = n._time;
        if (notes.containsKey(time)) {
            notes.get(time).add(n);
        } else {
            List<Note> list = new ArrayList<>();
            list.add(n);
            notes.put(time, list);
        }
    }

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder();
        s.append(Arrays.toString(tags).replace("[", "").replace("]", ""));
        s.append(Arrays.toString(genres).replace("[", "").replace("]", ""));
        s.append(nps).append(bpm).append("\n");

        List<Float> keys = new ArrayList<>(notes.keySet());
        Collections.sort(keys);

        for (Float f : keys) {
            for (Note n : notes.get(f)) {
                s.append(n.toString().replace("\n", "")).append(",");
            }
            s = new StringBuilder(s.substring(0, s.length() - 1));
            s.append("\n");
        }

        return s.toString();
    }

    /**
     * Retrieves a list of all notes in the sequence.
     *
     * @return A list of all notes in the sequence.
     */
    public List<Note> getNotes() {
        List<Note> list = new ArrayList<>();
        for (List<Note> notes : this.notes.values()) {
            list.addAll(notes);
        }
        Collections.sort(list);
        return list;
    }

    @Override
    public Iterator<Note> iterator() {
        return new Iterator<>() {
            private final Iterator<Note> iter = getNotes().iterator();

            @Override
            public boolean hasNext() {
                return iter.hasNext();
            }

            @Override
            public Note next() {
                return iter.next();
            }
        };
    }
}
