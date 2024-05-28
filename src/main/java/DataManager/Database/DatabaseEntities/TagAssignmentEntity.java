package DataManager.Database.DatabaseEntities;

import javax.persistence.*;

@Entity
@NamedQuery(name = "TagAssignment.findTagAssignment", query = "SELECT ta FROM TagAssignmentEntity ta WHERE fkTagId = :fkTagId AND fkPatternDescriptionId = :fkPatternDescriptionId")
@NamedQuery(name = "TagAssignment.findTagAssignmentByFkPatternDescriptionId", query = "SELECT ta FROM TagAssignmentEntity ta WHERE fkPatternDescriptionId = :fkPatternDescriptionId")
@Table(name = "assignment_tag", schema = "beatkenja", catalog = "")
public class TagAssignmentEntity {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column(name = "id")
    private int id;
    @Basic
    @Column(name = "fk_tag_id")
    private int fkTagId;
    @Basic
    @Column(name = "fk_pattern_description_id")
    private int fkPatternDescriptionId;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getFkTagId() {
        return fkTagId;
    }

    public void setFkTagId(int fkTagId) {
        this.fkTagId = fkTagId;
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

        TagAssignmentEntity that = (TagAssignmentEntity) o;

        if (id != that.id) return false;
        if (fkTagId != that.fkTagId) return false;
        if (fkPatternDescriptionId != that.fkPatternDescriptionId) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + fkTagId;
        result = 31 * result + fkPatternDescriptionId;
        return result;
    }

    @Override
    public String toString() {
        return "TagAssignmentEntity{" +
                "id=" + id +
                ", fkTagId=" + fkTagId +
                ", fkPatternDescriptionId=" + fkPatternDescriptionId +
                '}';
    }
}
