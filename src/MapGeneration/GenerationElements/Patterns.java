package MapGeneration.GenerationElements;

import MapGeneration.GenerationElements.Exceptions.MalformedFileExtensionException;
import MapGeneration.GenerationElements.Exceptions.MalformedSequenceException;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

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

    public Patterns() {
        initializeSequences("C:\\Program Files (x86)\\Steam\\steamapps\\common\\Beat Saber\\_SongsToTimings\\Patterns\\sequences", Sequence.class);
        initializeSequences("C:\\Program Files (x86)\\Steam\\steamapps\\common\\Beat Saber\\_SongsToTimings\\Patterns\\PatternProbabilities", Pattern.class);
    }

    public <T> void initializeSequences(String folderPath, Class<T> type) {
        try {
            Path start = Paths.get(folderPath);

            // Use Files.walkFileTree to traverse the directory recursively
            Files.walkFileTree(start, EnumSet.noneOf(FileVisitOption.class), Integer.MAX_VALUE,
                    new SimpleFileVisitor<>() {
                        @Override
                        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                            try {
                                if (type.equals(Sequence.class)) {
                                    sequences.add(new Sequence(file.toString()));
                                } else if (type.equals(Pattern.class)) {
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
