package org.zfin.gwt.root.dto;

/**
 * FeatureAssay Mutagen.
 */
public enum Mutagen {
    NOT_SPECIFIED("not specified"),
    DNA("DNA"),
    ENU("ENU"),
    TMP("TMP"),
    G_RAYS("g-rays"),
    SPONTANEOUS("spontaneous"),
    EMS("EMS"),
    ZINC_FINGER_NUCLEASE("zinc finger nuclease");

//        RENAMED_THROUGH_THE_NOMENCLATURE_PIPELINE("renamed through the nomenclature pipeline");

    private final String value;

    Mutagen(String type) {
        this.value = type;
    }

    public String toString() {
        return this.value;
    }
    public static Mutagen getType(String type) {
        for (Mutagen t : values()) {
            if (t.toString().equals(type))
                return t;
        }
        throw new RuntimeException("No mutagen of type " + type + " found.");
    }


}
