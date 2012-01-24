package org.zfin.framework.presentation;


/**
*/
public enum Area {
    TRANSCRIPT("Transcript"),
    ANATOMY("Anatomy")     ,
    ANTIBODY("Antibody")     ,
    CLONE("Clone")     ,
    GENE("Gene")     ,
    PSEUDOGENE("Pseudogene")     ,
    MARKER("Marker")     ,
    USER("User")     ,
    PUBLICATION("Publication")     ,
    ;

// area variables
    public final String EDIT = "Edit";
    public final String ADD = "Add";
    public final String DELETE = "Delete";
    public final String MERGE = "Merge";

    private final String display;

    Area(String type) {
        this.display= type;
    }

    public String getTitleString() {
        return this.display + ": ";
    }

    public String getEditTitleString() {
        return EDIT + " "+ getTitleString();
    }

    public String getAddTitleString() {
        return ADD + " "+ getTitleString();
    }

    public String getDeleteTitleString() {
        return DELETE+ " "+ getTitleString();
    }

    public String getMergeTitleString() {
        return MERGE+ " "+ getTitleString();
    }

    public String toString() {
        return this.display;
    }

    public static Area getType(String type) {
        for (Area t : values()) {
            if (t.toString().equals(type))
                return t;
        }
        throw new RuntimeException("Area not found " + type + " found.");
    }
}
