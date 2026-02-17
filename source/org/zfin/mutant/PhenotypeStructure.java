package org.zfin.mutant;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.zfin.framework.StringEnumValueUserType;
import org.zfin.ontology.GenericTerm;
import org.zfin.ontology.PostComposedEntity;
import org.zfin.ontology.Subset;
import org.zfin.profile.Person;
import org.zfin.publication.Publication;

import java.util.Date;

/**
 * This holds a single phenotype structure, consisting of a post-composed entity, a quality term,
 * a post-composed related entity  and a tag.
 */
@Getter
@Setter
@Entity
@Table(name = "apato_infrastructure")
public class PhenotypeStructure implements Comparable<PhenotypeStructure> {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "PhenotypeStructure")
    @GenericGenerator(name = "PhenotypeStructure",
            strategy = "org.zfin.database.ZdbIdGenerator",
            parameters = {
                    @Parameter(name = "type", value = "API"),
                    @Parameter(name = "insertActiveData", value = "true")
            })
    @Column(name = "api_zdb_id")
    private String zdbID;

    @ManyToOne
    @JoinColumn(name = "api_curator_zdb_id")
    private Person person;

    @ManyToOne
    @JoinColumn(name = "api_pub_zdb_id")
    private Publication publication;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "api_entity_1_superterm_zdb_id")
    private GenericTerm e1Superterm;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "api_entity_1_subterm_zdb_id")
    private GenericTerm e1Subterm;

    @Transient
    private PostComposedEntity entity;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "api_entity_2_superterm_zdb_id")
    private GenericTerm e2Superterm;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "api_entity_2_subterm_zdb_id")
    private GenericTerm e2Subterm;

    @Transient
    private PostComposedEntity relatedEntity;

    @ManyToOne
    @JoinColumn(name = "api_quality_zdb_id")
    private GenericTerm qualityTerm;

    @Column(name = "api_tag")
    @org.hibernate.annotations.Type(value = StringEnumValueUserType.class,
            parameters = {@Parameter(name = "enumClassname",
                    value = "org.zfin.mutant.PhenotypeStatement$Tag")})
    private PhenotypeStatement.Tag tag;

    @Column(name = "api_date")
    private Date date;

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

    public String getSubtermName() {
        return "";
    }

    @Override
    public String toString() {
        return "PhenotypeStructure{" +
                "entity=" + getEntity() +
                ", relatedEntity=" + getRelatedEntity() +
                ", quality=" + qualityTerm +
                ", tag=" + tag +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PhenotypeStructure that = (PhenotypeStructure) o;

        if (getEntity() != null ? !getEntity().equals(that.getEntity()) : that.getEntity() != null) return false;
        if (qualityTerm != null ? !qualityTerm.equals(that.qualityTerm) : that.qualityTerm != null) return false;
        if (getRelatedEntity() != null ? !getRelatedEntity().equals(that.getRelatedEntity()) : that.getRelatedEntity() != null)
            return false;
        if (tag != that.tag) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = getEntity() != null ? getEntity().hashCode() : 0;
        result = 31 * result + (getRelatedEntity() != null ? getRelatedEntity().hashCode() : 0);
        result = 31 * result + (qualityTerm != null ? qualityTerm.hashCode() : 0);
        result = 31 * result + (tag != null ? tag.hashCode() : 0);
        return result;
    }

    public int compareTo(PhenotypeStructure o) {

        if (!getEntity().getSuperterm().equals(o.getEntity().getSuperterm()))
            return getEntity().getSuperterm().getTermName().compareToIgnoreCase(o.getEntity().getSuperterm().getTermName());
        if (getEntity().getSubterm() != null && o.getEntity().getSubterm() == null)
            return 1;
        if (getEntity().getSubterm() == null && o.getEntity().getSubterm() != null)
            return -1;
        if (getEntity().getSubterm() != null && o.getEntity().getSubterm() != null)
            return getEntity().getSubterm().getTermName().compareToIgnoreCase(o.getEntity().getSubterm().getTermName());
        return qualityTerm.getTermName().compareToIgnoreCase(o.getQualityTerm().getTermName());

    }

    public PhenotypeStatement getPhenotypeStatement() {
        PhenotypeStatement statement = new PhenotypeStatement();
        statement.setEntity(getEntity());
        statement.setRelatedEntity(getRelatedEntity());
        statement.setTag(tag.toString());
        statement.setQuality(qualityTerm);
        return statement;
    }

    public boolean useForAnnotations() {
        GenericTerm superterm = getEntity().getSuperterm();
        if (!superterm.useForAnnotations())
            return false;
        if (!getEntity().getSubterm().useForAnnotations())
            return false;
        if (getRelatedEntity() != null) {
            if (!getRelatedEntity().getSubterm().useForAnnotations())
                return false;
            if (!getRelatedEntity().getSuperterm().useForAnnotations())
                return false;
        }
        return true;
    }

}
