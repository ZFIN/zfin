package org.zfin.orthology;

import org.zfin.marker.Marker;

/**
 */
public class OrthoEvidenceDisplay {

    private String zdbID ;
    private Marker gene ;
    private EvidenceCode evidenceCode;
    private String organismList;

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

    public EvidenceCode getEvidenceCode() {
        return evidenceCode;
    }

    public void setEvidenceCode(EvidenceCode evidenceCode) {
        this.evidenceCode = evidenceCode;
    }

    public String getOrganismList() {
        return organismList;
    }

    public void setOrganismList(String organismList) {
        this.organismList = organismList;
    }
}
