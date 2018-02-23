package org.zfin.mutant;

import java.io.Serializable;

/**
 */
public class MarkerGoTermAnnotationExtn implements Serializable {
private  Long id;
    private String markerGoTermEvidenceZdbID;
    private String relationshipTerm;
private Long annotExtnGroupID;

    public String getMarkerGoTermEvidenceZdbID() {
        return markerGoTermEvidenceZdbID;
    }

    public void setMarkerGoTermEvidenceZdbID(String markerGoTermEvidenceZdbID) {
        this.markerGoTermEvidenceZdbID = markerGoTermEvidenceZdbID;
    }

    public Long getAnnotExtnGroupID() {
        return annotExtnGroupID;
    }

    public void setAnnotExtnGroupID(Long annotExtnGroupID) {
        this.annotExtnGroupID = annotExtnGroupID;
    }

    public Long getId() {
        return id;

    }

    public void setId(Long id) {
        this.id = id;
    }

    private String identifierTerm;

    public String getRelationshipTerm() {
        return relationshipTerm;
    }

    public void setRelationshipTerm(String relationshipTerm) {
        this.relationshipTerm = relationshipTerm;
    }

    public String getIdentifierTerm() {
        return identifierTerm;
    }

    public void setIdentifierTerm(String identifierTerm) {
        this.identifierTerm = identifierTerm;
    }

}
