package BeatSaberObjects;

public class Obstacle {
    protected float _time;
    protected String _lineIndex;
    protected int _type;
    protected float _duration;
    protected float _width;

    public Obstacle(float _time, String _lineIndex, int _type, float _duration, float _width) {
        this._time = _time;
        this._lineIndex = _lineIndex;
        this._type = _type;
        this._duration = _duration;
        this._width = _width;
    }

    @Override
    public String toString() {
        return "{\"_time\":" + _time + ",\"_lineIndex\":" + _lineIndex + ",\"_type\":" + _type + ",\"_duration\":" + _duration + ",\"_width\":" + _width + "}";
    }
}