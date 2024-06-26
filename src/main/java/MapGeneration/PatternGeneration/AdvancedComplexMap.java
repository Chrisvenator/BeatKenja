package MapGeneration.PatternGeneration;

import BeatSaberObjects.Objects.Note;
import DataManager.Records.PatMetadata;
import MapGeneration.PatternGeneration.CommonMethods.FixErrorsInPatterns;
import MapGeneration.GenerationElements.Pattern;

import java.util.*;

import static MapGeneration.PatternGeneration.CommonMethods.StackPlacements.placeStacks;
import static MapGeneration.PatternGeneration.CommonMethods.StackPlacements.removeStacks;
import static MapGeneration.ComplexPatternFromTemplate.complexPatternFromTemplate;
import static MapGeneration.PatternGeneration.NextLinearNote.nextLinearNote;

public class AdvancedComplexMap {
    /**
     * Creates an advanced complex pattern for a Beat Saber map based on the provided notes, pattern, and metadata.
     * This method processes the notes in chunks to generate a complex pattern, calculates notes per second (NPS),
     * and handles potential pattern creation exceptions.
     *
     * @param timingsImmutable the list of immutable note timings
     * @param fallbackPattern  the fallback pattern to use if pattern creation fails
     * @param oneHanded        whether the pattern should be one-handed
     * @param stacks           whether to include stack placements
     * @param prevBlue         the previous blue note, for reference in pattern creation
     * @param prevRed          the previous red note, for reference in pattern creation
     * @param metadata         the metadata containing information such as BPM, NPS, difficulty, genre, and tags
     * @return a list of notes forming the advanced complex pattern
     * @throws IllegalArgumentException if there is an issue creating the patterns
     */
    public static List<Note> createAdvancedComplexPattern(List<Note> timingsImmutable, Pattern fallbackPattern, boolean oneHanded, boolean stacks, Note prevBlue, Note prevRed, PatMetadata metadata) throws IllegalArgumentException {
        List<Note> notes = new ArrayList<>();
        List<Note> timings = new ArrayList<>(timingsImmutable);
//        for (int i = 0; i < timingsImmutable.size(); i++) {
//            if (Math.random() < 0.3) timings.get(i)._type = 0;
//            if (Math.random() > 0.7) timings.add(new Note(timingsImmutable.get(i)._time, 0, 0, 0, 0));
//        }
        timings.sort(Comparator.comparingDouble(n -> n._time));

//        Pattern.normalizeCountArray(fallbackPattern.count, true);

        // Remove all the stack placements because they break everything YAY. They can be added later again
        // Remove all stack placements as they can cause issues, to be added back later if needed
        List<Note> stackPlacements = removeStacks(timings);

        Map<PatMetadata, Pattern> patternCache = new HashMap<>();


        int nps; // Notes per second
        // Process the timings in chunks of four notes at a time
        for (int i = 0; i < timings.size(); i += 4) {
            // Check if there are enough notes to process a complete chunk
            if (i + 3 < timings.size()) {
                double t1 = timings.get(i)._time * 60 / metadata.bpm();
                double t4 = timings.get(i + 3)._time * 60 / metadata.bpm();
                nps = (int) Math.round(4 / (t4 - t1));
            } else {
                // Handle remaining notes if less than a chunk of four
                if (i == timings.size() - 1) {
                    notes.add(nextLinearNote(
                            notes.get(i - 1),
                            timings.get(i)._time
                    ));
                } else {
                    // Create a new PatMetadata instance with the calculated NPS
                    notes.addAll(complexPatternFromTemplate(
                            timings.subList(i, timings.size()),
                            fallbackPattern,
                            oneHanded,
                            stacks,
                            prevBlue,
                            prevRed
                    ));
                    System.out.println("i" + i + " " + timings.size());
                }
                break;
            }

            // Create a new PatMetadata instance with the calculated NPS
            PatMetadata newMetadata = new PatMetadata(metadata.name(), metadata.bpm(), nps, metadata.difficulty(), metadata.genre(), metadata.tags());
            Pattern p = getCachedPattern(newMetadata, fallbackPattern, patternCache);

            // Add the generated notes to the list
            notes.addAll(complexPatternFromTemplate(
                    timings.subList(i, i + 4),
                    p,
                    oneHanded,
                    stacks,
                    prevBlue,
                    prevRed
            ));
        }

        FixErrorsInPatterns.fixSimpleMappingErrors(notes);

        // If stacks are enabled, re-add the stack placements to the notes
        if (stacks) notes = placeStacks(notes, stackPlacements);

        return notes;
    }


    //The dynamic switching between Patterns work. But the data in the database is very... trash
    private static Pattern getCachedPattern(PatMetadata metadata, Pattern fallbackPattern, Map<PatMetadata, Pattern> patternCache) {
        // Check if the pattern is already cached
        if (patternCache.containsKey(metadata)) {
            System.out.println("Using cached pattern for " + metadata.name() + " with " + metadata.bpm() + " BPM and " + metadata.nps() + " NPS");
            return patternCache.get(metadata);
        }

        // Try to create a new pattern with the provided metadata
        try {
            System.out.println("Fetching pattern for " + metadata.name() + " with " + metadata.bpm() + " BPM and " + metadata.nps() + " NPS");
            Pattern p = new Pattern(metadata);
            patternCache.put(metadata, p);
            return p;
        } catch (IllegalArgumentException e1) {
            // If creating the new pattern fails, try the original metadata
            try {
                Pattern p = new Pattern(metadata);
                patternCache.put(metadata, p);
                return p;
            } catch (IllegalArgumentException e2) {
                // If that also fails, use the fallback pattern
                return fallbackPattern;
            }
        }
    }

}
