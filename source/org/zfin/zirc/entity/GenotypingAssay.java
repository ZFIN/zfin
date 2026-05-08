package org.zfin.zirc.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * Per-mutation PCR / RFLP assay parameters used to genotype the line.
 */
@Entity(name = "ZircGenotypingAssay")
@Table(schema = "zirc", name = "genotyping_assay")
@Getter
@Setter
public class GenotypingAssay implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ga_id", updatable = false, nullable = false)
    private Long id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ga_mutation_id", referencedColumnName = "m_id", nullable = false)
    private Mutation mutation;

    @Column(name = "ga_sort_order", nullable = false)
    private Integer sortOrder;

    @Column(name = "ga_assay_type")
    private String assayType;

    @Column(name = "ga_forward_primer")
    private String forwardPrimer;

    @Column(name = "ga_reverse_primer")
    private String reversePrimer;

    @Column(name = "ga_expected_wt_pcr")
    private String expectedWtPcr;

    @Column(name = "ga_expected_mut_pcr")
    private String expectedMutPcr;

    @Column(name = "ga_restriction_enzyme")
    private String restrictionEnzyme;

    @Column(name = "ga_enzyme_cleaves")
    private String enzymeCleaves;

    @Column(name = "ga_expected_wt_digest")
    private String expectedWtDigest;

    @Column(name = "ga_expected_mut_digest")
    private String expectedMutDigest;

    @Column(name = "ga_additional_info")
    private String additionalInfo;

}
