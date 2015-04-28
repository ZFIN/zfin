package org.zfin.ontology;

import org.zfin.mutant.OmimPhenotype;
import org.zfin.sequence.ForeignDB;

import java.util.Set;

/**
 * Term definition reference.
 */
public class TermExternalReference implements Comparable {

    private long ID;

    public String getFullAccession() {
        return fullAccession;
    }

    public void setFullAccession(String fullAccession) {
        this.fullAccession = fullAccession;
    }

    public String getXrefPrefix() {
        return xrefPrefix;
    }

    public void setXrefPrefix(String xrefPrefix) {
        this.xrefPrefix = xrefPrefix;
    }

    public String getXrefAccessionNumber() {
        return xrefAccessionNumber;
    }

    public void setXrefAccessionNumber(String xrefAccessionNumber) {
        this.xrefAccessionNumber = xrefAccessionNumber;
    }

    private Term term;
    private ForeignDB foreignDB;
    private String fullAccession;
    private String xrefPrefix;
    private String xrefAccessionNumber;
    private Set<OmimPhenotype> omimPhenotypes;

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
        return foreignDB.getDbUrlPrefix() + xrefAccessionNumber;
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
