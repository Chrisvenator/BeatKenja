package AppLogic;

import BeatSaberObjects.Objects.Enums.BeatmapCharacteristic;
import BeatSaberObjects.Objects.Note;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests the stage-5 map utilities on a real map folder: no-arrow conversion,
 * note-type deletion, and placement snapping — no UI involved.
 */
class AppControllerUtilitiesTest {

    private static final String MAP_FOLDER = "src/main/resources/Beat Saber_Data/CustomWIPLevels/3df62";

    private AppController controller;

    @BeforeEach
    void setUp() throws Exception {
        controller = new AppController();
        controller.loadMapFileOrFolder(new File(MAP_FOLDER));
    }

    @Test
    void makeNoArrowsTurnsEveryNoteIntoADotNote() {
        DiffSession diff = controller.getActiveDiff();

        controller.makeNoArrows(List.of(diff));

        assertThat(Arrays.stream(diff.map()._notes).allMatch(n -> n._cutDirection == 8)).isTrue();
    }

    @Test
    void makeNoArrowsAllDiffsLeavesNoArrowAnywhere() {
        controller.makeNoArrows(List.copyOf(controller.session().diffs()));

        for (DiffSession diff : controller.session().diffs()) {
            assertThat(Arrays.stream(diff.map()._notes).allMatch(n -> n._cutDirection == 8)).isTrue();
        }
    }

    @Test
    void deleteNoteTypeRemovesOnlyThatColor() {
        DiffSession diff = controller.getActiveDiff();
        long blueBefore = Arrays.stream(diff.map()._notes).filter(n -> n._type == 1).count();
        assertThat(blueBefore).isGreaterThan(0);

        controller.deleteNoteType(List.of(diff), 0); // delete red

        assertThat(Arrays.stream(diff.map()._notes).noneMatch(n -> n._type == 0)).isTrue();
        assertThat(Arrays.stream(diff.map()._notes).filter(n -> n._type == 1).count()).isEqualTo(blueBefore);
    }

    @Test
    void fixPlacementsSnapsNotesToTheGrid() {
        DiffSession diff = controller.getActiveDiff();

        controller.fixPlacements(List.of(diff), 16);

        for (Note note : diff.map()._notes) {
            double snapped = note._time * 16;
            assertThat(Math.abs(snapped - Math.round(snapped))).isLessThan(1e-6);
        }
    }

    @Test
    void utilitiesKeepSessionStateAndActiveDiff() {
        DiffSession active = controller.getActiveDiff();

        controller.makeNoArrows(List.copyOf(controller.session().diffs()));
        controller.convertFlashingLights(List.of(active));

        assertThat(controller.state()).isEqualTo(AppState.LOADED);
        assertThat(controller.getActiveDiff()).isSameAs(active);
    }

    @Test
    void changeCharacteristic_createsNoArrowsDiffWithoutTransform() {
        DiffSession source = controller.getActiveDiff();
        int[] originalCutDirs = Arrays.stream(source.map()._notes).mapToInt(n -> n._cutDirection).toArray();
        int originalCount = source.map()._notes.length;

        DiffSession created = controller.changeCharacteristic(source, BeatmapCharacteristic.NO_ARROWS, false);

        assertThat(created).isNotNull();
        assertThat(created.difficultyFileName()).endsWith("NoArrows.dat");
        assertThat(created.characteristic()).isEqualTo(BeatmapCharacteristic.NO_ARROWS);
        // notes copied verbatim — count and cut directions unchanged (NOT converted to dots)
        assertThat(created.map()._notes).hasSize(originalCount);
        int[] createdCutDirs = Arrays.stream(created.map()._notes).mapToInt(n -> n._cutDirection).toArray();
        assertThat(createdCutDirs).isEqualTo(originalCutDirs);
        // source diff untouched
        assertThat(source.map()._notes).hasSize(originalCount);
        assertThat(Arrays.stream(source.map()._notes).mapToInt(n -> n._cutDirection).toArray())
                .isEqualTo(originalCutDirs);
    }

    /**
     * Regression for issue #35: exporting a map that has a non-Standard characteristic diff
     * used to re-serialize info.dat via org.json, whose unordered HashMap could push a nested
     * _customData._editors "version" ahead of the top-level _version. ArcViewer reads the
     * FIRST "version" token to detect the schema, so it saw an editor version and failed to load.
     * The exported info.dat must keep _version as the first "version"-keyed token.
     */
    @Test
    void exportKeepsMapVersionFirstInInfoDat(@TempDir Path tmp) throws Exception {
        // A non-Standard diff triggers the info.dat merge (Standard-only exports skip it entirely).
        DiffSession source = controller.getActiveDiff();
        controller.changeCharacteristic(source, BeatmapCharacteristic.LAWLESS, true);

        File zip = tmp.resolve("export.zip").toFile();
        controller.exportMapAsZip(zip);

        String infoDat = readZipEntry(zip, "Info.dat");
        assertThat(infoDat).isNotNull();

        // ArcViewer's version regex grabs the first version token; it must be the map's _version.
        Matcher m = Pattern.compile("version\"\\s*:\\s*\"([0-9.]+)").matcher(infoDat);
        assertThat(m.find()).isTrue();
        assertThat(m.group(1)).isEqualTo("2.1.0");
        // and the Lawless set was actually injected
        assertThat(infoDat).contains("Lawless");
    }

    /** Reads a zip entry's text by case-insensitive name; returns null if absent. */
    private static String readZipEntry(File zip, String name) throws Exception {
        try (ZipFile zf = new ZipFile(zip)) {
            ZipEntry entry = zf.stream()
                    .filter(e -> e.getName().equalsIgnoreCase(name))
                    .findFirst().orElse(null);
            if (entry == null) return null;
            return new String(zf.getInputStream(entry).readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    @Test
    void renameCharacteristic_replacesSourceDiff() {
        DiffSession source = controller.getActiveDiff();
        String originalFileName = source.difficultyFileName();

        DiffSession renamed = controller.renameCharacteristic(source, BeatmapCharacteristic.NO_ARROWS);

        assertThat(renamed).isNotNull();
        assertThat(renamed.characteristic()).isEqualTo(BeatmapCharacteristic.NO_ARROWS);
        // source diff removed
        assertThat(controller.session().diffs().stream()
                .anyMatch(d -> d.difficultyFileName().equals(originalFileName))).isFalse();
        // new diff present
        assertThat(controller.session().diffs()).contains(renamed);
    }
}
