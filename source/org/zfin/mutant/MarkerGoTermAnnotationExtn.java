package org.zfin.mutant;

/**
 */
public class MarkerGoTermAnnotationExtn {
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MarkerGoTermAnnotationExtn that = (MarkerGoTermAnnotationExtn) o;

        if (!relationshipTerm.equals(that.relationshipTerm)) return false;
        if (!identifierTermText.equals(that.identifierTermText)) return false;
        if (annotExtnDBLink != null ? !annotExtnDBLink.equals(that.annotExtnDBLink) : that.annotExtnDBLink != null)
            return false;
        return identifierTerm != null ? identifierTerm.equals(that.identifierTerm) : that.identifierTerm == null;
    }

    @Override
    public int hashCode() {
        int result = relationshipTerm.hashCode();
        result = 31 * result + identifierTermText.hashCode();
        result = 31 * result + (annotExtnDBLink != null ? annotExtnDBLink.hashCode() : 0);
        result = 31 * result + (identifierTerm != null ? identifierTerm.hashCode() : 0);
        return result;
    }
}
