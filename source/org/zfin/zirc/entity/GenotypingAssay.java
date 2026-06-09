package org.zfin.zirc.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.DynamicUpdate;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * Per-mutation PCR / RFLP assay parameters used to genotype the line.
 */
@Entity(name = "ZircGenotypingAssay")
@Table(schema = "zirc", name = "genotyping_assay")
// Same rationale as Mutation / LineSubmission: without DynamicUpdate two
// near-simultaneous field-path PATCHes against the same row clobber each
// other's untouched columns on commit.
@DynamicUpdate
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

    @Column(name = "ga_restriction_enzyme_name")
    private String restrictionEnzymeName;

    @Column(name = "ga_restriction_enzyme_catalog")
    private String restrictionEnzymeCatalog;

    @Column(name = "ga_enzyme_cleaves_wt")
    private Boolean enzymeCleavesWt;

    @Column(name = "ga_enzyme_cleaves_mut")
    private Boolean enzymeCleavesMut;

    @Column(name = "ga_expected_wt_digest")
    private String expectedWtDigest;

    @Column(name = "ga_expected_mut_digest")
    private String expectedMutDigest;

    @Column(name = "ga_additional_info")
    private String additionalInfo;

    @Column(name = "ga_sequencing_primer")
    private String sequencingPrimer;

    @Column(name = "ga_dcaps_mismatch_primer")
    private String dcapsMismatchPrimer;

    @Column(name = "ga_wt_specific_primer")
    private String wtSpecificPrimer;

    @Column(name = "ga_mut_specific_primer")
    private String mutSpecificPrimer;

    @Column(name = "ga_common_primer")
    private String commonPrimer;

    @Column(name = "ga_kasp_genomic_sequence")
    private String kaspGenomicSequence;

    @Column(name = "ga_sslp_marker_name")
    private String sslpMarkerName;

    @Column(name = "ga_sslp_distance")
    private String sslpDistance;

    @Column(name = "ga_sslp_genomic_location")
    private String sslpGenomicLocation;

    @Column(name = "ga_sslp_induced_background")
    private String sslpInducedBackground;

    @Column(name = "ga_sslp_outcrossed_background")
    private String sslpOutcrossedBackground;

    @Column(name = "ga_sslp_induced_pcr")
    private String sslpInducedPcr;

    @Column(name = "ga_sslp_outcrossed_pcr")
    private String sslpOutcrossedPcr;

    @JsonIgnore
    @OneToMany(mappedBy = "assay",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY)
    @OrderBy("uploadedAt")
    private Set<GenotypingAssayFile> files = new HashSet<>();

}
