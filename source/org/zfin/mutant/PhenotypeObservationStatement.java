package org.zfin.mutant;

import com.fasterxml.jackson.annotation.JsonView;
import lombok.Getter;
import lombok.Setter;
import org.zfin.framework.api.View;
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

    @JsonView(View.API.class)
    @ManyToOne
    @JoinColumn(name = "psg_pg_id")
    private PhenotypeSourceGenerated phenotypeSourceGenerated;

    @JsonView(View.API.class)
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

    @Column(name = "psg_tag")
    private String tag;

    @Override
    public int compareTo(PhenotypeObservationStatement o) {
        return 0;
    }

    @JsonView(View.API.class)
    public PhenotypeStatement getPhenotypeStatement() {
        PhenotypeStatement statement = new PhenotypeStatement();
        statement.setQuality(quality);
        PostComposedEntity entity = new PostComposedEntity();
        entity.setSuperterm(superTermE1);
        entity.setSubterm(subtermE1);
        statement.setEntity(entity);

        PostComposedEntity relatedEntity = new PostComposedEntity();
        relatedEntity.setSuperterm(superTermE2);
        relatedEntity.setSubterm(subtermE2);
        statement.setEntity(relatedEntity);

        statement.setTag(tag);
        return statement;
    }
}
