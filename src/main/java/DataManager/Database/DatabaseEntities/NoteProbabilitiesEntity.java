package DataManager.Database.DatabaseEntities;

import javax.persistence.*;

@Entity
@Table(name = "note_probabilities", schema = "beatkenja", catalog = "")
public class NoteProbabilitiesEntity {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column(name = "Note_Probabilities_PK_ID")
    private int noteProbabilitiesPkId;
    @Basic
    @Column(name = "BPM")
    private int bpm;
    @Basic
    @Column(name = "NPS")
    private double nps;
    @Basic
    @Column(name = "Difficulty_FK_ID")
    private int difficultyFkId;
    @Basic
    @Column(name = "Note_FK_ID")
    private int noteFkId;
    @Basic
    @Column(name = "Followed_By_Note_FK_ID")
    private int followedByNoteFkId;
    @Basic
    @Column(name = "count")
    private int count;
    @Basic
    @Column(name = "Tags_FK_ID")
    private int tagsFkId;
    @Basic
    @Column(name = "Genre_FK_ID")
    private int genreFkId;

    public int getNoteProbabilitiesPkId() {
        return noteProbabilitiesPkId;
    }

    public void setNoteProbabilitiesPkId(int noteProbabilitiesPkId) {
        this.noteProbabilitiesPkId = noteProbabilitiesPkId;
    }

    public int getBpm() {
        return bpm;
    }

    public void setBpm(int bpm) {
        this.bpm = bpm;
    }

    public double getNps() {
        return nps;
    }

    public void setNps(double nps) {
        this.nps = nps;
    }

    public int getDifficultyFkId() {
        return difficultyFkId;
    }

    public void setDifficultyFkId(int difficultyFkId) {
        this.difficultyFkId = difficultyFkId;
    }

    public int getNoteFkId() {
        return noteFkId;
    }

    public void setNoteFkId(int noteFkId) {
        this.noteFkId = noteFkId;
    }

    public int getFollowedByNoteFkId() {
        return followedByNoteFkId;
    }

    public void setFollowedByNoteFkId(int followedByNoteFkId) {
        this.followedByNoteFkId = followedByNoteFkId;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public int getTagsFkId() {
        return tagsFkId;
    }

    public void setTagsFkId(int tagsFkId) {
        this.tagsFkId = tagsFkId;
    }

    public int getGenreFkId() {
        return genreFkId;
    }

    public void setGenreFkId(int genreFkId) {
        this.genreFkId = genreFkId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        NoteProbabilitiesEntity that = (NoteProbabilitiesEntity) o;

        if (noteProbabilitiesPkId != that.noteProbabilitiesPkId) return false;
        if (bpm != that.bpm) return false;
        if (Double.compare(nps, that.nps) != 0) return false;
        if (difficultyFkId != that.difficultyFkId) return false;
        if (noteFkId != that.noteFkId) return false;
        if (followedByNoteFkId != that.followedByNoteFkId) return false;
        if (count != that.count) return false;
        if (tagsFkId != that.tagsFkId) return false;
        if (genreFkId != that.genreFkId) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        result = noteProbabilitiesPkId;
        result = 31 * result + bpm;
        temp = Double.doubleToLongBits(nps);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        result = 31 * result + difficultyFkId;
        result = 31 * result + noteFkId;
        result = 31 * result + followedByNoteFkId;
        result = 31 * result + count;
        result = 31 * result + tagsFkId;
        result = 31 * result + genreFkId;
        return result;
    }
}
