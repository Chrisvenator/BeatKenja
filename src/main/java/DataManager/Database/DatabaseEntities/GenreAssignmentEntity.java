package DataManager.Database.DatabaseEntities;

import javax.persistence.*;

@Entity
@NamedQuery(name = "GenreAssignment.findGenreAssignment", query = "SELECT ga FROM GenreAssignmentEntity ga WHERE fkGenreId = :fkGenreId AND fkPatternDescriptionId = :fkPatternDescriptionId")
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

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getFkGenreId() {
        return fkGenreId;
    }

    public void setFkGenreId(int fkGenreId) {
        this.fkGenreId = fkGenreId;
    }

    public Integer getFkPatternDescriptionId() {
        return fkPatternDescriptionId;
    }

    public void setFkPatternDescriptionId(Integer fkPatternDescriptionId) {
        this.fkPatternDescriptionId = fkPatternDescriptionId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        GenreAssignmentEntity that = (GenreAssignmentEntity) o;

        if (id != that.id) return false;
        if (fkGenreId != that.fkGenreId) return false;
        if (fkPatternDescriptionId != null ? !fkPatternDescriptionId.equals(that.fkPatternDescriptionId) : that.fkPatternDescriptionId != null) return false;

        return true;
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
