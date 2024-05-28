package DataManager.Database.DatabaseEntities;

import javax.persistence.*;

@Entity
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

        TagEntity tagEntity = (TagEntity) o;

        if (id != tagEntity.id) return false;
        if (name != null ? !name.equals(tagEntity.name) : tagEntity.name != null) return false;

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
        return "TagEntity{" +
                "id=" + id +
                ", name='" + name + '\'' +
                '}';
    }
}
