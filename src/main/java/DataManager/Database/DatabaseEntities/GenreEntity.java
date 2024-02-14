package DataManager.Database.DatabaseEntities;

import javax.persistence.*;

@Entity
@Table(name = "genre", schema = "beatkenja", catalog = "")
public class GenreEntity {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column(name = "Genre_pk")
    private int genrePk;
    @Basic
    @Column(name = "Genre_Name")
    private String genreName;

    public int getGenrePk() {
        return genrePk;
    }

    public void setGenrePk(int genrePk) {
        this.genrePk = genrePk;
    }

    public String getGenreName() {
        return genreName;
    }

    public void setGenreName(String genreName) {
        this.genreName = genreName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        GenreEntity that = (GenreEntity) o;

        if (genrePk != that.genrePk) return false;
        if (genreName != null ? !genreName.equals(that.genreName) : that.genreName != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = genrePk;
        result = 31 * result + (genreName != null ? genreName.hashCode() : 0);
        return result;
    }
}
