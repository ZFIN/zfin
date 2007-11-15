package org.zfin.sequence;

/**

 */
public class AccessionRelationship {

    private String zdbID ;
    private Accession accession ;
    private Accession relatedAccession;
    private AccessionRelationshipType relationshipType ;


    public String getZdbID() {
        return zdbID;
    }

    public void setZdbID(String zdbID) {
        this.zdbID = zdbID;
    }


    public Accession getAccession() {
        return accession;
    }

    public void setAccession(Accession accession) {
        this.accession = accession;
    }

    public Accession getRelatedAccession() {
        return relatedAccession;
    }

    public void setRelatedAccession(Accession relatedAccession) {
        this.relatedAccession = relatedAccession;
    }

    public AccessionRelationshipType getRelationshipType() {
        return relationshipType;
    }

    public void setRelationshipType(AccessionRelationshipType relationshipType) {
        this.relationshipType = relationshipType;
    }   
}
