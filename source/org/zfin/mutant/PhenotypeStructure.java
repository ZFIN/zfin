package org.zfin.mutant;

import org.zfin.ontology.GenericTerm;
import org.zfin.ontology.PostComposedEntity;
import org.zfin.people.Person;
import org.zfin.publication.Publication;

import java.util.Date;

/**
 * This holds a single phenotype structure, consisting of a post-composed entity, a quality term,
 * a post-composed related entity  and a tag.
 */
public class PhenotypeStructure implements Comparable<PhenotypeStructure> {

    private String zdbID;
    private Person person;
    private Publication publication;
    private PostComposedEntity entity;
    private PostComposedEntity relatedEntity;
    private GenericTerm qualityTerm;
    private PhenotypeStatement.Tag tag;
    private Date date;

    public String getZdbID() {
        return zdbID;
    }

    public void setZdbID(String zdbID) {
        this.zdbID = zdbID;
    }

    public Person getPerson() {
        return person;
    }

    public void setPerson(Person person) {
        this.person = person;
    }

    public Publication getPublication() {
        return publication;
    }

    public void setPublication(Publication publication) {
        this.publication = publication;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public PostComposedEntity getEntity() {
        return entity;
    }

    public void setEntity(PostComposedEntity entity) {
        this.entity = entity;
    }

    public PostComposedEntity getRelatedEntity() {
        return relatedEntity;
    }

    public void setRelatedEntity(PostComposedEntity relatedEntity) {
        this.relatedEntity = relatedEntity;
    }

    public GenericTerm getQualityTerm() {
        return qualityTerm;
    }

    public void setQualityTerm(GenericTerm qualityTerm) {
        this.qualityTerm = qualityTerm;
    }

    public PhenotypeStatement.Tag getTag() {
        return tag;
    }

    public void setTag(PhenotypeStatement.Tag tag) {
        this.tag = tag;
    }

    public String getSubtermName() {
        return "";
    }

    @Override
    public String toString() {
        return "PhenotypeStructure{" +
                "entity=" + entity +
                ", relatedEntity=" + relatedEntity +
                ", quality=" + qualityTerm +
                ", tag=" + tag +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PhenotypeStructure that = (PhenotypeStructure) o;

        if (entity != null ? !entity.equals(that.entity) : that.entity != null) return false;
        if (qualityTerm != null ? !qualityTerm.equals(that.qualityTerm) : that.qualityTerm != null) return false;
        if (relatedEntity != null ? !relatedEntity.equals(that.relatedEntity) : that.relatedEntity != null)
            return false;
        if (tag != that.tag) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = entity != null ? entity.hashCode() : 0;
        result = 31 * result + (relatedEntity != null ? relatedEntity.hashCode() : 0);
        result = 31 * result + (qualityTerm != null ? qualityTerm.hashCode() : 0);
        result = 31 * result + (tag != null ? tag.hashCode() : 0);
        return result;
    }

    public int compareTo(PhenotypeStructure o) {

        if (!entity.getSuperterm().equals(o.getEntity().getSuperterm()))
            return entity.getSuperterm().getTermName().compareToIgnoreCase(o.entity.getSuperterm().getTermName());
        if (entity.getSubterm() != null && o.entity.getSubterm() == null)
            return 1;
        if (entity.getSubterm() == null && o.entity.getSubterm() != null)
            return -1;
        if (entity.getSubterm() != null && o.entity.getSubterm() != null)
            return entity.getSubterm().getTermName().compareToIgnoreCase(o.entity.getSubterm().getTermName());
        return qualityTerm.getTermName().compareToIgnoreCase(o.getQualityTerm().getTermName());

    }

    public PhenotypeStatement getPhenotypeStatement() {
        PhenotypeStatement statement = new PhenotypeStatement();
        statement.setEntity(entity);
        statement.setRelatedEntity(relatedEntity);
        statement.setTag(tag.toString());
        statement.setQuality(qualityTerm);
        return statement;
    }
}