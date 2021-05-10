package org.zfin.orthology.presentation;

import java.io.Serializable;

/**
 */
public class OrthologySlimPresentation implements Serializable {
    private String organism;
    private String orthologySymbol;
    private String evidenceCode;
    private String publication;

    public String getOrganism() {
        return organism;
    }

    public void setOrganism(String organism) {
        this.organism = organism;
    }

    public String getOrthologySymbol() {
        return orthologySymbol;
    }

    public void setOrthologySymbol(String orthologySymbol) {
        this.orthologySymbol = orthologySymbol;
    }

    public String getEvidenceCode() {
        return evidenceCode;
    }

    public void setEvidenceCode(String evidenceCode) {
        this.evidenceCode = evidenceCode;
    }

    public String getPublication() {
        return publication;
    }

    public void setPublication(String publication) {
        this.publication = publication;
    }
}
