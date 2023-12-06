package BeatSaberObjects;

import java.util.Objects;

public interface BeatsaberObject {
    String _version = "2.2.0";

    @Override
    boolean equals(Object o);

    @Override
    int hashCode();

    String toString();
}
