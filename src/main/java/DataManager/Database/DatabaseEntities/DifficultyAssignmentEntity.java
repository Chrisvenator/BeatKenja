package DataManager.Database.DatabaseEntities;

import javax.persistence.*;

@Entity
@NamedQuery(name = "DifficultyAssignment.findDifficultyAssignmentByFkPatternDescriptionId", query = "SELECT da FROM DifficultyAssignmentEntity da WHERE fkPatternDescriptionId = :fkPatternDescriptionId")
@NamedQuery(name = "DifficultyAssignment.findDifficultyAssignment", query = "SELECT da FROM DifficultyAssignmentEntity da WHERE fkPatternDescriptionId = :fkPatternDescriptionId AND fkDifficultyId = :fkDifficultyId")
@Table(name = "assignment_difficulty", schema = "beatkenja", catalog = "")
public class DifficultyAssignmentEntity {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column(name = "id")
    private int id;
    @Basic
    @Column(name = "fk_difficulty_id")
    private int fkDifficultyId;
    @Basic
    @Column(name = "fk_pattern_description_id")
    private int fkPatternDescriptionId;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getFkDifficultyId() {
        return fkDifficultyId;
    }

    public void setFkDifficultyId(int fkDifficultyId) {
        this.fkDifficultyId = fkDifficultyId;
    }

    public int getFkPatternDescriptionId() {
        return fkPatternDescriptionId;
    }

    public void setFkPatternDescriptionId(int fkPatternDescriptionId) {
        this.fkPatternDescriptionId = fkPatternDescriptionId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DifficultyAssignmentEntity that = (DifficultyAssignmentEntity) o;

        if (id != that.id) return false;
        if (fkDifficultyId != that.fkDifficultyId) return false;
        if (fkPatternDescriptionId != that.fkPatternDescriptionId) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + fkDifficultyId;
        result = 31 * result + fkPatternDescriptionId;
        return result;
    }

    @Override
    public String toString() {
        return "DifficultyAssignmentEntity{" +
                "id=" + id +
                ", fkDifficultyId=" + fkDifficultyId +
                ", fkPatternDescriptionId=" + fkPatternDescriptionId +
                '}';
    }
}
