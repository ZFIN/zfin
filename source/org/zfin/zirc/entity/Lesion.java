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
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.io.Serializable;

/**
 * Per-mutation molecular lesion description (deletion / insertion specifics).
 */
@Entity(name = "ZircLesion")
@Table(schema = "zirc", name = "lesion")
// Same rationale as Mutation / GenotypingAssay / Gene — without
// @DynamicUpdate two near-simultaneous field-path PATCHes against this
// row would clobber each other's untouched columns on commit.
@DynamicUpdate
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

    @Column(name = "l_insertion_size_bp")
    private Integer insertionSizeBp;

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

    // ZFIN-10400 — where an insertion came from. Nullable three-state:
    // null is "unanswered", which the status computer treats differently
    // from an explicit false.
    @Column(name = "l_insertion_from_mutagenesis")
    private Boolean insertionFromMutagenesis;

    @Column(name = "l_insertion_from_construct")
    private Boolean insertionFromConstruct;

    @Column(name = "l_crispr_sequence")
    private String crisprSequence;

    @Column(name = "l_talen_sequence")
    private String talenSequence;

    @Column(name = "l_construct_name")
    private String constructName;

    /**
     * Legacy free-text amino-acid box, replaced by the from/to/position
     * columns below in ZFIN-10379. Retained but no longer read by the form,
     * the same treatment locationInline has.
     */
    @Column(name = "l_mutated_amino_acids")
    private String mutatedAminoAcids;

    // ZFIN-10379 — structured amino-acid change. from/to are amino_acid_term
    // ZDB IDs; end is null for a single-residue change.
    @Column(name = "l_aa_change_from")
    private String aaChangeFrom;

    @Column(name = "l_aa_change_to")
    private String aaChangeTo;

    @Column(name = "l_aa_position_start")
    private Integer aaPositionStart;

    @Column(name = "l_aa_position_end")
    private Integer aaPositionEnd;

    @Column(name = "l_mutated_amino_acids_hgvs")
    private String mutatedAminoAcidsHgvs;

    /**
     * Controlled-vocabulary term ZDB IDs from
     * {@code transcript_consequence_term} (ZFIN-10399). Ids rather than
     * display names so a term rename cannot orphan stored data; see
     * {@code ZircVocabularyService}. Empty array, never null, matching
     * {@code LineSubmission.previousNames}.
     */
    @Column(name = "l_transcript_consequences", columnDefinition = "text[]", nullable = false)
    @JdbcTypeCode(SqlTypes.ARRAY)
    private String[] transcriptConsequences = new String[0];

    /**
     * Controlled-vocabulary term ZDB IDs from
     * {@code protein_consequence_term} (ZFIN-10380). Same storage contract as
     * {@link #transcriptConsequences}.
     */
    @Column(name = "l_protein_consequences", columnDefinition = "text[]", nullable = false)
    @JdbcTypeCode(SqlTypes.ARRAY)
    private String[] proteinConsequences = new String[0];

    @Column(name = "l_additional_info")
    private String additionalInfo;

}
