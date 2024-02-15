package DataManager.Database.DatabaseEntities;


import DataManager.Database.PersistEntities;

import javax.persistence.*;
import java.util.ArrayList;

import static DataManager.Parameters.entityManager;

@Entity
@NamedQuery(name = "DifficultyEntity.findAll", query = "SELECT d FROM DifficultyEntity d")
@NamedQuery(name = "DifficultyEntity.findDifficulty", query = "SELECT d FROM DifficultyEntity d where d.difficultyName = :difficultyName")
@Table(name = "difficulty", schema = "beatkenja", catalog = "")
public class DifficultyEntity {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column(name = "Difficulty_PK")
    private int difficultyPk;
    @Basic
    @Column(name = "Difficulty_Name")
    private String difficultyName;

    public int getDifficultyPk() {
        return difficultyPk;
    }

    public void setDifficultyPk(int difficultyPk) {
        this.difficultyPk = difficultyPk;
    }

    public String getDifficultyName() {
        return difficultyName;
    }

    public void setDifficultyName(String difficultyName) {
        this.difficultyName = difficultyName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DifficultyEntity that = (DifficultyEntity) o;

        if (difficultyPk != that.difficultyPk) return false;
        if (difficultyName != null ? !difficultyName.equals(that.difficultyName) : that.difficultyName != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = difficultyPk;
        result = 31 * result + (difficultyName != null ? difficultyName.hashCode() : 0);
        return result;
    }

    public static DifficultyEntity getDifficulty(String difficultyName) {

        try {
            return (DifficultyEntity) entityManager.createNamedQuery("DifficultyEntity.findDifficulty").setParameter("difficultyName", difficultyName).getSingleResult();
        } catch (NoResultException e) {
            entityManager.close();
            return null;
        }
    }


    public static ArrayList<DifficultyEntity> getAllDifficulties() {
        TypedQuery<DifficultyEntity> query = entityManager.createNamedQuery("DifficultyEntity.findAll", DifficultyEntity.class);
        ArrayList<DifficultyEntity> difficulties = new ArrayList<>(query.getResultList());

        difficulties.addAll(query.getResultList());

        entityManager.close();
        return difficulties;
    }

    public boolean persist() {
        return PersistEntities.persistEntity(this);
    }
}
