package BeatSaberObjects.Objects;

import BeatSaberObjects.BeatsaberObject;
import BeatSaberObjects.Objects.Enums.ParityErrorEnum;
import DataManager.Parameters;
import UserInterface.UserInterface;
import javafx.util.Pair;
import lombok.extern.log4j.Log4j2;

import static DataManager.Parameters.*;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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
public class Note extends BeatsaberObject implements Comparable<Note>, Serializable {
    public float _time;
    public double _lineIndex;
    public double _lineLayer;
    public int _type;
    public int _cutDirection;
    public int amountOfStackedNotes = 0;

    // <editor-fold desc="constructor">

    public Note() {
        this._time = 0;
        this._lineIndex = 0;
        this._lineLayer = 0;
        this._type = 1;
        this._cutDirection = 8;
    }

    public Note(float time) {
        this._time = time;
        this._lineIndex = 0;
        this._lineLayer = 0;
        this._type = 1;
        this._cutDirection = 8;
    }

    public Note(float time, int lineIndex, int lineLayer, int type, int cutDirection) {
        this._time = time;
        this._lineIndex = lineIndex;
        this._lineLayer = lineLayer;
        this._type = type;
        this._cutDirection = cutDirection;
    }

    public Note(float time, double lineIndex, double lineLayer, int type, int cutDirection) {
        this._time = time;
        this._lineIndex = lineIndex;
        this._lineLayer = lineLayer;
        this._type = type;
        this._cutDirection = cutDirection;
    }

    public Note(Note n) {
        this._time = n._time;
        this._lineIndex = n._lineIndex;
        this._lineLayer = n._lineLayer;
        this._type = n._type;
        this._cutDirection = n._cutDirection;
        this.amountOfStackedNotes = n.amountOfStackedNotes;
    }

    // </editor-fold>

    public boolean isDD(Note previous) {
        if (ignoreDDs) return false;
        if (previous == null) return false;
        return previous._cutDirection == this._cutDirection
                || (previous._cutDirection == 6 || previous._cutDirection == 1 || previous._cutDirection == 7) && (this._cutDirection == 6 || this._cutDirection == 1 || this._cutDirection == 7)
                || (previous._cutDirection == 7 || previous._cutDirection == 3 || previous._cutDirection == 5) && (this._cutDirection == 7 || this._cutDirection == 3 || this._cutDirection == 5)
                || (previous._cutDirection == 4 || previous._cutDirection == 0 || previous._cutDirection == 5) && (this._cutDirection == 4 || this._cutDirection == 0 || this._cutDirection == 5)
                || (previous._cutDirection == 4 || previous._cutDirection == 2 || previous._cutDirection == 6) && (this._cutDirection == 4 || this._cutDirection == 2 || this._cutDirection == 6);
    }

    public boolean equalPlacement(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Note note = (Note) o;

        return _lineIndex == note._lineIndex && _lineLayer == note._lineLayer && _type == note._type && _cutDirection == note._cutDirection;
    }

    public boolean equalNotePlacement(Note note) {
        return _lineIndex == note._lineIndex && _lineLayer == note._lineLayer;
    }

