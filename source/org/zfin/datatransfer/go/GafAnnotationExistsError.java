package org.zfin.datatransfer.go;

import org.zfin.mutant.MarkerGoTermEvidence;

/**
 */
public class GafAnnotationExistsError extends GafValidationError {

    private MarkerGoTermEvidence annotation;

    public GafAnnotationExistsError(GafEntry gafEntry, MarkerGoTermEvidence markerGoTermEvidence) {
        super("Annotation exists" + ":\n" + gafEntry);
        this.annotation = markerGoTermEvidence;
    }

    public GafAnnotationExistsError(String s, GafEntry gafEntry) {
        super(s + ":\n" + gafEntry);
    }

    public GafAnnotationExistsError(String s, GafEntry gafEntry, Exception e) {
        super(s, gafEntry, e);
    }

    public MarkerGoTermEvidence getAnnotation() {
        return annotation;
    }
}
