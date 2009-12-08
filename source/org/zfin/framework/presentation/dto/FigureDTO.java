package org.zfin.framework.presentation.dto;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * Figure domain object for GWT.
 */
public class FigureDTO implements IsSerializable {

    private String zdbID;
    private String label;

    public String getZdbID() {
        return zdbID;
    }

    public void setZdbID(String zdbID) {
        this.zdbID = zdbID;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }
}