package DataManager.Database.DatabaseEntities;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Setter @Getter @Entity
@NamedQuery(name = "PatternEntity.findByPatternDescriptionId", query = "SELECT p FROM PatternEntity p WHERE patternDescriptionId = :patternDescriptionId")
@NamedQuery(name = "PatternEntity.findById", query = "SELECT p FROM PatternEntity p WHERE p.id = :id")
@NamedQuery(name = "PatternEntity.find", query = "SELECT p FROM PatternEntity p WHERE p.patternDescriptionId = :patternDescriptionId AND p.noteId = :noteId AND p.followedByNoteId = :followedByNoteId")
@Table(name = "pattern", schema = "beatkenja", catalog = "")
public class PatternEntity {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column(name = "id")
    private int id;
    @Basic
    @Column(name = "note_id")
    private int noteId;
    @Basic
    @Column(name = "followed_by_note_id")
    private int followedByNoteId;
    @Basic
    @Column(name = "count")
    private int count;
    @Basic
    @Column(name = "pattern_description_id")
    private int patternDescriptionId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PatternEntity that = (PatternEntity) o;

        if (id != that.id) return false;
        if (noteId != that.noteId) return false;
        if (followedByNoteId != that.followedByNoteId) return false;
        if (count != that.count) return false;
        if (patternDescriptionId != that.patternDescriptionId) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + noteId;
        result = 31 * result + followedByNoteId;
        result = 31 * result + count;
        result = 31 * result + patternDescriptionId;
        return result;
    }

    @Override
    public String toString() {
        return "PatternEntity{" +
                "id=" + id +
                ", noteId=" + noteId +
                ", followedByNoteId=" + followedByNoteId +
                ", count=" + count +
                ", patternDescriptionId=" + patternDescriptionId +
                '}';
    }
}
