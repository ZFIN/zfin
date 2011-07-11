package org.zfin.marker.presentation;

import org.zfin.marker.Marker;

/**
 */
public class GeneProductsBean {
    private Marker marker ;
    private String accession ;
    private String comment;

    public Marker getMarker() {
        return marker;
    }

    public void setMarker(Marker marker) {
        this.marker = marker;
    }

    public String getAccession() {
        return accession;
    }

    public void setAccession(String accession) {
        this.accession = accession;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}
