package DataManager.Database.DatabaseEntities;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.Objects;
@Deprecated
@Setter @Getter @Entity
@NamedQuery(name = "GenreEntity.findAllGenres", query = "SELECT d FROM GenreEntity d")
@NamedQuery(name = "GenreEntity.findGenre", query = "SELECT d FROM GenreEntity d where d.name = :GenreName")
@NamedQuery(name = "GenreEntity.findGenreById", query = "SELECT d FROM GenreEntity d where d.id = :id")
@Table(name = "genre", schema = "beatkenja", catalog = "")
public class GenreEntity {
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

        GenreEntity that = (GenreEntity) o;

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
        return "GenreEntity{" +
                "id=" + id +
                ", name='" + name + '\'' +
                '}';
    }
}
