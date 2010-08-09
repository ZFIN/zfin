package org.zfin.ontology;

import java.io.Serializable;

/**
 * This class is purely a helper class to store the relationships of terms from the database
 * without mapping the related terms to their GenericTerm objects as such an object would
 * not be serializable due to the highly relatedness of objects (stackOverflowException).
 * This class only used to be serialized and upon deserialization to populate the GenericTerm object.
 */
public class TermRelationshipHelper extends TermRelationship implements Serializable {

    private String termOneID;
    private String termTwoID;

    public String getTermOneID() {
        return termOneID;
    }

    public void setTermOneID(String termOneID) {
        this.termOneID = termOneID;
    }

    public String getTermTwoID() {
        return termTwoID;
    }

    public void setTermTwoID(String termTwoID) {
        this.termTwoID = termTwoID;
    }
}