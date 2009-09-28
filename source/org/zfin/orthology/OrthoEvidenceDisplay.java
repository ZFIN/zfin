package org.zfin.orthology;

import org.zfin.marker.Marker;

/**
 */
public class OrthoEvidenceDisplay {

    private String zdbID ;
    private Marker gene ;

    public String getZdbID() {
        return zdbID;
    }

    public void setZdbID(String zdbID) {
        this.zdbID = zdbID;
    }

    public Marker getGene() {
        return gene;
    }

    public void setGene(Marker gene) {
        this.gene = gene;
    }
}
