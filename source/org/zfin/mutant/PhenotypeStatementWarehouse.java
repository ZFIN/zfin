package org.zfin.mutant;

import org.zfin.marker.Marker;
import org.zfin.ontology.GenericTerm;
import org.zfin.ontology.PostComposedEntity;

import javax.persistence.*;

/**
 * Main Experiment object that contains expression annotations.
 */
@Entity
@Table(name = "PHENOTYPE_OBSERVATION_GENERATED")
public class PhenotypeStatementWarehouse implements Comparable<PhenotypeStatementWarehouse> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "psg_id")
    private long id;
    @Column(name = "psg_mrkr_relation")
    private String markerRelationship;
    @Column(name = "psg_short_name")
    private String shortName;
    @ManyToOne()
    @JoinColumn(name = "psg_pg_id")
    private PhenotypeWarehouse phenotypeWarehouse;
    @ManyToOne()
    @JoinColumn(name = "psg_mrkr_zdb_id")
    private Marker gene;
    @ManyToOne()
    @JoinColumn(name = "psg_e1a_zdb_id")
    private GenericTerm e1a;
    @ManyToOne()
    @JoinColumn(name = "psg_e1b_zdb_id")
    private GenericTerm e1b;
    @ManyToOne()
    @JoinColumn(name = "psg_e2a_zdb_id")
    private GenericTerm e2a;
    @ManyToOne()
    @JoinColumn(name = "psg_e2b_zdb_id")
    private GenericTerm e2b;
    @Column(name = "psg_tag")
    private String tag;
    @ManyToOne()
    @JoinColumn(name = "psg_quality_zdb_id")
    private GenericTerm quality;
    @Column(name = "psg_pre_eap_phenotype")
    private boolean isPreEap;

    public Marker getGene() {
        return gene;
    }

    public long getId() {
        return id;
    }

    public PhenotypeWarehouse getPhenotypeWarehouse() {
        return phenotypeWarehouse;
    }

    //this is a convenience method, for example to get to Fish from PhenotypeStatement, this method is often called
    public PhenotypeWarehouse getPhenotypeExperiment() {
        return phenotypeWarehouse;
    }

    public GenericTerm getE1a() {
        return e1a;
    }

    public GenericTerm getE1b() {
        return e1b;
    }

    public GenericTerm getE2a() {
        return e2a;
    }

    public GenericTerm getE2b() {
        return e2b;
    }

    public String getShortName() {
        return shortName;
    }

    public String getPhenoStatementString() {
        return getShortName();
    }

    public String getMarkerRelationship() {
        return markerRelationship;
    }

    public void setMarkerRelationship(String markerRelationship) {
        this.markerRelationship = markerRelationship;
    }

    @Override
    public String toString() {
        String message = "";
        message += getDisplayNameWithoutTag();
        if (gene != null) {
            message += ": ";
            message += gene.getAbbreviation();
        }
        return message;
    }

    @Transient
    private PostComposedEntity entity;
    @Transient
    private PostComposedEntity relatedEntity;

    public PostComposedEntity getEntity() {
        if (entity == null) {
            entity = new PostComposedEntity();
            entity.setSuperterm(e1a);
            if (e1b != null) {
                entity.setSubterm(e1b);
            }
        }
        return entity;
    }

    public GenericTerm getQuality() {
        return quality;
    }

    public PostComposedEntity getRelatedEntity() {
        if (relatedEntity == null) {
            // only if there is an E2a
            if (e2a != null) {
                relatedEntity = new PostComposedEntity();
                relatedEntity.setSuperterm(e2a);
                if (e2b != null) {
                    relatedEntity.setSubterm(e2b);
                }
            }
        }
        return relatedEntity;
    }

    public String getTag() {
        return tag;
    }

    public boolean isMorphologicalPhenotype() {
        return gene == null;
    }

    public boolean isNormal() {
        return tag.equals(PhenotypeStatement.Tag.NORMAL.toString());
    }

    public boolean isNotNormal() {
        return !isNormal();
    }

    public boolean isPreEap() {
        return isPreEap;
    }

    public void setIsPreEap(boolean isPreEap) {
        this.isPreEap = isPreEap;
    }

    public boolean equalsByPhenotype(PhenotypeStatementWarehouse statement) {
        return false;
    }

    public String getDisplayNameWithoutTag() {
        StringBuilder builder = new StringBuilder();
        builder.append(getEntity());
        builder.append(" - ");
        builder.append(quality.getTermName());
        if (getRelatedEntity() != null) {
            builder.append(" - ");
            builder.append(getRelatedEntity());
        }
        return builder.toString();

    }

    @Override
    public int compareTo(PhenotypeStatementWarehouse phenotypeObserved) {
        if (phenotypeObserved == null) {
            return -1;
        }
        return getDisplayNameWithoutTag().compareToIgnoreCase(phenotypeObserved.getDisplayNameWithoutTag());

    }

    public boolean equalsByName(PhenotypeStatementWarehouse phenotypeStatement) {
        if (getEntity() != null ? !getEntity().equals(phenotypeStatement.getEntity()) : phenotypeStatement.getEntity() != null) {
            return false;
        }
        if (quality != null ? !quality.equals(phenotypeStatement.getQuality()) : phenotypeStatement.getQuality() != null) {
            return false;
        }
        if (getRelatedEntity() != null ? !getRelatedEntity().equals(phenotypeStatement.getRelatedEntity()) : phenotypeStatement.getRelatedEntity() != null) {
            return false;
        }
        if (!tag.equals(phenotypeStatement.getTag())) {
            return false;
        }

        return true;
    }


}

