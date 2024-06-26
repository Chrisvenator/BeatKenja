package MapGeneration.GenerationElements;

import BeatSaberObjects.Objects.Note;
import DataManager.Parameters;
import MapGeneration.ComplexPatternFromTemplate;

import java.util.*;

import static MapGeneration.ComplexPatternFromTemplate.complexPatternFromTemplate;

public class PatternCache {
    private final int LENGTH_OF_PATTERN; //cache.size();
    private final int SIZE_OF_CACHE; // cache.get(i).size(); must be >= 3
    private final int NUMBER_OF_NOTES_TO_BE_CHANGED;

    private int cacheNumber = -1; //cache.get(i);
    private int patternPosition = -1; // cache.get(i).get(j);

    private final List<Note> timings;
    private final Pattern p;

    List<List<Pair<Note, Boolean>>> cache;

    public PatternCache(List<Note> timings, Pattern p, int numberOfPatterns, int lengthOfPatterns, int amountToBeChanged) {
        if (numberOfPatterns < 3) throw new IllegalArgumentException("[ERROR] Number of patterns must exceed 3");
        if (amountToBeChanged > lengthOfPatterns) throw new IllegalArgumentException("[ERROR] NUMBER_OF_NOTES_TO_BE_CHANGED must be smaller than the LENGTH_OF_PATTERN");

        SIZE_OF_CACHE = numberOfPatterns;
        LENGTH_OF_PATTERN = lengthOfPatterns;
        NUMBER_OF_NOTES_TO_BE_CHANGED = amountToBeChanged;

        this.timings = timings;
        this.p = p;
        cache = new ArrayList<>();

        initializeCache();
    }

    private void initializeCache() {

        for (int i = 0; i < SIZE_OF_CACHE; i++) {
            List<Note> l = new ArrayList<>(
                    complexPatternFromTemplate(
                            timings.subList(0, LENGTH_OF_PATTERN - 1),
                            p,
                            true,
                            false,
                            false,
                            new Note(0, 2, 2, 1, 0),
                            null
                    )
            );
            List<Pair<Note, Boolean>> list = new ArrayList<>();
            for (Note n : l) list.add(new Pair<>(n, false));

            cache.add(list);
        }
    }

    public Note getNext(Note previous, int invalidPlacesInARow, float timing) {
        if (patternPosition < 0 || patternPosition >= LENGTH_OF_PATTERN) selectNewCacheAndCalculateNewBooleanValues();
        if (cacheNumber < 0 || cacheNumber >= SIZE_OF_CACHE) cacheNumber = Parameters.RANDOM.nextInt(SIZE_OF_CACHE);

        Pair<Note, Boolean> next = cache.get(cacheNumber).get(patternPosition);
        if (next.bool)
            next.note = ComplexPatternFromTemplate.getComplexNote(p, previous, invalidPlacesInARow, timing);


        if (invalidPlacesInARow == 0) patternPosition++;
        return next.note;
    }

    private void selectNewCacheAndCalculateNewBooleanValues() {
        //Set back all boolean values to false
        for (Pair<Note, Boolean> entry : cache.get(cacheNumber))
            if (entry.bool) entry.bool = false;

        //randomly choose new cache number
        cacheNumber = Parameters.RANDOM.nextInt(SIZE_OF_CACHE);
        patternPosition = 0;

        //Create a temporary list to indicate what positions should change to true
        List<Integer> positionsToChange = new ArrayList<>();
        for (int i = 0; i < LENGTH_OF_PATTERN; i++) positionsToChange.add(i);

        //Randomly select values from the list, and set the corresponding entries to true;
        for (int i = 0; i < NUMBER_OF_NOTES_TO_BE_CHANGED; i++) {
            int pos = positionsToChange.remove(Parameters.RANDOM.nextInt(LENGTH_OF_PATTERN));
            cache.get(cacheNumber).get(pos).bool = true;
        }

    }

    private static class Pair<U, V> {
        public U note;
        public V bool;

        public Pair(U first, V second) {
            this.note = first;
            this.bool = second;
        }
    }
}
