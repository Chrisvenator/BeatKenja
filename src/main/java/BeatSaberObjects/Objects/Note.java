package BeatSaberObjects.Objects;

import BeatSaberObjects.BeatsaberObject;
import BeatSaberObjects.Objects.Enums.ParityErrorEnum;
import DataManager.Parameters;
import UserInterface.UserInterface;
import javafx.util.Pair;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static BeatSaberObjects.Objects.Enums.ParityMaps.cutDirectionSmallerThanOrEquals90Degrees;
import static DataManager.Parameters.SAVE_DID_NOT_PLACE_STACK_AS_BOOKMARK;
import static DataManager.Parameters.ignoreDDs;

/*
Red: 0
Blue: 1

Index - Layer:          Cut direction:
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
        if (previous._cutDirection < 0 || previous._cutDirection > 8) return false;
        return cutDirectionSmallerThanOrEquals90Degrees.get(previous._cutDirection).contains(_cutDirection);
    }
    
    /**
     * Checks if the placement and direction of the note is equal to the placement and direction of another note
     * <br>
     * This is the same as the .equals method, but it ignores the time
     *
     * @param o the other note
     * @return if the placement is equal
     */
    public boolean equalPlacement(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Note note = (Note) o;
        
        return _lineIndex == note._lineIndex && _lineLayer == note._lineLayer && _type == note._type && _cutDirection == note._cutDirection;
    }
    
    /**
     * Checks if only the placement of the note is equal to the placement of another note
     * <br>
     * This is the same as the .equals method, but it ignores the time and direction
     *
     * @param o the other note
     * @return if the placement is equal
     */
    public boolean equalNotePlacement(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Note note = (Note) o;
        
        return _lineIndex == note._lineIndex && _lineLayer == note._lineLayer;
    }
    
    public boolean isOutsideGrid() {
        return _lineIndex < 0 || _lineIndex > 3 || _lineLayer < 0 || _lineLayer > 2;
    }
    
    /**
     * creates a stacked note. If the amount of stacked notes is 0, it will return the note itself
     * <br>
     * The stacked notes will be created based on the cut direction and the line index of the note.
     * The postition of the stacked notes will be calculated based on the line index of the note.
     * Because of vision blocks, gird 1-2 and 2-2 will never contain a note.
     *
     * @return the note at [0] and the stacked notes (if they exist) at [1] and [2]
     */
    public Note[] createStacks() {
        if (amountOfStackedNotes <= 0) return new Note[]{this};
        if (amountOfStackedNotes > 2) amountOfStackedNotes = 2;
        boolean couldNotComputeStack = false;
        
        List<Note> notes = new ArrayList<>();
        notes.add(this);
        
        List<Note> stack = tryCreatingStackedNote();
        
        if (SAVE_DID_NOT_PLACE_STACK_AS_BOOKMARK && (stack == null || stack.isEmpty())
                && Parameters.PARITY_ERRORS_LIST.get(UserInterface.currentDiff) != null //For Unit tests
        ) {
            Parameters.PARITY_ERRORS_LIST.get(UserInterface.currentDiff).add(new Pair<>(this._time, ParityErrorEnum.DID_NOT_PLACE_STACK));
        }
        
        if (stack == null || stack.isEmpty()) return notes.toArray(new Note[0]);
        
        
        for (int i = 0; i < amountOfStackedNotes && i < stack.size(); i++) {
            if (stack.get(i) != null && !stack.get(i).isVisionBlock() && !stack.get(i).isOutsideGrid()) {
                notes.add(stack.get(i));
            }
        }

        return notes.toArray(new Note[0]);
    }
    
    //@Question: What is happening with cutDirection 8?
    public List<Note> tryCreatingStackedNote() {
        if (isOutsideGrid()) return new ArrayList<>();
        if (isVisionBlock()) return new ArrayList<>();
        
        List<Note> toReturn = new ArrayList<>();
        
        Note below = new Note(_time, whichLineIndexWillNoteCutInto(), whichLineLayerWillNoteCutInto(), _type, _cutDirection);
        Note above = new Note(_time, whichLineIndexWillNoteCutFrom(), whichLineLayerWillNoteCutFrom(), _type, _cutDirection);
        
        if (!below.isOutsideGrid() && !below.isVisionBlock()) toReturn.add(below);
        if (!above.isOutsideGrid() && !above.isVisionBlock()) toReturn.add(above);
        
        Note below2 = new Note(_time, below.whichLineIndexWillNoteCutInto(), below.whichLineLayerWillNoteCutInto(), _type, _cutDirection);
        Note above2 = new Note(_time, above.whichLineIndexWillNoteCutFrom(), above.whichLineLayerWillNoteCutFrom(), _type, _cutDirection);
        
        if (!below2.isOutsideGrid() && !below2.isVisionBlock()) toReturn.add(below2);
        if (!above2.isOutsideGrid() && !above2.isVisionBlock()) toReturn.add(above2);
        
        return toReturn;
    }
    
    
    public boolean isVisionBlock() {
        return (_lineIndex == 2 || _lineIndex == 1) && _lineLayer == 1;
    }
    
    public double whichLineLayerWillNoteCutInto() {
        double lineLayer;
        
        switch (_cutDirection) {
            case 6, 1, 7 -> lineLayer = _lineLayer - 1;
            case 4, 0, 5 -> lineLayer = _lineLayer + 1;
            default -> lineLayer = _lineLayer;
        }
        
        return lineLayer;
    }
    
    public double whichLineIndexWillNoteCutInto() {
        double lineIndex;
        
        switch (_cutDirection) {
            case 4, 2, 6 -> lineIndex = _lineIndex - 1;
            case 5, 3, 7 -> lineIndex = _lineIndex + 1;
            default -> lineIndex = _lineIndex;
        }
        
        return lineIndex;
    }
    
    public double whichLineLayerWillNoteCutFrom() {
        double lineLayer;
        
        switch (_cutDirection) {
            case 6, 1, 7 -> lineLayer = _lineLayer + 1;
            case 4, 0, 5 -> lineLayer = _lineLayer - 1;
            default -> lineLayer = _lineLayer;
        }
        
        return lineLayer;
    }
    
    public double whichLineIndexWillNoteCutFrom() {
        double lineIndex;
        
        switch (_cutDirection) {
            case 4, 2, 6 -> lineIndex = _lineIndex + 1;
            case 5, 3, 7 -> lineIndex = _lineIndex - 1;
            default -> lineIndex = _lineIndex;
        }
        
        return lineIndex;
    }


    /*
    Red: 0
    Blue: 1

    Index - Layer:          Cut direction:
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
    
    public void invertColor() {
        if (_type == 0) _type = 1;
        else if (_type == 1) _type = 0;
    }
    
    public void invertLineIndex() {
        _lineIndex = 3 - _lineIndex;
    }
    /*
    |---|---|---|---|      |---|---|---|---|
    |   |   |   |   |      |   |   |   |   |
    |---|---|---|---|      |---|---|---|---|
    |   |   |   |   |  ->  |   |   |   |   |
    |---|---|---|---|      |---|---|---|---|
    | 6 |   |   |   |      |   |   |   | 7 | 
    |---|---|---|---|      |---|---|---|---|
     */
    public void invertNoteRotation() {
        switch (_cutDirection) {
            case 0: _cutDirection = 0; break;
            case 1: _cutDirection = 1; break;
            case 2: _cutDirection = 3; break;
            case 3: _cutDirection = 2; break;
            case 4: _cutDirection = 5; break;
            case 5: _cutDirection = 4; break;
            case 6: _cutDirection = 7; break;
            case 7: _cutDirection = 6; break;
            case 8: _cutDirection = 8; break;
            // All other cut directions remain unchanged
            default: break;
        }
    }
    
    /*
    |---|---|---|
    | 4 | 0 | 5 |
    |---|---|---|
    | 2 | 8 | 3 |
    |---|---|---|
    | 6 | 1 | 7 |
    |---|---|---|
     */
    public void invertCutDirection() {
        switch (_cutDirection) {
            case 0: _cutDirection = 1; break;
            case 1: _cutDirection = 0; break;
            case 2: _cutDirection = 3; break;
            case 3: _cutDirection = 2; break;
            case 4: _cutDirection = 7; break;
            case 5: _cutDirection = 6; break;
            case 6: _cutDirection = 5; break;
            case 7: _cutDirection = 4; break;
            case 8: _cutDirection = 8; break;
            // All other cut directions remain unchanged
            default: break;
        }
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
        Note n = new Note(this._time, this._lineIndex, this._lineLayer, this._type, this._cutDirection);
        n.amountOfStackedNotes = this.amountOfStackedNotes;
        return n;
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

    Index - Layer:          Cut direction:
    |---|---|---|---|       |---|---|---|
    |   |   |   |3-2|       | 4 | 0 | 5 |
    |---|---|---|---|       |---|---|---|
    |   |   |   |3-1|       | 2 | 8 | 3 |
    |---|---|---|---|       |---|---|---|
    |0-0|1-0|2-0|3-0|       | 6 | 1 | 7 |
    |---|---|---|---|       |---|---|---|
     */