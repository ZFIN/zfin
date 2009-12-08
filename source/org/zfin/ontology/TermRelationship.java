package org.zfin.ontology;

/**
 * ToDo: ADD DOCUMENTATION!
 */
public class TermRelationship {

    private String ID;
    private GenericTerm termOne;
    private GenericTerm termTwo;
    private String type;
    private RelationshipType relationshipType;

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public RelationshipType getRelationshipType() {
        return relationshipType;
    }

    public void setRelationshipType(RelationshipType relationshipType) {
        this.relationshipType = relationshipType;
    }

    /**
     * Retrieve the term that is related to the term provided.
     * Return null if the term passed in is null or if the term passed in
     * is not one of the two intrinsic terms.
     *
     * @param term Generic Term
     * @return GenericTerm
     */
    public GenericTerm getRelatedTerm(GenericTerm term) {
        if (term == null)
            return null;
        if (!(term.equals(termOne) || term.equals(termTwo)))
            return null;

        if (term.equals(termOne))
            return termTwo;
        else
            return termOne;
    }
}
