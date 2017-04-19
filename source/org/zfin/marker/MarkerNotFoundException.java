package org.zfin.marker;

public class MarkerNotFoundException extends Exception {

    private String zdbID;

    public MarkerNotFoundException(String zdbID) {
        super("Could not find marker " + zdbID);
        this.zdbID = zdbID;
    }

    public String getZdbID() {
        return zdbID;
    }
}
