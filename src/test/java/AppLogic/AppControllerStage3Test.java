package AppLogic;

import BeatSaberObjects.Objects.BeatSaberMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * End-to-end test of the stage-3 critical path through the AppController:
 * load a real map folder, convert timings, generate, and save — no UI involved.
 */
class AppControllerStage3Test {

    private static final String MAP_FOLDER = "src/main/resources/Beat Saber_Data/CustomWIPLevels/3df62";

    private AppController controller;

    @BeforeEach
    void setUp() {
        controller = new AppController();
    }

    @Test
    void loadsFolderAndSelectsFirstDiffAsActive() throws Exception {
        List<String> loaded = controller.loadMapFileOrFolder(new File(MAP_FOLDER));

        assertThat(loaded).isNotEmpty();
        assertThat(controller.state()).isEqualTo(AppState.LOADED);
        assertThat(controller.getActiveDiff()).isNotNull();
        assertThat(controller.getActiveDiff().difficultyFileName()).isEqualTo(loaded.get(0));
    }

    @Test
    void convertsToTimingNotesInPlace() throws Exception {
        controller.loadMapFileOrFolder(new File(MAP_FOLDER));
        DiffSession diff = controller.getActiveDiff();

        controller.convertToTimingNotes(true, List.of(diff));

        long dotNotes = java.util.Arrays.stream(diff.map()._notes).filter(n -> n._cutDirection == 8).count();
        assertThat(dotNotes).isEqualTo(diff.map()._notes.length);
    }

    @Test
    void generatesComplexMapForActiveDiffOnly() throws Exception {
        controller.loadMapFileOrFolder(new File(MAP_FOLDER));
        DiffSession active = controller.getActiveDiff();
        BeatSaberMap untouched = controller.session().diffs().get(1).map();

        List<String> errors = controller.generateFor(GeneratorType.COMPLEX, false, List.of(active));

        assertThat(errors).isEmpty();
        assertThat(controller.state()).isEqualTo(AppState.GENERATED);
        assertThat(active.map()._notes).isNotEmpty();
        // difficulty name survives the map swap
        assertThat(active.map().difficultyFileName).isEqualTo(active.difficultyFileName());
        // other diffs stay untouched
        assertThat(controller.session().diffs().get(1).map()).isSameAs(untouched);
    }

    @Test
    void generatesLinearForAllDiffs() throws Exception {
        controller.loadMapFileOrFolder(new File(MAP_FOLDER));

        List<String> errors = controller.generateFor(GeneratorType.LINEAR, false, List.copyOf(controller.session().diffs()));

        assertThat(errors).isEmpty();
        assertThat(controller.state()).isEqualTo(AppState.GENERATED);
    }

    @Test
    void savesGeneratedMapWithBackup(@TempDir Path tempDir) throws Exception {
        controller.loadMapFileOrFolder(new File(MAP_FOLDER));
        DiffSession active = controller.getActiveDiff();
        controller.generateFor(GeneratorType.LINEAR, false, List.of(active));

        Path target = tempDir.resolve("out.dat");
        Files.writeString(target, "old content"); // pre-existing file → backup should be created

        boolean ok = controller.saveMap(active.map(), target.toString(), true);

        assertThat(ok).isTrue();
        assertThat(target).exists();
        assertThat(Files.readString(target)).contains("_notes");
        assertThat(tempDir.resolve("out.dat1")).exists(); // the backup
        assertThat(Files.readString(tempDir.resolve("out.dat1"))).isEqualTo("old content");
    }

    @Test
    void exportsMapAsZipWithInMemoryDiffs(@TempDir Path tempDir) throws Exception {
        controller.loadMapFileOrFolder(new File(MAP_FOLDER));
        DiffSession active = controller.getActiveDiff();
        controller.generateFor(GeneratorType.LINEAR, false, List.of(active));

        Path zipPath = tempDir.resolve("map.zip");
        controller.exportMapAsZip(zipPath.toFile());

        assertThat(zipPath).exists();
        try (java.util.zip.ZipFile zip = new java.util.zip.ZipFile(zipPath.toFile())) {
            // all diffs present as entries
            for (DiffSession diff : controller.session().diffs()) {
                assertThat(zip.getEntry(diff.difficultyFileName())).isNotNull();
            }
            // the generated diff entry contains the in-memory (generated) map, not the file on disk
            String written = new String(zip.getInputStream(zip.getEntry(active.difficultyFileName())).readAllBytes());
            assertThat(written).isEqualTo(active.map().exportAsMap());
            // no nested zips
            assertThat(zip.stream().noneMatch(entry -> entry.getName().endsWith(".zip"))).isTrue();
        }
    }

    @Test
    void unloadDiffMovesActiveSelection() throws Exception {
        controller.loadMapFileOrFolder(new File(MAP_FOLDER));
        String first = controller.getActiveDiff().difficultyFileName();

        controller.unloadDiff(first);

        assertThat(controller.state()).isEqualTo(AppState.LOADED);
        assertThat(controller.getActiveDiff().difficultyFileName()).isNotEqualTo(first);
    }
}