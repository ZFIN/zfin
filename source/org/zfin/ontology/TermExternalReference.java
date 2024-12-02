package org.zfin.ontology;

import lombok.Getter;
import lombok.Setter;
import org.zfin.mutant.OmimPhenotype;
import org.zfin.sequence.ForeignDB;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Set;

/**
 * Term definition reference.
 */
@Setter
@Getter
@Entity
@Table(name = "term_xref")
public class TermExternalReference implements Comparable, Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "tx_pk_id")
    private long ID;

    @ManyToOne
    @JoinColumn(name = "tx_term_zdb_id")
    private GenericTerm term;

    @ManyToOne
    @JoinColumn(name = "tx_fdb_db_id")
    private ForeignDB foreignDB;

    @Column(name = "tx_full_accession")
    private String fullAccession;

    @Column(name = "tx_prefix")
    private String prefix;

    @Column(name = "tx_accession")
    private String accessionNumber;

    @ManyToMany()
    @JoinTable(name = "omimp_termxref_mapping",
        joinColumns = {@JoinColumn(name = "otm_tx_id", nullable = false, updatable = false, insertable = false)},
        inverseJoinColumns = {@JoinColumn(name = "otm_omimp_id", nullable = false, updatable = false, insertable = false)})
    private Set<OmimPhenotype> omimPhenotypes;

    public String getXrefUrl() {
        if (foreignDB == null)
            return null;
        return foreignDB.getDbUrlPrefix() + accessionNumber;
    }

    public int compareTo(Object otherTermExRef) {
        return fullAccession.compareTo(((TermExternalReference) otherTermExRef).getFullAccession());
    }
}
