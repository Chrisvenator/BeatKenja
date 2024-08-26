package BeatSaberObjects.Objects;

import BeatSaberObjects.BeatsaberObject;

import java.util.Objects;

public class Obstacle extends BeatsaberObject {
    protected final float _time;
    protected final String _lineIndex;
    protected final int _type;
    protected final float _duration;
    protected final float _width;

    public Obstacle(float _time, String _lineIndex, int _type, float _duration, float _width) {
        this._time = _time;
        this._lineIndex = _lineIndex;
        this._type = _type;
        this._duration = _duration;
        this._width = _width;
    }

    // <editor-fold desc="default methods">

    @Override
    public String toString() {
        return "{\"_time\":" + _time + ",\"_lineIndex\":" + _lineIndex + ",\"_type\":" + _type + ",\"_duration\":" + _duration + ",\"_width\":" + _width + "}";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Obstacle obstacle = (Obstacle) o;
        return Float.compare(obstacle._time, _time) == 0 &&
                _type == obstacle._type &&
                Float.compare(obstacle._duration, _duration) == 0 &&
                Float.compare(obstacle._width, _width) == 0 &&
                Objects.equals(_lineIndex, obstacle._lineIndex);
    }

    @Override
    public int hashCode() {
        return Objects.hash(_time, _lineIndex, _type, _duration, _width);
    }

    // </editor-fold desc="default methods">

}