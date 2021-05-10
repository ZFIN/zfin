package org.zfin.feature.presentation;

/**
 */
public class LabLight {

    private String zdbID ;
    private String name ;
    private Boolean currentDesignation ;

    public String getZdbID() {
        return zdbID;
    }

    public void setZdbID(String zdbID) {
        this.zdbID = zdbID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Boolean getCurrentDesignation() {
        return currentDesignation;
    }

    public void setCurrentDesignation(Boolean currentDesignation) {
        this.currentDesignation = currentDesignation;
    }
}
