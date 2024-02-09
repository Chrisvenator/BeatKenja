package BeatSaberObjects.Objects;

import BeatSaberObjects.BeatsaberObject;

import java.util.Objects;

public class Events implements BeatsaberObject {
    protected float _time;
    protected int _type;
    public int _value;

    public Events(float _time, int _type, int _value) {
        this._time = _time;
        this._type = _type;
        this._value = _value;
    }

    // <editor-fold desc="default methods">

    @Override
    public String toString() {
        return "{\"_time\":" + _time + ",\"_type\":" + _type + ",\"_value\":" + _value + "}";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Events events = (Events) o;
        return Float.compare(events._time, _time) == 0 && _type == events._type && _value == events._value;
    }

    @Override
    public int hashCode() {
        return Objects.hash(_time, _type, _value);
    }

    // </editor-fold desc="default methods">

    public void convertFlashLightsToOnLights() {
        if (_value == 6) _value = 1;
    }
}
