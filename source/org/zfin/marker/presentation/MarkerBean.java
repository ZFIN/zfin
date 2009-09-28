package org.zfin.marker.presentation;

import org.zfin.marker.Marker;
import org.zfin.people.Person;

/**
 */
public class MarkerBean {
    protected Marker marker ;
    protected String zdbID;

    public void setMarker(Marker marker) {
        this.marker = marker;
    }

    public Marker getMarker() {
        return marker ;
    }

    public String getZdbID() {
        return zdbID;
    }

    public void setZdbID(String zdbID) {
        this.zdbID = zdbID;
    }

    public Person getUser() {
        return Person.getCurrentSecurityUser();
    }
}
