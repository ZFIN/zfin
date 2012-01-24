package org.zfin.mutant;

import org.zfin.feature.Feature;

/**
 * A genotype feature.
 */
public class GenotypeFeature {

    private String zdbID;
    private Genotype genotype;
    private Feature feature;
    private Zygosity zygosity;
    private Zygosity dadZygosity;
    private Zygosity momZygosity;

    public String getZdbID() {
        return zdbID;
    }

    public void setZdbID(String zdbID) {
        this.zdbID = zdbID;
    }

    public Genotype getGenotype() {
        return genotype;
    }

    public void setGenotype(Genotype genotype) {
        this.genotype = genotype;
    }

    public Feature getFeature() {
        return feature;
    }

    public void setFeature(Feature feature) {
        this.feature = feature;
    }

    public Zygosity getZygosity() {
        return zygosity;
    }

    public void setZygosity(Zygosity zygosity) {
        this.zygosity = zygosity;
    }

    public Zygosity getDadZygosity() {
        return dadZygosity;
    }

    public void setDadZygosity(Zygosity dadZygosity) {
        this.dadZygosity = dadZygosity;
    }

    public Zygosity getMomZygosity() {
        return momZygosity;
    }

    public void setMomZygosity(Zygosity momZygosity) {
        this.momZygosity = momZygosity;
    }

    public String getParentalZygosityDisplay() {
        StringBuilder displayString = new StringBuilder("");
        if (momZygosity.getZygositySymbol().length() > 0) {
            displayString.append("&#9792;");
            displayString.append(momZygosity.getZygositySymbol());
        }

        if (dadZygosity.getZygositySymbol().length() > 0) {
            displayString.append("&nbsp;");
            displayString.append("&#9794;");
            displayString.append(dadZygosity.getZygositySymbol());
        }
        return displayString.toString();
    }

}
