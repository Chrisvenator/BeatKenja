package DataManager.Database.DatabaseEntities;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.Objects;

@Setter @Getter @Entity
@NamedQuery(name = "DifficultyEntity.findAllDifficulties", query = "SELECT d FROM DifficultyEntity d")
@NamedQuery(name = "DifficultyEntity.findDifficulty", query = "SELECT d FROM DifficultyEntity d where d.name = :difficultyName")
@NamedQuery(name = "DifficultyEntity.findDifficultyById", query = "SELECT d FROM DifficultyEntity d where d.id = :id")
@Table(name = "difficulty", schema = "beatkenja", catalog = "")
public class DifficultyEntity {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column(name = "id")
    private int id;
    @Basic
    @Column(name = "name")
    private String name;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DifficultyEntity that = (DifficultyEntity) o;

        if (id != that.id) return false;
        return Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "DifficultyEntity{" +
                "id=" + id +
                ", name='" + name + '\'' +
                '}';
    }
}
