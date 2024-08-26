package BeatSaberObjects.Objects;

import BeatSaberObjects.BeatsaberObject;

import java.util.Arrays;
import java.util.Objects;

public class Bookmark extends BeatsaberObject {
    public final float _time;
    public final String _name;
    protected final float[] _color;

    public Bookmark(float _time, String _name, float[] _color) {
        this._time = _time;
        this._name = _name;
        this._color = _color;
    }

    @Override
    public String toString() {
        return "{\"_time\":" + _time + ",\"_name\":\"" + _name + "\",\"_color\":" + Arrays.toString(_color) + "}";
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj.getClass() != this.getClass()) return false;
        Bookmark b = (Bookmark) obj;

        return b._time == this._time && Objects.equals(b._name, this._name);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(_time, _name);
        result = 31 * result + Arrays.hashCode(_color);
        return result;
    }
}
