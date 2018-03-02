package org.zfin.mutant;

import java.io.Serializable;
import java.util.Set;

/**
 */
public class MarkerGoTermAnnotationExtn implements Serializable {
    private Long id;
    private String relationshipTerm;
    private MarkerGoTermAnnotationExtnGroup annotExtnGroupID;
    private String identifierTermText;
    private String annotExtnDBLink;





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

    public MarkerGoTermAnnotationExtnGroup getAnnotExtnGroupID() {
        return annotExtnGroupID;
    }

    public void setAnnotExtnGroupID(MarkerGoTermAnnotationExtnGroup annotExtnGroupID) {
        this.annotExtnGroupID = annotExtnGroupID;
    }
}
