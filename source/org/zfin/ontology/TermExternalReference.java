package org.zfin.ontology;

import org.zfin.mutant.OmimPhenotype;
import org.zfin.sequence.ForeignDB;

import javax.persistence.*;
import java.util.Set;

/**
 * Term definition reference.
 */
@Entity
@Table(name = "term_xref")
public class TermExternalReference implements Comparable {

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
    @JoinTable(name = "omimp_termxref_mapping", joinColumns = {
            @JoinColumn(name = "otm_tx_id", nullable = false, updatable = false)},
            inverseJoinColumns = {@JoinColumn(name = "otm_omimp_id",
                    nullable = false, updatable = false)})
    private Set<OmimPhenotype> omimPhenotypes;


    public String getFullAccession() {
        return fullAccession;
    }

    public void setFullAccession(String fullAccession) {
        this.fullAccession = fullAccession;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public String getAccessionNumber() {
        return accessionNumber;
    }

    public void setAccessionNumber(String accessionNumber) {
        this.accessionNumber = accessionNumber;
    }

    public long getID() {
        return ID;
    }

    public void setID(long ID) {
        this.ID = ID;
    }

    public ForeignDB getForeignDB() {
        return foreignDB;
    }

    public void setForeignDB(ForeignDB foreignDB) {
        this.foreignDB = foreignDB;
    }

    public String getXrefUrl() {
        if (foreignDB == null)
            return null;
        return foreignDB.getDbUrlPrefix() + accessionNumber;
    }

    public GenericTerm getTerm() {
        return term;
    }

    public void setTerm(GenericTerm term) {
        this.term = term;
    }

    public int compareTo(Object otherTermExRef) {
        return fullAccession.compareTo(((TermExternalReference) otherTermExRef).getFullAccession());
    }


    public Set<OmimPhenotype> getOmimPhenotypes() {
        return omimPhenotypes;
    }

    public void setOmimPhenotypes(Set<OmimPhenotype> omimPhenotypes) {
        this.omimPhenotypes = omimPhenotypes;
    }
}
