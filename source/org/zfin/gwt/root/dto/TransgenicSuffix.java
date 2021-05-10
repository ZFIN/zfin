package org.zfin.gwt.root.dto;

/**
 * FeatureAssay Mutagen.
 */
public enum TransgenicSuffix {

    TG("Tg"),
    GT("Gt"),
    ET("Et"),
    PT("Pt"),
    ;

    private final String value;

    TransgenicSuffix(String type) {
        this.value = type;
    }

    public String toString() {
        return this.value;
    }
    public static TransgenicSuffix getType(String type) {
        for (TransgenicSuffix t : values()) {
            if (t.toString().equals(type))
                return t;
        }
        throw new RuntimeException("No transgenic suffix of type " + type + " found.");
    }

}
