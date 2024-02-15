package DataManager.Database.DatabaseEntities;

import DataManager.Database.PersistEntities;

import javax.persistence.*;
import java.util.ArrayList;

import static DataManager.Parameters.entityManager;

@Entity
@NamedQuery(name = "TagEntity.findAll", query = "SELECT d FROM TagEntity d")
@NamedQuery(name = "TagEntity.findTags", query = "SELECT d FROM TagEntity d where d.tagName = :tagName")
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

    public static TagEntity getGenre(String genreName) {
        try {
            return (TagEntity) entityManager.createNamedQuery("TagEntity.findTags").setParameter("tagName", genreName).getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    public static ArrayList<TagEntity> getAllGenres() {
        TypedQuery<TagEntity> query = entityManager.createNamedQuery("TagEntity.findAll", TagEntity.class);
        ArrayList<TagEntity> difficulties = new ArrayList<>(query.getResultList());

        difficulties.addAll(query.getResultList());

        return difficulties;
    }

    public boolean persist() {
        return PersistEntities.persistEntity(this);
    }


}
