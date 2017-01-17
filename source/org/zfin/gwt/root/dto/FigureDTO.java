package org.zfin.gwt.root.dto;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * Figure domain object for GWT.
 */
public class FigureDTO implements IsSerializable, FilterSelectionBoxEntry {

    private String zdbID;
    private String label;
    private String orderingLabel;

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

    public String getOrderingLabel() {
        return orderingLabel;
    }

    public void setOrderingLabel(String orderingLabel) {
        this.orderingLabel = orderingLabel;
    }

    @Override
    public String getValue() {
        return zdbID;
    }
}