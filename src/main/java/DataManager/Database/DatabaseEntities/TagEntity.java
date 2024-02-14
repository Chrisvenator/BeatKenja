package DataManager.Database.DatabaseEntities;

import javax.persistence.*;

@Entity
@Table(name = "tag", schema = "beatkenja", catalog = "")
public class TagEntity {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column(name = "Tag_pk")
    private int tagPk;
    @Basic
    @Column(name = "Tag_Name")
    private String tagName;

    public int getTagPk() {
        return tagPk;
    }

    public void setTagPk(int tagPk) {
        this.tagPk = tagPk;
    }

    public String getTagName() {
        return tagName;
    }

    public void setTagName(String tagName) {
        this.tagName = tagName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TagEntity tagEntity = (TagEntity) o;

        if (tagPk != tagEntity.tagPk) return false;
        if (tagName != null ? !tagName.equals(tagEntity.tagName) : tagEntity.tagName != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = tagPk;
        result = 31 * result + (tagName != null ? tagName.hashCode() : 0);
        return result;
    }
}
