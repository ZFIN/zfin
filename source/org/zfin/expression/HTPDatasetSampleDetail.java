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
@Table(name = "htp_dataset_sample_detail")
@Setter
@Getter

public class HTPDatasetSampleDetail {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "hdsd_pk_id")
    private long ID;

    @ManyToOne
    @JoinColumn(name = "hdsd_anatomy_super_term_Zdb_id")
    private GenericTerm anatomySuperTerm;
    @ManyToOne
    @JoinColumn(name = "hdsd_anatomy_sub_term_Zdb_id")
    private GenericTerm anatomySubTerm;

    @ManyToOne
    @JoinColumn(name = "hdsd_anatomy_super_term_qualifier_zdb_id")
    private GenericTerm anatomySuperQualifierTerm;
    @ManyToOne
    @JoinColumn(name = "hdsd_anatomy_sub_term_qualifier_zdb_id ")
    private GenericTerm anatomySubQualifierTerm;

    @ManyToOne
    @JoinColumn(name = "hdsd_cellular_component_term_Zdb_id")
    private GenericTerm cellularComponentTerm;
    @ManyToOne
    @JoinColumn(name = "hdsd_cellular_component_term_qualifier_Zdb_id")
    private GenericTerm cellularComponentQualifierTerm;

    @ManyToOne
    @JoinColumn(name = "hdsd_hds_id")
    private HTPDatasetSample htpDatasetSample;
}
