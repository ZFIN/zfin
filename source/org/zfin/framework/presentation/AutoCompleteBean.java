package org.zfin.framework.presentation;

import org.zfin.marker.MarkerFamilyName;

import java.util.List;


public class AutoCompleteBean {

    private List<MarkerFamilyName> markerFamilyNames;
    private String query;

    public List<MarkerFamilyName> getMarkerFamilyNames() {
        return markerFamilyNames;
    }

    public void setMarkerFamilyNames(List<MarkerFamilyName> markerFamilyNames) {
        this.markerFamilyNames = markerFamilyNames;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }
}
