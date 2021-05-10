package org.zfin.antibody;

/**
 * Enumeration of antibody type values.
 */
public enum AntibodyType {
    ANY("Any"),
    MONOCLONAL("monoclonal"),
    POLYCLONAL("polyclonal"),
    UNSPECIFIED(""),
    ;

    private final String value;

    private AntibodyType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static AntibodyType getType(String type) {
        for (AntibodyType t : values()) {
            if (t.getValue().equals(type))
                return t;
        }
        throw new RuntimeException("No clonal antibodyType of string " + type + " found.");
    }

    public static boolean isTypeAny(String name) {
        if (name == null || name.length() == 0)
            return true;
        AntibodyType type = getType(name);
        return type == ANY;
    }

    public String toString(){
        return value ;
    }

}

