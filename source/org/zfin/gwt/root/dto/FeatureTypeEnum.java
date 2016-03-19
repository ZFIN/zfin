package org.zfin.gwt.root.dto;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 */
public enum FeatureTypeEnum implements IsSerializable{
    POINT_MUTATION(1,"Point Mutation"),
    INSERTION(2, "Insertion"),
    TRANSGENIC_INSERTION(3, "Transgenic Insertion"),
    INVERSION(4, "Inversion"),
    INDEL(5, "Indel"),
    TRANSLOC(6, "Translocation"),
    DEFICIENCY(7, "Deficiency"),
    DELETION(8, "Small Deletion"),
    COMPLEX_SUBSTITUTION(9, "Complex"),
    SEQUENCE_VARIANT(10, "Unknown"),
    UNSPECIFIED(11, "Unspecified")
    ;


    private String display;
    private int order;

    FeatureTypeEnum(int order, String value) {
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
        return this == UNSPECIFIED ;
    }

    public boolean isTransgenic(){
        return this == TRANSGENIC_INSERTION ;
    }
}
