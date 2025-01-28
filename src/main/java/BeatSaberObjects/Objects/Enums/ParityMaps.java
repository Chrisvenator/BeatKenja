package BeatSaberObjects.Objects.Enums;

import java.util.List;
import java.util.Map;

/*
Red: 0
Blue: 1

Index - Layer:          Cut direction:
|---|---|---|---|       |---|---|---|
|   |   |   |3-2|       | 4 | 0 | 5 |
|---|---|---|---|       |---|---|---|
|   |   |   |3-1|       | 2 | 8 | 3 |
|---|---|---|---|       |---|---|---|
|0-0|1-0|2-0|3-0|       | 6 | 1 | 7 |
|---|---|---|---|       |---|---|---|
 */


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

    
//    public static final Map<Integer, List<Integer>> cutDirectionSmallerThan90Degrees = Map.of(
//            0, List.of(2, 3),
//            1, List.of(2, 3),
//            2, List.of(0, 1),
//            3, List.of(0, 1),
//            4, List.of(5, 6),
//            7, List.of(5, 6),
//            5, List.of(4, 7),
//            6, List.of(4, 7),
//            8, List.of()
//    );


}
