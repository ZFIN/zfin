package org.zfin;

/**
 * Main domain object.
 */
public class Species {

    private int taxonomyID;
    private String speciesName;
    private String commonName;
    int displayOrder;
    boolean antibodyImmunogen;
    boolean antibodyHost;
    private org.zfin.orthology.Species organism;

    public int getTaxonomyID() {
        return taxonomyID;
    }

    public void setTaxonomyID(int taxonomyID) {
        this.taxonomyID = taxonomyID;
    }

    public String getSpeciesName() {
        return speciesName;
    }

    public void setSpeciesName(String speciesName) {
        this.speciesName = speciesName;
    }

    public String getCommonName() {
        return commonName;
    }

    public void setCommonName(String commonName) {
        this.commonName = commonName;
    }

    public int getDisplayOrder() {
        return displayOrder;
    }

    public void setDisplayOrder(int displayOrder) {
        this.displayOrder = displayOrder;
    }

    public boolean isAntibodyImmunogen() {
        return antibodyImmunogen;
    }

    public void setAntibodyImmunogen(boolean antibodyImmunogen) {
        this.antibodyImmunogen = antibodyImmunogen;
    }

    public boolean isAntibodyHost() {
        return antibodyHost;
    }

    public void setAntibodyHost(boolean antibodyHost) {
        this.antibodyHost = antibodyHost;
    }

    public org.zfin.orthology.Species getOrganism() {
        return organism;
    }

    public void setOrganism(org.zfin.orthology.Species organism) {
        this.organism = organism;
    }
}