    public Note[] createStackedNote() {
        if (amountOfStackedNotes == 0) return new Note[]{this};

        List<Note> notes = new ArrayList<>();
        switch (_cutDirection) {
            case 0, 1 -> {
                if (_lineIndex == 2) {
                    notes.add(new Note(_time, _lineIndex, 0, _type, _cutDirection));
                    notes.add(new Note(_time, _lineIndex, 2, _type, _cutDirection));
                } else if (_lineIndex == 3) {
                    if (amountOfStackedNotes >= 3) notes.add(new Note(_time, _lineIndex, 0, _type, _cutDirection));
                    notes.add(new Note(_time, _lineIndex, 1, _type, _cutDirection));
                    notes.add(new Note(_time, _lineIndex, 2, _type, _cutDirection));
                } else notes.add(new Note(this._time, this._lineIndex, this._lineLayer, this._type, this._cutDirection));
            }
            case 5, 6 -> {
                notes.add(new Note(_time, 2, 0, _type, _cutDirection));
                notes.add(new Note(_time, 3, 1, _type, _cutDirection));
            }
            case 2, 3, 4, 7, 8 -> {
                notes.add(new Note(this._time, this._lineIndex, this._lineLayer, this._type, this._cutDirection));
                if (SAVE_DID_NOT_PLACE_STACK_AS_BOOKMARK)
                    Parameters.PARITY_ERRORS_LIST.get(UserInterface.currentDiff).add(new Pair<>(this._time, ParityErrorEnum.DID_NOT_PLACE_STACK));
            }
        }

        return notes.toArray(new Note[0]);
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

    // <editor-fold desc="Invert functions">

    /**
     * Makes the blue Note a red Note and vice versa
     *
     * @return the inverted Note
     */
    public Note invertNote() {
        invertColor();
        invertLineIndex();
        invertNoteRotation();

        return this;
    }

    public Note getInvertedNote() {
        Note n = new Note(_time, _lineIndex, _lineLayer, _type, _cutDirection);
        n.invertNote();
        return n;
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

    public void invertCutDirection() {
        invertNoteRotation();
        if (_cutDirection == 0) _cutDirection = 1;
        else if (_cutDirection == 1) _cutDirection = 0;
        else if (_cutDirection == 2) _cutDirection = 3;
        else if (_cutDirection == 3) _cutDirection = 2;
    }

    //   _time,_lineIndex,_lineLayer,_type,_cutDirection ; _time,_lineIndex,_lineLayer,_type,_cutDirection,count ; ... (If there are more than one notes in the pattern) <br>
    public String exportInPatFormat() {
        return "" + (int) _lineIndex + (int) _lineLayer + _type + _cutDirection;
    }

    public Note getInverted() {
        invertNote();
        Note n = new Note(_time, _lineIndex, _lineLayer, _type, _cutDirection);
        invertNote();
        return n;
    }

    // </editor-fold>


    // <editor-fold desc="override methods">
    @Override
    public String toString() {
        return "{" + "\"_time\":" + _time +
                ",\"_lineIndex\":" + ((String.valueOf(_lineIndex)).indexOf(".") != (String.valueOf(_lineIndex)).length() - 2 ? _lineIndex : (String.valueOf(_lineIndex).substring(0, String.valueOf(_lineIndex).lastIndexOf(".")))) +
                ",\"_lineLayer\":" + ((String.valueOf(_lineLayer)).indexOf(".") != (String.valueOf(_lineLayer)).length() - 2 ? _lineLayer : (String.valueOf(_lineLayer).substring(0, String.valueOf(_lineLayer).lastIndexOf(".")))) +
                ",\"_type\":" + _type +
                ",\"_cutDirection\":" + _cutDirection +
                "}\n";
    }

    @Override
    public Note clone() {
        return new Note(this._time, this._lineIndex, this._lineLayer, this._type, this._cutDirection);
    }

    public String toV3String() {
        return "{\"b\":" + _time + ",\"x\":" + _lineIndex + ",\"y\":" + _lineLayer + ",\"a\":" + 0 + ",\"c\":" + _type + ",\"d\":" + _cutDirection + "}";
    }

    @Override
    public int hashCode() {
        return Objects.hash(_time, _lineIndex, _lineLayer, _type, _cutDirection);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || o.getClass() != Note.class && o.getClass() != TimingNote.class) return false;

        Note note;
        if (o.getClass() == TimingNote.class) {
            note = (TimingNote) o;
            System.out.println(Float.compare(note._time, _time) == 0);
            return Float.compare(note._time, _time) == 0;
        } else {
            note = (Note) o;
            return Float.compare(note._time, _time) == 0 && _lineIndex == note._lineIndex && _lineLayer == note._lineLayer && _type == note._type && _cutDirection == note._cutDirection;
        }
    }

    @Override
    public int compareTo(Note o) {
        if (this._time == o._time) return Float.compare(this._type, o._type);
        return Float.compare(this._time, o._time);
    }

    // </editor-fold>

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