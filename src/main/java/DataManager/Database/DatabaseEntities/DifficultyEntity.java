package DataManager.Database.DatabaseEntities;

import javax.persistence.*;

@Entity
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

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DifficultyEntity that = (DifficultyEntity) o;

        if (id != that.id) return false;
        if (name != null ? !name.equals(that.name) : that.name != null) return false;

        return true;
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
