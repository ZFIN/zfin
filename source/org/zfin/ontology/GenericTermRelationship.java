package org.zfin.ontology;

import org.zfin.gwt.root.dto.RelationshipType;

/**
 * Domain object for a term-term relationship entity.
 */
public class GenericTermRelationship implements TermRelationship {

    private String zdbId;
    private String type;

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

    public String getZdbId() {
        return zdbId;
    }

    public void setZdbId(String zdbId) {
        this.zdbId = zdbId;
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
    public GenericTerm getRelatedTerm(GenericTerm term) {
        if (term == null)
            return null;
        if (!(term.equals(getTermOne()) || term.equals(getTermTwo())))
            return null;

        if (term.equals(getTermOne()))
            return getTermTwo();
        else
            return getTermOne();
    }

    @Override
    public int compareTo(TermRelationship o) {
        if (!type.equalsIgnoreCase(o.getType()))
            return type.compareTo(o.getType());
        if (!termOne.equals(o.getTermOne()))
            return termOne.compareTo(o.getTermOne());
        return termTwo.compareTo(o.getTermTwo());
    }

}
