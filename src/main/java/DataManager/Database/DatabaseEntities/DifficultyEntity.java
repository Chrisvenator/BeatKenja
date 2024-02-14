package DataManager.Database.DatabaseEntities;

import javax.persistence.*;

@Entity
@Table(name = "difficulty", schema = "beatkenja", catalog = "")
public class DifficultyEntity {
    @Basic
    @Column(name = "Difficulty_Name")
    private String difficultyName;
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column(name = "Difficulty_PK")
    private int difficultyPk;

    public String getDifficultyName() {
        return difficultyName;
    }

    public void setDifficultyName(String difficultyName) {
        this.difficultyName = difficultyName;
    }

    public int getDifficultyPk() {
        return difficultyPk;
    }

    public void setDifficultyPk(int difficultyPk) {
        this.difficultyPk = difficultyPk;
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
        int result = difficultyName != null ? difficultyName.hashCode() : 0;
        result = 31 * result + difficultyPk;
        return result;
    }
}
