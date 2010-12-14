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
        StringBuffer sb = new StringBuffer("");
        StringBuffer sbMsign = new StringBuffer("&#9792;");
        StringBuffer sbMZ = new StringBuffer(momZygosity.getZygositySymbol());
        if (sbMZ.length() > 0) {
           sb.append(sbMsign);
           sb.append(sbMZ);
        }

        StringBuffer sbDsign = new StringBuffer("&#9794;");
        StringBuffer sbDZ = new StringBuffer(dadZygosity.getZygositySymbol());
        if (sbDZ.length() > 0) {
           sb.append("&nbsp;");
           sb.append(sbDsign);
           sb.append(sbDZ);
        }

        if (sb.length() == 0)
            return "";
        else
            return feature.getName() + "&nbsp;&nbsp;" + sb.toString();
    }
}
