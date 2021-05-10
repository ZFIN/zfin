package org.zfin.datatransfer.go;

import java.util.ArrayList;
import java.util.List;

public class GafAnnotationGroup {
    List<GafAnnotationExtension> annotationExtensions;

    public List<GafAnnotationExtension> getAnnotationExtensions() {
        return annotationExtensions;
    }

    public void setAnnotationExtensions(List<GafAnnotationExtension> annotationExtensions) {
        this.annotationExtensions = annotationExtensions;
    }

    public void addAnnotationExtendsion(GafAnnotationExtension extension) {
        if (annotationExtensions == null)
            annotationExtensions = new ArrayList<>();
        annotationExtensions.add(extension);
    }
}
