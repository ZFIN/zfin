package org.zfin.ontology;

/**
 * Basic implementation of the Term interface.
 */
public class ConsiderTerm {

    private long id;
    private GenericTerm obsoletedTerm;
    private GenericTerm considerTerm;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public GenericTerm getObsoletedTerm() {
        return obsoletedTerm;
    }

    public void setObsoletedTerm(GenericTerm obsoletedTerm) {
        this.obsoletedTerm = obsoletedTerm;
    }

    public GenericTerm getConsiderTerm() {
        return considerTerm;
    }

    public void setConsiderTerm(GenericTerm considerTerm) {
        this.considerTerm = considerTerm;
    }
}
