package org.zfin.marker.presentation;

import org.zfin.marker.Marker;

/**
 */
public class ProteinAddBean {

    private Marker marker;
    private String accession;

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
}