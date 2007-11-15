package org.zfin.anatomy.repository;

/**
 * This is a convenience class only used for data retrieval. 
 */
public class AnatomyRelationshipTypePersistence {

    private String typeID;
    private String relationOneToTwo;
    private String relationTwoToOne;

    public String getTypeID() {
        return typeID;
    }

    public void setTypeID(String typeID) {
        this.typeID = typeID;
    }

    public String getRelationOneToTwo() {
        return relationOneToTwo;
    }

    public void setRelationOneToTwo(String relationOneToTwo) {
        this.relationOneToTwo = relationOneToTwo;
    }

    public String getRelationTwoToOne() {
        return relationTwoToOne;
    }

    public void setRelationTwoToOne(String relationTwoToOne) {
        this.relationTwoToOne = relationTwoToOne;
    }


    public String toString() {
        return "AnatomyRelationshipType{" +
                "typeID='" + typeID + '\'' +
                ", relationOneToTwo='" + relationOneToTwo + '\'' +
                ", relationTwoToOne='" + relationTwoToOne + '\'' +
                '}';
    }
}
