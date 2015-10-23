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
    private Type organism;

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

    public Type getOrganism() {
        return organism;
    }

    public void setOrganism(Type organism) {
        this.organism = organism;
    }

    public enum Type {

        ZEBRAFISH("Zebrafish", 1),
        HUMAN("Human", 2),
        MOUSE("Mouse", 3),
        FRUIT_FLY("Fruit fly", 4),
        YEAST("Yeast", 5);

        private String value;
        private int index;

        Type(String value, int index) {
            this.value = value;
            this.index = index;
        }

        public String toString() {
            return this.value;
        }

        public int getIndex() {
            return this.index;
        }

        static public Type getType(String value) {
            for (Type item : values()) {
                if (item.toString().equals(value))
                    return item;
            }
            return null;
        }

    }

}
