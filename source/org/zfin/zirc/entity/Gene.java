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
import org.zfin.marker.Marker;

import java.io.Serializable;

/**
 * Per-mutation gene record. {@code g_mutated_gene} is FK'd to {@link Marker} but is
 * left optional since submitters may not know the ZDB ID at submission time.
 */
@Entity(name = "ZircGene")
@Table(schema = "zirc", name = "gene")
@Getter
@Setter
public class Gene implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "g_id", updatable = false, nullable = false)
    private Long id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "g_mutation_id", referencedColumnName = "m_id", nullable = false)
    private Mutation mutation;

    @Column(name = "g_sort_order", nullable = false)
    private Integer sortOrder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "g_mutated_gene", referencedColumnName = "mrkr_zdb_id")
    private Marker mutatedGene;

    @Column(name = "g_linkage_group")
    private String linkageGroup;

    @Column(name = "g_genbank_genomic_dna")
    private String genbankGenomicDna;

    @Column(name = "g_genbank_cdna")
    private String genbankCdna;

}
