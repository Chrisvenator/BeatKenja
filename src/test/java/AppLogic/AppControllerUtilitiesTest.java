package AppLogic;

import BeatSaberObjects.Objects.Note;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.Arrays;
import java.util.List;

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
}
