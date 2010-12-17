package org.zfin.gwt.root.dto;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 */
public enum FeatureTypeEnum implements IsSerializable{
    TRANSGENIC_INSERTION("Transgenic Insertion"),
    POINT_MUTATION("Point Mutation"),
    DELETION("Small Deletion"),
    INSERTION("Insertion"),
    TRANSLOC("Translocation"),
    INVERSION("Inversion"),
    DEFICIENCY("Deficiency"),
    COMPLEX_SUBSTITUTION("Complex"),
    SEQUENCE_VARIANT("unknown"),
    TRANSGENIC_UNSPECIFIED("unspecified transgenic insertion"),
    UNSPECIFIED("unspecified"),
    ;


    private String display;

    FeatureTypeEnum(String value) {
        this.display = value;
    }

    public String toString() {
        return name();
    }


    public static FeatureTypeEnum getTypeForName(String type) {
        for (FeatureTypeEnum t : values()) {
            if (t.name().equals(type))
                return t;
        }
        return null ;
    }

    public static FeatureTypeEnum getTypeForDisplay(String type) {
        for (FeatureTypeEnum t : values()) {
            if (t.getDisplay().equals(type))
                return t;
        }
        return null ;
    }

    // provided for bean access
    public String getDisplay(){
        return display ;
    }

    // provided for bean access
    public String getName(){
        return this.name() ;
    }

    public boolean isUnspecified(){
        return this == UNSPECIFIED || this == TRANSGENIC_UNSPECIFIED ;
    }

    public boolean isTransgenic(){
        return this == TRANSGENIC_INSERTION || this == TRANSGENIC_UNSPECIFIED;
    }
}
