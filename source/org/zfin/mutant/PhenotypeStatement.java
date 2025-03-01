package org.zfin.mutant;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.zfin.infrastructure.EntityZdbID;
import org.zfin.ontology.GenericTerm;
import org.zfin.ontology.PostComposedEntity;
import org.zfin.ontology.Term;

import java.util.Date;

/**
 * An individual observation of phenotype
 */
@Setter
@Getter
public class PhenotypeStatement implements Comparable<PhenotypeStatement>, EntityZdbID {
    private long id;
    private PhenotypeExperiment phenotypeExperiment;
    private PostComposedEntity entity;
    private GenericTerm quality;
    private PostComposedEntity relatedEntity;
    private String tag;
    private Date dateCreated;

    @Override
    public String toString() {
        return id + "";
    }

    public String getDisplayNameWithoutTag() {
        StringBuilder builder = new StringBuilder();
        builder.append(entity.getDisplayName());
        builder.append(" - ");
        builder.append(quality.getTermName());
        if (relatedEntity != null) {
            builder.append(" - ");
            builder.append(relatedEntity.getDisplayName());
        }
        return builder.toString();

    }

    public String getDisplayName() {
        StringBuilder builder = new StringBuilder();
        if (isNormal())
            builder.append("(normal or recovered)");
        builder.append(getDisplayNameWithoutTag());
        return builder.toString();
    }

    public boolean isNormal() {

        return tag.equals(Tag.NORMAL.toString());
    }

    public boolean isNotNormal() {

        return !isNormal();
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
    public int compareTo(PhenotypeStatement statement) {
        return getDisplayNameWithoutTag().compareToIgnoreCase(statement.getDisplayNameWithoutTag());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PhenotypeStatement that = (PhenotypeStatement) o;

        String displayName = getDisplayName();
        String thatDisplayName = that.getDisplayName();
        String thatTag = that.getTag();
        if (displayName != null ? !displayName.equals(thatDisplayName) : thatDisplayName != null) return false;
        if (tag != null ? !tag.equals(thatTag) : thatTag != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        String displayName = getDisplayName();
        int hash = displayName != null ? displayName.hashCode() : 0;
        hash += tag != null ? tag.hashCode() : 0;
        return hash;
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

    @Override
    public String getAbbreviation() {
        return getDisplayName();
    }

    @Override
    public String getAbbreviationOrder() {
        return getDisplayName();
    }

    @Override
    public String getEntityType() {
        return "Phenotype Statement";
    }

    @Override
    public String getEntityName() {
        return getDisplayName();
    }

    @Override
    public String getZdbID() {
        return String.valueOf(id);
    }

    @Override
    public void setZdbID(String zdbID) {

    }

    public boolean hasObsoletePhenotype() {
        if (entity.getSuperterm().isObsolete())
            return true;
        if (entity.getSubterm() != null && entity.getSubterm().isObsolete())
            return true;
        if (relatedEntity != null && relatedEntity.getSuperterm() != null && relatedEntity.getSuperterm().isObsolete())
            return true;
        if (relatedEntity != null && relatedEntity.getSubterm() != null && relatedEntity.getSubterm().isObsolete())
            return true;
        return quality.isObsolete();
    }


    public static enum Tag {
        NORMAL("normal"),
        ABNORMAL("abnormal"),
        AMELIORATED("ameliorated"),
        EXACERBATED("exacerbated");

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
