package DataManager.Database.DatabaseEntities;

import BeatSaberObjects.Objects.Note;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
@Deprecated
@Setter @Getter @Entity
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
        result = id;
        result = 31 * result + Double.hashCode(lineIndex);
        result = 31 * result + Double.hashCode(lineLayer);
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
        return new Note(0, lineIndex, lineLayer, type, cutDirection);
    }
}
