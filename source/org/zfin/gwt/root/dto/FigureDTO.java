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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FigureDTO)) return false;

        FigureDTO figureDTO = (FigureDTO) o;

        if (zdbID != null ? !zdbID.equals(figureDTO.zdbID) : figureDTO.zdbID != null) return false;
        if (label != null ? !label.equals(figureDTO.label) : figureDTO.label != null) return false;
        return orderingLabel != null ? orderingLabel.equals(figureDTO.orderingLabel) : figureDTO.orderingLabel == null;
    }

    @Override
    public int hashCode() {
        int result = zdbID != null ? zdbID.hashCode() : 0;
        result = 31 * result + (label != null ? label.hashCode() : 0);
        result = 31 * result + (orderingLabel != null ? orderingLabel.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "FigureDTO{" +
                "zdbID='" + zdbID + '\'' +
                ", label='" + label + '\'' +
                ", orderingLabel='" + orderingLabel + '\'' +
                '}';
    }
}