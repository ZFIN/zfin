package org.zfin.marker.presentation;

import org.zfin.sequence.MarkerDBLinkList;
import org.zfin.marker.MarkerRelationship;
import org.zfin.marker.Clone;

import java.util.List;

/**
 */
public class CloneAddBean {

    private String zdbID ;
    private String name ;
    private String libraryZdbID ;
    private String markerType ;
    private String ownerZdbID;

    public String getZdbID() {
        return zdbID;
    }

    public void setZdbID(String zdbID) {
        this.zdbID = zdbID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLibraryZdbID() {
        return libraryZdbID;
    }

    public void setLibraryZdbID(String libraryZdbID) {
        this.libraryZdbID = libraryZdbID;
    }

    public String getMarkerType() {
        return markerType;
    }

    public void setMarkerType(String markerType) {
        this.markerType = markerType;
    }

    public String getOwnerZdbID() {
        return ownerZdbID;
    }

    public void setOwnerZdbID(String ownerZdbID) {
        this.ownerZdbID = ownerZdbID;
    }

}
