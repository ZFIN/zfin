package org.zfin.marker.presentation;

import org.zfin.sequence.MarkerDBLinkList;
import org.zfin.marker.MarkerRelationship;
import org.zfin.marker.Clone;
import org.zfin.marker.Marker;

import java.util.List;

/**
 */
public class ProteinAddBean {

    private Marker marker ;
    private String accession ;

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