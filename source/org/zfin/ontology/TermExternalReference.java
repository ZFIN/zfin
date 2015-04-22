package org.zfin.ontology;

import org.zfin.sequence.ForeignDB;

/**
 * Term definition reference.
 */
public class TermExternalReference implements Comparable{

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



    public Term getTerm() {
        return term;
    }

    public void setTerm(Term term) {
        this.term = term;
    }

    public int compareTo(Object otherTermExRef) {
        return fullAccession.compareTo(((TermExternalReference) otherTermExRef).getFullAccession());
    }


}
