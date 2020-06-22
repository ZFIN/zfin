package org.zfin.expression;

import lombok.Getter;
import lombok.Setter;
import org.zfin.mutant.Fish;
import org.zfin.ontology.Term;

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
    private Term stage;

    @ManyToOne
    @JoinColumn(name = "hds_anatomy_super_term_Zdb_id")
    private Term anatomySuperTerm;
    @ManyToOne
    @JoinColumn(name = "hds_anatomy_sub_term_Zdb_id")
    private Term anatomySubTerm;

    @ManyToOne
    @JoinColumn(name = "hds_anatomy_super_qualifier_term_Zdb_id")
    private Term anatomySuperQualifierTerm;
    @ManyToOne
    @JoinColumn(name = "hds_anatomy_sub_qualifier_term_Zdb_id")
    private Term anatomySubQualifierTerm;

    @ManyToOne
    @JoinColumn(name = "hds_cellular_component_term_Zdb_id")
    private Term cellularComponentTerm;
    @ManyToOne
    @JoinColumn(name = "hds_cellular_component_qualifier_term_Zdb_id")
    private Term cellularComponentQualifierTerm;
}
