package org.zfin.mutant;

import org.zfin.ontology.GenericTerm;
import org.zfin.ontology.PostComposedEntity;
import org.zfin.ontology.Term;

import java.util.Date;

/**
 * An individual observation of phenotype
 */
public class PhenotypeStatement implements Comparable<PhenotypeStatement> {
    private long id;
    private PhenotypeExperiment phenotypeExperiment;
    private PostComposedEntity entity;
    private GenericTerm quality;
    private PostComposedEntity relatedEntity;
    private String tag;
    private Date dateCreated;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public PhenotypeExperiment getPhenotypeExperiment() {
        return phenotypeExperiment;
    }

    public void setPhenotypeExperiment(PhenotypeExperiment phenotypeExperiment) {
        this.phenotypeExperiment = phenotypeExperiment;
    }

    public PostComposedEntity getEntity() {
        return entity;
    }

    public void setEntity(PostComposedEntity entity) {
        this.entity = entity;
    }

    public GenericTerm getQuality() {
        return quality;
    }

    public void setQuality(GenericTerm quality) {
        this.quality = quality;
    }

    public PostComposedEntity getRelatedEntity() {
        return relatedEntity;
    }

    public void setRelatedEntity(PostComposedEntity relatedEntity) {
        this.relatedEntity = relatedEntity;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    @Override
    public String toString() {
        return "PhenotypeStatement{" +
                "id=" + id +
                ", phenotypeExperiment=" + phenotypeExperiment +
                ", entity=" + entity +
                ", quality=" + quality +
                ", relatedEntity=" + relatedEntity +
                ", tag='" + tag + '\'' +
                '}';
    }

    public Date getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(Date dateCreated) {
        this.dateCreated = dateCreated;
    }

    public String getDisplayName() {
        StringBuilder builder = new StringBuilder();
        if (tag.equals(Tag.NORMAL.toString()))
            builder.append("(normal or recovered)");
        builder.append(entity.getDisplayName());
        builder.append(" - ");
        builder.append(quality.getTermName());
        if (relatedEntity != null) {
            builder.append(" - ");
            builder.append(relatedEntity.getDisplayName());
        }
        return builder.toString();
    }

    public boolean equalsByName(PhenotypeStatement defaultStatement) {
        if (entity != null ? !entity.equals(defaultStatement.getEntity()) : defaultStatement.getEntity() != null)
            return false;
        if (quality != null ? !quality.equals(defaultStatement.getQuality()) : defaultStatement.getQuality() != null)
            return false;
        if (relatedEntity != null ? !relatedEntity.equals(defaultStatement.getRelatedEntity()) : defaultStatement.getRelatedEntity() != null)
            return false;
        if (!tag.equals(defaultStatement.getTag())) return false;

        return true;
    }

    public boolean equalsByPhenotype(PhenotypeStatement compStatement) {
        if (this == compStatement)
            return true;
        if (compStatement == null)
            return false;

        if (entity != null ? !entity.equals(compStatement.entity) : compStatement.entity != null) return false;
        if (quality != null ? !quality.equals(compStatement.quality) : compStatement.quality != null) return false;
        if (relatedEntity != null ? !relatedEntity.equals(compStatement.relatedEntity) : compStatement.relatedEntity != null)
            return false;
        if (tag != null ? !tag.equals(compStatement.tag) : compStatement.tag != null) return false;

        return true;
    }

    @Override
    public int compareTo(PhenotypeStatement o) {
        String o1GenotypeName = getPhenotypeExperiment().getGenotypeExperiment().getGenotype().getName();
        final String o2GenotypeName = o.getPhenotypeExperiment().getGenotypeExperiment().getGenotype().getName();
        if (!o1GenotypeName.equals(o2GenotypeName))
            return o1GenotypeName.compareTo(o2GenotypeName);
        return 0;
    }

    /**
     * Checks if a given term is used in the entity, quality or the related entity.
     *
     * @param term Term
     * @return true or false
     */
    public boolean hasAffectedTerm(Term term) {
        if (term == null)
            return false;

        if (entity == null)
            return false;

        if (entity.getSuperterm().equals(term))
            return true;

        if (entity.getSubterm() != null && entity.getSubterm().equals(term))
            return true;

        if (quality != null && quality.equals(term))
            return true;

        if (relatedEntity != null && relatedEntity.getSuperterm() != null && relatedEntity.getSuperterm().equals(term))
            return true;

        if (relatedEntity != null && relatedEntity.getSubterm() != null && relatedEntity.getSubterm().equals(term))
            return true;

        return false;
    }

    /**
     * Checks if a given term is found in E1 or E2 position
     * Note: If the provided term is null this method return false.
     *
     * @param term Term
     * @return true or false
     */
    public boolean contains(GenericTerm term) {
        if (term == null)
            return false;
        if (entity != null) {
            if (entity.contains(term))
                return true;
        }
        if (relatedEntity != null) {
            if (relatedEntity.contains(term))
                return true;
        }
        return false;
    }

    public static enum Tag {
        NORMAL("normal"),
        ABNORMAL("abnormal");

        private String value;

        Tag(String value) {
            this.value = value;
        }

        public String toString() {
            return value;
        }

        public static Tag getTagFromName(String name) {
            for (Tag tag : values()) {
                if (tag.value.equals(name))
                    return tag;
            }
            throw new RuntimeException("No Tag object with name '" + name + "' found.");
        }
    }

    public String getPhenoStatementString() {
        if (relatedEntity == null)
            return entity.toString() + " " + quality.getTermName() + " " + tag;
        else
            return entity.toString() + " " + quality.getTermName() + " " + relatedEntity.toString() + " " + tag;
    }
}
