package BeatSaberObjects.Objects.Enums;

import java.util.List;
import java.util.Map;

public class ParityMaps {
    public static final Map<Integer, List<Integer>> cutDirectionSmallerThanOrEquals90Degrees = Map.of(
            0, List.of(4, 0, 5),
            1, List.of(6, 1, 7),
            2, List.of(4, 2, 6),
            3, List.of(5, 3, 7),
            4, List.of(2, 4, 0),
            5, List.of(0, 5, 3),
            6, List.of(2, 6, 1),
            7, List.of(1, 7, 3),
            8, List.of()
    );
}
