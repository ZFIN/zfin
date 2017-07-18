package org.zfin.marker.presentation;

import org.zfin.framework.presentation.ProvidesLink;

import java.io.Serializable;

/**
 */
public class TranscriptPresentation implements ProvidesLink, Serializable {
    private String name;
    private String zdbID;

    @Override
    public String getLink() {
        String link = "<a href='/" + zdbID + "'>";
        return link;
    }


    @Override
    public String getLinkWithAttribution() {
        return getLink();
    }

    @Override
    public String getLinkWithAttributionAndOrderThis() {
        return getLink();
    }

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
