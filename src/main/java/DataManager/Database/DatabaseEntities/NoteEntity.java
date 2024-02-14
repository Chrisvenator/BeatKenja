package DataManager.Database.DatabaseEntities;

import javax.persistence.*;

@Entity
@Table(name = "note", schema = "beatkenja", catalog = "")
public class NoteEntity {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column(name = "Note_PK")
    private int notePk;
    @Basic
    @Column(name = "LineIndex")
    private int lineIndex;
    @Basic
    @Column(name = "LineLayer")
    private int lineLayer;
    @Basic
    @Column(name = "CutDirection")
    private int cutDirection;
    @Basic
    @Column(name = "Type")
    private int type;
    @Basic
    @Column(name = "Color")
    private int color;

    public int getNotePk() {
        return notePk;
    }

    public void setNotePk(int notePk) {
        this.notePk = notePk;
    }

    public int getLineIndex() {
        return lineIndex;
    }

    public void setLineIndex(int lineIndex) {
        this.lineIndex = lineIndex;
    }

    public int getLineLayer() {
        return lineLayer;
    }

    public void setLineLayer(int lineLayer) {
        this.lineLayer = lineLayer;
    }

    public int getCutDirection() {
        return cutDirection;
    }

    public void setCutDirection(int cutDirection) {
        this.cutDirection = cutDirection;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        NoteEntity that = (NoteEntity) o;

        if (notePk != that.notePk) return false;
        if (lineIndex != that.lineIndex) return false;
        if (lineLayer != that.lineLayer) return false;
        if (cutDirection != that.cutDirection) return false;
        if (type != that.type) return false;
        if (color != that.color) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = notePk;
        result = 31 * result + lineIndex;
        result = 31 * result + lineLayer;
        result = 31 * result + cutDirection;
        result = 31 * result + type;
        result = 31 * result + color;
        return result;
    }
}
