package org.zfin.expression;

import lombok.Getter;
import lombok.Setter;
import org.zfin.mutant.Fish;
import org.zfin.ontology.GenericTerm;

import javax.persistence.*;

/**
 * High throughput meta datasample detailing which structures at what stages were sampled.
 */
@Entity
@Table(name = "htp_dataset_sample_stage")
@Setter
@Getter

public class HTPDatasetSampleDetail {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "hdss_pk_id")
    private long ID;

    @ManyToOne
    @JoinColumn(name = "hds_stage_term_Zdb_id")
    private GenericTerm stage;

    @ManyToOne
    @JoinColumn(name = "hds_anatomy_super_term_Zdb_id")
    private GenericTerm anatomySuperTerm;
    @ManyToOne
    @JoinColumn(name = "hds_anatomy_sub_term_Zdb_id")
    private GenericTerm anatomySubTerm;

    @ManyToOne
    @JoinColumn(name = "hds_anatomy_super_qualifier_term_Zdb_id")
    private GenericTerm anatomySuperQualifierTerm;
    @ManyToOne
    @JoinColumn(name = "hds_anatomy_sub_qualifier_term_Zdb_id")
    private GenericTerm anatomySubQualifierTerm;

    @ManyToOne
    @JoinColumn(name = "hds_cellular_component_term_Zdb_id")
    private GenericTerm cellularComponentTerm;
    @ManyToOne
    @JoinColumn(name = "hds_cellular_component_qualifier_term_Zdb_id")
    private GenericTerm cellularComponentQualifierTerm;
}
