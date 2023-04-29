import com.google.gson.Gson;

import java.util.Arrays;
import java.util.Objects;

public class BeatsaberObjects {
}

class BeatSaberMap {
    public String originalJSON;
    protected String _version = "2.1.0";
    protected Events[] _events;
    protected Note[] _notes;
    protected Obstacle[] _obstacles;
    //    private CustomData[] customData; // not working yet

    public BeatSaberMap(Note[] notes) {
        this._notes = notes;
    }

    public BeatSaberMap(Note[] notes, String originalJSON) {
        this._notes = notes;
        this.originalJSON = originalJSON;
    }


    //Make the note timing divisible by 64 so that is not being flagged by ScoreSaber as "unsure"
    public void fixPlacements(double precision) {
        for (Note n : _notes) {
            n._time = (float) Math.round(n._time / precision) * (float) precision;
        }
    }

    public void invertAllNotes() {
        for (Note n : _notes) {
            n.invertColor();
        }
    }

    public static Note[] getNotesFromJSON(String jsonInput) {
        return newMapFromJSON(jsonInput)._notes;
    }

    public static BeatSaberMap newMapFromJSON(String jsonInput) {
        Gson gson = new Gson();
        BeatSaberMap map = gson.fromJson(jsonInput, BeatSaberMap.class);

        map.originalJSON = jsonInput;
        return map;
    }

    @Override
    public String toString() {
        return "\"_notes\":" + Arrays.toString(_notes);
    }

    public String exportAsMap() {
        if (originalJSON == null) {
            return ("{\"_version\":\"" + _version + "\",\"_notes\":" + Arrays.toString(_notes) + ",\"_obstacles\":" + Arrays.toString(_obstacles) + ",\"_events\":" + Arrays.toString(_events) + ",\"_waypoints\":[],\"_customData\":{\"_time\":0.121}}")
                    .replace("\n", "")
                    .replace(".0,", ",")
                    .replace(".0}", "}")
                    .replace(" ", "")
                    .replace("\":null\",", ":[],")
                    .replace(", null, ", ", ")
                    .replace("null", "");
        } else {
            String s = ("{\"_version\":\"" + _version + "\",\"_notes\":" + Arrays.toString(_notes) + ",\"_obstacles\":" + Arrays.toString(_obstacles) + ",\"_events\":" + Arrays.toString(_events) + ",")
                    .replace("\n", "")
                    .replace(".0,", ",")
                    .replace(".0}", "}")
                    .replace(" ", "");
            s = s.replaceAll("\":null,\"", ":\":[],\"")
                    .replace(", null, ", ", ")
                    .replace("null", "");

            return s + "" + originalJSON.split("}],")[1];
        }
    }
}

class Note {
    protected float _time;
    protected int _lineIndex;
    protected int _lineLayer;
    protected int _type;
    protected int _cutDirection;

    public Note(float time, int lineIndex, int lineLayer, int type, int cutDirection) {
        this._time = time;
        this._lineIndex = lineIndex;
        this._lineLayer = lineLayer;
        this._type = type;
        this._cutDirection = cutDirection;
    }

    public Note(float time) {
        this._time = time;
        this._lineIndex = 0;
        this._lineLayer = 0;
        this._type = 1;
        this._cutDirection = 8;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Note note = (Note) o;
        return Float.compare(note._time, _time) == 0 && _lineIndex == note._lineIndex && _lineLayer == note._lineLayer && _type == note._type && _cutDirection == note._cutDirection;
    }

    @Override
    public int hashCode() {
        return Objects.hash(_time, _lineIndex, _lineLayer, _type, _cutDirection);
    }

    /*
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

    @Override
    public String toString() {
//        if (_time % 1 != 0) {
        return "{" + "\"_time\":" + _time + ",\"_lineIndex\":" + _lineIndex + ",\"_lineLayer\":" + _lineLayer + ",\"_type\":" + _type + ",\"_cutDirection\":" + _cutDirection + "}\n";
//        } else {
//            return "{" + "\"_time\":" + (int) _time + ",\"_lineIndex\":" + _lineIndex + ",\"_lineLayer\":" + _lineLayer + ",\"_type\":" + _type + ",\"_cutDirection\":" + _cutDirection + "}\n";
//        }
    }

    public void invertNote() {
        invertColor();
        invertLineIndex();
        invertNoteRotation();
    }

    public void invertColor() {
        if (_type == 0) _type = 1;
        else if (_type == 1) _type = 0;
    }

    public void invertLineIndex() {
        if (_lineIndex == 0) _lineIndex = 3;
        else if (_lineIndex == 1) _lineIndex = 2;
        else if (_lineIndex == 2) _lineIndex = 1;
        else if (_lineIndex == 3) _lineIndex = 0;
    }

    public void invertNoteRotation() {
        if (_cutDirection == 2) _cutDirection = 3;
        else if (_cutDirection == 3) _cutDirection = 2;
        else if (_cutDirection == 4) _cutDirection = 5;
        else if (_cutDirection == 5) _cutDirection = 4;
        else if (_cutDirection == 6) _cutDirection = 7;
        else if (_cutDirection == 7) _cutDirection = 6;
    }

    public Note getInverted() {
        invertNote();
        Note n = new Note(_time, _lineIndex, _lineLayer, _type, _cutDirection);
        invertNote();
        return n;
    }
}

class Obstacle {
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
//        if (_time % 1 == 0) {
        return "{\"_time\":" + _time + ",\"_lineIndex\":" + _lineIndex + ",\"_type\":" + _type + ",\"_duration\":" + _duration + ",\"_width\":" + _width + "}";
//        } else {
//            return "{\"_time\":" + (int) _time + ",\"_lineIndex\":" + _lineIndex + ",\"_type\":" + _type + ",\"_duration\":" + _duration + ",\"_width\":" + _width + "}";
//        }
    }
}

class Events {
    protected float _time;
    protected int _type;
    protected int _value;

    public Events(float _time, int _type, int _value) {
        this._time = _time;
        this._type = _type;
        this._value = _value;
    }

    @Override
    public String toString() {
//        if (_time % 1 == 0) {
        return "{\"_time\":" + _time + ",\"_type\":" + _type + ",\"_value\":" + _value + "}";
//        } else {
//            return "{\"_time\":" + (int) _time + ",\"_type\":" + _type + ",\"_value\":" + _value + "}";
//        }
    }
}

//class CustomData { //not working ATM
//    protected float _time;
//    protected Bookmarks[] bookmarks;
//
//    public CustomData(float _time) {
//        this._time = _time;
//    }
//
//    protected class Bookmarks extends CustomData {
//        protected String _name;
//        protected float[] _color;
//
//        public Bookmarks(String _name, float[] _color, float time) {
//            super(time);
//            this._name = _name;
//            this._color = _color;
//        }
//
//        @Override
//        public String toString() {
//            return "{\"_time\":" + _time + ",\"_name\":\"" + _name + "\",\"_color\":" + Arrays.toString(_color) + "}";
//        }
//    }
//
//    @Override
//    public String toString() {
//        return "\"_time=\"" + _time + ",";
//    }
//}