package org.zfin.gwt.root.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonView;
import com.google.gwt.user.client.rpc.IsSerializable;
import org.zfin.framework.api.View;

@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum FeatureTypeEnum implements IsSerializable{
    TRANSGENIC_INSERTION("Transgenic Insertion"),
    POINT_MUTATION("Point Mutation"),
    DELETION("Small Deletion"),
    INSERTION("Insertion"),
    INDEL("Indel"),
    TRANSLOC("Translocation"),
    INVERSION("Inversion"),
    DEFICIENCY("Deficiency"),
    COMPLEX_SUBSTITUTION("Complex"),
    SEQUENCE_VARIANT("Unknown"),
    UNSPECIFIED("Unspecified"),
    MNV("MNV")
    ;


    @JsonView(View.API.class)
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
        return this == UNSPECIFIED ;
    }

    public boolean isTransgenic(){
        return this == TRANSGENIC_INSERTION ;
    }
}
