package org.zfin.ontology;

import org.zfin.sequence.ForeignDB;

/**
 * Term definition reference.
 */
public class TermDefinitionReference {

    private long ID;
    private Term term;
    private ForeignDB foreignDB;
    private String reference;

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

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public Term getTerm() {
        return term;
    }

    public void setTerm(Term term) {
        this.term = term;
    }
}
