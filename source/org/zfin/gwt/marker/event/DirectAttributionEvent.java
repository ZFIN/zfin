package org.zfin.gwt.marker.event;

public class DirectAttributionEvent {
    private String pubZdbID ;

    public DirectAttributionEvent(String pubZdbID){
        this.pubZdbID = pubZdbID ;
    }

    public String getPubZdbID() {
        return pubZdbID;
    }
}
