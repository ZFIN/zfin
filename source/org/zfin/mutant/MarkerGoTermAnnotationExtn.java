package org.zfin.mutant;

import java.io.Serializable;
import java.util.Set;

/**
 */
public class MarkerGoTermAnnotationExtn implements Serializable {
    private Long id;
    private Set<MarkerGoTermEvidence> markerGoTermEvidenceZdbID;
    private String relationshipTerm;
    private Set<MarkerGoTermAnnotationExtnGroup> annotExtnGroupID;
    private String identifierTermText;
    private String annotExtnDBLink;

    public Set<MarkerGoTermEvidence> getMarkerGoTermEvidenceZdbID() {
        return markerGoTermEvidenceZdbID;
    }

    public void setMarkerGoTermEvidenceZdbID(Set<MarkerGoTermEvidence> markerGoTermEvidenceZdbID) {
        this.markerGoTermEvidenceZdbID = markerGoTermEvidenceZdbID;
    }

    public Set<MarkerGoTermAnnotationExtnGroup> getAnnotExtnGroupID() {
        return annotExtnGroupID;
    }

    public void setAnnotExtnGroupID(Set<MarkerGoTermAnnotationExtnGroup> annotExtnGroupID) {
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

    public String getIdentifierTermText() {
        return identifierTermText;
    }

    public void setIdentifierTermText(String identifierTermText) {
        this.identifierTermText = identifierTermText;
    }

    public String getAnnotExtnDBLink() {
        return annotExtnDBLink;
    }

    public void setAnnotExtnDBLink(String annotExtnDBLink) {
        this.annotExtnDBLink = annotExtnDBLink;
    }
}
