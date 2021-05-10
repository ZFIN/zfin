package org.zfin.datatransfer.go;

public class GafAnnotationExtension {

    public static final String RELATIONSHIP_TERM = "relationship";
    public static final String ENTITY_TERM = "entityTerm";

    private String relationshipName;
    private String annotationTermID;

    public GafAnnotationExtension(String relationshipName, String annotationTermID) {
        this.relationshipName = relationshipName;
        this.annotationTermID = annotationTermID;
    }

    public String getRelationshipName() {
        return relationshipName;
    }

    public String getAnnotationTermID() {
        return annotationTermID;
    }
}
