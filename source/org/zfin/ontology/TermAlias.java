package org.zfin.ontology;

import org.zfin.infrastructure.DataAlias;

/**
 * Synonyms of term names.
 */
public class TermAlias extends DataAlias {

    private GenericTerm term;

    public GenericTerm getTerm() {
        return term;
    }

    public void setTerm(GenericTerm term) {
        this.term = term;
    }
}
