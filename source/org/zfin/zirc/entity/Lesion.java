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
 * Per-mutation molecular lesion description (deletion / insertion specifics).
 */
@Entity(name = "ZircLesion")
@Table(schema = "zirc", name = "lesion")
@Getter
@Setter
public class Lesion implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "l_id", updatable = false, nullable = false)
    private Long id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "l_mutation_id", referencedColumnName = "m_id", nullable = false)
    private Mutation mutation;

    @Column(name = "l_sort_order", nullable = false)
    private Integer sortOrder;

    @Column(name = "l_lesion_type")
    private String lesionType;

    @Column(name = "l_lesion_size_bp")
    private Integer lesionSizeBp;

    @Column(name = "l_nucleotide_change")
    private String nucleotideChange;

    @Column(name = "l_deleted_sequence")
    private String deletedSequence;

    @Column(name = "l_inserted_sequence")
    private String insertedSequence;

    @Column(name = "l_transgene_sequence")
    private String transgeneSequence;

    @Column(name = "l_location_inline")
    private String locationInline;

    @Column(name = "l_5prime_flank")
    private String fivePrimeFlank;

    @Column(name = "l_3prime_flank")
    private String threePrimeFlank;

    @Column(name = "l_has_large_variant")
    private Boolean hasLargeVariant;

    @Column(name = "l_mutated_amino_acids")
    private String mutatedAminoAcids;

    @Column(name = "l_mutated_amino_acids_hgvs")
    private String mutatedAminoAcidsHgvs;

    @Column(name = "l_additional_info")
    private String additionalInfo;

}
