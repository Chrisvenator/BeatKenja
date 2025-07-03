package BeatSaberObjects.Objects.BeatSaberMapTests;

import BeatSaberObjects.Objects.BeatSaberMap;
import BeatSaberObjects.Objects.Events;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ConvertAllFlashLightsToOnLightsTest {
    
    BeatSaberMap map;
    
    @BeforeEach
    public void setUp() {
        map = BeatSaberMap.newMapFromJSON("src/test/resources/Template--ISeeFire.txt");
    }
    
    @Test
    public void convertAllFlashLightsToOnLights_withMap_shouldSucceed() {
        assertThat(Arrays.stream(map._events).filter(e -> e._value == 6).toArray()).isNotEmpty();
        map.convertAllFlashLightsToOnLights();
        System.out.println(Arrays.stream(map._events).filter(e -> e._value == 6).toList());
        assertThat(Arrays.stream(map._events).filter(e -> e._value == 6).toArray()).isEmpty();
    }
    
    @Test
    public void convertAllFlashLightsToOnLights_withManualInput_shouldSucceed() {
        List<Events> events = new ArrayList<>();
        events.add(new Events(0,6,4));
        events.add(new Events(0,1,6));
        events.add(new Events(0,2,7));

        map._events = events.toArray(new Events[0]);
        
        assertThat(Arrays.stream(map._events).filter(e -> e._value == 6).toArray()).isNotEmpty();
        map._events = events.toArray(new Events[0]);
        map.convertAllFlashLightsToOnLights();
        assertThat(Arrays.stream(map._events).filter(e -> e._value == 6).toArray()).isEmpty();
    }
    
}
