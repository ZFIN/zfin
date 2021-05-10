package org.zfin.mapping;

import org.zfin.marker.Marker;
import org.zfin.mutant.Genotype;

import java.io.Serializable;

public class PrimerSet implements Serializable {

    private String zdbID;
    private Marker marker ;
    private Genotype genotype;
    private String forwardPrimer;
    private String reversePrimer;
    private String bandSize;
    private String restrictionEnzyme;
    private String annealingTemperature;

    public String getAnnealingTemperature() {
        return annealingTemperature;
    }

    public void setAnnealingTemperature(String annealingTemperature) {
        this.annealingTemperature = annealingTemperature;
    }

    public String getBandSize() {
        return bandSize;
    }

    public void setBandSize(String bandSize) {
        this.bandSize = bandSize;
    }

    public String getForwardPrimer() {
        return forwardPrimer;
    }

    public void setForwardPrimer(String forwardPrimer) {
        this.forwardPrimer = forwardPrimer;
    }

    public Genotype getGenotype() {
        return genotype;
    }

    public void setGenotype(Genotype genotype) {
        this.genotype = genotype;
    }

    public Marker getMarker() {
        return marker;
    }

    public void setMarker(Marker marker) {
        this.marker = marker;
    }

    public String getRestrictionEnzyme() {
        return restrictionEnzyme;
    }

    public void setRestrictionEnzyme(String restrictionEnzyme) {
        this.restrictionEnzyme = restrictionEnzyme;
    }

    public String getReversePrimer() {
        return reversePrimer;
    }

    public void setReversePrimer(String reversePrimer) {
        this.reversePrimer = reversePrimer;
    }

    public String getZdbID() {
        return zdbID;
    }

    public void setZdbID(String zdbID) {
        this.zdbID = zdbID;
    }
}
