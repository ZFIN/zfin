package org.zfin.marker.presentation;

import org.zfin.marker.Marker;

/**
 */
public class DeleteBean {

    private String zdbIDToDelete;
    private Marker markerToDelete;
    private String markerToDeleteViewString;

    public String getZdbIDToDelete() {
        return zdbIDToDelete;
    }

    public void setZdbIDToDelete(String zdbIDToDelete) {
        this.zdbIDToDelete = zdbIDToDelete;
    }


    public Marker getMarkerToDelete() {
        return markerToDelete;
    }

    public void setMarkerToDelete(Marker markerToDelete) {
        this.markerToDelete = markerToDelete;
        setMarkerToDeleteViewString(this.markerToDelete.getAbbreviation());
    }

    public String getMarkerToDeleteViewString() {
        return markerToDeleteViewString;
    }

    public void setMarkerToDeleteViewString(String markerToDeleteViewString) {
        this.markerToDeleteViewString = markerToDeleteViewString;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("DeleteBean");
        sb.append("{zdbIDToDelete='").append(zdbIDToDelete).append('\'');
        sb.append(", markerToDelete=").append(markerToDelete);
        sb.append(", markerToDeleteViewString='").append(markerToDeleteViewString).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
