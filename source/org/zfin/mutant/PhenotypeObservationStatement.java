package org.zfin.mutant;

import lombok.Getter;
import lombok.Setter;
import org.zfin.marker.Marker;
import org.zfin.ontology.GenericTerm;
import org.zfin.ontology.PostComposedEntity;

import javax.persistence.*;

/**
 * An individual observation of phenotype
 */
@Entity
@Table(name = "phenotype_observation_generated")
@Setter
@Getter
public class PhenotypeObservationStatement implements Comparable<PhenotypeObservationStatement> {


    @Id
    @Column(name = "psg_id")
    private long id;

    @ManyToOne
    @JoinColumn(name = "psg_mrkr_zdb_id")
    private Marker gene;


/*
    @AttributeOverride(name = "active", column = @Column(name = "home_address_active"))
    @AssociationOverride(name = "lines", joinTable = @JoinTable(name = "Person_HomeAddress_Line"))
*/

    @ManyToOne
    @JoinColumn(name = "psg_e1a_zdb_id")
    private GenericTerm superTermE1;

    @ManyToOne
    @JoinColumn(name = "psg_e1b_zdb_id")
    private GenericTerm subtermE1;

    @ManyToOne
    @JoinColumn(name = "psg_quality_zdb_id")
    private GenericTerm quality;

    @ManyToOne
    @JoinColumn(name = "psg_e2a_zdb_id")
    private GenericTerm superTermE2;

    @ManyToOne
    @JoinColumn(name = "psg_e2b_zdb_id")
    private GenericTerm subtermE2;

    /*
    private PhenotypeExperiment phenotypeExperiment;
        private PostComposedEntity entity;
        private GenericTerm quality;
        private PostComposedEntity relatedEntity;
    private Date dateCreated;
    */
    @Column(name = "psg_tag")
    private String tag;

    @Override
    public int compareTo(PhenotypeObservationStatement o) {
        return 0;
    }
}
