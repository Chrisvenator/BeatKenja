package DataManager.Database.DatabaseEntities;

import BeatSaberObjects.Objects.Note;

import javax.persistence.*;

@Entity
@NamedQuery(name = "NoteEntity.findAllNotes", query = "SELECT d FROM NoteEntity d")
@NamedQuery(name = "NoteEntity.findById", query = "SELECT d FROM NoteEntity d WHERE d.id = :id")
@Table(name = "note", schema = "beatkenja", catalog = "")
public class NoteEntity {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column(name = "id")
    private int id;
    @Basic
    @Column(name = "line_index")
    private double lineIndex;
    @Basic
    @Column(name = "line_layer")
    private double lineLayer;
    @Basic
    @Column(name = "cut_direction")
    private int cutDirection;
    @Basic
    @Column(name = "type")
    private int type;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public double getLineIndex() {
        return lineIndex;
    }

    public void setLineIndex(double lineIndex) {
        this.lineIndex = lineIndex;
    }

    public double getLineLayer() {
        return lineLayer;
    }

    public void setLineLayer(double lineLayer) {
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        NoteEntity that = (NoteEntity) o;

        if (id != that.id) return false;
        if (Double.compare(lineIndex, that.lineIndex) != 0) return false;
        if (Double.compare(lineLayer, that.lineLayer) != 0) return false;
        if (cutDirection != that.cutDirection) return false;
        if (type != that.type) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        result = id;
        temp = Double.doubleToLongBits(lineIndex);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(lineLayer);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        result = 31 * result + cutDirection;
        result = 31 * result + type;
        return result;
    }

    @Override
    public String toString() {
        return "NoteEntity{" +
                "id=" + id +
                ", lineIndex=" + lineIndex +
                ", lineLayer=" + lineLayer +
                ", cutDirection=" + cutDirection +
                ", type=" + type +
                '}';
    }

    public Note toNote() {
        return new Note(0, lineIndex, lineLayer, cutDirection, type);
    }
}
