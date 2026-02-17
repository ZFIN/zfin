package org.zfin.mutant;

import jakarta.persistence.*;
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
@Entity
@Table(name = "phenotype_statement")
public class PhenotypeStatement implements Comparable<PhenotypeStatement>, EntityZdbID {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "phenos_pk_id")
    private long id;

    @ManyToOne
    @JoinColumn(name = "phenos_phenox_pk_id", nullable = false)
    private PhenotypeExperiment phenotypeExperiment;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "phenos_entity_1_superterm_zdb_id")
    private GenericTerm e1Superterm;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "phenos_entity_1_subterm_zdb_id")
    private GenericTerm e1Subterm;

    @Transient
    private PostComposedEntity entity;

    @ManyToOne
    @JoinColumn(name = "phenos_quality_zdb_id")
    private GenericTerm quality;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "phenos_entity_2_superterm_zdb_id")
    private GenericTerm e2Superterm;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "phenos_entity_2_subterm_zdb_id")
    private GenericTerm e2Subterm;

    @Transient
    private PostComposedEntity relatedEntity;

    @Column(name = "phenos_tag")
    private String tag;

    @Column(name = "phenos_created_date", nullable = false)
    private Date dateCreated;

    public PostComposedEntity getEntity() {
        if (entity == null && e1Superterm != null) {
            entity = new PostComposedEntity();
            entity.setSuperterm(e1Superterm);
            entity.setSubterm(e1Subterm);
        }
        return entity;
    }

    public void setEntity(PostComposedEntity entity) {
        this.entity = entity;
        if (entity != null) {
            this.e1Superterm = entity.getSuperterm();
            this.e1Subterm = entity.getSubterm();
        } else {
            this.e1Superterm = null;
            this.e1Subterm = null;
        }
    }

    public PostComposedEntity getRelatedEntity() {
        if (relatedEntity == null && e2Superterm != null) {
            relatedEntity = new PostComposedEntity();
            relatedEntity.setSuperterm(e2Superterm);
            relatedEntity.setSubterm(e2Subterm);
        }
        return relatedEntity;
    }

    public void setRelatedEntity(PostComposedEntity relatedEntity) {
        this.relatedEntity = relatedEntity;
        if (relatedEntity != null) {
            this.e2Superterm = relatedEntity.getSuperterm();
            this.e2Subterm = relatedEntity.getSubterm();
        } else {
            this.e2Superterm = null;
            this.e2Subterm = null;
        }
    }

    @Override
    public String toString() {
        return id + "";
    }

    public String getDisplayNameWithoutTag() {
        StringBuilder builder = new StringBuilder();
        builder.append(getEntity().getDisplayName());
        builder.append(" - ");
        builder.append(quality.getTermName());
        if (getRelatedEntity() != null) {
            builder.append(" - ");
            builder.append(getRelatedEntity().getDisplayName());
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
        if (getEntity() != null ? !getEntity().equals(defaultStatement.getEntity()) : defaultStatement.getEntity() != null)
            return false;
        if (quality != null ? !quality.equals(defaultStatement.getQuality()) : defaultStatement.getQuality() != null)
            return false;
        if (getRelatedEntity() != null ? !getRelatedEntity().equals(defaultStatement.getRelatedEntity()) : defaultStatement.getRelatedEntity() != null)
            return false;
        if (!tag.equals(defaultStatement.getTag())) return false;

        return true;
    }

    public boolean equalsByPhenotype(PhenotypeStatement compStatement) {
        if (this == compStatement)
            return true;
        if (compStatement == null)
            return false;

        if (getEntity() != null ? !getEntity().equals(compStatement.getEntity()) : compStatement.getEntity() != null) return false;
        if (quality != null ? !quality.equals(compStatement.quality) : compStatement.quality != null) return false;
        if (getRelatedEntity() != null ? !getRelatedEntity().equals(compStatement.getRelatedEntity()) : compStatement.getRelatedEntity() != null)
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

        if (getEntity() == null)
            return false;

        if (getEntity().getSuperterm().equals(term))
            return true;

        if (getEntity().getSubterm() != null && getEntity().getSubterm().equals(term))
            return true;

        if (quality != null && quality.equals(term))
            return true;

        if (getRelatedEntity() != null && getRelatedEntity().getSuperterm() != null && getRelatedEntity().getSuperterm().equals(term))
            return true;

        if (getRelatedEntity() != null && getRelatedEntity().getSubterm() != null && getRelatedEntity().getSubterm().equals(term))
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
        if (getEntity() != null) {
            if (getEntity().contains(term))
                return true;
        }
        if (getRelatedEntity() != null) {
            if (getRelatedEntity().contains(term))
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
        if (getEntity().getSuperterm().isObsolete())
            return true;
        if (getEntity().getSubterm() != null && getEntity().getSubterm().isObsolete())
            return true;
        if (getRelatedEntity() != null && getRelatedEntity().getSuperterm() != null && getRelatedEntity().getSuperterm().isObsolete())
            return true;
        if (getRelatedEntity() != null && getRelatedEntity().getSubterm() != null && getRelatedEntity().getSubterm().isObsolete())
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
        if (getRelatedEntity() == null)
            return getEntity().toString() + " " + quality.getTermName() + " " + tag;
        else
            return getEntity().toString() + " " + quality.getTermName() + " " + getRelatedEntity().toString() + " " + tag;
    }
}
