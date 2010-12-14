package org.zfin.gwt.root.dto;


/**
 * FeatureAssay Mutagee.
 */
public enum Mutagee {
    NOT_SPECIFIED("not specified"),
    ADULT_FEMALES("adult females"),
    ADULT_MALES("adult males"),
    EMBRYOS("embryos"),
    SPERM("sperm");

//        RENAMED_THROUGH_THE_NOMENCLATURE_PIPELINE("renamed through the nomenclature pipeline");
    private final String value;
    Mutagee(String type) {
        this.value = type;
    }

    public String toString() {
        return this.value;
    }
    public static Mutagee getType(String type) {
        for (Mutagee t : values()) {
            if (t.toString().equals(type))
                return t;
        }
        throw new RuntimeException("No mutagee of type " + type + " found.");
    }
}
