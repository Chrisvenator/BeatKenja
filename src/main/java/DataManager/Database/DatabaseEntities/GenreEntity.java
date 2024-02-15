package DataManager.Database.DatabaseEntities;

import DataManager.Database.PersistEntities;

import javax.persistence.*;
import java.util.ArrayList;
import static DataManager.Parameters.entityManager;


@Entity
@NamedQuery(name = "GenreEntity.findAll", query = "SELECT d FROM GenreEntity d")
@NamedQuery(name = "GenreEntity.findGenres", query = "SELECT d FROM GenreEntity d where d.genreName = :genreName")
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

    public static GenreEntity getGenre(String genreName) {
        try {
            return (GenreEntity) entityManager.createNamedQuery("GenreEntity.findGenres").setParameter("genreName", genreName).getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    public static ArrayList<GenreEntity> getAllGenres() {
        TypedQuery<GenreEntity> query = entityManager.createNamedQuery("GenreEntity.findAll", GenreEntity.class);
        ArrayList<GenreEntity> difficulties = new ArrayList<>(query.getResultList());

        difficulties.addAll(query.getResultList());

        return difficulties;
    }

    public boolean persist() {
        return PersistEntities.persistEntity(this);
    }


}
