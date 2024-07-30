package DataManager.Database.DatabaseEntities;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.Objects;

@Setter @Getter @Entity
@NamedQuery(name = "TagEntity.findAllTags", query = "SELECT d FROM TagEntity d")
@NamedQuery(name = "TagEntity.findTag", query = "SELECT d FROM TagEntity d where d.name = :TagName")
@NamedQuery(name = "TagEntity.findTagById", query = "SELECT d FROM TagEntity d where d.id = :id")
@Table(name = "tag", schema = "beatkenja", catalog = "")
public class TagEntity {
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

        TagEntity tagEntity = (TagEntity) o;

        if (id != tagEntity.id) return false;
        return Objects.equals(name, tagEntity.name);
    }

    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "TagEntity{" +
                "id=" + id +
                ", name='" + name + '\'' +
                '}';
    }
}
