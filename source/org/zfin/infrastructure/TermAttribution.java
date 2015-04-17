package org.zfin.infrastructure;

import org.zfin.ontology.GenericTerm;

import java.io.Serializable;


/**
 */
public class TermAttribution extends PublicationAttribution implements Serializable {

    private GenericTerm term;

    public GenericTerm getTerm() {
        return term;
    }

    public void setTerm(GenericTerm term) {
        this.term = term;
        setDataZdbID(term.getZdbID());
    }

}