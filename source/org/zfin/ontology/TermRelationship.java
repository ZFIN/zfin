package org.zfin.ontology;

import java.io.Serializable;

/**
 * ToDo: ADD DOCUMENTATION!
 */
public class TermRelationship implements Serializable {

    private String zdbId;
    private Term termOne;
    private Term termTwo;
    private String type;

    // this is unmapped helper class
    private RelationshipType relationshipType;

    public String getZdbId() {
        return zdbId;
    }

    public void setZdbId(String zdbId) {
        this.zdbId = zdbId;
    }

    public Term getTermOne() {
        return termOne;
    }

    public void setTermOne(Term termOne) {
        this.termOne = termOne;
    }

    public Term getTermTwo() {
        return termTwo;
    }

    public void setTermTwo(Term termTwo) {
        this.termTwo = termTwo;
    }

    public String getTermOneID() {
        return termOne == null ? null : termOne.getZdbID();
    }

    public String getTermTwoID() {
        return termTwo == null ? null : termTwo.getZdbID();
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public RelationshipType getRelationshipType() {
        return RelationshipType.getRelationshipTypeByDbName(type);
    }

    public void setRelationshipType(RelationshipType relationshipType) {
        type = relationshipType.getDbMappedName();
    }

    /**
     * Retrieve the term that is related to the term provided.
     * Return null if the term passed in is null or if the term passed in
     * is not one of the two intrinsic terms.
     *
     * @param term Generic Term
     * @return GenericTerm
     */
    public Term getRelatedTerm(Term term) {
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
