package org.zfin.gwt.root.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonView;
import com.google.gwt.user.client.rpc.IsSerializable;
import org.zfin.framework.api.View;

@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum FeatureTypeEnum implements IsSerializable{
    TRANSGENIC_INSERTION("Transgenic Insertion", "Transgenic insertion"),
    POINT_MUTATION("Point Mutation", "Allele with one point mutation"),
    DELETION("Small Deletion", "Allele with one Deletion"),
    INSERTION("Insertion", "Allele with one Insertion"),
    INDEL("Indel", "Allele with one Delins"),
    TRANSLOC("Translocation", "Translocation"),
    INVERSION("Inversion", "Inversion"),
    DEFICIENCY("Deficiency", "Deficiency"),
    COMPLEX_SUBSTITUTION("Complex", "Allele with multiple variants"),
    SEQUENCE_VARIANT("Unknown", "unknown"),
    UNSPECIFIED("Unspecified", "Unspecified Allele"),
    MNV("MNV", "Allele with one MNV")
    ;


    @JsonView(View.API.class)
    private String display;
    private String typeDisplay;

    FeatureTypeEnum(String value, String typeDisplay) {
        this.display = value;
        this.typeDisplay = typeDisplay;
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

    public String getTypeDisplay() {
        return typeDisplay;
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
