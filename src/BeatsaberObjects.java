import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class BeatsaberObjects {
}

class BeatSaberMap {
    public String originalJSON;
    protected String _version = "2.1.0";
    protected Events[] _events;
    protected Note[] _notes;
    protected Obstacle[] _obstacles;
    protected List<Bookmark> bookmarks;
    //    private CustomData[] customData; // not working yet

    public BeatSaberMap(Note[] notes) {
        this._notes = notes;
    }

    public BeatSaberMap(Note[] notes, String originalJSON) {
        this._notes = notes;
        this.originalJSON = originalJSON;
        calculateBookmarks();
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
        map.calculateBookmarks();
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
                    .replace("null", "")
                    .replaceAll(",,", "");
        } else {
            String s = ("{\"_version\":\"" + _version + "\",\"_notes\":" + Arrays.toString(_notes) + ",\"_obstacles\":" + Arrays.toString(_obstacles) + ",\"_events\":" + Arrays.toString(_events) + ",")
                    .replace("\n", "")
                    .replace(".0,", ",")
                    .replace(".0}", "}")
                    .replace(" ", "");
            s = s.replaceAll("\":null,\"", ":\":[],\"")
                    .replace(", null, ", ", ")
                    .replace("null", "")
                    .replaceAll(",,", "");

            return s + "" + originalJSON.split("}],")[1];
        }
    }

    public void convertAllFlashLightsToOnLights() {
        for (Events e : _events) {
            e.convertFlashLightsToOnLights();
        }
    }

    //removes all notes of Type "removeType"
    public void makeOneHanded(int removeType) {
        List<Note> notes = new ArrayList<>();
        for (Note n : _notes) {
            if (n._type != removeType) {
                notes.add(n);
            }
        }
        Note[] n = new Note[notes.size()];
        for (int i = 0; i < notes.size(); i++) {
            n[i] = notes.get(i);
        }

        this._notes = n;
    }

    //This function is only here to make the List in a List into a one dimensional array, so that it is compatible with
    //the other functions
    public void toTimingNotes() {

        List<List<Note>> note = mapToTimingNotesAsList(_notes);
        List<Note> list = new ArrayList<>();

        //traversing every note
        for (List<Note> l : note) {
            list.addAll(l);
        }

        //returning the List as an Array
        this._notes = list.toArray(new Note[0]);
    }

    //This function takes all the notes of a map and converts it to a List inside a List in which all the notes are saved
    //as a dot on the leftmost lane.
    //If there are more notes on the same beat, then the notes are being converted into stacks
    //Red Notes are only created if there is a blue and a red note on the same beat. They are saved on the second lane
    //Note that there can only be a maximum of 6 Notes in one Beat or else the script will not create a 7th note;
    public static List<List<Note>> mapToTimingNotesAsList(Note[] notes) {
        //Here is a List, where all grids are being saved.
        List<List<Note>> timings = new ArrayList<>(List.of(new ArrayList<>(List.of(new Note(notes[0]._time, notes[0]._type == 0 ? 1 : 0, 0, notes[0]._type, 8)))));

        for (Note n : notes) {
            //the first note must be set manually
            if (notes[0] == n) continue;

            //retrieving the grid
            List<Note> grid = timings.get(timings.size() - 1);

            //if grid exists
            if (grid.get(0)._time == n._time) {
                int ctBlue = 0;
                int ctRed = 0;
                for (Note note : grid) {
                    if (note._type == 0) ctRed++;
                    if (note._type == 1) ctBlue++;
                }

                //It will only create a note, when there are up to 6 notes already saved.
                //It will create it in the desired lane: Red lane 1; Blue lane 0
                Note newNote = new Note(n._time, n._type == 0 ? 1 : 0, n._type == 0 ? ctRed : ctBlue, n._type, 8);
                if (ctBlue < 3 && ctRed < 3) grid.add(newNote);
            } else {
                //creating a new grid
                Note newNote = new Note(n._time, n._type == 0 ? 1 : 0, 0, n._type, 8);
                timings.add(new ArrayList<>(List.of(newNote)));
            }
        }

        //when there is only one red note, the we will be converting this red note into a blue one.
        // (It makes copying someone else's map way harder)
        for (List<Note> l : timings) {
            if (l.size() == 1) {
                l.get(0)._lineIndex = 0;
                l.get(0)._type = 1;
            }
//            for (Note n : l) { //for debugging purposes
//                System.out.println("Note:" + n.toString().replaceAll("\n", ""));
//            }
//            System.out.println("");
        }
        return timings;
    }

    //This function makes timings from a map.
    //Every note is converted into a blue dot block on the leftmost lane
    //WARNING: If there is more than 1 note in the same beat, then all but one are erased (for example stacks)
    //If you want to keep them, then have a look at "mapToTimingNotesArray" or "mapToTimingNotesList"
    public void toBlueLeftBottomRowDotTimings() {
        Note[] timings = new Note[_notes.length];
        int numberOfNulls = 0;

        //traversing every note
        for (int i = 0; i < _notes.length; i++) {

            //when the note exists, then DON'T place another one on top of it
            if (i >= 2 && (_notes[i - 1]._time == _notes[i]._time || _notes[i]._time - _notes[i - 1]._time <= (float) 1 / 8)) {
                _notes[i - 1].amountOfStackedNotes++;
                numberOfNulls++;
                continue;
            }

            //else:
            timings[i] = new Note(_notes[i]._time);
        }

        //Since there may be null values, we need to remove them
        Note[] toReturn = new Note[_notes.length - numberOfNulls];
        int ct = 0;
        for (Note n : timings) {
            if (n != null) {
                toReturn[ct] = n;
                ct++;
            }
        }

        this._notes = toReturn;
    }

    public List<Bookmark> calculateBookmarks() {
        if (this.originalJSON == null) return new ArrayList<>();
        if (!this.originalJSON.contains("\"_bookmarks\":[")) return new ArrayList<>();

        String sub = this.originalJSON.substring(this.originalJSON.indexOf("\"_bookmarks\":["));
        sub = sub.substring(14, sub.indexOf("],") - 1);
        String[] arr = sub.split("},");
        List<Bookmark> l = new ArrayList<>();

        for (String s : arr) {
            s += "}";
            Gson g = new Gson();
            l.add(new Gson().fromJson(s, Bookmark.class));
        }

        this.bookmarks = l;
        return l;
    }
}

