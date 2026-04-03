package org.zfin.marker.presentation;

import org.zfin.marker.Marker;
import org.zfin.marker.MarkerType;

import java.util.List;

public class ConvertMarkerTypeBean {

    private String zdbIDToConvert;
    private Marker marker;
    private String newMarkerTypeName;
    private String newZdbId;
    private List<MarkerType> availableTypes;

    public String getZdbIDToConvert() {
        return zdbIDToConvert;
    }

    public void setZdbIDToConvert(String zdbIDToConvert) {
        this.zdbIDToConvert = zdbIDToConvert;
    }

    public Marker getMarker() {
        return marker;
    }

    public void setMarker(Marker marker) {
        this.marker = marker;
    }

    public String getNewMarkerTypeName() {
        return newMarkerTypeName;
    }

    public void setNewMarkerTypeName(String newMarkerTypeName) {
        this.newMarkerTypeName = newMarkerTypeName;
    }

    public String getNewZdbId() {
        return newZdbId;
    }

    public void setNewZdbId(String newZdbId) {
        this.newZdbId = newZdbId;
    }

    public List<MarkerType> getAvailableTypes() {
        return availableTypes;
    }

    public void setAvailableTypes(List<MarkerType> availableTypes) {
        this.availableTypes = availableTypes;
    }
}
