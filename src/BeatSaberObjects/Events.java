package BeatSaberObjects;

public class Events {
    protected float _time;
    protected int _type;
    public int _value;

    public Events(float _time, int _type, int _value) {
        this._time = _time;
        this._type = _type;
        this._value = _value;
    }

    @Override
    public String toString() {
        return "{\"_time\":" + _time + ",\"_type\":" + _type + ",\"_value\":" + _value + "}";
    }

    public void convertFlashLightsToOnLights() {
        if (_value == 6) _value = 1;
    }
}
