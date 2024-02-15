package DataManager.Database.DatabaseEntities;

import DataManager.Database.PersistEntities;

import javax.persistence.*;
import java.util.ArrayList;

import static DataManager.Parameters.entityManager;

@Entity
@NamedQuery(name = "PatternEntity.findAll", query = "SELECT d FROM PatternEntity d")
@NamedQuery(name = "PatternEntity.findPatterns", query = "SELECT d FROM PatternEntity d where d.patternName = :patternName")
@Table(name = "pattern", schema = "beatkenja", catalog = "")
public class PatternEntity {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column(name = "Pattern_PK_ID")
    private int patternPkId;
    @Basic
    @Column(name = "Pattern_Name")
    private String patternName;

    public int getPatternPkId() {
        return patternPkId;
    }

    public void setPatternPkId(int patternPkId) {
        this.patternPkId = patternPkId;
    }

    public String getPatternName() {
        return patternName;
    }

    public void setPatternName(String patternName) {
        this.patternName = patternName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PatternEntity that = (PatternEntity) o;

        if (patternPkId != that.patternPkId) return false;
        if (patternName != null ? !patternName.equals(that.patternName) : that.patternName != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = patternPkId;
        result = 31 * result + (patternName != null ? patternName.hashCode() : 0);
        return result;
    }

    public static PatternEntity getGenre(String genreName) {
        try {
            return (PatternEntity) entityManager.createNamedQuery("PatternEntity.findPatterns").setParameter("patternName", genreName).getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    public static ArrayList<PatternEntity> getAllGenres() {
        TypedQuery<PatternEntity> query = entityManager.createNamedQuery("PatternEntity.findAll", PatternEntity.class);
        ArrayList<PatternEntity> difficulties = new ArrayList<>(query.getResultList());

        difficulties.addAll(query.getResultList());

        return difficulties;
    }

    public boolean persist() {
        return PersistEntities.persistEntity(this);
    }

}
