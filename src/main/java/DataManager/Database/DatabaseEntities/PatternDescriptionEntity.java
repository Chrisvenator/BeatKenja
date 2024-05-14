package DataManager.Database.DatabaseEntities;

import javax.persistence.*;

@Entity
@NamedQuery(name = "DifficultyEntity.findPatternDescription", query = "SELECT p FROM PatternDescriptionEntity p " +
        "JOIN DifficultyAssignmentEntity da ON da.fkPatternDescriptionId = p.id " +
        "JOIN GenreAssignmentEntity ga ON ga.fkPatternDescriptionId = p.id " +
        "JOIN TagAssignmentEntity ta ON ta.fkPatternDescriptionId = p.id " +
        "JOIN DifficultyEntity d ON da.fkDifficultyId = d.id " +
        "JOIN GenreEntity g ON ga.fkGenreId = g.id " +
        "JOIN TagEntity t ON  ta.fkTagId = t.id " +
        "WHERE p.name = :name  AND p.nps = :nps " + // "AND p.bpm = :bpm" +
        "AND d.name IN :difficultyNames " +
//        "AND g.name IN :genreNames " +
        "AND t.name IN :tagNames")
@Table(name = "pattern_description", schema = "beatkenja", catalog = "")
public class PatternDescriptionEntity {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column(name = "id")
    private int id;
    @Basic
    @Column(name = "name")
    private String name;
    @Basic
    @Column(name = "bpm")
    private double bpm;
    @Basic
    @Column(name = "nps")
    private double nps;

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

    public double getBpm() {
        return bpm;
    }

    public void setBpm(double bpm) {
        this.bpm = bpm;
    }

    public double getNps() {
        return nps;
    }

    public void setNps(double nps) {
        this.nps = nps;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PatternDescriptionEntity that = (PatternDescriptionEntity) o;

        if (id != that.id) return false;
        if (Double.compare(bpm, that.bpm) != 0) return false;
        if (Double.compare(nps, that.nps) != 0) return false;
        if (name != null ? !name.equals(that.name) : that.name != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        result = id;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        temp = Double.doubleToLongBits(bpm);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(nps);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }

    @Override
    public String toString() {
        return "PatternDescriptionEntity{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", bpm=" + bpm +
                ", nps=" + nps +
                '}';
    }
}
