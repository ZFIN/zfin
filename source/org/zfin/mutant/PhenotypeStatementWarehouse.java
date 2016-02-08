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
public class PhenotypeStatementWarehouse {

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
    public PhenotypeWarehouse getPhenotypeExperiment() { return phenotypeWarehouse; }

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

    public String getShortName() { return shortName; }

    public String getPhenoStatementString() { return getShortName(); }

    public String getMarkerRelationship() {
        return markerRelationship;
    }

    public void setMarkerRelationship(String markerRelationship) {
        this.markerRelationship = markerRelationship;
    }

    @Override
    public String toString() {
        String message = "";
        if (gene != null)
            message += gene.getAbbreviation();
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
            if (e1b != null)
                entity.setSubterm(e1b);
        }
        return entity;
    }

    public GenericTerm getQuality() {
        return null;
    }

    public PostComposedEntity getRelatedEntity() {
        if (relatedEntity == null) {
            // only if there is an E2a
            if (e2a != null) {
                relatedEntity = new PostComposedEntity();
                relatedEntity.setSuperterm(e2a);
                if (e2b != null)
                    relatedEntity.setSubterm(e2b);
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


}

