package DataManager.Database.DatabaseEntities;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.Objects;
@Deprecated
@Setter @Getter @Entity
@NamedQuery(name = "GenreAssignment.findGenreAssignment", query = "SELECT ga FROM GenreAssignmentEntity ga WHERE fkGenreId = :fkGenreId AND fkPatternDescriptionId = :fkPatternDescriptionId")
@NamedQuery(name = "GenreAssignment.findGenreAssignmentByFkPatternDescriptionId", query = "SELECT ga FROM GenreAssignmentEntity ga WHERE fkPatternDescriptionId = :fkPatternDescriptionId")
@Table(name = "assignment_genre", schema = "beatkenja", catalog = "")
public class GenreAssignmentEntity {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column(name = "id")
    private int id;
    @Basic
    @Column(name = "fk_genre_id")
    private int fkGenreId;
    @Basic
    @Column(name = "fk_pattern_description_id")
    private Integer fkPatternDescriptionId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        GenreAssignmentEntity that = (GenreAssignmentEntity) o;

        if (id != that.id) return false;
        if (fkGenreId != that.fkGenreId) return false;
        return Objects.equals(fkPatternDescriptionId, that.fkPatternDescriptionId);
    }

    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + fkGenreId;
        result = 31 * result + (fkPatternDescriptionId != null ? fkPatternDescriptionId.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "GenreAssignmentEntity{" +
                "id=" + id +
                ", fkGenreId=" + fkGenreId +
                ", fkPatternDescriptionId=" + fkPatternDescriptionId +
                '}';
    }
}
