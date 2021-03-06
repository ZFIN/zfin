package org.zfin.framework.presentation;


/**
 */
public enum Area {
    ANATOMY("Anatomy"),
    ANTIBODY("Antibody"),
    CLONE("Clone"),
    COMPANY("Company"),
    EFG("Engineered Foreign Gene"),
    EREGION("Engineered Region"),
    FEATURE("Feature"),
    GENE("Gene"),
    LAB("Lab"),
    MAPPING("Mapping"),
    MARKER("Marker"),
    PERSON("Person"),
    PSEUDOGENE("Pseudogene"),
    PUBLICATION("Publication"),
    TRANSCRIPT("Transcript"),
    USER("User");

    // area variables
    public final String EDIT = "Edit";
    public final String ADD = "Add";
    public final String DELETE = "Delete";
    public final String MERGE = "Merge";

    private final String display;

    Area(String type) {
        this.display = type;
    }

    public String getTitleString() {
        return this.display + ": ";
    }

    public String getEditTitleString() {
        return EDIT + " " + getTitleString();
    }

    public String getAddTitleString() {
        return ADD + " " + getTitleString();
    }

    public String getDeleteTitleString() {
        return DELETE + " " + getTitleString();
    }

    public String getMergeTitleString() {
        return MERGE + " " + getTitleString();
    }

    public String toString() {
        return this.display;
    }

    public static Area getType(String type) {
        for (Area t : values()) {
            if (t.toString().equals(type)) {
                return t;
            }
        }
        throw new RuntimeException("Area not found " + type + " found.");
    }
}
