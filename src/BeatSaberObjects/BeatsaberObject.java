package BeatSaberObjects;

/*
float time, int lineIndex, int lineLayer, int type, int cutDirection

Red: 0
Blue: 1

Layer - Index:          Cut direction:
|---|---|---|---|       |---|---|---|
|   |   |   |3-2|       | 4 | 0 | 5 |
|---|---|---|---|       |---|---|---|
|   |   |   |3-1|       | 2 | 8 | 3 |
|---|---|---|---|       |---|---|---|
|0-0|1-0|2-0|3-0|       | 6 | 1 | 7 |
|---|---|---|---|       |---|---|---|
 */
public interface BeatsaberObject {
    String _version = "2.2.0";

    @Override
    boolean equals(Object o);

    @Override
    int hashCode();

    String toString();
}
