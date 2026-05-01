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

    @Column(name = "l_index_deletion_pos")
    private Integer indexDeletionPos;

    @Column(name = "l_index_insertion_size")
    private Integer indexInsertionSize;

    @Column(name = "l_deleted_base_pairs")
    private String deletedBasePairs;

    @Column(name = "l_inserted_base_pairs")
    private String insertedBasePairs;

    @Column(name = "l_wt_genomic_sequence")
    private String wtGenomicSequence;

    @Column(name = "l_mutated_amino_acids")
    private String mutatedAminoAcids;

    @Column(name = "l_additional_info")
    private String additionalInfo;

}
