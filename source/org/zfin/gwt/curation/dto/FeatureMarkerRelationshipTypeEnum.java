package org.zfin.gwt.curation.dto;

/**
 * Created by IntelliJ IDEA.
 * User: nathandunn
 * Date: 12/22/10
 * Time: 9:38 AM
 * To change this template use File | Settings | File Templates.
 */
public enum FeatureMarkerRelationshipTypeEnum {

    IS_ALLELE_OF("is allele of"),
    CONTAINS_PHENOTYPIC_SEQUENCE_FEATURE("contains phenotypic sequence feature"),
    CONTAINS_INNOCUOUS_SEQUENCE_FEATURE("contains innocuous sequence feature"),
    MARKERS_MISSING("markers missing"),
    MARKERS_MOVED("markers moved"),
    MARKERS_PRESENT("markers present");

    private final String value;

    FeatureMarkerRelationshipTypeEnum(String value) {
        this.value = value;
    }

    public String toString() {
        return value;
    }

    public static FeatureMarkerRelationshipTypeEnum getType(String type) {
        for (FeatureMarkerRelationshipTypeEnum t : values()) {
            if (t.toString().equals(type))
                return t;
        }
        throw new RuntimeException("No MarkerRelationship type of string " + type + " found.");
    }

    public static String dumpValues() {
        String returnString = "";
        for (FeatureMarkerRelationshipTypeEnum featureMarkerRelationshipTypeEnum : values()) {
            returnString += "'" + featureMarkerRelationshipTypeEnum + "' ";
        }
        return returnString;
    }
}
