package org.zfin.ontology;

import org.zfin.mutant.OmimPhenotype;
import org.zfin.sequence.ForeignDB;

import java.util.Set;

/**
 * Term definition reference.
 */
public class TermExternalReference implements Comparable {

    private long ID;
    private Term term;
    private ForeignDB foreignDB;
    private String fullAccession;
    private String prefix;
    private String accessionNumber;
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
        if(foreignDB == null)
            return null;
        return foreignDB.getDbUrlPrefix() + accessionNumber;
    }

    public Term getTerm() {
        return term;
    }

    public void setTerm(Term term) {
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
