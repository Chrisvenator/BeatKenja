package MapGeneration.GenerationElements;

import BeatSaberObjects.Note;
import DataManager.FileManager;
import DataManager.Parameters;
import UserInterface.UserInterface;
import com.google.gson.Gson;

import java.io.File;
import java.nio.file.NoSuchFileException;
import java.util.*;

/**
 * MapGeneration.GenerationElements.Sequence is a pre-made sequence of notes.
 * MapGeneration.GenerationElements.Sequence is what normal people call pattern in the Beat Saber community.
 * <p>
 * But since there is already a class named MapGeneration.GenerationElements.Pattern, I named it MapGeneration.GenerationElements.Sequence.
 */
public class Sequence implements Iterable {

    private final List<Note> notes = new ArrayList<>();

    public static void main(String[] args) {
//        MapGeneration.GenerationElements.Sequence s = new MapGeneration.GenerationElements.Sequence("Patterns/test.txt");
//        new MapGeneration.GenerationElements.Sequence();
    }

    public Sequence() throws NoSuchFileException {
        File[] files = new File("Patterns").listFiles();

        Random rand = new Random(Parameters.SEED);

        if (files != null && files.length != 0) {
            File file = files[rand.nextInt(files.length)];
            loadSequence(file.getAbsolutePath());
            System.out.println("MapGeneration.GenerationElements.Sequence File chosen: " + file);
        } else throw new NoSuchFileException("There is no sequence template");
    }

    public Sequence(String pathToSequenceFile) {
        if (!pathToSequenceFile.contains("/")) pathToSequenceFile = "Patterns/" + pathToSequenceFile;
        System.out.println("MapGeneration.GenerationElements.Sequence File chosen: " + pathToSequenceFile);
        loadSequence(pathToSequenceFile);
    }

    public Note getFirstBlue() {
        for (Note n : notes) {
            if (n._type == 1) return n;
        }
        return null;
    }

    public Note getFirstRed() {
        for (Note n : notes) {
            if (n._type == 0) return n;
        }
        return null;
    }

    private void loadSequence(String pathToSequenceFile) {
        List<String> content = FileManager.readFile(pathToSequenceFile);
        if (!checkIfStringIsAValidSequenceFile(content))
            throw new InputMismatchException("Error in MapGeneration.GenerationElements.Sequence: MapGeneration.GenerationElements.Sequence File has wrong format!");

        Gson gson = new Gson();
        for (String s : content) notes.add(gson.fromJson(s.replace(",{", "{"), Note.class));
        for (Note n : notes) n._time = 0;
    }

    private static boolean checkIfStringIsAValidSequenceFile(List<String> content) {
        if (content == null || content.size() == 0) return false;
        for (String s : content) {
            if (!s.contains("_lineIndex") ||
                    !s.contains("_lineLayer") ||
                    !s.contains("_type") ||
                    !s.contains("_cutDirection") ||
                    !s.contains("{") ||
                    !s.contains("}"))
                return false;
        }
        return true;
    }

    @Override
    public Iterator iterator() {
        return new Iterator() {
            int i = -1;
            int iterations = 0;

            @Override
            public boolean hasNext() {
                //preventing an infinite loop
                if (iterations <= 100) {
                    iterations = 0;
                    return false;
                }
                return true;
            }

            @Override
            public Note next() {
                if (i >= notes.size() - 1) {
                    i = -1;
                    iterations++;
                }
                i++;
                return notes.get(i);
            }
        };
    }
}