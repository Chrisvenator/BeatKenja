package MapGeneration.GenerationElements;

import MapGeneration.GenerationElements.Exceptions.MalformedFileExtensionException;
import MapGeneration.GenerationElements.Exceptions.MalformedSequenceException;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;

public class Patterns {
    public static void main(String[] args) {
        Patterns patterns = new Patterns();
//        System.out.println(patterns.sequences);
//        System.out.println(patterns.patterns);
        System.out.println(patterns.patterns.get(0).exportInPatFormat());
//        System.out.println(patterns.patterns.get(1).exportInPatFormat());
//        Pattern p = new Pattern("C:\\Program Files (x86)\\Steam\\steamapps\\common\\Beat Saber\\_SongsToTimings\\Patterns\\PatternProbabilities\\test1.pat");
//        System.out.println(p);
//        System.out.println(p.exportInPatFormat());
    }

    private final List<Sequence> sequences = new ArrayList<>();
    private final List<Pattern> patterns = new ArrayList<>();


    public Patterns(HashMap<String, String> type) {
        for (String path : type.keySet()) {
            initialize(path, type.get(path));
        }
    }

    public Patterns() {
        HashMap<String, String> map = new HashMap<>();
        map.put("C:\\Program Files (x86)\\Steam\\steamapps\\common\\Beat Saber\\_SongsToTimings\\Patterns\\sequences", Sequence.class.toString());
        map.put("C:\\Program Files (x86)\\Steam\\steamapps\\common\\Beat Saber\\_SongsToTimings\\Patterns\\PatternProbabilities", Pattern.class.toString());

        Patterns p = new Patterns(map);
        this.sequences.addAll(p.sequences);
        this.patterns.addAll(p.patterns);
    }

    /**
     * Initializes the sequences or patterns list with the files in the given folder path.
     * The type parameter is used to determine whether to initialize sequences or patterns.
     *
     * @param folderPath The path to the folder containing the sequences or patterns.
     * @param type       The type of the files in the folder. This will determine which list to initialize.
     */
    public void initialize(String folderPath, String type) {
        try {
            Path start = Paths.get(folderPath);

            // Use Files.walkFileTree to traverse the directory recursively
            Files.walkFileTree(start, EnumSet.noneOf(FileVisitOption.class), Integer.MAX_VALUE,
                    new SimpleFileVisitor<>() {
                        @Override
                        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                            try {
                                if (type.equals(Sequence.class.toString())) {
                                    sequences.add(new Sequence(file.toString()));
                                } else if (type.equals(Pattern.class.toString())) {
                                    patterns.add(new Pattern(file.toString()));
                                }
                            } catch (MalformedSequenceException | MalformedFileExtensionException e) {
                                throw new RuntimeException(e);
                            }

                            return FileVisitResult.CONTINUE;
                        }

                        @Override
                        public FileVisitResult visitFileFailed(Path file, IOException exc) {
                            System.err.println("Failed to visit file: " + file.toString());
                            return FileVisitResult.CONTINUE;
                        }
                    });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
