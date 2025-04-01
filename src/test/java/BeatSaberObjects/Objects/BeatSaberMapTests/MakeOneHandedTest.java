package BeatSaberObjects.Objects.BeatSaberMapTests;

import BeatSaberObjects.Objects.BeatSaberMap;
import BeatSaberObjects.Objects.Note;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class MakeOneHandedTest {
    BeatSaberMap map;
    
    @BeforeEach
    public void setUp() {
        map = BeatSaberMap.newMapFromJSON("src/test/resources/BeatSaberMapTests/ISeeFire.txt");
    }
    
    @Test
    public void MakeMapOneHanded_withMap_shouldSucceed() {
        assertThat(Arrays.stream(map._notes).filter(e -> e._type == 1).toArray()).isNotEmpty();
        map.makeOneHanded(1);
        assertThat(Arrays.stream(map._notes).filter(e -> e._type == 1).toArray()).isEmpty();
    }
    
    @Test
    public void MakeMapOneHanded_withMapDynamic_shouldSucceed() {
        map._notes = Arrays.stream(map._notes).filter(e -> e._type == 0 || e._type == 1).toList().toArray(new Note[0]);
        
        int noteCount = map._notes.length;
        int redCount = (int) Arrays.stream(map._notes).filter(e -> e._type == 0).count();
        int blueCount = (int) Arrays.stream(map._notes).filter(e -> e._type == 1).count();
        assertThat(Arrays.stream(map._notes).filter(e -> e._type != 0 && e._type != 1).count()).isEqualTo(0);
        assertThat(noteCount).isEqualTo(redCount + blueCount);
        
        assertThat(Arrays.stream(map._notes).filter(e -> e._type == 1).toArray()).isNotEmpty();
        map.makeOneHanded(1);
        assertThat(Arrays.stream(map._notes).filter(e -> e._type == 1).toArray()).isEmpty();
        
        assertThat(map._notes.length).isEqualTo(redCount);
        
        noteCount = map._notes.length;
        redCount = (int) Arrays.stream(map._notes).filter(e -> e._type == 0).count();
        blueCount = (int) Arrays.stream(map._notes).filter(e -> e._type == 1).count();
        assertThat(noteCount).isEqualTo(redCount + blueCount);
    }
    
    @Test
    public void MakeMapOneHanded_withBombs_withMapDynamic_shouldSucceed() {
        int noteCount = map._notes.length;
        int redCount = (int) Arrays.stream(map._notes).filter(e -> e._type == 0).count();
        int blueCount = (int) Arrays.stream(map._notes).filter(e -> e._type == 1).count();
        int bombCount = (int) Arrays.stream(map._notes).filter(e -> e._type == 3).count();
        assertThat(Arrays.stream(map._notes).filter(e -> e._type != 0 && e._type != 1).count()).isNotEqualTo(0);
        
        assertThat(Arrays.stream(map._notes).filter(e -> e._type == 3).toArray()).isNotEmpty();
        map.makeOneHanded(1);
        assertThat(Arrays.stream(map._notes).filter(e -> e._type == 3).toArray()).isNotEmpty();
    }
    
}