class Note {
    protected float _time;
    protected int _lineIndex;
    protected int _lineLayer;
    protected int _type;
    protected int _cutDirection;
    protected int amountOfStackedNotes = 0;

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

    public boolean equalPlacement(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Note note = (Note) o;
        return _lineIndex == note._lineIndex && _lineLayer == note._lineLayer && _type == note._type && _cutDirection == note._cutDirection;
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
        return "{" + "\"_time\":" + _time + ",\"_lineIndex\":" + _lineIndex + ",\"_lineLayer\":" + _lineLayer + ",\"_type\":" + _type + ",\"_cutDirection\":" + _cutDirection + "}\n";
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

    public boolean isTimingNote() {
        return _cutDirection == 8;
    }

    public Note[] createStackedNote() {
        if (amountOfStackedNotes == 0) return new Note[]{this};

        List<Note> notes = new ArrayList<>();
        switch (_cutDirection) {
            case 0, 1 -> {
                if (_lineIndex == 2) {
                    notes.add(new Note(_time, _lineIndex, 0, _type, _cutDirection));
                    notes.add(new Note(_time, _lineIndex, 2, _type, _cutDirection));
                }
                if (_lineIndex == 3) {
                    if (amountOfStackedNotes >= 3) notes.add(new Note(_time, _lineIndex, 0, _type, _cutDirection));
                    notes.add(new Note(_time, _lineIndex, 1, _type, _cutDirection));
                    notes.add(new Note(_time, _lineIndex, 2, _type, _cutDirection));
                }
            }
            case 5, 6 -> {
                notes.add(new Note(_time, 2, 0, _type, _cutDirection));
                notes.add(new Note(_time, 3, 1, _type, _cutDirection));
            }
        }
        return notes.toArray(new Note[0]);
    }
}

class TimingNote extends Note {
    public TimingNote(float time) {
        super(time);
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
        return "{\"_time\":" + _time + ",\"_lineIndex\":" + _lineIndex + ",\"_type\":" + _type + ",\"_duration\":" + _duration + ",\"_width\":" + _width + "}";
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
        return "{\"_time\":" + _time + ",\"_type\":" + _type + ",\"_value\":" + _value + "}";
    }

    public void convertFlashLightsToOnLights() {
        if (_value == 6) _value = 1;
    }
}

class Bookmark {
    protected float _time;
    protected String _name;
    protected float[] _color;

    public Bookmark(float _time, String _name, float[] _color) {
        this._time = _time;
        this._name = _name;
        this._color = _color;
    }

    @Override
    public String toString() {
        return "{\"_time\":" + _time + ",\"_name\":\"" + _name + "\",\"_color\":" + Arrays.toString(_color) + "}";
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