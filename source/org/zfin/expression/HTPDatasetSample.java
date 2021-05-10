package org.zfin.expression;

import lombok.Getter;
import lombok.Setter;
import javax.persistence.*;
import org.zfin.mutant.Fish;
import org.zfin.ontology.GenericTerm;

/**
 * High throughput meta datasample object
 */
@Entity
@Table(name = "htp_dataset_sample")
@Setter
@Getter
public class HTPDatasetSample {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "hds_pk_id")
    private long ID;

    @Column(name = "hds_sample_id")
    private String sampleId;
    @Column(name = "hds_sample_title")
    private String sampleTitle;
    @Column(name = "hds_sample_type")
    private String sampleType;
    @Column(name = "hds_assay_type")
    private String assayType;
    @Column(name = "hds_sex")
    private String sex;
    @Column(name = "hds_sequencing_format")
    private String sequencingFormat;
    @Column(name = "hds_abundance")
    private String abundance;
    @Column(name = "hds_assembly")
    private String assembly;
    @Column(name = "hds_notes")
    private String notes;

    @ManyToOne
    @JoinColumn(name = "hds_hd_zdb_id")
    private HTPDataset htpDataset;

    @ManyToOne
    @JoinColumn(name = "hds_fish_Zdb_id")
    private Fish fish;

    @ManyToOne
    @JoinColumn(name = "hds_stage_term_zdb_id")
    private GenericTerm stage;
}
