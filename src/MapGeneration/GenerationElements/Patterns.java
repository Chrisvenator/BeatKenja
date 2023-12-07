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
        System.out.println(patterns.sequences);
    }

    private final List<Sequence> sequences = new ArrayList<>();

    public Patterns() {
        initializeSequences("C:\\Program Files (x86)\\Steam\\steamapps\\common\\Beat Saber\\_SongsToTimings\\Patterns\\squences");
    }

    public void initializeSequences(String folderPath) {

        try {
            Path start = Paths.get(folderPath);

            // Use Files.walkFileTree to traverse the directory recursively
            Files.walkFileTree(start, EnumSet.noneOf(FileVisitOption.class), Integer.MAX_VALUE,
                    new SimpleFileVisitor<>() {
                        @Override
                        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                            // Process the file
                            try {
                                sequences.add(new Sequence(file.toString()));
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
