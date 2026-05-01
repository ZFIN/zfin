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

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * One mutation submitted as part of a {@link LineSubmission}. Owns the per-mutation
 * children: genes, lesions, genotyping assays, phenotypes.
 */
@Entity(name = "ZircMutation")
@Table(schema = "zirc", name = "mutation")
@Getter
@Setter
public class Mutation implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "m_id", updatable = false, nullable = false)
    private Long id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "m_line_submission_id", referencedColumnName = "ls_id", nullable = false)
    private LineSubmission lineSubmission;

    @Column(name = "m_sort_order", nullable = false)
    private Integer sortOrder;

    @Column(name = "m_allele_designation")
    private String alleleDesignation;

    @Column(name = "m_mutagenesis_protocol")
    private String mutagenesisProtocol;

    @Column(name = "m_molecularly_characterized")
    private Boolean molecularlyCharacterized;

    @Column(name = "m_mutation_type")
    private String mutationType;

    @Column(name = "m_homozygous_lethal")
    private Boolean homozygousLethal;

    @Column(name = "m_lethality_stage_typical")
    private String lethalityStageTypical;

    @Column(name = "m_lethality_specific_timepoint")
    private String lethalitySpecificTimepoint;

    @Column(name = "m_lethality_window_start")
    private String lethalityWindowStart;

    @Column(name = "m_lethality_window_end")
    private String lethalityWindowEnd;

    @Column(name = "m_lethality_additional_info")
    private String lethalityAdditionalInfo;

    @Column(name = "m_zfin_record_established")
    private Boolean zfinRecordEstablished;

    @Column(name = "m_cell_genomic_feature")
    private String cellGenomicFeature;

    @Column(name = "m_mutation_discoverer")
    private String mutationDiscoverer;

    @Column(name = "m_mutation_institution")
    private String mutationInstitution;

    @OneToMany(mappedBy = "mutation",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY)
    @OrderBy("sortOrder")
    private Set<Gene> genes = new HashSet<>();

    @OneToMany(mappedBy = "mutation",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY)
    @OrderBy("sortOrder")
    private Set<Lesion> lesions = new HashSet<>();

    @OneToMany(mappedBy = "mutation",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY)
    @OrderBy("sortOrder")
    private Set<GenotypingAssay> genotypingAssays = new HashSet<>();

    @OneToMany(mappedBy = "mutation",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY)
    @OrderBy("sortOrder")
    private Set<Phenotype> phenotypes = new HashSet<>();

}
