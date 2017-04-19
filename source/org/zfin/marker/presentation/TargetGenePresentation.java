package org.zfin.marker.presentation;

import org.zfin.framework.presentation.ProvidesLink;

import java.io.Serializable;

/**
 */
public class TargetGenePresentation implements ProvidesLink, Serializable {
    private String symbol;
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

    public String getSymbol() {
        return symbol;
    }

    public String getZdbID() {
        return zdbID;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public void setZdbID(String zdbID) {
        this.zdbID = zdbID;
    }
}
