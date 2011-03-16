package org.zfin.ontology;

/**
 * ToDo: ADD DOCUMENTATION!
 */
public class GenericTermRelationship extends AbstractTermRelationship{

    private GenericTerm termOne;
    private GenericTerm termTwo;

    public GenericTerm getTermOne() {
        return termOne;
    }

    public void setTermOne(GenericTerm termOne) {
        this.termOne = termOne;
    }

    public GenericTerm getTermTwo() {
        return termTwo;
    }

    public void setTermTwo(GenericTerm termTwo) {
        this.termTwo = termTwo;
    }


}
