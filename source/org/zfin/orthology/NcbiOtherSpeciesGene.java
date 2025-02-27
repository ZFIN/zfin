package org.zfin.orthology;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Immutable;
import org.zfin.Species;

import java.io.Serializable;
import java.util.Set;
import jakarta.persistence.*;


/**
 * External ortholog, i.e. the non-zebrafish ortholog found at NCBI
 */
@Setter
@Getter
@Entity
@Table(name = "ncbi_ortholog")
@Immutable
public class NcbiOtherSpeciesGene implements Serializable {

    @Id
    @Column(name = "noi_ncbi_gene_id", nullable = false)
    private String ID;

    @Column(name = "noi_name")
    private String name;

    @Column(name = "noi_symbol")
    private String abbreviation;

    @Column(name = "noi_chromosome")
    private String chromosome;

    @ManyToOne
    @JoinColumn(name = "noi_taxid")
    private Species organism;

    @OneToMany(mappedBy = "ncbiOtherSpeciesGene", fetch = FetchType.EAGER)
    @OrderBy
    private Set<NcbiOrthoExternalReference> ncbiExternalReferenceList;

}