package org.zfin.orthology.presentation;

import java.io.Serializable;

/**
 */
public class OrthologySlimPresentation implements Serializable {
    private String organism;
    private String orthologySymbol;

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
}
