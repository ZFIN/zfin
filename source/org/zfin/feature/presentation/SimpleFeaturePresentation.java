package org.zfin.feature.presentation;

import org.zfin.framework.presentation.ProvidesLink;

import java.io.Serializable;

/**
 */
public class SimpleFeaturePresentation implements Serializable {
    private String name;
    private String zdbID;

    public String getName() {
        return name;
    }

    public String getZdbID() {
        return zdbID;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setZdbID(String zdbID) {
        this.zdbID = zdbID;
    }
}
